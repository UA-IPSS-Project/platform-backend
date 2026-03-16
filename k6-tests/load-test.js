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

const MAX_ERROR_SAMPLES_PER_VU = Number(__ENV.MAX_ERROR_SAMPLES || 0);
let createErrorSamplesPrinted = 0;

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
      return { res, retries: retry - 1 };
    }

    sleep(0.05 * retry);
    res = createMarcacaoRemota(session, seed + retry);
  }

  return { res, retries: MAX_SERIALIZATION_RETRIES };
}

function logUnexpectedCreateFailure(res) {
  if (createErrorSamplesPrinted >= MAX_ERROR_SAMPLES_PER_VU || __VU > 2) {
    return;
  }

  createErrorSamplesPrinted += 1;

  let bodySnippet = '';
  try {
    bodySnippet = (res.body || '').slice(0, 280).replace(/\s+/g, ' ');
  } catch {
    bodySnippet = '<body-unavailable>';
  }

  console.error(
    `[load][unexpected-create] vu=${__VU} iter=${__ITER} status=${res.status} body="${bodySnippet}"`
  );
}

function metricCount(data, metricName) {
  return data.metrics?.[metricName]?.values?.count ?? 0;
}

function metricRate(data, metricName) {
  return data.metrics?.[metricName]?.values?.rate ?? 0;
}

function metricP95(data, metricName) {
  return data.metrics?.[metricName]?.values?.['p(95)'] ?? 0;
}

function buildCompactSummary(data) {
  const httpReqs = metricCount(data, 'http_reqs');
  const iterations = metricCount(data, 'iterations');
  const p95 = metricP95(data, 'http_req_duration');
  const httpReqFailedRate = (metricRate(data, 'http_req_failed') * 100).toFixed(2);
  const flowErrorRate = (metricRate(data, 'errors') * 100).toFixed(2);

  return [
    '=== Load Test (Resumo) ===',
    `requests: ${httpReqs} | iterations: ${iterations}`,
    `latencia p95: ${p95.toFixed(2)}ms | http_req_failed: ${httpReqFailedRate}% | erros fluxo: ${flowErrorRate}%`,
    'create marcacao: ver check "load create status acceptable" no relatorio',
  ].join('\n');
}

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

  const { res: createRes } = createMarcacaoWithRetry(session, (__VU * 100000) + __ITER);

  const createOk = check(createRes, {
    'load create status acceptable': (r) => [200, 400, 409].includes(r.status),
  });

  const businessFailure = ![200, 400, 409].includes(createRes.status);
  marcacaoErrorRate.add(businessFailure);

  if (businessFailure) {
    logUnexpectedCreateFailure(createRes);
  }

  errorRate.add(!(meOk && listOk && createOk));

  sleep(1);
}

export function handleSummary(data) {
  const testName = 'load_test';
  const reportName = __ENV.REPORT_NAME || testName;
  const reportDir = (__ENV.REPORT_DIR || `./results/${testName}`).replace(/\/$/, '');
  const fullSummary = String(__ENV.FULL_SUMMARY || 'false').toLowerCase() === 'true';
  const saveSummaryFile = String(__ENV.SAVE_SUMMARY_FILE || 'false').toLowerCase() === 'true';
  const compactSummary = buildCompactSummary(data);

  const summary = {
    'stdout': fullSummary
      ? `${compactSummary}\n\n${textSummary(data, { indent: ' ', enableColors: true })}`
      : `${compactSummary}\n`,
    [`${reportDir}/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`${reportDir}/${reportName}.json`]: JSON.stringify(data),
  };

  if (saveSummaryFile) {
    summary[`${reportDir}/${reportName}_resumo.txt`] = `${compactSummary}\n`;
  }

  return summary;
}