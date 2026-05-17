import http from 'k6/http';

export const BASE = 'https://20.63.17.2:3443';


export function doLogin(email, password) {
    const res = http.post(
        `${BASE}/api/auth/login/funcionario`,
        JSON.stringify({ email, password }),
        {
            headers: {
                'Content-Type': 'application/json',
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
// Gera um slot sequencial e único por VU, garantindo que salta fins de semana e feriados
export function dataFuturaSequencial(vuId, holidays = []) {
    let d = new Date();
    
    // Distribuímos as VUs por dias diferentes para evitar conflitos na mesma hora.
    let daysToAdd = vuId + Math.floor(slotCounter / 32);
    
    while (daysToAdd > 0) {
        d.setDate(d.getDate() + 1);
        const dayOfWeek = d.getDay();
        
        // Obtém YYYY-MM-DD de forma imune a fusos horários/UTC
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const yyyymmdd = `${year}-${month}-${day}`;
        
        const isFeriado = holidays.includes(yyyymmdd);
        
        if (dayOfWeek !== 0 && dayOfWeek !== 6 && !isFeriado) {
            daysToAdd--;
        }
    }

    const slotOfDay = slotCounter % 32;
    const totalMinutes = slotOfDay * 15;
    const horas = 9 + Math.floor(totalMinutes / 60);
    const minutos = totalMinutes % 60;

    d.setHours(horas, minutos, 0, 0);
    slotCounter++;
    return d.toISOString();
}