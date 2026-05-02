# Sistema de Backups Automáticos

## Visão Geral

O sistema implementa backups automáticos para PostgreSQL e MinIO com política de retenção de 3 níveis:
- **Diários:** 7 dias
- **Semanais:** 4 semanas
- **Mensais:** 12 meses

## Configuração

### PostgreSQL
- **Serviço:** `postgres-backup` (prodrigestivill/postgres-backup-local)
- **Horário:** Diariamente às 00:00 (meia-noite)
- **Localização:** `./backups/postgres/`
- **Formato:** SQL comprimido com gzip (nível 9)

### MinIO
- **Serviço:** `minio-backup` (minio/mc)
- **Horário:** Diariamente às 03:00
- **Localização:** `./backups/minio/`
- **Formato:** TAR.GZ

## Estrutura de Diretórios

```
platform-backend/
├── backups/
│   ├── postgres/
│   │   ├── daily/          # Últimos 7 dias
│   │   ├── weekly/         # Últimas 4 semanas
│   │   └── monthly/        # Últimos 12 meses
│   └── minio/
│       ├── daily/          # Últimos 7 dias
│       ├── weekly/         # Últimas 4 semanas
│       └── monthly/        # Últimos 12 meses
└── scripts/
    ├── backup-minio.sh     # Script de backup MinIO
    └── crontab             # Agendamento cron
```

## Restore PostgreSQL

### 1. Listar backups disponíveis
```bash
ls -lh platform-backend/backups/postgres/daily/
ls -lh platform-backend/backups/postgres/weekly/
ls -lh platform-backend/backups/postgres/monthly/
```

### 2. Parar os serviços
```bash
cd platform-backend
docker-compose stop api-gateway marcacoes requisicoes notificacoes
```

### 3. Restaurar backup
```bash
# Escolher o ficheiro de backup (exemplo: daily/florinhas_db-2026-05-02_00-00-00.sql.gz)
BACKUP_FILE="backups/postgres/daily/florinhas_db-2026-05-02_00-00-00.sql.gz"

# Restaurar
docker exec -i florinhas_db psql -U postgres -d florinhas_db < <(gunzip -c $BACKUP_FILE)
```

### 4. Reiniciar serviços
```bash
docker-compose start api-gateway marcacoes requisicoes notificacoes
```

## Restore MinIO

### 1. Listar backups disponíveis
```bash
ls -lh platform-backend/backups/minio/daily/
ls -lh platform-backend/backups/minio/weekly/
ls -lh platform-backend/backups/minio/monthly/
```

### 2. Parar os serviços
```bash
cd platform-backend
docker-compose stop marcacoes requisicoes minio
```

### 3. Extrair e restaurar backup
```bash
# Escolher o ficheiro de backup
BACKUP_FILE="backups/minio/daily/minio_backup_20260502_030000.tar.gz"

# Extrair
tar -xzf $BACKUP_FILE -C /tmp/

# Restaurar dados
docker-compose start minio
sleep 5

# Configurar alias
docker exec minio-backup mc alias set local http://minio:9000 $MINIO_ROOT_USER $MINIO_ROOT_PASSWORD

# Restaurar buckets
docker exec minio-backup mc mirror --overwrite /tmp/temp_20260502_030000/ local/
```

### 4. Reiniciar serviços
```bash
docker-compose start marcacoes requisicoes
```

## Restore Completo (PostgreSQL + MinIO)

Para restaurar todo o sistema para um ponto específico no tempo:

```bash
# 1. Parar todos os serviços
docker-compose down

# 2. Restaurar PostgreSQL
docker-compose up -d postgres
sleep 10
BACKUP_FILE="backups/postgres/daily/florinhas_db-2026-05-02_00-00-00.sql.gz"
docker exec -i florinhas_db psql -U postgres -d florinhas_db < <(gunzip -c $BACKUP_FILE)

# 3. Restaurar MinIO
docker-compose up -d minio
sleep 5
MINIO_BACKUP="backups/minio/daily/minio_backup_20260502_030000.tar.gz"
tar -xzf $MINIO_BACKUP -C /tmp/
docker exec minio-backup mc alias set local http://minio:9000 $MINIO_ROOT_USER $MINIO_ROOT_PASSWORD
docker exec minio-backup mc mirror --overwrite /tmp/temp_20260502_030000/ local/

# 4. Iniciar todos os serviços
docker-compose up -d
```

## Verificação de Backups

### PostgreSQL
```bash
# Ver últimos backups
docker exec postgres_backup ls -lh /backups/daily/

# Ver logs
docker logs postgres_backup
```

### MinIO
```bash
# Ver últimos backups
ls -lh platform-backend/backups/minio/daily/

# Ver logs
docker logs minio_backup
```

## Monitorização

### Healthcheck PostgreSQL Backup
O serviço `postgres-backup` expõe um healthcheck na porta 8080:
```bash
curl http://localhost:8080/health
```

### Logs de Backup MinIO
```bash
docker exec minio_backup cat /var/log/minio-backup.log
```

## Política de Retenção

| Tipo | Retenção | Frequência | Localização |
|------|----------|------------|-------------|
| Diário | 7 dias | Todos os dias | `daily/` |
| Semanal | 4 semanas | Domingos | `weekly/` |
| Mensal | 12 meses | Dia 1 do mês | `monthly/` |

## Notas Importantes

1. **Espaço em Disco:** Monitorizar regularmente o espaço disponível em `./backups/`
2. **Testes de Restore:** Realizar testes periódicos de restore em ambiente de desenvolvimento
3. **Backup Offsite:** Considerar copiar backups mensais para localização externa (cloud storage)
4. **Encriptação:** Os backups PostgreSQL são comprimidos mas não encriptados. Para dados sensíveis, considerar encriptação adicional
5. **Permissões:** Garantir que os diretórios de backup têm permissões adequadas (700)

## Troubleshooting

### Backup PostgreSQL não está a executar
```bash
# Verificar logs
docker logs postgres_backup

# Reiniciar serviço
docker-compose restart postgres-backup
```

### Backup MinIO falha
```bash
# Verificar logs
docker logs minio_backup

# Testar script manualmente
docker exec minio_backup /backup-minio.sh
```

### Espaço em disco insuficiente
```bash
# Ver uso de espaço
du -sh platform-backend/backups/*

# Limpar backups antigos manualmente se necessário
find platform-backend/backups/postgres/daily/ -name "*.sql.gz" -mtime +7 -delete
find platform-backend/backups/minio/daily/ -name "*.tar.gz" -mtime +7 -delete
```
