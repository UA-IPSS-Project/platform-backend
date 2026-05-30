# Manual de Utilização — DPO (Encarregado de Proteção de Dados)

## Autenticação

**Endpoint:** `POST /api/auth/login/funcionario`

```bash
curl -X POST http://localhost:8080/api/auth/login/funcionario \
  -H "Content-Type: application/json" \
  -d '{"email":"dpo@florinhasdovouga.pt","password":"<PASSWORD>"}'
```

**Resposta:**
```json
{
  "id": 4,
  "email": "dpo@florinhasdovouga.pt",
  "nome": "Encarregado Proteção Dados",
  "role": "DPO",
  "active": true
}
```

O token JWT é devolvido no cookie `jwt`. Deve ser incluído em todos os pedidos:
```bash
-b "jwt=<TOKEN>"
```

> **Nota:** O DPO não tem acesso à plataforma web. Toda a interação é feita via CLI/API.

---

## Gestão de Termos e Condições

O DPO é responsável por gerir os termos e condições da plataforma. Quando os termos são atualizados, todos os utilizadores são obrigados a re-aceitar na próxima sessão.

### 1. Consultar conteúdo atual dos termos

**Endpoint:** `GET /api/utilizadores/admin/terms-content?lang={pt|en}`

```bash
curl -b "jwt=$TOKEN_DPO" \
  "http://localhost:8080/api/utilizadores/admin/terms-content?lang=pt"
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
curl -X PUT -b "jwt=$TOKEN_DPO" \
  "http://localhost:8080/api/utilizadores/admin/terms-content?lang=pt" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "# Termos e Condições de Utilização\n\n## 1. Identificação do Responsável pelo Tratamento\n\nA Florinhas do Vouga – IPSS, com sede em Aveiro, é a entidade responsável pelo tratamento dos dados pessoais recolhidos através desta plataforma digital.\n\n## 2. Finalidade do Tratamento\n\nOs dados pessoais são recolhidos e tratados para as seguintes finalidades:\n- Gestão de marcações e agendamentos;\n- Gestão de requisições de material, transporte e manutenção;\n- Comunicação institucional;\n- Cumprimento de obrigações legais.\n\n## 3. Base Legal\n\nO tratamento dos dados pessoais baseia-se no consentimento do titular (Art.º 6.º, n.º 1, al. a) do RGPD) e na execução de contrato ou diligências pré-contratuais (Art.º 6.º, n.º 1, al. b) do RGPD).\n\n## 4. Direitos dos Titulares\n\nNos termos do RGPD, o titular dos dados tem direito a:\n- Aceder aos seus dados pessoais;\n- Retificar dados inexatos;\n- Solicitar a eliminação dos dados (direito ao esquecimento);\n- Portabilidade dos dados;\n- Opor-se ao tratamento.\n\nPara exercer estes direitos, contacte o DPO através do email: dpo@florinhasdovouga.pt\n\n## 5. Prazo de Conservação\n\nOs dados pessoais são conservados pelo período estritamente necessário às finalidades que motivaram a sua recolha, respeitando os prazos legais aplicáveis.\n\n## 6. Segurança\n\nA plataforma implementa medidas técnicas e organizativas adequadas para proteger os dados pessoais contra acessos não autorizados, perda ou destruição."
  }'
```

**Resposta (sucesso):** `200 OK`

### 3. Publicar nova versão dos termos

Publica uma nova versão dos termos (PT + EN) e incrementa a versão. **Todos os utilizadores terão de re-aceitar os termos.**

**Endpoint:** `POST /api/utilizadores/admin/terms-publish`

```bash
curl -X POST -b "jwt=$TOKEN_DPO" \
  http://localhost:8080/api/utilizadores/admin/terms-publish \
  -H "Content-Type: application/json" \
  -d '{
    "contentPt": "# Termos e Condições de Utilização\n\n## 1. Identificação do Responsável pelo Tratamento\n\nA Florinhas do Vouga – IPSS, com sede em Aveiro, é a entidade responsável pelo tratamento dos dados pessoais recolhidos através desta plataforma digital.\n\n## 2. Finalidade do Tratamento\n\nOs dados pessoais são recolhidos e tratados para as seguintes finalidades:\n- Gestão de marcações e agendamentos;\n- Gestão de requisições de material, transporte e manutenção;\n- Comunicação institucional;\n- Cumprimento de obrigações legais.\n\n## 3. Base Legal\n\nO tratamento baseia-se no consentimento do titular (Art.º 6.º, n.º 1, al. a) do RGPD).\n\n## 4. Direitos dos Titulares\n\nO titular tem direito a: acesso, retificação, eliminação, portabilidade e oposição.\nContacto DPO: dpo@florinhasdovouga.pt\n\n## 5. Prazo de Conservação\n\nDados conservados pelo período necessário, respeitando prazos legais.\n\n## 6. Segurança\n\nMedidas técnicas e organizativas adequadas implementadas.",
    "contentEn": "# Terms and Conditions of Use\n\n## 1. Data Controller\n\nFlorinhas do Vouga – IPSS, based in Aveiro, is the entity responsible for processing personal data collected through this digital platform.\n\n## 2. Purpose of Processing\n\nPersonal data is collected and processed for:\n- Appointment and scheduling management;\n- Material, transport and maintenance requisitions;\n- Institutional communication;\n- Compliance with legal obligations.\n\n## 3. Legal Basis\n\nProcessing is based on the data subject consent (Art. 6(1)(a) GDPR).\n\n## 4. Data Subject Rights\n\nThe data subject has the right to: access, rectification, erasure, portability and objection.\nDPO contact: dpo@florinhasdovouga.pt\n\n## 5. Retention Period\n\nData retained for the necessary period, respecting legal deadlines.\n\n## 6. Security\n\nAppropriate technical and organisational measures implemented.",
    "changeDescription": "Atualização dos termos para incluir novas finalidades de tratamento"
  }'
```

**Resposta:**
```json
{
  "version": 3
}
```

> **Importante:** Após publicar, todos os utilizadores verão um ecrã de aceitação obrigatória no próximo login.

### 4. Atualizar versão dos termos (forçar re-aceitação)

Incrementa a versão sem alterar o conteúdo — útil para forçar re-aceitação.

**Endpoint:** `POST /api/utilizadores/admin/terms-version?newVersion={N}&changeDescription={texto}`

```bash
curl -X POST -b "jwt=$TOKEN_DPO" \
  "http://localhost:8080/api/utilizadores/admin/terms-version?newVersion=4&changeDescription=Revisão%20anual%20obrigatória"
```

**Resposta:** `200 OK`

---

## Configuração de Retenção de Documentos

O DPO pode configurar o prazo de retenção de documentos conforme o RGPD.

### 1. Consultar prazo atual

**Endpoint:** `GET /api/config/documento/retencao`

```bash
curl -b "jwt=$TOKEN_DPO" \
  http://localhost:8080/api/config/documento/retencao
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
curl -X PUT -b "jwt=$TOKEN_DPO" \
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
|----------|--------|
| Gerir termos e condições (consultar, editar, publicar) | ✅ |
| Configurar retenção de documentos | ✅ |
| Consultar logs de auditoria | ❌ |
| Gerir utilizadores | ❌ |
| Criar/ver requisições | ❌ |
| Enviar relatórios por email | ❌ |
| Aceder à plataforma web | ❌ |
