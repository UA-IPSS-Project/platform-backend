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

# Verificar se o backend está a responder (o /actuator/health pode estar protegido)
echo "Verificando backend..."
backend_ready=false
for i in $(seq 1 20); do
  http_code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/api/auth/login/utente" \
    -H "Content-Type: application/json" \
    -d '{}')

  if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 500 ] && [ "$http_code" -ne 404 ]; then
    backend_ready=true
    break
  fi

  echo "A aguardar backend... tentativa $i/20"
  sleep 2
done

if [ "$backend_ready" != true ]; then
  echo "Backend não está acessível em $BASE_URL (último status: ${http_code:-N/A})"
  exit 1
fi

echo "Backend online (HTTP $http_code)"
echo ""

# Array de utilizadores para criar
declare -a users=(
  '{"nome":"K6 User 1","email":"k6test1@gmail.com","nif":"123456781","telefone":"912345671","password":"Test1234","dataNasc":"1990-01-01","termsAccepted":true}'
  '{"nome":"K6 User 2","email":"k6test2@gmail.com","nif":"123456782","telefone":"912345672","password":"User5678","dataNasc":"1990-01-02","termsAccepted":true}'
  '{"nome":"K6 User 3","email":"k6test3@gmail.com","nif":"123456783","telefone":"912345673","password":"Load9012","dataNasc":"1990-01-03","termsAccepted":true}'
  '{"nome":"K6 User 4","email":"k6test4@gmail.com","nif":"123456784","telefone":"912345674","password":"Perf3456","dataNasc":"1990-01-04","termsAccepted":true}'
  '{"nome":"K6 User 5","email":"k6test5@gmail.com","nif":"123456785","telefone":"912345675","password":"Auto7890","dataNasc":"1990-01-05","termsAccepted":true}'
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
    ((success_count+=1))
  elif [ "$http_code" -eq 400 ] && echo "$body" | grep -Eiq "já está em uso|ja esta em uso|Email|NIF"; then
    echo "ℹ️ Já existe: $email"
    ((already_exists_count+=1))
  elif [ "$http_code" -eq 409 ]; then
    echo "ℹ️ Conflito (já existe): $email"
    ((already_exists_count+=1))
  else
    echo "❌ Erro ao criar $email (HTTP $http_code)"
    echo "   Resposta: $body"
    ((error_count+=1))
  fi
done

echo ""
echo "Resumo:"
echo "  Criados: $success_count"
echo "  Já existentes: $already_exists_count"
echo "  Erros: $error_count"

if [ "$error_count" -gt 0 ]; then
  exit 1
fi

echo "Setup concluído! Pode executar os testes k6"
