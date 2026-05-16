import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { doLogin, BASE, randomUser } from './utils/auth.js';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const marcacoesDur = new Trend('marcacoes_duration');
const calendarioDur = new Trend('calendario_duration');
const requisicoesDur = new Trend('requisicoes_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '1m', target: 10 }, // funcionários a entrar de manhã
        { duration: '3m', target: 10 }, // trabalho normal
        { duration: '1m', target: 0 }, // fim do dia
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<500'],
        'calendario_duration': ['p(95)<300'],
        'requisicoes_duration': ['p(95)<300'],
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

    // Simula fluxo real: ver marcações → ver calendário → ver requisições
    let t = Date.now();
    const marcacoes = http.get(`${BASE}/api/marcacoes`, auth);
    marcacoesDur.add(Date.now() - t);
    check(marcacoes, { 'marcações 200': (r) => r.status === 200 });

    sleep(1); // utilizador lê os dados

    t = Date.now();
    const calendario = http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth);
    calendarioDur.add(Date.now() - t);
    check(calendario, { 'calendario 200': (r) => r.status === 200 });

    sleep(2); // utilizador analisa disponibilidade

    t = Date.now();
    const requisicoes = http.get(`${BASE}/api/requisicoes`, auth);
    requisicoesDur.add(Date.now() - t);
    check(requisicoes, { 'requisições 200': (r) => r.status === 200 });

    sleep(Math.random() * 3 + 1); // pausa realista entre ações
}

export function handleSummary(data) {
    return {
        'resultados/load/load-report.html': htmlReport(data),
        'resultados/load/load-result.json': JSON.stringify(data, null, 2),
    };
}