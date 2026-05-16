import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { doLogin, BASE, randomUser } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const marcacoesDur = new Trend('marcacoes_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '1m', target: 20 }, // normal
        { duration: '1m', target: 50 }, // elevado
        { duration: '1m', target: 100 }, // stress
        { duration: '1m', target: 150 }, // rutura
        { duration: '1m', target: 0 }, // recuperação
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<2000'],
        http_req_failed: ['rate<0.15'],
    },
};

let auth = null;

export default function () {
    if (!auth) {
        const u = randomUser();
        auth = doLogin(u.email, u.password);
        if (!auth) { sleep(1); return; }
    }

    const t = Date.now();
    const res = http.get(`${BASE}/api/marcacoes`, auth);
    marcacoesDur.add(Date.now() - t);

    check(res, {
        'responde': (r) => r.status !== 0,
        'sem crash': (r) => r.status !== 502 && r.status !== 503,
    });

    sleep(0.5);
}

export function handleSummary(data) {
    return {
        'resultados/stress/stress-report.html': htmlReport(data),
        'resultados/stress/stress-result.json': JSON.stringify(data, null, 2),
    };
}