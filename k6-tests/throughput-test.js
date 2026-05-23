import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { doLogin, BASE } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const reqsSuccess = new Counter('requests_success');
const marcacoesDur = new Trend('marcacoes_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 50 },
        { duration: '15s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_reqs: ['rate>50'],
        'marcacoes_duration': ['p(95)<300'],
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

    const t = Date.now();
    const res = http.get(`${BASE}/api/marcacoes`, auth);
    marcacoesDur.add(Date.now() - t);

    if (res.status === 200) reqsSuccess.add(1);
    check(res, { 'throughput ok': (r) => r.status === 200 });
}

export function handleSummary(data) {
    return {
        'resultados/throughput/throughput-report.html': htmlReport(data),
        'resultados/throughput/throughput-result.json': JSON.stringify(data, null, 2),
        stdout: 'Throughput test concluído.\n',
    };
}