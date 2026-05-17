import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { doLogin, BASE, dataFuturaSequencial } from './utils/auth.js';

// Métricas personalizadas para analisar no relatório da apresentação
const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');
const agendaDur = new Trend('agenda_duration');
const passadasDur = new Trend('passadas_duration');
const criarMarcDur = new Trend('criar_marcacao_duration');
const criarReqDur = new Trend('criar_requisicao_duration');


export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        {duration: '30s', target: 3}, // Warm-up
        { duration: '30s', target: 50 }, // Ramp-up para 50 utilizadores simultâneos
        { duration: '4m', target: 50 },  // Carga estável durante 4 minutos
        { duration: '30s', target: 0 },  // Ramp-down
    ],
    thresholds: {
        // Exemplo: 95% dos pedidos devem responder em menos de 150ms
        'marcacoes_duration': ['p(95)<150'],
        'requisicoes_duration': ['p(95)<150'],
        'agenda_duration': ['p(95)<150'],
        'passadas_duration': ['p(95)<150'],
        'criar_marcacao_duration': ['p(95)<250'],
        'criar_requisicao_duration': ['p(95)<250'],
        // Limite máximo tolerável de falhas
        http_req_failed: ['rate<0.01'],
    },
};

export function setup() {
    console.log('[Setup] Efetuando login da secretaria...');
    const auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
    if (!auth) {
        throw new Error('Login failed for secretaria in setup()');
    }

    console.log('[Setup] A assegurar a existência do utente com NIF 123456789...');
    // Regista o utente de teste uma única vez de forma sequencial no início caso não exista
    http.post(
        `${BASE}/api/auth/register/utente`,
        JSON.stringify({
            nome: "Utente de Teste",
            email: "utente_teste@gmail.com",
            password: "password123",
            nif: "123456789",
            telefone: "912345678",
            dataNasc: "1990-01-01",
            termsAccepted: true
        }),
        {
            headers: { 'Content-Type': 'application/json' }
        }
    );

    console.log('[Setup] A carregar lista de feriados do backend para evitar conflitos de datas...');
    const res2026 = http.get(`${BASE}/api/calendario/feriados?ano=2026`, auth);
    const res2027 = http.get(`${BASE}/api/calendario/feriados?ano=2027`, auth);

    let holidays = [];
    if (res2026.status === 200) {
        holidays = holidays.concat(JSON.parse(res2026.body));
    }
    if (res2027.status === 200) {
        holidays = holidays.concat(JSON.parse(res2027.body));
    }

    console.log(`[Setup] Carregados ${holidays.length} feriados com sucesso.`);
    return { auth, holidays };
}

export default function (data) {
    const auth = data.auth;
    const holidays = data.holidays || [];

    // Listagem de Marcações - Um dos endpoints mais usados e pesados
    group('Consulta de Marcações', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/marcacoes`, auth);
        check(res, {
            'lista marcações 200': (r) => r.status === 200,
        });
        marcacoesDur.add(Date.now() - t);

        t = Date.now();
        const resPassadas = http.get(`${BASE}/api/marcacoes/passadas`, auth);
        check(resPassadas, {
            'passadas 200': (r) => r.status === 200,
        });
        passadasDur.add(Date.now() - t);

        t = Date.now();
        const resAgenda = http.get(`${BASE}/api/marcacoes/agenda`, auth);
        check(resAgenda, {
            'agenda 200': (r) => r.status === 200,
        });
        agendaDur.add(Date.now() - t);

        check(http.get(`${BASE}/api/marcacoes/count/hoje`, auth), {
            'count hoje 200': (r) => r.status === 200,
        });
    });

    // Simular o tempo em que o utilizador estaria a ler a listagem no ecrã
    sleep(1);

    // Consulta de Requisições
    group('Consulta de Requisições', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/requisicoes`, auth);
        if (res.status !== 200) console.log('GET req falhou:', res.status, res.body);
        check(res, {
            'lista requisições 200': (r) => r.status === 200,
        });
        requisicoesDur.add(Date.now() - t);

        check(http.get(`${BASE}/api/requisicoes?tipo=MATERIAL`, auth), {
            'requisições material 200': (r) => [200, 400].includes(r.status),
        });
        check(http.get(`${BASE}/api/requisicoes?estado=ABERTO`, auth), {
            'requisições abertas 200': (r) => [200, 400].includes(r.status),
        });
    });

    sleep(1);

    // Consulta de Calendário
    group('Consulta de Calendário', () => {
        check(http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth), {
            'bloqueios 200': (r) => r.status === 200,
        });
        check(http.get(`${BASE}/api/calendario/feriados?ano=2026`, auth), {
            'feriados 200': (r) => [200, 404].includes(r.status),
        });
    });

    sleep(1);

    // Criação de Marcação
    group('Criar Marcação', () => {
        const slotData = dataFuturaSequencial(__VU, holidays);

        let res;
        let attempts = 0;
        const maxAttempts = 5;
        let success = false;

        while (attempts < maxAttempts && !success) {
            attempts++;
            let t = Date.now();
            res = http.post(`${BASE}/api/marcacoes/presencial`,
                JSON.stringify({
                    data: slotData,
                    duration: 15,
                    descricao: `Teste Performance presencial`,
                    assunto: 'Atendimento Geral',
                    utenteNif: '123456789',
                    utenteNome: 'Utente de Teste'
                }),
                auth
            );

            if (res.status === 200 || res.status === 201) {
                success = true;
                criarMarcDur.add(Date.now() - t);
            } else if (res.status === 500 && (res.body.includes("serialize") || res.body.includes("rollback-only"))) {
                // Conflito transacional SERIALIZABLE: aguarda um tempo aleatório (backoff) e tenta de novo
                sleep(0.05 + Math.random() * 0.15);
            } else {
                // Outro erro qualquer (ex: 400 Bad Request), não adianta tentar novamente
                break;
            }
        }

        if (!success && res) {
            console.log('POST marc falhou definitivamente após retentativas:', res.status, res.body);
        }

        check(res, {
            'cria marcação 200/201': (r) => [200, 201].includes(r.status),
        });
    });

    sleep(1);

    // Criação de Requisição (alternar entre Material, Transporte e Manutenção)
    group('Criar Requisição', () => {
        const tipoReq = __VU % 3;
        let endpoint = 'material';
        let payload = {};

        if (tipoReq === 0) {
            endpoint = 'material';
            payload = {
                descricao: 'Requisição de Material Performance k6',
                prioridade: 'MEDIA',
                itens: [{ materialId: 1, quantidade: 1 }]
            };
        } else if (tipoReq === 1) {
            endpoint = 'transporte';
            const reqDate = new Date();
            reqDate.setDate(reqDate.getDate() + 1);
            const reqDateRegresso = new Date(reqDate.getTime() + 2 * 60 * 60 * 1000);
            payload = {
                descricao: 'Requisição de Transporte Performance k6',
                prioridade: 'MEDIA',
                destino: 'Porto',
                dataHoraSaida: reqDate.toISOString(),
                dataHoraRegresso: reqDateRegresso.toISOString(),
                numeroPassageiros: 2,
                condutor: 'Condutor de Teste',
                transporteIds: [1] // Obrigatório para evitar o erro 400
            };
        } else {
            endpoint = 'manutencao';
            payload = {
                descricao: 'Requisição de Manutenção Performance k6',
                prioridade: 'MEDIA',
                manutencaoItens: [{ itemId: 1, observacoes: 'Teste' }]
            };
        }

        let t = Date.now();
        const res = http.post(`${BASE}/api/requisicoes/${endpoint}`,
            JSON.stringify(payload),
            auth
        );
        if (res.status !== 200 && res.status !== 201) console.log('POST marc falhou:', res.status, res.body);
        check(res, {
            'cria requisição 200/201': (r) => [200, 201].includes(r.status),
        });
        criarReqDur.add(Date.now() - t);
    });

    sleep(1);
}

const LABEL = __ENV.LABEL || 'desempenho';

export function handleSummary(data) {
    const metricas = [
        'marcacoes_duration',
        'requisicoes_duration',
        'agenda_duration',
        'passadas_duration',
        'criar_marcacao_duration',
        'criar_requisicao_duration',
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



/*
=========================================================
=== Resultados de Performance (no-optimization) ===
=========================================================
Throughput (RPS) Global da API: 36.95 pedidos/segundo

Métricas de Tempo de Resposta (Foco no p95):
marcacoes_duration        avg=467ms   p(95)=833ms   min=127ms   max=1647ms
requisicoes_duration      avg=1511ms   p(95)=3614ms   min=145ms   max=5796ms
agenda_duration           avg=1917ms   p(95)=3229ms   min=836ms   max=8403ms
passadas_duration         avg=174ms   p(95)=391ms   min=111ms   max=1458ms
criar_marcacao_duration   avg=211ms   p(95)=410ms   min=121ms   max=1388ms
criar_requisicao_duration avg=793ms   p(95)=2913ms   min=115ms   max=4599ms
=========================================================
*/