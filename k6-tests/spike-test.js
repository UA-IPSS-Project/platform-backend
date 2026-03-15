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

const MAX_SERIALIZATION_RETRIES = Number(__ENV.MAX_SERIALIZATION_RETRIES || 2);

function isSerializableConflict(res) {
  if (!res || res.status !== 500) {
    return false;
  }

  const body = (res.body || '').toLowerCase();
  return body.includes('could not serialize access') || body.includes('transaction might succeed if retried');
}

function createMarcacaoWithRetry(session, seed) {
  let res = createMarcacaoRemota(session, seed);

  for (let retry = 1; retry <= MAX_SERIALIZATION_RETRIES; retry += 1) {
    if (!isSerializableConflict(res)) {
      return res;
    }

    sleep(0.05 * retry);
    res = createMarcacaoRemota(session, seed + retry);
  }

  return res;
}

export const options = {
  stages: [
    { duration: '45s', target: 5 },
    { duration: '20s', target: 90 },
    { duration: '1m', target: 90 },
    { duration: '30s', target: 8 },
    { duration: '25s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<3500'],
    http_req_failed: ['rate<0.2'],
    errors: ['rate<0.35'],
    login_errors: ['rate<0.12'],
    auth_errors: ['rate<0.2'],
    marcacao_errors: ['rate<0.4'],
  },
};

export function setup() {
  checkHealth();
  seedUsers(USERS);
  return { users: USERS };
}

export default function spikeTest(data) {
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
    'spike auth me 200': (r) => r.status === 200,
  });
  authErrorRate.add(!meOk);

  const listRes = listMarcacoesUtente(session);
  const listOk = check(listRes, {
    'spike list status 200': (r) => r.status === 200,
    'spike list body is array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch {
        return false;
      }
    },
  });

  const seed = (__VU * 1000000) + __ITER;
  const createRes = createMarcacaoWithRetry(session, seed);
  const createOk = check(createRes, {
    'spike create status acceptable': (r) => [200, 400, 409].includes(r.status),
  });

  const businessFailure = ![200, 400, 409].includes(createRes.status);
  marcacaoErrorRate.add(businessFailure);

  errorRate.add(!(meOk && listOk && createOk));

  sleep(1);
}

export function handleSummary(data) {
  const testName = 'spike_test';
  const reportName = __ENV.REPORT_NAME || testName;
  const reportDir = (__ENV.REPORT_DIR || `./results/${testName}`).replace(/\/$/, '');

  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    [`${reportDir}/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`${reportDir}/${reportName}.json`]: JSON.stringify(data),
  };
}
