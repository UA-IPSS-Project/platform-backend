import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { doLogin, BASE, dataFuturaSequencial } from './utils/auth.js';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const marcacoesDur = new Trend('marcacoes_duration');
const requisicoesDur = new Trend('requisicoes_duration');
const agendaDur = new Trend('agenda_duration');
const criarMarcDur = new Trend('criar_marcacao_duration');
const criarReqDur = new Trend('criar_requisicao_duration');

export const options = {
    insecureSkipTLSVerify: true,
    stages: [
        { duration: '1m', target: 10 },
        { duration: '3m', target: 10 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'marcacoes_duration': ['p(95)<500'],
        'requisicoes_duration': ['p(95)<800'],
        'agenda_duration': ['p(95)<300'],
        'criar_marcacao_duration': ['p(95)<800'],
        'criar_requisicao_duration': ['p(95)<800'],
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

    // FLUXO 1: Dashboard de manhã
    group('Dashboard', () => {
        let t = Date.now();
        http.get(`${BASE}/api/marcacoes`, auth);
        marcacoesDur.add(Date.now() - t);

        t = Date.now();
        http.get(`${BASE}/api/marcacoes/agenda`, auth);
        agendaDur.add(Date.now() - t);

        http.get(`${BASE}/api/marcacoes/count/hoje`, auth);
        http.get(`${BASE}/api/marcacoes/passadas`, auth);
        sleep(2);
    });

    // FLUXO 2: Criar marcação presencial
    group('Criar Marcação', () => {
        const slotData = dataFuturaSequencial(__VU);
        const datePart = slotData.substring(0, 10);
        const timePart = slotData.substring(11, 16);
        
        // Verifica slot disponível
        const slotRes = http.get(
            `${BASE}/api/calendario/verificar-slot?data=${datePart}&hora=${timePart}&tipo=SECRETARIA`,
            auth
        );
        check(slotRes, { 'slot verificado': (r) => [200, 400].includes(r.status) });

        const utenteNif = "999999" + (900 + __VU);
        const utenteNome = "Utente VU " + __VU;

        // Cria marcação
        const t = Date.now();
        const marcRes = http.post(
            `${BASE}/api/marcacoes/presencial`,
            JSON.stringify({
                data: slotData,
                duration: 30,
                descricao: 'Marcação de teste k6',
                assunto: 'Atendimento Geral',
                utenteNif: utenteNif,
                utenteNome: utenteNome,
            }),
            auth
        );
        criarMarcDur.add(Date.now() - t);
        check(marcRes, { 'marcação criada': (r) => [200, 201, 400, 409].includes(r.status) });

        sleep(1);
    });

    // FLUXO 3: Criar requisição de material
    group('Criar Requisição', () => {
        const t = Date.now();
        const reqRes = http.post(
            `${BASE}/api/requisicoes/material`,
            JSON.stringify({
                descricao: 'Requisição de teste k6',
                prioridade: 'MEDIA',
                itens: [{ materialId: 1, quantidade: 1 }]
            }),
            auth
        );
        criarReqDur.add(Date.now() - t);
        check(reqRes, { 'requisição criada': (r) => [200, 201, 400].includes(r.status) });

        // Lista requisições após criar
        let t2 = Date.now();
        http.get(`${BASE}/api/requisicoes`, auth);
        requisicoesDur.add(Date.now() - t2);

        sleep(1);
    });

    // FLUXO 4: Gestão de agenda
    group('Gestão Agenda', () => {
        http.get(`${BASE}/api/calendario/bloqueios?tipo=SECRETARIA`, auth);
        http.get(`${BASE}/api/calendario/feriados?ano=2026`, auth);
        http.get(`${BASE}/api/calendario/configuracao-slots`, auth);
        sleep(1);
    });

    sleep(Math.random() * 2 + 1);
}

export function handleSummary(data) {
    return {
        'resultados/load/load-report.html': htmlReport(data),
        'resultados/load/load-result.json': JSON.stringify(data, null, 2),
        stdout: 'Load test concluído.\n',
    };
}