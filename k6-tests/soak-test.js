import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { doLogin, BASE, randomUser } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const marcacoesDur = new Trend('marcacoes_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '2m', target: 10 },
        { duration: '30m', target: 10 }, // simula dia de trabalho completo
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<500'],
        http_req_failed: ['rate<0.05'],
    },
};

let auth = null;

export default function () {
    if (!auth) {
        const u = randomUser();
        auth = doLogin(u.email, u.password);
        if (!auth) { sleep(2); return; }
    }

    const t = Date.now();
    const res = http.get(`${BASE}/api/marcacoes`, auth);
    marcacoesDur.add(Date.now() - t);

    check(res, {
        'sem degradação': (r) => r.status === 200,
        'tempo estável': (r) => r.timings.duration < 500,
    });

    http.get(`${BASE}/api/requisicoes`, auth);

    sleep(Math.random() * 2 + 1);
}

export function handleSummary(data) {
    return {
        'resultados/soak/soak-report.html': htmlReport(data),
        'resultados/soak/soak-result.json': JSON.stringify(data, null, 2),
    };
}