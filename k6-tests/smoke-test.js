import http from 'k6/http';
import { check, sleep } from 'k6';
// Importações para relatórios
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.1'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // 1. Health Check
  const healthRes = http.get(`${BASE_URL}/actuator/health`);
  const healthOk = check(healthRes, {
    'health check OK': (r) => r.status === 200,
  });

  if (!healthOk) {
    console.error('Health check falhou - backend pode não estar rodando');
    return;
  }

  sleep(1);

  // 2. Registar novo utilizador
  const randomId = Date.now() + Math.floor(Math.random() * 1000);
  const randomNum = Math.floor(Math.random() * 9000) + 1000;
  const newUser = {
    nome: `Teste K6 ${randomId}`,
    email: `k6test${randomId}@gmail.com`,
    nif: String(randomId).slice(-9).padStart(9, '1'),
    telefone: String(randomId).slice(-9).padStart(9, '9'),
    password: `Test${randomNum}`,
    dataNasc: '1990-01-01',
  };

  const registerRes = http.post(
    `${BASE_URL}/api/auth/register/utente`,
    JSON.stringify(newUser),
    { 
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Register' },
    }
  );

  check(registerRes, {
    'registro aceito ou usuário já existe': (r) => r.status === 200 || r.status === 201 || r.status === 409,
  });

  sleep(0.5);

  // 3. Login
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login/utente`,
    JSON.stringify({
      nif: newUser.nif,
      password: newUser.password,
    }),
    { 
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Login' },
    }
  );

  const loginOk = check(loginRes, {
    'login OK': (r) => r.status === 200,
    'token presente': (r) => {
      try {
        return r.json('token') !== undefined;
      } catch {
        return false;
      }
    },
  });

  if (loginOk) {
    const token = loginRes.json('token');
    sleep(0.5);

    // 4. Listar marcações
    const listRes = http.get(`${BASE_URL}/api/marcacoes`, {
      headers: { 
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      tags: { name: 'List Appointments' },
    });

    check(listRes, {
      'listagem OK': (r) => r.status === 200,
      'retorna array': (r) => Array.isArray(r.json()),
    });
  } else {
    console.error(`Login falhou: ${loginRes.status} - ${loginRes.body}`);
  }

  sleep(1);
}

// --- FUNÇÃO DE RELATÓRIO ---
export function handleSummary(data) {
  const reportName = __ENV.REPORT_NAME || "smoke-test";
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    [`./results/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`./results/${reportName}.json`]: JSON.stringify(data),
  };
}