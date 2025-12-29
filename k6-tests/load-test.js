import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
// Importações para relatórios
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

// Métricas customizadas
const errorRate = new Rate('errors');
const loginErrorRate = new Rate('login_errors');
const marcacaoErrorRate = new Rate('marcacao_errors');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 20 },
    { duration: '2m', target: 20 },
    { duration: '30s', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    errors: ['rate<0.1'],
    login_errors: ['rate<0.05'],
    marcacao_errors: ['rate<0.15'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// ... (MANTER AS TUAS FUNÇÕES: ensureUserExists, getRandomFutureDateTime, login, criarMarcacao, listarMarcacoes IGUAIS) ...
// Para poupar espaço aqui, assume que o código das funções auxiliares e a lista 'users' continuam iguais ao teu ficheiro original.
// Se precisares que eu copie tudo, diz-me.

// Pool de utilizadores de teste
const users = [
  { email: 'k6test1@gmail.com', nif: '123456781', password: 'Test1234', nome: 'K6 User 1' },
  { email: 'k6test2@gmail.com', nif: '123456782', password: 'User5678', nome: 'K6 User 2' },
  { email: 'k6test3@gmail.com', nif: '123456783', password: 'Load9012', nome: 'K6 User 3' },
  { email: 'k6test4@gmail.com', nif: '123456784', password: 'Perf3456', nome: 'K6 User 4' },
  { email: 'k6test5@gmail.com', nif: '123456785', password: 'Auto7890', nome: 'K6 User 5' },
];

function ensureUserExists(user) {
    // ... manter lógica original ...
    const registerRes = http.post(
        `${BASE_URL}/api/auth/register/utente`,
        JSON.stringify({
          nome: user.nome,
          email: user.email,
          nif: user.nif,
          telefone: '9' + user.nif.slice(1),
          password: user.password,
          dataNasc: '1990-01-01',
        }),
        { headers: { 'Content-Type': 'application/json' } }
      );
      return registerRes.status === 200 || registerRes.status === 201 || registerRes.status === 409;
}

// ... manter restante lógica ...

export default function () {
  // ... Manter a lógica exata do teu default function ...
  // Apenas a copiar um excerto para referência:
  const user = users[Math.floor(Math.random() * users.length)];
  ensureUserExists(user);
  sleep(0.2);

  const loginRes = http.post(
    `${BASE_URL}/api/auth/login/utente`,
    JSON.stringify({ nif: user.nif, password: user.password }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'Login' } }
  );
  
  // ... resto do teu código ...
  const loginOk = check(loginRes, { 'login OK': (r) => r.status === 200 });
  if(!loginOk) return;

  const token = loginRes.json('token');
  
  // Listar e Criar logic...
  const listRes = http.get(`${BASE_URL}/api/marcacoes`, { headers: { Authorization: `Bearer ${token}` } });
  
  sleep(1);
}

// --- NOVA FUNÇÃO DE RELATÓRIO (SUBSTITUI A MANUAL) ---
export function handleSummary(data) {
  const reportName = __ENV.REPORT_NAME || "load-test";
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    [`./results/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`./results/${reportName}.json`]: JSON.stringify(data),
  };
}