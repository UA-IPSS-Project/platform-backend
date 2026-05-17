import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { doLogin, BASE } from './utils/auth.js';

const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');
const errorRate = new Rate('erros_5xx');

export const options = {
    insecureSkipTLSVerify: true,
    // Num Stress Test, vamos subir gradualmente a carga para tentar "partir" o servidor
    stages: [
        { duration: '1m', target: 50 },   // Começa com uma carga normal (50)
        { duration: '1m', target: 150 },  // Dá um salto para 150 (possivelmente esgota HikariCP se não estiver otimizado)
        { duration: '1m', target: 300 },  // Começa a forçar CPU/RAM seriamente
        { duration: '1m', target: 500 },  // Ponto crítico (muitas apps default falham aqui)
        { duration: '30s', target: 0 },   // Desce para recuperar e fechar o teste
    ],
    // Não vamos definir thresholds que falhem o teste, porque O OBJETIVO é que o teste mostre falhas
    // Mas podíamos definir um limite para abortar o teste preventivamente se houver demasiados erros
};

let auth = null;

export default function () {
    if (!auth) {
        auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
        if (!auth) {
            // Se o próprio login falhar, o sistema já está inacessível.
            sleep(2);
            return;
        }
    }

    group('Carga Agressiva - Marcações', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/marcacoes`, auth);

        // Verificamos ativamente se o servidor começa a devolver 500, 502, 503, 504
        if (res.status >= 500) {
            errorRate.add(1);
        } else {
            errorRate.add(0);
        }

        check(res, {
            'marcações status 200': (r) => r.status === 200,
        });
        marcacoesDur.add(Date.now() - t);
    });

    // Menos tempo de "sleep" num stress test, queremos metralhar o servidor
    sleep(0.5);

    group('Carga Agressiva - Requisições', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/requisicoes`, auth);

        if (res.status >= 500) {
            errorRate.add(1);
        } else {
            errorRate.add(0);
        }

        check(res, {
            'requisições status 200': (r) => r.status === 200,
        });
        requisicoesDur.add(Date.now() - t);
    });

    sleep(0.5);
}

const LABEL = __ENV.LABEL || 'stress';

export function handleSummary(data) {
    const metricas = [
        'marcacoes_duration',
        'requisicoes_duration',
    ];

    const filtrado = { metrics: {} };
    for (const m of metricas) {
        if (data.metrics[m]) filtrado.metrics[m] = data.metrics[m];
    }

    const rps = data.metrics.http_reqs ? data.metrics.http_reqs.values.rate : 0;
    const errors = data.metrics.erros_5xx ? (data.metrics.erros_5xx.values.rate * 100) : 0;
    const reqFailed = data.metrics.http_req_failed ? (data.metrics.http_req_failed.values.rate * 100) : 0;

    return {
        [`resultados/stress/stress-${LABEL}-report.html`]: htmlReport(data),
        [`resultados/stress/stress-${LABEL}-result.json`]: JSON.stringify(data, null, 2),
        stdout: `
=========================================================
=== Resultados do STRESS TEST (${LABEL}) ===
=========================================================
Throughput Máximo (RPS): ${rps.toFixed(2)} pedidos/segundo

TAXA DE FALHAS (Erros HTTP >= 400): ${reqFailed.toFixed(2)}%
TAXA DE COLAPSO (Erros HTTP >= 500): ${errors.toFixed(2)}%

Métricas de Tempo de Resposta:
${metricas.map(m => {
            const v = data.metrics[m]?.values;
            if (!v) return `${m}: sem dados`;
            return `${m.padEnd(25)} avg=${Math.round(v.avg)}ms   p(95)=${Math.round(v['p(95)'])}ms   max=${Math.round(v.max)}ms`;
        }).join('\n')}
=========================================================
Avaliação para Apresentação:
Se a "Taxa de Colapso" for maior que 0%, significa que 
encontraste o bottleneck do servidor.
Consulta os logs do Docker (backend e BD) para descobrir
o que falhou primeiro (ex: org.postgresql.util.PSQLException:
FATAL: sorry, too many clients already).
=========================================================
`,
    };
}

/*
Como Correr:
k6 run --insecure-skip-tls-verify stress-test.js
*/