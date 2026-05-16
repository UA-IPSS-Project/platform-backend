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

// Utilizadores reais do sistema
export const USERS = [
    { email: 'secretaria@florinhasdovouga.pt', password: 'sec123' },
    // Adiciona mais se tiveres
];

export function randomUser() {
    return USERS[Math.floor(Math.random() * USERS.length)];
}