import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
import {
	USERS,
	checkHealth,
	seedUsers,
	loginUtente,
	listMarcacoesUtente,
	getCurrentUser,
	createMarcacaoRemota,
} from './common.js';

const errorRate = new Rate('errors');
const loginErrorRate = new Rate('login_errors');
const marcacaoErrorRate = new Rate('marcacao_errors');
const authErrorRate = new Rate('auth_errors');

export const options = {
	stages: [
		{ duration: '1m', target: 20 },
		{ duration: '2m', target: 40 },
		{ duration: '3m', target: 60 },
		{ duration: '2m', target: 60 },
		{ duration: '2m', target: 0 },
	],
	thresholds: {
		http_req_duration: ['p(95)<3500'],
		errors: ['rate<0.2'],
		login_errors: ['rate<0.08'],
		auth_errors: ['rate<0.15'],
		marcacao_errors: ['rate<0.25'],
	},
};

export function setup() {
	checkHealth();
	seedUsers(USERS);
	return { users: USERS };
}

export default function stressTest(data) {
	const user = data.users[__VU % data.users.length];

	const session = loginUtente(user);
	const loginOk = !!session;
	loginErrorRate.add(!loginOk);

	if (!loginOk) {
		errorRate.add(true);
		sleep(1);
		return;
	}

	const meRes = getCurrentUser(session);
	const meOk = check(meRes, {
		'stress auth me 200': (r) => r.status === 200,
	});
	authErrorRate.add(!meOk);

	const listRes = listMarcacoesUtente(session);
	const listOk = check(listRes, {
		'stress list status 200': (r) => r.status === 200,
		'stress list body is array': (r) => {
			try {
				return Array.isArray(r.json());
			} catch {
				return false;
			}
		},
	});

	const seed = (__VU * 1000000) + __ITER;
	const createRes = createMarcacaoRemota(session, seed);
	const createOk = check(createRes, {
		'stress create status acceptable': (r) => [200, 400, 409].includes(r.status),
	});

	const businessFailure = ![200, 400, 409].includes(createRes.status);
	marcacaoErrorRate.add(businessFailure);

	errorRate.add(!(meOk && listOk && createOk));

	sleep(1);
}

export function handleSummary(data) {
	const testName = 'stress_test';
	const reportName = __ENV.REPORT_NAME || testName;
	const reportDir = (__ENV.REPORT_DIR || `./results/${testName}`).replace(/\/$/, '');
	return {
		'stdout': textSummary(data, { indent: ' ', enableColors: true }),
		[`${reportDir}/${reportName}.html`]: htmlReport(data, { title: `${reportName} Results` }),
		[`${reportDir}/${reportName}.json`]: JSON.stringify(data),
	};
}
