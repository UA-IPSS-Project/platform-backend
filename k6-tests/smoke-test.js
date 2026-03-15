import { check, sleep } from 'k6';
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
  const reportName = __ENV.REPORT_NAME || "smoke-test";
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    [`./results/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
    [`./results/${reportName}.json`]: JSON.stringify(data),
  };
}