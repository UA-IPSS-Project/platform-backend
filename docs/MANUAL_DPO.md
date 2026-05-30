# Manual de Utilização — DPO (Encarregado de Proteção de Dados)

## Autenticação

**Endpoint:** `POST /api/auth/login/funcionario`

```bash
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login/funcionario \
  -H "Content-Type: application/json" \
  -d '{"email":"dpo@florinhasdovouga.pt","password":"<PASSWORD>"}'
```

**Resposta (body):**

```json
{
  "id": 4,
  "email": "dpo@florinhasdovouga.pt",
  "nome": "Encarregado de Proteção de Dados",
  "role": "DPO",
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
  -d '{"email":"dpo@florinhasdovouga.pt","password":"<PASSWORD>"}'

# Pedidos seguintes — usa o cookie guardado
curl -b cookies.txt http://localhost:8080/api/config/documento/retencao
```

Em alternativa, pode extrair o token manualmente e usá-lo diretamente:

```bash
# Extrair token do header Set-Cookie
TOKEN=$(curl -s -D - -X POST http://localhost:8080/api/auth/login/funcionario \
  -H "Content-Type: application/json" \
  -d '{"email":"dpo@florinhasdovouga.pt","password":"<PASSWORD>"}' \
  | grep -oP 'jwt=\K[^;]+')

# Usar o token diretamente
curl -b "jwt=$TOKEN" http://localhost:8080/api/config/documento/retencao
```

> **Nota:** O DPO não tem acesso à plataforma web. Toda a interação é feita via CLI/API. O cookie expira após 8 horas.

---

## Gestão de Termos e Condições

O DPO é responsável por gerir os termos e condições da plataforma. Quando os termos são atualizados e publicados, todos os utilizadores são obrigados a re-aceitar na próxima sessão.

### 1. Consultar conteúdo atual dos termos

**Endpoint:** `GET /api/utilizadores/admin/terms-content?lang={pt|en}`

```bash
curl -b cookies.txt "http://localhost:8080/api/utilizadores/admin/terms-content?lang=pt"
```

**Resposta:**

```json
{
  "content": "# Termos e Condições de Utilização\n\n## 1. Identificação do Responsável..."
}
```

### 2. Atualizar conteúdo dos termos (sem publicar)

Atualiza o texto dos termos num idioma específico sem incrementar a versão.

**Endpoint:** `PUT /api/utilizadores/admin/terms-content?lang={pt|en}`

```bash
curl -X PUT -b cookies.txt \
  "http://localhost:8080/api/utilizadores/admin/terms-content?lang=pt" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "# Termos e Condições de Utilização\n\n## 1. Identificação...\n\n(texto completo aqui)"
  }'
```

**Resposta (sucesso):** `200 OK`

### 3. Publicar nova versão dos termos

Publica uma nova versão dos termos (PT + EN) e incrementa a versão. **Todos os utilizadores terão de re-aceitar os termos.**

**Endpoint:** `POST /api/utilizadores/admin/terms-publish`

```bash
curl -X POST -b cookies.txt \
  http://localhost:8080/api/utilizadores/admin/terms-publish \
  -H "Content-Type: application/json" \
  -d '{
    "contentPt": "texto em português...",
    "contentEn": "text in english...",
    "changeDescription": "Descrição da alteração"
  }'
```

**Resposta:**

```json
{
  "version": 3
}
```

> **Importante:** Após publicar, todos os utilizadores verão um ecrã de aceitação obrigatória no próximo login.

#### Publicar a partir de ficheiros .txt

Se tiver os termos guardados em ficheiros de texto (ex: `termos_pt.txt` e `termos_en.txt`), pode publicá-los diretamente:

**Preparação dos ficheiros:**

`termos_pt.txt`:

```md
# Termos e Condições de Utilização

## 1. Identificação do Responsável pelo Tratamento

A Florinhas do Vouga – IPSS, com sede em Aveiro, é a entidade responsável pelo tratamento dos dados pessoais recolhidos através desta plataforma digital.

## 2. Finalidade do Tratamento

Os dados pessoais são recolhidos e tratados para as seguintes finalidades:
- Gestão de marcações e agendamentos;
- Gestão de requisições de material, transporte e manutenção;
- Comunicação institucional;
- Cumprimento de obrigações legais.

## 3. Base Legal

O tratamento dos dados pessoais baseia-se no consentimento do titular (Art.º 6.º, n.º 1, al. a) do RGPD) e na execução de contrato ou diligências pré-contratuais (Art.º 6.º, n.º 1, al. b) do RGPD).

## 4. Direitos dos Titulares

Nos termos do RGPD, o titular dos dados tem direito a:
- Aceder aos seus dados pessoais;
- Retificar dados inexatos;
- Solicitar a eliminação dos dados (direito ao esquecimento);
- Portabilidade dos dados;
- Opor-se ao tratamento.

Para exercer estes direitos, contacte o DPO através do email: dpo@florinhasdovouga.pt

## 5. Prazo de Conservação

Os dados pessoais são conservados pelo período estritamente necessário às finalidades que motivaram a sua recolha, respeitando os prazos legais aplicáveis.

## 6. Segurança

A plataforma implementa medidas técnicas e organizativas adequadas para proteger os dados pessoais contra acessos não autorizados, perda ou destruição.
```

`termos_en.txt`:

```md
# Terms and Conditions of Use

## 1. Data Controller

Florinhas do Vouga – IPSS, based in Aveiro, is the entity responsible for processing personal data collected through this digital platform.

## 2. Purpose of Processing

Personal data is collected and processed for:
- Appointment and scheduling management;
- Material, transport and maintenance requisitions;
- Institutional communication;
- Compliance with legal obligations.

## 3. Legal Basis

Processing is based on the data subject's consent (Art. 6(1)(a) GDPR) and the performance of a contract (Art. 6(1)(b) GDPR).

## 4. Data Subject Rights

Under the GDPR, the data subject has the right to:
- Access their personal data;
- Rectify inaccurate data;
- Request erasure (right to be forgotten);
- Data portability;
- Object to processing.

To exercise these rights, contact the DPO at: dpo@florinhasdovouga.pt

## 5. Retention Period

Personal data is retained for the period strictly necessary for the purposes for which it was collected, respecting applicable legal deadlines.

## 6. Security

The platform implements appropriate technical and organisational measures to protect personal data against unauthorised access, loss or destruction.
```

**Comando para publicar a partir dos ficheiros:**

```bash
# Ler os ficheiros e construir o JSON com jq
jq -n \
  --rawfile pt termos_pt.txt \
  --rawfile en termos_en.txt \
  '{contentPt: $pt, contentEn: $en, changeDescription: "Atualização dos termos v3"}' \
| curl -X POST -b cookies.txt \
    http://localhost:8080/api/utilizadores/admin/terms-publish \
    -H "Content-Type: application/json" \
    -d @-
```

**Alternativa sem jq (usando variáveis bash):**

```bash
# Ler ficheiros para variáveis (escapa newlines para JSON)
PT=$(cat termos_pt.txt | python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))")
EN=$(cat termos_en.txt | python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))")

# Publicar
curl -X POST -b cookies.txt \
  http://localhost:8080/api/utilizadores/admin/terms-publish \
  -H "Content-Type: application/json" \
  -d "{\"contentPt\": $PT, \"contentEn\": $EN, \"changeDescription\": \"Atualização dos termos v3\"}"
```

**Resposta:**

```json
{
  "version": 3
}
```

### 4. Atualizar versão dos termos (forçar re-aceitação)

Incrementa a versão sem alterar o conteúdo — útil para forçar re-aceitação.

**Endpoint:** `POST /api/utilizadores/admin/terms-version?newVersion={N}&changeDescription={texto}`

```bash
curl -X POST -b cookies.txt \
  "http://localhost:8080/api/utilizadores/admin/terms-version?newVersion=4&changeDescription=Revisão%20anual%20obrigatória"
```

**Resposta:** `200 OK`

---

## Configuração de Retenção de Documentos

O DPO pode configurar o prazo de retenção de documentos conforme o RGPD.

### 1. Consultar prazo atual

**Endpoint:** `GET /api/config/documento/retencao`

```bash
curl -b cookies.txt http://localhost:8080/api/config/documento/retencao
```

**Resposta:**

```json
{
  "anos": 5,
  "descricao": "Prazo de retenção de documentos em anos"
}
```

### 2. Alterar prazo de retenção

**Endpoint:** `PUT /api/config/documento/retencao`

Valor permitido: entre 1 e 50 anos.

```bash
curl -X PUT -b cookies.txt \
  http://localhost:8080/api/config/documento/retencao \
  -H "Content-Type: application/json" \
  -d '{"anos": 7}'
```

**Resposta:**

```json
{
  "anos": 7,
  "mensagem": "Prazo de retenção atualizado com sucesso"
}
```

---

## Resumo de Permissões do DPO

| Operação | Acesso |
| ---------- | -------- |
| Gerir termos e condições (consultar, editar, publicar) | ✅ |
| Configurar retenção de documentos | ✅ |
| Consultar logs de auditoria | ❌ |
| Gerir utilizadores | ❌ |
| Criar/ver requisições | ❌ |
| Enviar relatórios por email | ❌ |
| Aceder à plataforma web | ❌ |
