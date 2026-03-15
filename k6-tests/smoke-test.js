import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
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

const smokeMarcacaoCriada = new Counter('smoke_marcacao_criada');
const smokeMarcacaoNaoCriada = new Counter('smoke_marcacao_nao_criada');

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
  const created = metricCount(data, 'smoke_marcacao_criada');
  const notCreated = metricCount(data, 'smoke_marcacao_nao_criada');

  return [
    '=== Smoke Test (Resumo) ===',
    `requests: ${httpReqs} | iterations: ${iterations}`,
    `latencia p95: ${p95.toFixed(2)}ms | http_req_failed: ${httpReqFailedRate}%`,
    `marcacao criada: ${created} | marcacao nao criada: ${notCreated}`,
  ].join('\n');
}

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.05'],
  },
};

export function setup() {
  checkHealth();
  seedUsers(USERS);
  return { users: USERS };
}

export default function smokeTest(data) {
  const user = data.users[0];
  const session = loginUtente(user);

  if (!session) {
    console.error('Login falhou no smoke test');
    return;
  }

  const meRes = getCurrentUser(session);
  check(meRes, {
    'auth me status 200': (r) => r.status === 200,
    'auth me id matches': (r) => {
      try {
        return r.json('id') === session.userId;
      } catch {
        return false;
      }
    },
  });

  const beforeListRes = listMarcacoesUtente(session);
  check(beforeListRes, {
    'list before status 200': (r) => r.status === 200,
    'list before is array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch {
        return false;
      }
    },
  });

  const maxCreateAttempts = 64;
  let createRes = null;
  let createSucceeded = false;
  const seedBase = Date.now() % 11456;

  for (let attempt = 0; attempt < maxCreateAttempts; attempt += 1) {
    const seed = seedBase + (attempt * 97);
    createRes = createMarcacaoRemota(session, seed);

    if (createRes.status === 200) {
      createSucceeded = true;
      break;
    }
  }

  check(createRes || { status: 0 }, {
    'create marcacao status 200': () => createSucceeded,
    'create marcacao has id': (r) => {
      if (!createSucceeded) {
        return false;
      }
      try {
        return !!r.json('id');
      } catch {
        return false;
      }
    },
  });

  if (createSucceeded) {
    smokeMarcacaoCriada.add(1);
  } else {
    smokeMarcacaoNaoCriada.add(1);
  }

  const afterListRes = listMarcacoesUtente(session);
  check(afterListRes, {
    'list after status 200': (r) => r.status === 200,
    'list after is array': (r) => {
      try {
        return Array.isArray(r.json());
      } catch {
        return false;
      }
    },
  });

  sleep(1);
}

export function handleSummary(data) {
  const testName = 'smoke_test';
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