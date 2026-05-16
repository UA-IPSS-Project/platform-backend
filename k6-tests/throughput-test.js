import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { doLogin, BASE, randomUser } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const reqsSuccess = new Counter('requests_success');

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
    },
};

let auth = null;

export default function () {
    if (!auth) {
        const u = randomUser();
        auth = doLogin(u.email, u.password);
        if (!auth) { sleep(1); return; }
    }

    const res = http.get(`${BASE}/api/marcacoes`, auth);
    if (res.status === 200) reqsSuccess.add(1);

    check(res, { 'throughput ok': (r) => r.status === 200 });
}

export function handleSummary(data) {
    return {
        'resultados/throughput/throughput-report.html': htmlReport(data),
        'resultados/throughput/throughput-result.json': JSON.stringify(data, null, 2),
    };
}