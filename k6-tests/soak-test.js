import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
import {
	USERS,
	BASE_URL,
	checkHealth,
	seedUsers,
	loginUtente,
	listMarcacoesUtente,
	getCurrentUser,
	createMarcacaoRemota,
	buildHeaders,
} from './common.js';

const errorRate = new Rate('errors');
const loginErrorRate = new Rate('login_errors');
const marcacaoErrorRate = new Rate('marcacao_errors');
const authErrorRate = new Rate('auth_errors');

// Soak test: carga moderada e constante durante longo período para detetar
// fugas de memória, degradação de performance e esgotamento de recursos.
export const options = {
	stages: [
		{ duration: '2m',  target: 20 },  // ramp up
		{ duration: '56m', target: 20 },  // carga estável (1h total)
		{ duration: '2m',  target: 0  },  // ramp down
	],
	thresholds: {
		http_req_duration: ['p(95)<2500'],
		errors:            ['rate<0.1'],
		login_errors:      ['rate<0.05'],
		auth_errors:       ['rate<0.08'],
		marcacao_errors:   ['rate<0.15'],
	},
};

export function setup() {
	checkHealth();
	seedUsers(USERS);
	return { users: USERS };
}

export default function soakTest(data) {
	const user = data.users[__VU % data.users.length];

	const session = loginUtente(user);
	const loginOk = !!session;
	loginErrorRate.add(!loginOk);

	if (!loginOk) {
		errorRate.add(true);
		sleep(1);
		return;
	}

	// Read 1: perfil do utilizador (sempre)
	const meRes = getCurrentUser(session);
	const meOk = check(meRes, {
		'soak auth me 200': (r) => r.status === 200,
	});
	authErrorRate.add(!meOk);

	// Read 2: lista de marcações do utilizador (sempre)
	const listRes = listMarcacoesUtente(session);
	const listOk = check(listRes, {
		'soak list status 200': (r) => r.status === 200,
		'soak list body is array': (r) => {
			try {
				return Array.isArray(r.json());
			} catch {
				return false;
			}
		},
	});

	// Read 3: detalhe de uma marcação existente (70% das vezes, se houver alguma)
	let detailOk = true;
	if (Math.random() < 0.7) {
		let marcacoes = [];
		try { marcacoes = listRes.json(); } catch { /* ignorar */ }
		if (Array.isArray(marcacoes) && marcacoes.length > 0) {
			const m = marcacoes[Math.floor(Math.random() * marcacoes.length)];
			const detailRes = http.get(`${BASE_URL}/api/marcacoes/${m.id}`, {
				headers: buildHeaders(session, false, false),
				tags: { name: 'Get Marcacao Detail' },
				responseCallback: http.expectedStatuses(200, 403, 404),
			});
			detailOk = check(detailRes, {
				'soak detail status acceptable': (r) => [200, 403, 404].includes(r.status),
			});
		}
	}

	// Write: criar marcação (apenas 10% das vezes)
	let createOk = true;
	if (Math.random() < 0.1) {
		const seed = (__VU * 1000000) + __ITER;
		const createRes = createMarcacaoRemota(session, seed);
		createOk = check(createRes, {
			'soak create status acceptable': (r) => [200, 400, 409].includes(r.status),
		});

		const businessFailure = ![200, 400, 409].includes(createRes.status);
		marcacaoErrorRate.add(businessFailure);
	}

	errorRate.add(!(meOk && listOk && detailOk && createOk));

	sleep(1);
}

export function handleSummary(data) {
	const testName = 'soak_test';
	const reportName = __ENV.REPORT_NAME || testName;
	const reportDir = (__ENV.REPORT_DIR || `./results/${testName}`).replace(/\/$/, '');
	return {
		'stdout': textSummary(data, { indent: ' ', enableColors: true }),
		[`${reportDir}/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
		[`${reportDir}/${reportName}.json`]: JSON.stringify(data),
	};
}
