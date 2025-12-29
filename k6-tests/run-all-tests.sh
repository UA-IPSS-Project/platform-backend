#!/bin/bash

# Script para executar todos os testes k6 sequencialmente
# Uso: ./run-all-tests.sh

# 1. Preparar pasta de resultados (CRÍTICO: Cria a pasta se não existir)
mkdir -p results
# Limpa resultados antigos para não misturares relatórios de execuções passadas
rm -f results/*.json results/*.html

# --- MODO RIGOROSO LIGADO (Para o Setup) ---
# Se o k6 não existir, o backend estiver em baixo ou o setup falhar, o script PARA.
set -e

BASE_URL=${BASE_URL:-"http://localhost:8080"}

echo "========================================="
echo "  K6 Tests - Sistema de Marcações IPSS"
echo "========================================="
echo "BASE_URL: $BASE_URL"
echo ""

# Verificar se k6 está instalado
if ! command -v k6 &> /dev/null; then
    echo "ERRO: k6 não está instalado"
    echo "Instale com: sudo apt-get install k6"
    exit 1
fi

# Verificar se o backend está rodando
echo "Verificando se o backend está rodando..."
if curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "Backend está online"
else
    echo "Backend não está acessível em $BASE_URL"
    echo "Inicie o backend antes de executar os testes"
    exit 1
fi

echo ""
echo "========================================="
echo "SETUP: Criando utilizadores de teste"
echo "========================================="
# Certifica-te que este ficheiro existe e tem permissão de execução
if [ -f "./setup-test-users.sh" ]; then
    bash ./setup-test-users.sh
else
    echo "AVISO: setup-test-users.sh não encontrado. Saltando criação de utilizadores."
fi

# --- MODO RIGOROSO DESLIGADO (Para os Testes) ---
# A partir daqui, usamos 'set +e'.
# Se um teste falhar (thresholds vermelhos), o script CONTINUA para o próximo.
set +e

echo ""
echo "========================================="
echo "SMOKE TEST (Teste básico - 30s)"
echo "========================================="
# REPORT_NAME define o nome dos ficheiros na pasta results
k6 run smoke-test.js -e REPORT_NAME=smoke_test

echo ""
echo "========================================="
echo "LOAD TEST (Teste de carga - ~5min)"
echo "========================================="
k6 run load-test.js -e REPORT_NAME=load_test

echo ""
echo "========================================="
echo "STRESS TEST (Teste extremo - ~10min)"
echo "========================================="
k6 run stress-test.js -e REPORT_NAME=stress_test

echo ""
echo "========================================="
echo "TODOS OS TESTES CONCLUÍDOS"
echo "Relatórios gerados na pasta ./results/ :"
echo "========================================="
ls -l results/