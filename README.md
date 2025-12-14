# Plataforma Digital IPSS Florinhas

<div align="center">

**Sistema Integrado de Gestão de Marcações e Atendimento**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18-61dafb.svg)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

*Projeto de Engenharia Informática — Universidade de Aveiro*

</div>

---

## Contexto

Este repositório contém a **Plataforma Digital para Marcação Eficiente de Atendimento na Secretaria da IPSS Florinhas**. O sistema é responsável por gerir a lógica de negócio, processar pedidos REST do frontend e comunicar com a base de dados PostgreSQL.

Plataforma digital desenvolvida para a **IPSS Florinhas** que revoluciona o agendamento de atendimentos na secretaria, eliminando filas e otimizando o tempo dos utentes e funcionários.

O sistema centraliza funcionalidades de:
- Gestão de marcações (Secretaria e Balneário Social)
- Requisições de materiais, transportes e manutenções
- Submissão de formulários e notificações automáticas

### Funcionalidades Principais

#### Para Utentes
- Agendamento online de marcações com escolha de horário
- Visualização do calendário de disponibilidade
- Sistema de notificações de confirmação e lembretes
- Gestão completa do perfil pessoal
- Histórico de marcações e documentos

#### Para Secretaria
- Agenda semanal interativa com visualização em tempo real
- Criação rápida de marcações para utentes (presencial/telefone)
- Bloqueio de horários para fins de semana e feriados
- Dashboard com estatísticas e marcações do dia
- Gestão de utilizadores e permissões
- Histórico completo de atendimentos

### Características Técnicas
- **Autenticação JWT** com roles (UTENTE/FUNCIONARIO)
- **Reserva temporária de slots** para evitar conflitos de agendamento simultâneo
- **Validação em tempo real** de disponibilidade
- **Bloqueio automático** de fins de semana e feriados
- **API RESTful** completa e documentada
- **Responsive Design** para mobile e desktop
- **Spring Security** para autenticação e autorização
- **Spring Data JPA** para mapeamento objeto-relacional

---

## Tecnologias Principais

| Tecnologia | Versão | Função |
|------------|--------|--------|
| **Java** | 17+ | Linguagem principal |
| **Spring Boot** | 3.2.x | Framework de backend |
| **Spring Security** | 6.x | Autenticação e autorização |
| **Spring Data JPA** | 3.x | Mapeamento objeto-relacional e persistência |
| **PostgreSQL** | 15+ | Base de dados relacional |
| **JWT** | - | Tokens de autenticação |
| **Maven** | 3.6+ | Gestão de dependências |
| **Lombok** | 1.18+ | Simplificação de código Java e redução de boilerplate |
| **Spring Validation** | 3.x | Validação de dados de entrada |
| **Spring Web** | - | Exposição da API REST |
| **React** | 18 | UI library (frontend) |
| **TypeScript** | 5.x | Type safety (frontend) |
| **Vite** | 5.x | Build tool e dev server (frontend) |
| **Tailwind CSS** | 3.x | Styling framework (frontend) |
| **Docker** | - | Containerização |
| **Docker Compose** | - | Orquestração multi-container |
| **Nginx** | - | Servidor web (produção frontend) |

---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    PLATAFORMA IPSS                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐         ┌──────────────┐                 │
│  │   Frontend   │◄───────►│   Backend    │                 │
│  │  React/Vite  │   HTTP  │ Spring Boot  │                 │
│  │  Port: 3000  │         │  Port: 8080  │                 │
│  └──────────────┘         └───────┬──────┘                 │
│                                    │                         │
│                                    ▼                         │
│                           ┌─────────────────┐               │
│                           │   PostgreSQL    │               │
│                           │   Port: 5432    │               │
│                           └─────────────────┘               │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Estrutura de Módulos

**Backend (Spring Boot)**
```
marcacoes/
├── controller/         → Endpoints REST (Auth, Marcações, Calendário, Utilizadores)
├── service/           → Lógica de negócio e regras de validação
├── repository/        → Acesso a dados via JPA
├── domain/           → Entidades JPA (Utilizador, Marcacao, BloqueioAgenda)
├── dto/              → Data Transfer Objects
├── security/         → JWT, autenticação e autorização
└── config/           → Configurações Spring e CORS
```

**Frontend (React + TypeScript)**
```
src/
├── components/       → Componentes React reutilizáveis
│   ├── secretary/   → Dashboard, agenda, gestão de marcações
│   ├── client/      → Interface do utente
│   └── ui/          → Componentes base (shadcn/ui)
├── services/        → API client e comunicação HTTP
├── contexts/        → Estado global (AuthContext)
└── styles/          → Estilos globais e temas
```

---

## Fluxo de Desenvolvimento

### Padrão de Arquitetura
O microserviço segue o padrão MVC com separação de responsabilidades:

```
Controller → Service → Repository → Entity
     ↓
   DTO ↔ Mapper
```

**Camadas:**
- **Controller**: Endpoints REST e validação de entrada
- **Service**: Regras de negócio e orquestração
- **Repository**: Interface de acesso a dados (JPA)
- **Domain/Entity**: Entidades JPA mapeadas para o banco
- **DTO**: Data Transfer Objects para comunicação API
- **Security**: JWT, autenticação e filtros

### Configuração Spring Boot Initializer
- **Group**: pt.florinhas
- **Artifact**: marcacoes
- **Description**: Serviço de marcações da plataforma Florinhas

**Dependências:**
- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- H2 Database (desenvolvimento)
- PostgreSQL Driver (produção)
- Lombok
- Spring Boot DevTools
- Spring Boot Actuator

---

## Como Executar o Projeto

### Pré-requisitos

- **Docker** e **Docker Compose** instalados
- **Porta 3000** (frontend) livre
- **Porta 8080** (backend) livre  
- **Porta 5432** (PostgreSQL) livre

### Início Rápido com Docker Compose (RECOMENDADO)

1. **Clone o repositório**
```bash
git clone https://github.com/UA-IPSS-Project/platform-backend.git
cd platform-backend
```

2. **Clone o frontend** (no mesmo diretório pai)
```bash
cd ..
git clone https://github.com/UA-IPSS-Project/platform-frontend.git
```

Estrutura esperada:
```
PEI/
├── platform-backend/
│   ├── marcacoes/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/pt/florinhas/marcacoes/
│   │   │   │   │   ├── controller/       → Endpoints REST
│   │   │   │   │   ├── service/          → Regras de negócio
│   │   │   │   │   ├── repository/       → Interfaces JPA
│   │   │   │   │   ├── domain/           → Entidades JPA
│   │   │   │   │   ├── dto/              → Data Transfer Objects
│   │   │   │   │   ├── security/         → JWT e autenticação
│   │   │   │   │   └── config/           → Configurações Spring
│   │   │   │   └── resources/
│   │   │   │       ├── application.properties
│   │   │   │       └── application-prod.properties
│   │   │   └── test/
│   │   │       └── java/                 → Testes unitários
│   │   ├── pom.xml
│   │   └── Dockerfile
│   ├── docker-compose.yml
│   └── README.md
└── platform-frontend/
```

3. **Inicie todos os serviços**
```bash
cd platform-backend
docker-compose up --build
```

4. **Acesse a aplicação**
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432


## Desenvolvimento Local (Sem Docker)

### Backend

1. **Requisitos**
   - Java 17+
   - Maven 3.6+
   - PostgreSQL 15+ rodando localmente

2. **Configure o banco de dados**
```bash
# Criar base de dados
createdb -U postgres florinhas
```

3. **Configure application.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/florinhas
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
```

4. **Execute o backend**
```bash
cd platform-backend/marcacoes
mvn spring-boot:run
```

Backend estará em: `http://localhost:8080`

### Frontend

1. **Requisitos**
   - Node.js 18+
   - npm ou yarn

2. **Instale dependências**
```bash
cd platform-frontend
npm install
```

3. **Execute em modo desenvolvimento**
```bash
npm run dev
```

Frontend estará em: `http://localhost:5173`

4. **Build para produção**
```bash
npm run build
```

---

## API Endpoints Principais

### Autenticação
```
POST   /api/auth/login           → Login (retorna JWT)
POST   /api/auth/register        → Registro de novo utente
```

### Marcações
```
GET    /api/marcacoes            → Listar marcações do utilizador
POST   /api/marcacoes/remota     → Criar marcação (utente)
POST   /api/marcacoes/presencial → Criar marcação (secretaria)
POST   /api/marcacoes/reservar-slot → Reservar temporariamente
DELETE /api/marcacoes/libertar-slot/{id} → Liberar reserva
PUT    /api/marcacoes/{id}       → Atualizar marcação
DELETE /api/marcacoes/{id}       → Cancelar marcação
```

### Calendário
```
GET    /api/calendario/bloqueios → Listar bloqueios (feriados)
POST   /api/calendario/bloquear  → Criar bloqueio
GET    /api/calendario/verificar-slot → Verificar disponibilidade
DELETE /api/calendario/{id}      → Remover bloqueio
```

### Utilizadores
```
GET    /api/utilizadores/{id}    → Obter perfil
PUT    /api/utilizadores/{id}    → Atualizar perfil
GET    /api/utilizadores/utentes/count → Contar utentes
GET    /api/utilizadores/funcionarios/count → Contar funcionários
```


---

## Funcionalidades Destacadas

### Sistema de Reserva Temporária
Quando um utente ou secretária abre o dialog de marcação, o slot é **reservado temporariamente** por 15 minutos, evitando conflitos de agendamento simultâneo. Se o utilizador não completar a marcação nesse período, a reserva é automaticamente liberada.

### Bloqueio Inteligente de Horários
- Fins de semana automaticamente bloqueados
- Feriados configuráveis pela secretaria
- Validação em tempo real de disponibilidade
- Filtragem automática no calendário (apenas dias úteis aparecem nos dropdowns)
- Bloqueios podem ser para dia inteiro ou horários específicos

### Atualização em Tempo Real
- Refresh automático da agenda ao visualizar slots reservados
- Validação de disponibilidade ao abrir dialogs de marcação
- Sincronização entre interface de utentes e secretaria
- Notificações via toast para feedback imediato

### UX Otimizada
- Pré-seleção do primeiro dia útil disponível no dialog de marcação rápida
- Cores distintas por status de marcação (scheduled, in-progress, completed, cancelled)
- Histórico acessível de marcações passadas (secretaria pode visualizar mas não criar no passado)
- Perfil editável com campos protegidos (Nome, Email, NIF, Data Nascimento não editáveis)
- Validação de campos em tempo real (NIF 9 dígitos, telefone 9 dígitos, email válido)

---

## Configuração de Ambiente

### Variáveis de Ambiente (Docker)

O `docker-compose.yml` define as seguintes variáveis:

```yaml
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/florinhas
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin123
SPRING_JPA_HIBERNATE_DDL_AUTO=update
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### Profiles do Spring

- **default**: Usa `application.properties` (desenvolvimento local)
- **prod**: Usa `application-prod.properties` (Docker/produção)

---

## Contribuidores

Projeto desenvolvido no âmbito da UC de **Projeto de Engenharia Informática** da Universidade de Aveiro.

---

## Licença

Este projeto está sob licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## Suporte

Para questões ou problemas:
- Abra uma [issue](https://github.com/UA-IPSS-Project/platform-backend/issues)
- Contacte a equipa de desenvolvimento

---

<div align="center">

**Desenvolvido para a IPSS Florinhas**

[Backend](https://github.com/UA-IPSS-Project/platform-backend) • [Frontend](https://github.com/UA-IPSS-Project/platform-frontend)

</div>
