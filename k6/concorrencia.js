import http from 'k6/http';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const N = Number(__ENV.N || 20);
const DATE = __ENV.DATE;
const PROFESSIONAL_ID = Number(__ENV.PROFESSIONAL_ID || 1);
const SERVICE_ID = Number(__ENV.SERVICE_ID || 1);

const criados = new Counter('agendamentos_criados');
const recusados = new Counter('agendamentos_recusados');
const erros = new Counter('outros_erros');

export const options = {
  scenarios: {
    simultaneos: { executor: 'per-vu-iterations', vus: N, iterations: 1, maxDuration: '1m' },
  },
};

export function setup() {
  if (!DATE) throw new Error('Informe a data com -e DATE=aaaa-mm-dd');
  const res = http.get(`${BASE_URL}/professionals/${PROFESSIONAL_ID}/availability?date=${DATE}&service_id=${SERVICE_ID}`);
  const slots = res.json('slots');
  if (!slots || slots.length === 0) throw new Error(`Sem horário livre em ${DATE}`);
  return { time: slots[0] };
}

export default function (data) {
  const token = `00000000-0000-4000-8000-${String(__VU).padStart(12, '0')}`;
  const body = JSON.stringify({
    professional_id: PROFESSIONAL_ID,
    service_id: SERVICE_ID,
    scheduled_date: DATE,
    scheduled_time: data.time,
  });
  const res = http.post(`${BASE_URL}/appointments`, body, {
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
  });

  if (res.status === 200 || res.status === 201) criados.add(1);
  else if (res.status === 409) recusados.add(1);
  else erros.add(1);
}

export function handleSummary(data) {
  const count = (name) => (data.metrics[name] ? data.metrics[name].values.count : 0);
  const p95 = data.metrics.http_req_duration.values['p(95)'].toFixed(0);
  return {
    stdout: `\nRequisições simultâneas: ${N} (${DATE})\n`
      + `Criados: ${count('agendamentos_criados')}\n`
      + `Recusados (409): ${count('agendamentos_recusados')}\n`
      + `Outros erros: ${count('outros_erros')}\n`
      + `Tempo de resposta p95: ${p95} ms\n`,
  };
}
