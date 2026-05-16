import http from 'k6/http';
import { check, sleep } from 'k6';
import { doLogin, BASE, randomUser } from './utils/auth.js';
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
    if (!auth) {
        const u = randomUser();
        auth = doLogin(u.email, u.password);
    }
    if (!auth) { sleep(2); return; }

    // Fluxo real: utilizador abre o dashboard
    check(http.get(BASE, { insecureSkipTLSVerify: true }), {
        'frontend carrega': (r) => r.status === 200,
    });
    check(http.get(`${BASE}/api/marcacoes`, auth), {
        'lista marcações ok': (r) => r.status === 200,
    });
    check(http.get(`${BASE}/api/requisicoes`, auth), {
        'lista requisições ok': (r) => r.status === 200,
    });
    check(http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth), {
        'calendario ok': (r) => r.status === 200,
    });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'resultados/smoke/smoke-report.html': htmlReport(data),
        'resultados/smoke/smoke-result.json': JSON.stringify(data, null, 2),
    };
}