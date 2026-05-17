#!/usr/bin/env bash
# Reimporta o realm florinhas no Keycloak em execução, sobrescrevendo o existente.
# Uso: ./keycloak-reimport.sh
set -e

echo "A reimportar realm florinhas..."
docker compose exec keycloak /opt/keycloak/bin/kc.sh import \
  --file /opt/keycloak/data/import/florinhas-realm.json \
  --override true

echo "Reimport concluído. Reinicia o Keycloak para aplicar as alterações:"
echo "  docker compose restart keycloak"
