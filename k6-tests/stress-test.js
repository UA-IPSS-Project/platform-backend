import http from 'k6/http';
import { check, sleep } from 'k6';
// Importações para relatórios
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

export const options = {
  stages: [
    { duration: '1m', target: 100 },
    { duration: '3m', target: 200 },
    { duration: '2m', target: 500 },
    { duration: '1m', target: 500 },
    { duration: '2m', target: 50 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(99)<5000'],
    http_req_failed: ['rate<0.3'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const users = [
  { email: 'k6test1@gmail.com', nif: '123456781', password: 'Test1234', nome: 'K6 User 1' },
  { email: 'k6test2@gmail.com', nif: '123456782', password: 'User5678', nome: 'K6 User 2' },
  { email: 'k6test3@gmail.com', nif: '123456783', password: 'Load9012', nome: 'K6 User 3' },
  { email: 'k6test4@gmail.com', nif: '123456784', password: 'Perf3456', nome: 'K6 User 4' },
  { email: 'k6test5@gmail.com', nif: '123456785', password: 'Auto7890', nome: 'K6 User 5' },
];

export default function () {
  const user = users[Math.floor(Math.random() * users.length)];

  // 1. Login
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login/utente`,
    JSON.stringify({ nif: user.nif, password: user.password }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'Login' } }
  );

  const loginOk = check(loginRes, {
    'login OK': (r) => r.status === 200,
    'token presente': (r) => {
        try { return r.json('token') !== undefined; } catch { return false; }
    },
  });

  if (!loginOk) {
    console.error(`Login falhou para ${user.email}: ${loginRes.status}`);
    sleep(1);
    return;
  }

  const token = loginRes.json('token');
  const utenteId = loginRes.json('id');

  // 2. Criar múltiplas marcações
  for (let i = 0; i < 3; i++) {
    const futureDate = new Date(Date.now() + (i + 1) * 86400000).toISOString().slice(0, 19);
    http.post(
      `${BASE_URL}/api/marcacoes/remota`,
      JSON.stringify({
        data: futureDate,
        assunto: `Teste Stress ${i + 1}`,
        descricao: 'Marcação de teste de stress',
        utenteId: utenteId,
      }),
      {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        tags: { name: 'Criar Marcação' }
      }
    );
    sleep(0.1);
  }

  sleep(1);
}

// --- FUNÇÃO DE RELATÓRIO ---
export function handleSummary(data) {
  const reportName = __ENV.REPORT_NAME || "stress-test";
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    [`./results/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`./results/${reportName}.json`]: JSON.stringify(data),
  };
}