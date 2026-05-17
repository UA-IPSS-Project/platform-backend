import http from 'k6/http';

export const BASE = 'https://20.63.17.2:3443';

export function randomIp() {
    return `${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`;
}

export function doLogin(email, password) {
    const res = http.post(
        `${BASE}/api/auth/login/funcionario`,
        JSON.stringify({ email, password }),
        {
            headers: {
                'Content-Type': 'application/json',
                'X-Forwarded-For': randomIp(),
            },
        }
    );

    if (res.status !== 200) return null;

    const jwt = res.cookies['jwt'] ? res.cookies['jwt'][0].value : null;
    const xsrf = res.cookies['XSRF-TOKEN'] ? res.cookies['XSRF-TOKEN'][0].value : null;
    if (!jwt) return null;

    return {
        headers: {
            'Content-Type': 'application/json',
            'Cookie': `jwt=${jwt}; XSRF-TOKEN=${xsrf}`,
            'X-XSRF-TOKEN': xsrf,
            'X-Forwarded-For': randomIp(),
        },
    };
}

export const USERS = [
    { email: 'secretaria@florinhasdovouga.pt', password: 'sec123' },
    { email: 'balneario@florinhasdovouga.pt', password: 'bal123' },
    { email: 'escola@florinhasdovouga.pt', password: 'esc123' },
];

export function randomUser() {
    return USERS[Math.floor(Math.random() * USERS.length)];
}


let slotCounter = 0;
// Gera um slot sequencial e único por VU
export function dataFuturaSequencial(vuId) {
    const d = new Date();
    const extraDays = Math.floor(slotCounter / 16);
    // vuId garante que VUs diferentes operam em dias base diferentes.
    // extraDays * 1000 previne que VUs se cruzem quando avançam para dias extra.
    d.setDate(d.getDate() + vuId + (extraDays * 1000));

    const slotOfDay = slotCounter % 16;
    const horas = 9 + Math.floor(slotOfDay / 2);
    const minutos = (slotOfDay % 2) * 30;

    d.setHours(horas, minutos, 0, 0);
    slotCounter++;
    return d.toISOString();
}