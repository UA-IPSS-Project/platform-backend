import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const loginDuration = new Trend('login_duration');
const marcacoesDuration = new Trend('marcacoes_list_duration');
const calendarioDuration = new Trend('calendario_duration');
const requisicoesDuration = new Trend('requisicoes_duration');

export const options = {
  insecureSkipTLSVerify: true,
  scenarios: {
    tempo_resposta: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
      tags: { scenario: 'tempo_resposta' },
    },
    throughput: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 20 },
        { duration: '40s', target: 50 },
        { duration: '20s', target: 0 },
      ],
      startTime: '35s',
      tags: { scenario: 'throughput' },
    },
  },
  thresholds: {
    'login_duration': ['p(95)<1000'],
    'marcacoes_list_duration': ['p(95)<500'],
    'calendario_duration': ['p(95)<300'],
    'requisicoes_duration': ['p(95)<300'],
    http_req_failed: ['rate<0.05'],
    http_reqs: ['rate>10'],
  },
};

const BASE_URL = 'https://20.63.17.2:3443';

// Estado por VU — persiste entre iterações
let authHeaders = null;

// Executado UMA VEZ por VU antes das iterações
export function setup() { }

export default function () {
  // Login só na primeira iteração de cada VU
  if (!authHeaders) {
    const start = Date.now();
    const loginRes = http.post(
      `${BASE_URL}/api/auth/login/funcionario`,
      JSON.stringify({
        email: 'secretaria@florinhasdovouga.pt',
        password: 'sec123',
      }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    loginDuration.add(Date.now() - start);

    const ok = check(loginRes, { 'login 200': (r) => r.status === 200 });
    if (!ok) { sleep(2); return; }

    const jwt = loginRes.cookies['jwt'] ? loginRes.cookies['jwt'][0].value : null;
    const xsrf = loginRes.cookies['XSRF-TOKEN'] ? loginRes.cookies['XSRF-TOKEN'][0].value : null;

    if (!jwt) { sleep(2); return; }

    // Guarda headers para reutilizar em todas as iterações seguintes
    authHeaders = {
      headers: {
        'Content-Type': 'application/json',
        'Cookie': `jwt=${jwt}; XSRF-TOKEN=${xsrf}`,
        'X-XSRF-TOKEN': xsrf,
      },
    };
  }

  // MARCAÇÕES — GET, sem rate limit
  group('Marcações - Leitura', () => {
    const t = Date.now();
    const res = http.get(`${BASE_URL}/api/marcacoes`, authHeaders);
    marcacoesDuration.add(Date.now() - t);
    check(res, {
      'marcações 200': (r) => r.status === 200,
      'marcações <500ms': (r) => r.timings.duration < 500,
    });
  });

  // CALENDÁRIO — GET, sem rate limit
  group('Calendário - Disponibilidade', () => {
    const t = Date.now();
    const res = http.get(
      `${BASE_URL}/api/calendario/bloqueios?tipo=SECRETARIA`,
      authHeaders
    );
    calendarioDuration.add(Date.now() - t);
    check(res, {
      'calendario 200': (r) => r.status === 200,
      'calendario <300ms': (r) => r.timings.duration < 300,
    });
  });

  // REQUISIÇÕES — GET, sem rate limit
  group('Requisições - Leitura', () => {
    const t = Date.now();
    const res = http.get(`${BASE_URL}/api/requisicoes`, authHeaders);
    requisicoesDuration.add(Date.now() - t);
    check(res, {
      'requisições 200': (r) => r.status === 200,
      'requisições <300ms': (r) => r.timings.duration < 300,
    });
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    'resumo_final.json': JSON.stringify(data, null, 2),
    'relatorio.html': htmlReport(data),
    stdout: 'Teste finalizado.\n',
  };
}