# Scripts de Backup

## backup-minio.sh

Script de backup automático para MinIO que:
- Executa diariamente às 3h da manhã via cron
- Cria backups diários, semanais e mensais
- Aplica política de retenção automática
- Comprime backups em formato TAR.GZ

## crontab

Configuração do cron para agendamento do backup MinIO.

## Uso

Os scripts são executados automaticamente pelos containers Docker.
Para executar manualmente:

```bash
docker exec minio_backup /backup-minio.sh
```

Para mais informações, consulte `BACKUP_RESTORE.md` na raiz do projeto backend.
