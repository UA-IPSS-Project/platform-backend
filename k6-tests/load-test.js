import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
import {
  USERS,
  checkHealth,
  seedUsers,
  loginUtente,
  listMarcacoesUtente,
  getCurrentUser,
  createMarcacaoRemota,
} from './common.js';

const errorRate = new Rate('errors');
const loginErrorRate = new Rate('login_errors');
const marcacaoErrorRate = new Rate('marcacao_errors');
const authErrorRate = new Rate('auth_errors');

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '1m', target: 15 },
    { duration: '2m', target: 25 },
    { duration: '1m', target: 25 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2500'],
    errors: ['rate<0.15'],
    login_errors: ['rate<0.05'],
    auth_errors: ['rate<0.1'],
    marcacao_errors: ['rate<0.2'],
  },
};

export function setup() {
  checkHealth();
  seedUsers(USERS);
  return { users: USERS };
}

export default function loadTest(data) {
  const user = data.users[__VU % data.users.length];

  const session = loginUtente(user);
  const loginOk = !!session;
  loginErrorRate.add(!loginOk);

  if (!loginOk) {
    errorRate.add(true);
    sleep(1);
    return;
  }

  const meRes = getCurrentUser(session);
  const meOk = check(meRes, {
    'load auth me 200': (r) => r.status === 200,
  });
  authErrorRate.add(!meOk);

  const listRes = listMarcacoesUtente(session);
  const listOk = check(listRes, {
    'load list status 200': (r) => r.status === 200,
    'load list body is array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch {
        return false;
      }
    },
  });

  const createRes = createMarcacaoRemota(session, (__VU * 100000) + __ITER);
  const createOk = check(createRes, {
    'load create status acceptable': (r) => r.status === 200 || r.status === 400 || r.status === 409,
  });

  const businessFailure = !(createRes.status === 200 || createRes.status === 400 || createRes.status === 409);
  marcacaoErrorRate.add(businessFailure);

  errorRate.add(!(meOk && listOk && createOk));

  sleep(1);
}

export function handleSummary(data) {
  const reportName = __ENV.REPORT_NAME || "load-test";
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    [`./results/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`./results/${reportName}.json`]: JSON.stringify(data),
  };
}