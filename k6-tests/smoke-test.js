import http from 'k6/http';
import { check, sleep } from 'k6';
import { doLogin, BASE } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

export const options = {
    insecureSkipTLSVerify: true,
    vus: 1,
    duration: '1m',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

let auth = null;

export default function () {
    if (!auth) auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
    if (!auth) { sleep(2); return; }

    // Verifica endpoints principais
    check(http.get(`${BASE}/api/marcacoes`, auth), { 'marcações ok': (r) => r.status === 200 });
    check(http.get(`${BASE}/api/requisicoes`, auth), { 'requisições ok': (r) => r.status === 200 });
    check(http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth), { 'calendario ok': (r) => r.status === 200 });
    check(http.get(`${BASE}/api/marcacoes/count/hoje`, auth), { 'count hoje ok': (r) => r.status === 200 });
    check(http.get(`${BASE}/api/marcacoes/agenda`, auth), { 'agenda ok': (r) => r.status === 200 });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'resultados/smoke/smoke-report.html': htmlReport(data),
        'resultados/smoke/smoke-result.json': JSON.stringify(data, null, 2),
        stdout: 'Smoke test concluído.\n',
    };
}