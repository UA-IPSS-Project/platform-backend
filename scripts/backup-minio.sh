#!/bin/sh

# Script de backup automático para MinIO
# Executa diariamente às 3h da manhã
# Política de retenção: 7 dias, 4 semanas, 12 meses

set -e

BACKUP_DIR="/backups"
MINIO_ALIAS="local"
MINIO_URL="http://minio:9000"
DATE=$(date +%Y%m%d_%H%M%S)
DAILY_DIR="$BACKUP_DIR/daily"
WEEKLY_DIR="$BACKUP_DIR/weekly"
MONTHLY_DIR="$BACKUP_DIR/monthly"

# Criar diretórios se não existirem
mkdir -p "$DAILY_DIR" "$WEEKLY_DIR" "$MONTHLY_DIR"

# Configurar alias MinIO
mc alias set "$MINIO_ALIAS" "$MINIO_URL" "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"

echo "[$(date)] Iniciando backup MinIO..."

# Backup diário
BACKUP_FILE="$DAILY_DIR/minio_backup_$DATE.tar.gz"
mc mirror --preserve "$MINIO_ALIAS" "$DAILY_DIR/temp_$DATE"
tar -czf "$BACKUP_FILE" -C "$DAILY_DIR" "temp_$DATE"
rm -rf "$DAILY_DIR/temp_$DATE"

echo "[$(date)] Backup diário criado: $BACKUP_FILE"

# Backup semanal (aos domingos)
if [ "$(date +%u)" -eq 7 ]; then
    WEEKLY_FILE="$WEEKLY_DIR/minio_backup_week_$(date +%Y_W%V).tar.gz"
    cp "$BACKUP_FILE" "$WEEKLY_FILE"
    echo "[$(date)] Backup semanal criado: $WEEKLY_FILE"
fi

# Backup mensal (dia 1 de cada mês)
if [ "$(date +%d)" -eq 01 ]; then
    MONTHLY_FILE="$MONTHLY_DIR/minio_backup_month_$(date +%Y_%m).tar.gz"
    cp "$BACKUP_FILE" "$MONTHLY_FILE"
    echo "[$(date)] Backup mensal criado: $MONTHLY_FILE"
fi

# Limpeza de backups antigos
echo "[$(date)] Limpando backups antigos..."

# Manter apenas 7 dias de backups diários
find "$DAILY_DIR" -name "minio_backup_*.tar.gz" -type f -mtime +7 -delete

# Manter apenas 4 semanas de backups semanais
find "$WEEKLY_DIR" -name "minio_backup_week_*.tar.gz" -type f -mtime +28 -delete

# Manter apenas 12 meses de backups mensais
find "$MONTHLY_DIR" -name "minio_backup_month_*.tar.gz" -type f -mtime +365 -delete

echo "[$(date)] Backup MinIO concluído com sucesso!"
