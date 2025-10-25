# Plataforma Digital IPSS — Backend

### Projeto de Engenharia Informática — Universidade de Aveiro  
**Módulo:** Lógica de Negócio e API REST (Spring Boot)

---

## Contexto

Este repositório contém o **backend** da Plataforma Digital para Marcação Eficiente de Atendimento na Secretaria da IPSS.  
É responsável por gerir a lógica de negócio, processar pedidos REST do frontend e comunicar com a base de dados **PostgreSQL**.  

O sistema centraliza funcionalidades de:
- Gestão de marcações (Secretaria e Balneário Social);  
- Requisições de materiais, transportes e manutenções;  
- Submissão de formulários e notificações automáticas.

---

## Tecnologias Principais

| Tecnologia | Função |
|-------------|--------|
| **Java 17+** | Linguagem principal |
| **Spring Boot 3.x** | Framework de backend |
| **Spring Data JPA** | Mapeamento objeto-relacional |
| **PostgreSQL** | Base de dados relacional |
| **Maven** | Gestão de dependências |
| **Lombok** | Simplificação de código Java |
| **Spring Validation** | Validação de dados de entrada |
| **Spring Web** | Exposição da API REST |

---

## Estrutura do Projeto

```plaintext
platform-backend/
│
├── src/
│   ├── main/
│   │   ├── java/com/ipss/platform/
│   │   │   ├── controller/       → Endpoints REST
│   │   │   ├── service/          → Regras de negócio
│   │   │   ├── repository/       → Interfaces JPA
│   │   │   ├── model/            → Entidades e DTOs
│   │   │   └── config/           → Configurações globais
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql / schema.sql
│   └── test/
│       └── ...                   → Testes unitários
│
├── pom.xml
└── README.md
