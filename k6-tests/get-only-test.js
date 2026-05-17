// get-only-test.js
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { doLogin, BASE } from './utils/auth.js';

const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');
const agendaDur = new Trend('agenda_duration');
const passadasDur = new Trend('passadas_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '30s', target: 20 },
        { duration: '2m', target: 20 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<500'],
        'requisicoes_duration': ['p(95)<500'],
        'agenda_duration': ['p(95)<300'],
        'passadas_duration': ['p(95)<500'],
        http_req_failed: ['rate<0.05'],
    },
};

let auth = null;

export default function () {
    if (!auth) {
        auth = doLogin('secretaria@florinhasdovouga.pt', 'sec123');
        if (!auth) { sleep(2); return; }
    }

    group('Marcações', () => {
        let t = Date.now();
        check(http.get(`${BASE}/api/marcacoes`, auth), {
            'lista marcações 200': (r) => r.status === 200,
        });
        marcacoesDur.add(Date.now() - t);

        t = Date.now();
        check(http.get(`${BASE}/api/marcacoes/passadas`, auth), {
            'passadas 200': (r) => r.status === 200,
        });
        passadasDur.add(Date.now() - t);

        t = Date.now();
        check(http.get(`${BASE}/api/marcacoes/agenda`, auth), {
            'agenda 200': (r) => r.status === 200,
        });
        agendaDur.add(Date.now() - t);

        check(http.get(`${BASE}/api/marcacoes/count/hoje`, auth), {
            'count hoje 200': (r) => r.status === 200,
        });
    });

    group('Requisições', () => {
        let t = Date.now();
        check(http.get(`${BASE}/api/requisicoes`, auth), {
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

    group('Calendário', () => {
        check(http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth), {
            'bloqueios 200': (r) => r.status === 200,
        });
        check(http.get(`${BASE}/api/calendario/feriados?ano=2026`, auth), {
            'feriados 200': (r) => [200, 404].includes(r.status),
        });
    });

    sleep(1);
}

const LABEL = __ENV.LABEL || 'sem-indices';

export function handleSummary(data) {
    // Filtra apenas as métricas de duração customizadas
    const metricas = [
        'marcacoes_duration',
        'requisicoes_duration',
        'agenda_duration',
        'passadas_duration',
    ];

    const filtrado = { metrics: {} };
    for (const m of metricas) {
        if (data.metrics[m]) filtrado.metrics[m] = data.metrics[m];
    }

    return {
        [`resultados/get-only/get-only-${LABEL}-report.html`]: htmlReport(data),
        [`resultados/get-only/get-only-${LABEL}-result.json`]: JSON.stringify(filtrado, null, 2),
        stdout: `
=== Resultados GET-only (${LABEL}) ===
${metricas.map(m => {
            const v = data.metrics[m]?.values;
            if (!v) return `${m}: sem dados`;
            return `${m.padEnd(25)} avg=${Math.round(v.avg)}ms  p95=${Math.round(v['p(95)'])}ms  min=${Math.round(v.min)}ms  max=${Math.round(v.max)}ms`;
        }).join('\n')}
`,
    };
}

/*
 * ─────────────────────────────────────────────────────────────
 *  DADOS DE TESTE NA BD
 * ─────────────────────────────────────────────────────────────
 *  marcacao   : 22.000 registos
 *  requisicao : 21.224 registos
 *
 * ─────────────────────────────────────────────────────────────
 *  PROBLEMA IDENTIFICADO — Query de marcações (SELECT DISTINCT)
 * ─────────────────────────────────────────────────────────────
 *  O Spring JPA gera uma query com SELECT DISTINCT + 6 LEFT JOINs
 *  para cada listagem de marcações:
 *
 *    SELECT DISTINCT m1_0.id, ...
 *    FROM marcacao m1_0
 *    LEFT JOIN marcacao_secretaria ms1_0 ON m1_0.id = ms1_0.marcacao_id
 *    LEFT JOIN utente u1_0 ...
 *    LEFT JOIN utilizador u1_1 ...
 *    LEFT JOIN utilizador cp1_0 ...
 *    LEFT JOIN funcionario cp1_1 ...
 *    LEFT JOIN utente cp1_2 ...
 *    ORDER BY m1_0.data
 *
 *  Sem índices: até 2535ms por execução sob carga concorrente.
 *  Com índice composto (data, estado): 0.154ms em execução isolada
 *  → melhoria de 16.000x na query isolada, 3x sob carga (1008ms → 324ms).
 *
 * ─────────────────────────────────────────────────────────────
 *  CRIAR ÍNDICES
 * ─────────────────────────────────────────────────────────────
 *  docker exec florinhas_db psql -U SpringTestAdmin -d florinhas -c "
 *  CREATE INDEX idx_requisicao_estado              ON requisicao(estado);
 *  CREATE INDEX idx_requisicao_tipo                ON requisicao(tipo);
 *  CREATE INDEX idx_requisicao_criado_por          ON requisicao(criado_por_id);
 *  CREATE INDEX idx_requisicao_criado_em           ON requisicao(criado_em DESC);
 *  CREATE INDEX idx_marcacao_estado                ON marcacao(estado);
 *  CREATE INDEX idx_marcacao_data                  ON marcacao(data DESC);
 *  CREATE INDEX idx_marcacao_utilizador            ON marcacao(utilizador_id);
 *  CREATE INDEX idx_marcacao_atendente             ON marcacao(atendente_id);
 *  CREATE INDEX idx_marcacao_data_estado           ON marcacao(data, estado);
 *  CREATE INDEX idx_marcacao_secretaria_marcacao   ON marcacao_secretaria(marcacao_id);
 *  CREATE INDEX idx_marcacao_balneario_marcacao    ON marcacao_balneario(marcacao_id);
 *  "
 *
 * ─────────────────────────────────────────────────────────────
 *  REMOVER ÍNDICES (para teste sem índices)
 * ─────────────────────────────────────────────────────────────
 *  docker exec florinhas_db psql -U SpringTestAdmin -d florinhas -c "
 *  DROP INDEX IF EXISTS idx_requisicao_estado;
 *  DROP INDEX IF EXISTS idx_requisicao_tipo;
 *  DROP INDEX IF EXISTS idx_requisicao_criado_por;
 *  DROP INDEX IF EXISTS idx_requisicao_criado_em;
 *  DROP INDEX IF EXISTS idx_marcacao_estado;
 *  DROP INDEX IF EXISTS idx_marcacao_data;
 *  DROP INDEX IF EXISTS idx_marcacao_utilizador;
 *  DROP INDEX IF EXISTS idx_marcacao_atendente;
 *  DROP INDEX IF EXISTS idx_marcacao_data_estado;
 *  DROP INDEX IF EXISTS idx_marcacao_secretaria_marcacao;
 *  DROP INDEX IF EXISTS idx_marcacao_balneario_marcacao;
 *  "
 *
 * ─────────────────────────────────────────────────────────────
 *  EXECUTAR TESTES
 * ─────────────────────────────────────────────────────────────
 *  Sem índices:
 *    k6 run --insecure-skip-tls-verify -e LABEL=sem-indices get-only-test.js
 *
 *  Com índices:
 *    k6 run --insecure-skip-tls-verify -e LABEL=com-indices get-only-test.js
 * ─────────────────────────────────────────────────────────────
 * 
 *  Popular a db para testes
 *  -------------------------------------
 *  docker exec florinhas_db psql -U SpringTestAdmin -d florinhas -c "
    -- 500 marcações
    INSERT INTO marcacao (criado_em, data, duration, estado, version, atendente_id)
    SELECT
    NOW() - (random() * interval '365 days'),
    NOW() + (random() * interval '180 days') - interval '90 days',
    (ARRAY[15,30,45,60])[floor(random()*4+1)::int],
    (ARRAY['AGENDADO','CONCLUIDO','CANCELADO','EM_PROGRESSO','NAO_COMPARECIDO'])[floor(random()*5+1)::int],
    0,
    1
    FROM generate_series(1, 500);

    -- 500 requisições
    INSERT INTO requisicao (criado_em, descricao, estado, prioridade, tipo, ultima_alteracao_estado_em, version, criado_por_id)
    SELECT
    NOW() - (random() * interval '365 days'),
    'Requisição de teste número ' || i,
    (ARRAY['ABERTO','EM_PROGRESSO','FECHADO','RECUSADO'])[floor(random()*4+1)::int],
    (ARRAY['BAIXA','MEDIA','ALTA','URGENTE'])[floor(random()*4+1)::int],
    (ARRAY['MATERIAL','TRANSPORTE','MANUTENCAO'])[floor(random()*3+1)::int],
    NOW() - (random() * interval '30 days'),
    0,
    1
    FROM generate_series(1, 500) AS i;

    -- Subtipos
    INSERT INTO requisicao_material (requisicao_id)
    SELECT id FROM requisicao WHERE tipo = 'MATERIAL'
    AND id NOT IN (SELECT requisicao_id FROM requisicao_material);

    INSERT INTO requisicao_transporte (requisicao_id)
    SELECT id FROM requisicao WHERE tipo = 'TRANSPORTE'
    AND id NOT IN (SELECT requisicao_id FROM requisicao_transporte);

    INSERT INTO requisicao_manutencao (requisicao_id)
    SELECT id FROM requisicao WHERE tipo = 'MANUTENCAO'
    AND id NOT IN (SELECT requisicao_id FROM requisicao_manutencao);

    -- Confirma
    SELECT 'marcacao' as tabela, count(*) FROM marcacao
    UNION ALL
    SELECT 'requisicao', count(*) FROM requisicao;" 
*/
