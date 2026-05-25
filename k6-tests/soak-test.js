import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';
import { doLogin, BASE } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '2m', target: 10 },
        { duration: '30m', target: 10 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<800'],
        'requisicoes_duration': ['p(95)<800'],
        http_req_failed: ['rate<0.15'],
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

    group('Leitura contínua', () => {
        let t = Date.now();
        check(http.get(`${BASE}/api/marcacoes`, auth), {
            'sem degradação': (r) => r.status === 200,
            'tempo estável': (r) => r.timings.duration < 500,
        });
        marcacoesDur.add(Date.now() - t);

        t = Date.now();
        check(http.get(`${BASE}/api/requisicoes`, auth), {
            'requisições ok': (r) => r.status === 200,
        });
        requisicoesDur.add(Date.now() - t);
    });

    group('Escrita contínua', () => {
        // Cria requisição e vê se mantém performance
        http.post(
            `${BASE}/api/requisicoes/material`,
            JSON.stringify({
                descricao: `Soak test ${Date.now()}`,
                prioridade: 'BAIXA',
                itens: [{ materialId: 1, quantidade: 1 }]
            }),
            auth
        );
        sleep(1);
    });

    sleep(Math.random() * 2 + 1);
}

export function handleSummary(data) {
    return {
        'resultados/soak/soak-report.html': htmlReport(data),
        'resultados/soak/soak-result.json': JSON.stringify(data, null, 2),
        stdout: 'Soak test concluído.\n',
    };
}