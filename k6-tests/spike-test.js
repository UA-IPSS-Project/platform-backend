import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { doLogin, BASE } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '30s', target: 5 },
        { duration: '10s', target: 100 }, // todos entram às 9h
        { duration: '1m', target: 100 },
        { duration: '10s', target: 10 },
        { duration: '30s', target: 10 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.20'],
        http_req_duration: ['p(95)<3000'],
    },
};

export function setup() {
    const auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
    if (!auth) {
        throw new Error('Login failed in setup()');
    }
    return { auth };
}

export default function (data) {
    const auth = data.auth;

    // No pico todos abrem o dashboard simultaneamente
    group('Pico de acesso', () => {
        check(http.get(`${BASE}/api/marcacoes`, auth), {
            'marcações sobrevive': (r) => r.status === 200,
        });
        check(http.get(`${BASE}/api/marcacoes/agenda`, auth), {
            'agenda sobrevive': (r) => r.status === 200,
        });
        check(http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth), {
            'calendario sobrevive': (r) => r.status === 200,
        });
    });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'resultados/spike/spike-report.html': htmlReport(data),
        'resultados/spike/spike-result.json': JSON.stringify(data, null, 2),
        stdout: 'Spike test concluído.\n',
    };
}