import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';
import { doLogin, BASE, dataFutura } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const marcacoesDur = new Trend('marcacoes_duration');
const criarDur = new Trend('criar_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '1m', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '1m', target: 150 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<2000'],
        'criar_duration': ['p(95)<3000'],
        http_req_failed: ['rate<0.25'],
    },
};

let auth = null;

export default function () {
    if (!auth) {
        auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
        if (!auth) { sleep(1); return; }
    }

    group('Leitura sob stress', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/marcacoes`, auth);
        marcacoesDur.add(Date.now() - t);
        check(res, {
            'responde': (r) => r.status !== 0,
            'sem crash': (r) => ![502, 503].includes(r.status),
        });
    });

    group('Escrita sob stress', () => {
        const t = Date.now();
        const res = http.post(
            `${BASE}/api/requisicoes/material`,
            JSON.stringify({
                descricao: `Requisição stress test ${Date.now()}`,
                prioridade: 'ALTA',
                itens: [{ materialId: 1, quantidade: 1 }]
            }),
            auth
        );
        criarDur.add(Date.now() - t);
        check(res, { 'escrita ok': (r) => [200, 201, 400, 409].includes(r.status) });
    });

    sleep(0.5);
}

export function handleSummary(data) {
    return {
        'resultados/stress/stress-report.html': htmlReport(data),
        'resultados/stress/stress-result.json': JSON.stringify(data, null, 2),
        stdout: 'Stress test concluído.\n',
    };
}