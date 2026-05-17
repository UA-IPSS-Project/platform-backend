import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { doLogin, BASE } from './utils/auth.js';

// Métricas personalizadas para analisar no relatório da apresentação
const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '30s', target: 50 }, // Ramp-up para 50 utilizadores simultâneos
        { duration: '4m', target: 50 },  // Carga estável durante 4 minutos
        { duration: '30s', target: 0 },  // Ramp-down
    ],
    thresholds: {
        // Exemplo: 95% dos pedidos devem responder em menos de 150ms
        'marcacoes_duration': ['p(95)<150'],
        'requisicoes_duration': ['p(95)<150'],
        // Limite máximo tolerável de falhas
        http_req_failed: ['rate<0.01'],
    },
};

let auth = null;

export default function () {
    if (!auth) {
        auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
        // Se a autenticação falhar, espera um bocado antes de tentar novamente para não espamar o servidor
        if (!auth) { sleep(2); return; }
    }

    // Listagem de Marcações - Um dos endpoints mais usados e pesados
    group('Consulta de Marcações', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/marcacoes`, auth);
        check(res, {
            'lista marcações 200': (r) => r.status === 200,
        });
        marcacoesDur.add(Date.now() - t);
    });

    // Simular o tempo em que o utilizador estaria a ler a listagem no ecrã
    sleep(1);

    // Consulta de Requisições
    group('Consulta de Requisições', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/requisicoes`, auth);
        check(res, {
            'lista requisições 200': (r) => r.status === 200,
        });
        requisicoesDur.add(Date.now() - t);
    });

    sleep(1);
}

const LABEL = __ENV.LABEL || 'desempenho';

export function handleSummary(data) {
    const metricas = [
        'marcacoes_duration',
        'requisicoes_duration',
    ];

    const filtrado = { metrics: {} };
    for (const m of metricas) {
        if (data.metrics[m]) filtrado.metrics[m] = data.metrics[m];
    }

    // Calcula o Throughput (Requests Per Second)
    const rps = data.metrics.http_reqs ? data.metrics.http_reqs.values.rate : 0;

    return {
        [`resultados/desempenho/desempenho-${LABEL}-report.html`]: htmlReport(data),
        [`resultados/desempenho/desempenho-${LABEL}-result.json`]: JSON.stringify(filtrado, null, 2),
        stdout: `
=========================================================
=== Resultados de Performance (${LABEL}) ===
=========================================================
Throughput (RPS) Global da API: ${rps.toFixed(2)} pedidos/segundo

Métricas de Tempo de Resposta (Foco no p95):
${metricas.map(m => {
            const v = data.metrics[m]?.values;
            if (!v) return `${m}: sem dados`;
            return `${m.padEnd(25)} avg=${Math.round(v.avg)}ms   p(95)=${Math.round(v['p(95)'])}ms   min=${Math.round(v.min)}ms   max=${Math.round(v.max)}ms`;
        }).join('\n')}
=========================================================
Nota para a Apresentação: Usa o valor de p(95) listado acima para 
comprovar que 95% dos pedidos são respondidos em menos de X milissegundos.
=========================================================
`,
    };
}


/*
k6 run --insecure-skip-tls-verify -e LABEL=sem-otimizacao desempenho-test.js
k6 run --insecure-skip-tls-verify -e LABEL=com-otimizacao desempenho-test.js
*/
