import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const SLUG = __ENV.SLUG || 'studio-carga';
const CITY = __ENV.CITY || 'João Monlevade';
const PROFESSIONAL_ID = Number(__ENV.PROFESSIONAL_ID || 1);
const SERVICE_ID = Number(__ENV.SERVICE_ID || 1);

const primeiraRequisicao = new Trend('primeira_requisicao', true);

export const options = {
  scenarios: {
    navegacao: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 5),
      duration: __ENV.DURATION || '1m',
    },
  },
  thresholds: {
    'http_req_duration{rota:busca}': ['p(95)<2000'],
    'http_req_duration{rota:perfil}': ['p(95)<2000'],
    'http_req_duration{rota:disponibilidade}': ['p(95)<2000'],
  },
  setupTimeout: '3m',
};

export function setup() {
  const res = http.get(`${BASE_URL}/health`, { timeout: '150s', tags: { rota: 'acordar' } });
  primeiraRequisicao.add(res.timings.duration);
  check(res, { 'servidor respondeu': (r) => r.status === 200 });
}

function tomorrow() {
  const date = new Date(Date.now() + 24 * 60 * 60 * 1000);
  return date.toISOString().slice(0, 10);
}

export default function () {
  const busca = http.get(`${BASE_URL}/search?city=${encodeURIComponent(CITY)}`, { tags: { rota: 'busca' } });
  check(busca, { 'busca 200': (r) => r.status === 200 });
  sleep(1);

  const perfil = http.get(`${BASE_URL}/p/${SLUG}`, { tags: { rota: 'perfil' } });
  check(perfil, { 'perfil 200': (r) => r.status === 200 });
  sleep(1);

  const disponibilidade = http.get(
    `${BASE_URL}/professionals/${PROFESSIONAL_ID}/availability?date=${tomorrow()}&service_id=${SERVICE_ID}`,
    { tags: { rota: 'disponibilidade' } },
  );
  check(disponibilidade, { 'disponibilidade 200': (r) => r.status === 200 });
  sleep(1);
}

export function handleSummary(data) {
  const route = (name) => {
    const metric = data.metrics[`http_req_duration{rota:${name}}`];
    if (!metric) return `${name}: sem dados`;
    const v = metric.values;
    return `${name}: média ${v.avg.toFixed(0)} ms, p95 ${v['p(95)'].toFixed(0)} ms, máximo ${v.max.toFixed(0)} ms`;
  };
  const first = data.metrics.primeira_requisicao ? data.metrics.primeira_requisicao.values.max.toFixed(0) : '-';
  const failed = (data.metrics.http_req_failed.values.rate * 100).toFixed(1);
  return {
    stdout: `\n${BASE_URL}\n`
      + `Primeira requisição (servidor possivelmente dormindo): ${first} ms\n`
      + `${route('busca')}\n${route('perfil')}\n${route('disponibilidade')}\n`
      + `Requisições com erro: ${failed}%\n`,
  };
}
