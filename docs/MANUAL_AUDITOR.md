# Manual de Utilização — Auditor

## Autenticação

**Endpoint:** `POST /api/auth/login/funcionario`

```bash
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login/funcionario \
  -H "Content-Type: application/json" \
  -d '{"email":"auditor@florinhasdovouga.pt","password":"<PASSWORD>"}'
```

**Resposta (body):**

```json
{
  "id": 5,
  "email": "auditor@florinhasdovouga.pt",
  "nome": "Auditor",
  "role": "AUDITOR",
  "active": true
}
```

O token JWT é devolvido no header `Set-Cookie` da resposta HTTP como um cookie `HttpOnly`:

```txt
Set-Cookie: jwt=eyJhbG...; Path=/; Max-Age=28800; HttpOnly; SameSite=Lax
```

**Como guardar e reutilizar o cookie:**

```bash
# Login — guarda o cookie no ficheiro cookies.txt
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login/funcionario \
  -H "Content-Type: application/json" \
  -d '{"email":"auditor@florinhasdovouga.pt","password":"<PASSWORD>"}'

# Pedidos seguintes — usa o cookie guardado
curl -b cookies.txt "http://localhost:8080/api/audit/logs?page=0&size=10"
```

Em alternativa, pode extrair o token manualmente e usá-lo diretamente:

```bash
# Extrair token do header Set-Cookie
TOKEN=$(curl -s -D - -X POST http://localhost:8080/api/auth/login/funcionario \
  -H "Content-Type: application/json" \
  -d '{"email":"auditor@florinhasdovouga.pt","password":"<PASSWORD>"}' \
  | grep -oP 'jwt=\K[^;]+')

# Usar o token diretamente
curl -b "jwt=$TOKEN" "http://localhost:8080/api/audit/logs?page=0&size=10"
```

> **Nota:** O Auditor não tem acesso à plataforma web. Toda a interação é feita via CLI/API. O Auditor tem acesso **exclusivo** à consulta de logs de auditoria e não pode realizar nenhuma outra operação. O cookie expira após 8 horas.

---

## Consulta de Logs de Auditoria

### Endpoint

`GET /api/audit/logs`

### Parâmetros (todos opcionais)

| Parâmetro | Tipo | Descrição |
| ----------- | ------ | ----------- |
| `page` | int | Página (começa em 0) |
| `size` | int | Registos por página (1-200, default: 50) |
| `userId` | long | Filtrar por ID do utilizador |
| `action` | string | Filtrar por tipo de ação |
| `entityType` | string | Filtrar por tipo de entidade |
| `startDate` | ISO datetime | Data/hora início (ex: `2026-05-01T00:00:00`) |
| `endDate` | ISO datetime | Data/hora fim (ex: `2026-05-31T23:59:59`) |

---

### Exemplo: Listar todos os logs (paginado)

```bash
curl -b cookies.txt "http://localhost:8080/api/audit/logs?page=0&size=10"
```

**Resposta:**

```json
{
  "content": [
    {
      "id": 5,
      "userId": 4,
      "userName": "Encarregado Proteção Dados",
      "action": "PUBLICAR_TERMOS",
      "entityType": "SYSTEM_CONFIG",
      "entityId": null,
      "details": "Termos v2 publicados por utilizador 4: teste curl",
      "ipAddress": "172.18.0.7",
      "timestamp": "2026-05-30T17:33:33.697257"
    },
    {
      "id": 4,
      "userId": 5,
      "userName": "Auditor",
      "action": "LOGIN_FUNCIONARIO",
      "entityType": "UTILIZADOR",
      "entityId": 5,
      "details": "Login de funcionario (AUDITOR): auditor@florinhasdovouga.pt",
      "ipAddress": "172.18.0.1",
      "timestamp": "2026-05-30T17:30:58.689401"
    }
  ],
  "totalPages": 1,
  "totalElements": 5,
  "number": 0,
  "size": 10
}
```

### Exemplo: Filtrar por ação (logins)

```bash
curl -b cookies.txt "http://localhost:8080/api/audit/logs?action=LOGIN_FUNCIONARIO&page=0&size=50"
```

### Exemplo: Filtrar por utilizador específico

```bash
curl -b cookies.txt "http://localhost:8080/api/audit/logs?userId=4&page=0&size=50"
```

### Exemplo: Filtrar por intervalo de datas

```bash
curl -b cookies.txt \
  "http://localhost:8080/api/audit/logs?startDate=2026-05-01T00:00:00&endDate=2026-05-31T23:59:59&page=0&size=100"
```

### Exemplo: Filtrar por tipo de entidade

```bash
curl -b cookies.txt "http://localhost:8080/api/audit/logs?entityType=SYSTEM_CONFIG&page=0&size=50"
```

### Exemplo: Combinar filtros

```bash
curl -b cookies.txt \
  "http://localhost:8080/api/audit/logs?action=PUBLICAR_TERMOS&startDate=2026-05-30T00:00:00&endDate=2026-05-30T23:59:59&page=0&size=20"
```

---

## Tipos de Ação Registados

| Ação | Descrição |
| ------ | ----------- |
| `LOGIN_FUNCIONARIO` | Login de funcionário |
| `LOGIN_UTENTE` | Login de utente |
| `LOGOUT` | Logout |
| `REGISTO_UTENTE` | Registo de novo utente |
| `REGISTO_FUNCIONARIO` | Registo de novo funcionário |
| `PUBLICAR_TERMOS` | Publicação de nova versão dos termos |
| `CRIAR_REQUISICAO_MATERIAL` | Criação de requisição de material |
| `CRIAR_REQUISICAO_TRANSPORTE` | Criação de requisição de transporte |
| `CRIAR_REQUISICAO_MANUTENCAO` | Criação de requisição de manutenção |
| `ATUALIZAR_ESTADO_REQUISICAO` | Alteração de estado de requisição |
| `CRIAR_MATERIAL_CATALOGO` | Adição de material ao catálogo |
| `CRIAR_TRANSPORTE_CATALOGO` | Adição de transporte ao catálogo |
| `CRIAR_TIPO_MANUTENCAO` | Criação de tipo de manutenção |

## Tipos de Entidade

| Entidade | Descrição |
| ---------- | ----------- |
| `UTILIZADOR` | Operações sobre utilizadores |
| `SYSTEM_CONFIG` | Configurações do sistema |
| `REQUISICAO` | Requisições |
| `MATERIAL` | Materiais do catálogo |
| `TRANSPORTE` | Transportes do catálogo |
| `TIPO_MANUTENCAO` | Tipos de manutenção |
| `MANUTENCAO_ITEM` | Itens de manutenção |

---

## Resumo de Permissões do Auditor

| Operação | Acesso |
| ---------- | -------- |
| Consultar logs de auditoria | ✅ |
| Gerir termos e condições | ❌ |
| Configurar retenção de documentos | ❌ |
| Gerir utilizadores | ❌ |
| Criar/ver requisições | ❌ |
| Enviar relatórios por email | ❌ |
| Aceder à plataforma web | ❌ |
