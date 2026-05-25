import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { doLogin, BASE, dataFuturaSequencial } from './utils/auth.js';

// Métricas de stress
const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');
const agendaDur = new Trend('agenda_duration');
const passadasDur = new Trend('passadas_duration');
const criarMarcDur = new Trend('criar_marcacao_duration');
const criarReqDur = new Trend('criar_requisicao_duration');
const errorRate = new Rate('erros_5xx');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '30s', target: 50 },   // Começa com carga moderada
        { duration: '1m', target: 150 },  // Salto para 150 VUs (esgotamento de pool HikariCP?)
        { duration: '1m', target: 350 },  // Força concorrência extrema / CPU
        { duration: '1m', target: 500 },  // Ponto de ruptura (500 VUs)
        { duration: '30s', target: 0 },   // Desce para recuperar
    ],
};

export function setup() {
    console.log('[Setup] Efetuando login da secretaria...');
    const auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
    if (!auth) {
        throw new Error('Login failed for secretaria in setup()');
    }

    console.log('[Setup] A assegurar a existência do utente com NIF 123456789...');
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

    console.log('[Setup] A carregar lista de feriados do backend...');
    const res2026 = http.get(`${BASE}/api/calendario/feriados?ano=2026`, auth);
    const res2027 = http.get(`${BASE}/api/calendario/feriados?ano=2027`, auth);

    let holidays = [];
    if (res2026.status === 200) {
        holidays = holidays.concat(JSON.parse(res2026.body));
    }
    if (res2027.status === 200) {
        holidays = holidays.concat(JSON.parse(res2027.body));
    }

    console.log(`[Setup] Carregados ${holidays.length} feriados.`);
    return { auth, holidays };
}

export default function (data) {
    const auth = data.auth;
    const holidays = data.holidays || [];

    // Consulta de Marcações (GET /api/marcacoes e históricos)
    group('Consulta de Marcações', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/marcacoes`, auth);
        if (res.status >= 500) errorRate.add(1); else errorRate.add(0);
        check(res, { 'lista marcações status 200': (r) => r.status === 200 });
        marcacoesDur.add(Date.now() - t);

        t = Date.now();
        const resPassadas = http.get(`${BASE}/api/marcacoes/passadas`, auth);
        if (resPassadas.status >= 500) errorRate.add(1); else errorRate.add(0);
        check(resPassadas, { 'passadas 200': (r) => r.status === 200 });
        passadasDur.add(Date.now() - t);

        t = Date.now();
        const resAgenda = http.get(`${BASE}/api/marcacoes/agenda`, auth);
        if (resAgenda.status >= 500) errorRate.add(1); else errorRate.add(0);
        check(resAgenda, { 'agenda 200': (r) => r.status === 200 });
        agendaDur.add(Date.now() - t);
    });

    sleep(0.5);

    // Consulta de Requisições
    group('Consulta de Requisições', () => {
        let t = Date.now();
        const res = http.get(`${BASE}/api/requisicoes`, auth);
        if (res.status >= 500) errorRate.add(1); else errorRate.add(0);
        check(res, { 'lista requisições status 200': (r) => r.status === 200 });
        requisicoesDur.add(Date.now() - t);
    });

    sleep(0.5);

    // Criação de Marcação
    group('Criar Marcação', () => {
        const slotData = dataFuturaSequencial(__VU, holidays);

        let t = Date.now();
        const res = http.post(`${BASE}/api/marcacoes/presencial`,
            JSON.stringify({
                data: slotData,
                duration: 15,
                descricao: `Teste Stress presencial`,
                assunto: 'Atendimento Geral',
                utenteNif: '123456789',
                utenteNome: 'Utente de Teste'
            }),
            auth
        );

        if (res.status >= 500) {
            errorRate.add(1);
        } else {
            errorRate.add(0);
        }

        criarMarcDur.add(Date.now() - t);

        check(res, {
            'cria marcação 200/201': (r) => [200, 201].includes(r.status),
        });
    });

    sleep(0.5);

    // Criação de Requisição
    group('Criar Requisição', () => {
        const tipoReq = __VU % 3;
        let endpoint = 'material';
        let payload = {};

        if (tipoReq === 0) {
            endpoint = 'material';
            payload = {
                descricao: 'Requisição de Material Stress k6',
                prioridade: 'MEDIA',
                itens: [{ materialId: 1, quantidade: 1 }]
            };
        } else if (tipoReq === 1) {
            endpoint = 'transporte';
            const reqDate = new Date();
            reqDate.setDate(reqDate.getDate() + 1);
            const reqDateRegresso = new Date(reqDate.getTime() + 2 * 60 * 60 * 1000);
            payload = {
                descricao: 'Requisição de Transporte Stress k6',
                prioridade: 'MEDIA',
                destino: 'Porto',
                dataHoraSaida: reqDate.toISOString(),
                dataHoraRegresso: reqDateRegresso.toISOString(),
                numeroPassageiros: 2,
                condutor: 'Condutor de Teste',
                transporteIds: [1]
            };
        } else {
            endpoint = 'manutencao';
            payload = {
                descricao: 'Requisição de Manutenção Stress k6',
                prioridade: 'MEDIA',
                manutencaoItens: [{ itemId: 1, observacoes: 'Teste' }]
            };
        }

        let t = Date.now();
        const res = http.post(`${BASE}/api/requisicoes/${endpoint}`,
            JSON.stringify(payload),
            auth
        );
        if (res.status >= 500) errorRate.add(1); else errorRate.add(0);
        check(res, {
            'cria requisição 200/201': (r) => [200, 201].includes(r.status),
        });
        criarReqDur.add(Date.now() - t);
    });

    sleep(0.5);
}

const LABEL = __ENV.LABEL || 'stress';

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

    const rps = data.metrics.http_reqs ? data.metrics.http_reqs.values.rate : 0;
    const errors = data.metrics.erros_5xx ? (data.metrics.erros_5xx.values.rate * 100) : 0;
    const reqFailed = data.metrics.http_req_failed ? (data.metrics.http_req_failed.values.rate * 100) : 0;

    return {
        [`resultados/stress/stress-${LABEL}-report.html`]: htmlReport(data),
        [`resultados/stress/stress-${LABEL}-result.json`]: JSON.stringify(filtrado, null, 2),
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
o que falhou primeiro (ex: HikariCP pool exhaustion, CPU 100%,
ou error connection refused).
=========================================================
`,
    };
}

/*
docker logs florinhas_db --tail 200
2026-05-17 15:33:56.976 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:56.976 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during commit attempt.
2026-05-17 15:33:56.976 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:56.976 UTC [268] STATEMENT:  COMMIT
2026-05-17 15:33:57.033 UTC [270] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.033 UTC [270] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.033 UTC [270] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.033 UTC [270] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:57.049 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.049 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:57.049 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.049 UTC [258] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:57.140 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.140 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.140 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.140 UTC [258] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:33:57.154 UTC [269] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.154 UTC [269] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.154 UTC [269] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.154 UTC [269] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:33:57.338 UTC [266] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.338 UTC [266] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:57.338 UTC [266] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.338 UTC [266] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:57.498 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.498 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.498 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.498 UTC [268] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:33:57.500 UTC [266] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.500 UTC [266] DETAIL:  Reason code: Canceled on identification as a pivot, during commit attempt.
2026-05-17 15:33:57.500 UTC [266] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.500 UTC [266] STATEMENT:  COMMIT
2026-05-17 15:33:57.501 UTC [269] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.501 UTC [269] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.501 UTC [269] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.501 UTC [269] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:57.510 UTC [267] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.510 UTC [267] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.510 UTC [267] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.510 UTC [267] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:57.615 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.615 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:57.615 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.615 UTC [268] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:57.711 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.711 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:57.711 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.711 UTC [268] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:57.816 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.816 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:57.816 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.816 UTC [268] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:57.932 UTC [269] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.932 UTC [269] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:57.932 UTC [269] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.932 UTC [269] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:57.945 UTC [266] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:57.945 UTC [266] DETAIL:  Reason code: Canceled on identification as a pivot, during commit attempt.
2026-05-17 15:33:57.945 UTC [266] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:57.945 UTC [266] STATEMENT:  COMMIT
2026-05-17 15:33:58.005 UTC [262] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.005 UTC [262] DETAIL:  Reason code: Canceled on identification as a pivot, during commit attempt.
2026-05-17 15:33:58.005 UTC [262] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.005 UTC [262] STATEMENT:  COMMIT
2026-05-17 15:33:58.176 UTC [267] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.176 UTC [267] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:58.176 UTC [267] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.176 UTC [267] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:33:58.216 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.216 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:58.216 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.216 UTC [258] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:58.329 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.329 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:58.329 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.329 UTC [268] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:58.526 UTC [267] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.526 UTC [267] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:58.526 UTC [267] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.526 UTC [267] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:33:58.751 UTC [266] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.751 UTC [266] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:58.751 UTC [266] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.751 UTC [266] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:58.935 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:58.935 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:58.935 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:58.935 UTC [258] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.126 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.126 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.126 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.126 UTC [258] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.171 UTC [270] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.171 UTC [270] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.171 UTC [270] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.171 UTC [270] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.281 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.281 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:59.281 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.281 UTC [258] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:59.281 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.281 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.281 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.281 UTC [268] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.282 UTC [264] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.282 UTC [264] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:59.282 UTC [264] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.282 UTC [264] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:59.351 UTC [270] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.351 UTC [270] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.351 UTC [270] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.351 UTC [270] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.428 UTC [262] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.428 UTC [262] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.428 UTC [262] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.428 UTC [262] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.432 UTC [265] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.432 UTC [265] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:59.432 UTC [265] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.432 UTC [265] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:33:59.436 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.436 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:59.436 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.436 UTC [258] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:59.457 UTC [268] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.457 UTC [268] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.457 UTC [268] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.457 UTC [268] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.470 UTC [253] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.470 UTC [253] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.470 UTC [253] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.470 UTC [253] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.475 UTC [270] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.475 UTC [270] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.475 UTC [270] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.475 UTC [270] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:33:59.512 UTC [262] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.512 UTC [262] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:33:59.512 UTC [262] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.512 UTC [262] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:33:59.592 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:33:59.592 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:33:59.592 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:33:59.592 UTC [258] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:34:00.298 UTC [262] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:34:00.298 UTC [262] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:34:00.298 UTC [262] HINT:  The transaction might succeed if retried.
2026-05-17 15:34:00.298 UTC [262] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:34:00.298 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:34:00.298 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:34:00.298 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:34:00.298 UTC [258] STATEMENT:  insert into audit_log (action,details,entity_id,entity_type,ip_address,timestamp,user_id,user_name) values ($1,$2,$3,$4,$5,$6,$7,$8)
    RETURNING *
2026-05-17 15:34:00.366 UTC [262] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:34:00.366 UTC [262] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:34:00.366 UTC [262] HINT:  The transaction might succeed if retried.
2026-05-17 15:34:00.366 UTC [262] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:34:00.391 UTC [253] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:34:00.391 UTC [253] DETAIL:  Reason code: Canceled on identification as a pivot, during conflict in checking.
2026-05-17 15:34:00.391 UTC [253] HINT:  The transaction might succeed if retried.
2026-05-17 15:34:00.391 UTC [253] STATEMENT:  insert into marcacao_secretaria (assunto,descricao,tipo_atendimento,utente_id,marcacao_id) values ($1,$2,$3,$4,$5)
2026-05-17 15:34:00.550 UTC [258] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:34:00.550 UTC [258] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:34:00.550 UTC [258] HINT:  The transaction might succeed if retried.
2026-05-17 15:34:00.550 UTC [258] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:34:01.266 UTC [262] ERROR:  could not serialize access due to read/write dependencies among transactions
2026-05-17 15:34:01.266 UTC [262] DETAIL:  Reason code: Canceled on identification as a pivot, during write.
2026-05-17 15:34:01.266 UTC [262] HINT:  The transaction might succeed if retried.
2026-05-17 15:34:01.266 UTC [262] STATEMENT:  insert into marcacao (atendente_id,criado_em,utilizador_id,data,descricao,duration,estado,motivo_cancelamento,version) values ($1,$2,$3,$4,$5,$6,$7,$8,$9)
    RETURNING *
2026-05-17 15:34:20.092 UTC [74] LOG:  checkpoint complete: wrote 661 buffers (4.0%); 1 WAL file(s) added, 0 removed, 0 recycled; write=66.844 s, sync=0.056 s, total=67.135 s; sync files=192, longest=0.008 s, average=0.001 s; distance=4563 kB, estimate=4563 kB; lsn=0/1F48368, redo lsn=0/1D94050
florinhasuser@VM-IPSS-Digital:~/ipss-digital/platform-backend$ 
*/