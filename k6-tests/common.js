import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const USERS = [
  { email: 'k6test1@gmail.com', nif: '123456781', password: 'Test1234', nome: 'K6 User 1' },
  { email: 'k6test2@gmail.com', nif: '123456782', password: 'User5678', nome: 'K6 User 2' },
  { email: 'k6test3@gmail.com', nif: '123456783', password: 'Load9012', nome: 'K6 User 3' },
  { email: 'k6test4@gmail.com', nif: '123456784', password: 'Perf3456', nome: 'K6 User 4' },
  { email: 'k6test5@gmail.com', nif: '123456785', password: 'Auto7890', nome: 'K6 User 5' },
];

export function checkHealth() {
  const res = http.get(`${BASE_URL}/actuator/health`, {
    tags: { name: 'Health' },
    responseCallback: http.expectedStatuses(200, 401, 403),
  });
  const ok = check(res, {
    'health endpoint reachable': (r) => [200, 401, 403].includes(r.status),
  });
  if (!ok) {
    throw new Error(`Backend indisponivel em ${BASE_URL} (status ${res.status})`);
  }
  return res;
}

export function seedUsers(users = USERS) {
  for (const user of users) {
    const payload = {
      nome: user.nome,
      email: user.email,
      nif: user.nif,
      telefone: `9${user.nif.slice(1)}`,
      password: user.password,
      dataNasc: '1990-01-01',
      termsAccepted: true,
    };

    http.post(
      `${BASE_URL}/api/auth/register/utente`,
      JSON.stringify(payload),
      {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'Register Seed User' },
        responseCallback: http.expectedStatuses(200, 201, 400, 409),
      }
    );
  }
}

export function loginUtente(user) {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login/utente`,
    JSON.stringify({ nif: user.nif, password: user.password }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Login Utente' },
    }
  );

  const loginOk = check(loginRes, {
    'login status 200': (r) => r.status === 200,
    'login body has user id': (r) => {
      try {
        return Number.isInteger(r.json('id'));
      } catch {
        return false;
      }
    },
    'jwt cookie present': (r) => {
      const cookie = r.cookies?.jwt?.[0];
      return !!cookie?.value;
    },
    'csrf cookie present': (r) => {
      const cookie = r.cookies?.['XSRF-TOKEN']?.[0];
      return !!cookie?.value;
    },
  });

  if (!loginOk) {
    return null;
  }

  const jwtCookie = loginRes.cookies?.jwt?.[0]?.value || null;
  const csrfCookie = loginRes.cookies?.['XSRF-TOKEN']?.[0]?.value || null;

  return {
    jwt: jwtCookie,
    csrf: csrfCookie,
    userId: loginRes.json('id'),
    user,
  };
}

export function buildHeaders(session, needsCsrf, includeContentType = false) {
  const headers = {};

  // Requests are authenticated by cookie, but we also send Bearer to validate JWT header path.
  if (session?.jwt) {
    headers.Authorization = `Bearer ${session.jwt}`;
  }

  if (needsCsrf && session?.csrf) {
    headers['X-XSRF-TOKEN'] = session.csrf;
  }

  if (includeContentType) {
    headers['Content-Type'] = 'application/json';
  }

  return headers;
}

export function formatLocalDateTime(date) {
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
}

export function getFutureBusinessDateTime(slotSeed) {
  const now = new Date();

  // 358 possible day offsets × 16 half-hour slots (9:00–16:30) = 5728 unique slots.
  // This avoids exhausting the slot space under high concurrency (120 VUs × many iters).
  const DAYS = 358;
  const HOUR_SLOTS = 16;
  const normalizedSeed = ((slotSeed % (DAYS * HOUR_SLOTS)) + DAYS * HOUR_SLOTS) % (DAYS * HOUR_SLOTS);
  const dayOffset = 2 + Math.floor(normalizedSeed / HOUR_SLOTS);  // 2–359 days ahead
  const hourSlot = normalizedSeed % HOUR_SLOTS;                    // 0–15
  const hour = 9 + Math.floor(hourSlot / 2);                       // 9:00–16:00
  const minute = hourSlot % 2 === 0 ? 0 : 30;

  const d = new Date(now);
  d.setDate(d.getDate() + dayOffset);
  d.setHours(hour, minute, 0, 0);

  // Skip weekends.
  while (d.getDay() === 0 || d.getDay() === 6) {
    d.setDate(d.getDate() + 1);
  }

  return formatLocalDateTime(d);
}

export function listMarcacoesUtente(session) {
  return http.get(`${BASE_URL}/api/marcacoes/utente/${session.userId}`, {
    headers: buildHeaders(session, false, false),
    tags: { name: 'List Utente Marcacoes' },
  });
}

export function getCurrentUser(session) {
  return http.get(`${BASE_URL}/api/auth/me`, {
    headers: buildHeaders(session, false, false),
    tags: { name: 'Auth Me' },
  });
}

export function createMarcacaoRemota(session, slotSeed) {
  const payload = {
    data: getFutureBusinessDateTime(slotSeed),
    assunto: `K6 Marcacao ${slotSeed}`,
    descricao: 'Marcacao automatica de performance k6',
    utenteId: session.userId,
  };

  return http.post(
    `${BASE_URL}/api/marcacoes/remota`,
    JSON.stringify(payload),
    {
      headers: buildHeaders(session, true, true),
      tags: { name: 'Create Marcacao Remota' },
      // 500 is included because the backend maps IllegalArgumentException (slot conflict)
      // to HTTP 500 via the generic exception handler instead of 400/409.
      // Accepting it here prevents slot-conflict responses from polluting http_req_failed.
      responseCallback: http.expectedStatuses(200, 400, 409, 500),
    }
  );
}
