import http from 'k6/http';
import { check, sleep } from 'k6';
import { doLogin, BASE, randomUser } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '30s', target: 5 }, // normal
        { duration: '10s', target: 100 }, // pico: todos acedem às 9h
        { duration: '1m', target: 100 }, // pico mantido
        { duration: '10s', target: 10 }, // normaliza
        { duration: '30s', target: 10 }, // recuperação
    ],
    thresholds: {
        http_req_failed: ['rate<0.20'],
        http_req_duration: ['p(95)<3000'],
    },
};

let auth = null;

export default function () {
    if (!auth) {
        const u = randomUser();
        auth = doLogin(u.email, u.password);
        if (!auth) { sleep(1); return; }
    }

    // No pico, utilizadores verificam disponibilidade e marcações
    check(http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth), {
        'calendario sobrevive ao pico': (r) => r.status === 200,
    });
    check(http.get(`${BASE}/api/marcacoes`, auth), {
        'marcações sobrevive ao pico': (r) => r.status === 200,
    });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'resultados/spike/spike-report.html': htmlReport(data),
        'resultados/spike/spike-result.json': JSON.stringify(data, null, 2),
    };
}