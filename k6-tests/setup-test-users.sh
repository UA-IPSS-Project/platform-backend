#!/bin/bash

# Script para criar utilizadores de teste para k6
# Executa antes dos testes de carga

set -e

BASE_URL=${BASE_URL:-"http://localhost:8080"}

echo "========================================="
echo "  Criação de Utilizadores de Teste K6"
echo "========================================="
echo "BASE_URL: $BASE_URL"
echo ""

# Verificar se o backend está rodando
echo "Verificando backend..."
if ! curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "Backend não está acessível em $BASE_URL"
    exit 1
fi
echo "Backend online"
echo ""

# Array de utilizadores para criar
declare -a users=(
  '{"nome":"K6 User 1","email":"k6test1@gmail.com","nif":"123456781","telefone":"912345671","password":"Test1234","dataNasc":"1990-01-01"}'
  '{"nome":"K6 User 2","email":"k6test2@gmail.com","nif":"123456782","telefone":"912345672","password":"User5678","dataNasc":"1990-01-02"}'
  '{"nome":"K6 User 3","email":"k6test3@gmail.com","nif":"123456783","telefone":"912345673","password":"Load9012","dataNasc":"1990-01-03"}'
  '{"nome":"K6 User 4","email":"k6test4@gmail.com","nif":"123456784","telefone":"912345674","password":"Perf3456","dataNasc":"1990-01-04"}'
  '{"nome":"K6 User 5","email":"k6test5@gmail.com","nif":"123456785","telefone":"912345675","password":"Auto7890","dataNasc":"1990-01-05"}'
)

echo " Criando utilizadores de teste..."
echo ""

success_count=0
already_exists_count=0
error_count=0

for user in "${users[@]}"; do
  email=$(echo "$user" | grep -o '"email":"[^"]*"' | cut -d'"' -f4)
  
  response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register/utente" \
    -H "Content-Type: application/json" \
    -d "$user")
  
  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | sed '$d')
  
  if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
    echo "✅ Criado: $email"
    ((success_count++))
  fi
done

echo ""
echo "Setup concluído! Pode executar os testes k6"
