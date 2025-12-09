# Sistema de Autenticação JWT

## Configuração Completa

O sistema de autenticação JWT foi implementado com sucesso. Aqui está o que foi criado:

### 📁 Estrutura de Arquivos

#### DTOs (`dto/`)
- `LoginRequest.java` - Request para login
- `RegisterRequest.java` - Request para registro
- `AuthResponse.java` - Response com token JWT

#### Security (`security/`)
- `JwtService.java` - Geração e validação de tokens JWT
- `JwtAuthenticationFilter.java` - Filtro que intercepta requests e valida tokens
- `CustomUserDetailsService.java` - Carrega dados do utilizador para autenticação

#### Service (`service/`)
- `AuthService.java` - Lógica de negócio para login e registro

#### Controller (`controller/`)
- `AuthController.java` - Endpoints REST de autenticação

#### Config (`config/`)
- `SecurityConfig.java` - Configuração do Spring Security

### 🔧 Configurações

As seguintes propriedades foram adicionadas ao `application.properties`:

```properties
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000  # 24 horas
```

### 📡 Endpoints Disponíveis

#### 1. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "nome": "João Silva",
  "role": "FUNCIONARIO"
}
```

#### 2. Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@example.com",
  "password": "password123",
  "nif": "123456789",
  "telefone": "912345678",
  "role": "UTENTE"
}
```

**Response:** Igual ao login

### 🔐 Como Usar no React

#### 1. Login
```javascript
const login = async (email, password) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();
    
    // Guardar token no localStorage
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify({
      id: data.id,
      email: data.email,
      nome: data.nome,
      role: data.role
    }));
    
    return data;
  } catch (error) {
    console.error('Erro no login:', error);
    throw error;
  }
};
```

#### 2. Fazer Requests Autenticados
```javascript
const criarMarcacao = async (marcacaoData) => {
  const token = localStorage.getItem('token');
  
  try {
    const response = await fetch('http://localhost:8080/api/marcacoes', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,  // 👈 Token no header
      },
      body: JSON.stringify(marcacaoData),
    });

    return await response.json();
  } catch (error) {
    console.error('Erro ao criar marcação:', error);
    throw error;
  }
};
```

#### 3. Context API para Autenticação (Recomendado)
```javascript
// AuthContext.jsx
import { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  useEffect(() => {
    // Recuperar token ao carregar
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
  }, []);

  const login = async (email, password) => {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();
    
    setToken(data.token);
    setUser({
      id: data.id,
      email: data.email,
      nome: data.nome,
      role: data.role
    });
    
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    
    return data;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const api = async (url, options = {}) => {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return fetch(`http://localhost:8080${url}`, {
      ...options,
      headers,
    });
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, api }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

#### 4. Usar o Context
```javascript
// App.jsx
import { AuthProvider } from './context/AuthContext';

function App() {
  return (
    <AuthProvider>
      <YourRoutes />
    </AuthProvider>
  );
}

// MarcacaoForm.jsx
import { useAuth } from './context/AuthContext';

function MarcacaoForm() {
  const { api, user } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const response = await api('/api/marcacoes', {
      method: 'POST',
      body: JSON.stringify(marcacaoData),
    });
    
    const data = await response.json();
    console.log('Marcação criada:', data);
  };

  return <form onSubmit={handleSubmit}>...</form>;
}
```

### 🛡️ Segurança

- **Token JWT**: Válido por 24 horas
- **Senha**: Encriptada com BCrypt
- **CORS**: Configurado para React (localhost:3000 e localhost:5173)
- **Stateless**: Sem sessão no servidor

### 🔑 Roles Disponíveis

- `FUNCIONARIO` - Para funcionários (secretária, balneário, etc.)
- `UTENTE` - Para utentes da plataforma

### 🧪 Testar a API

Após iniciar a aplicação, pode testar com:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'

# Request autenticado (substituir TOKEN)
curl -X GET http://localhost:8080/api/marcacoes \
  -H "Authorization: Bearer TOKEN_AQUI"
```

### ⚠️ Próximos Passos

1. Alterar a `jwt.secret` em produção para uma chave segura
2. Configurar HTTPS em produção
3. Implementar refresh tokens (opcional)
4. Adicionar rate limiting para prevenir ataques
