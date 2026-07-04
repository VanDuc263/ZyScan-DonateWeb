import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";

const baseUrl = __ENV.API_BASE_URL || "http://localhost:8080";
const donatePath = __ENV.DONATE_PATH || "/api/donate/qr";
const streamerId = Number(__ENV.STREAMER_ID || 1);
const methodId = Number(__ENV.METHOD_ID || 1);
const amount = Number(__ENV.AMOUNT || 1000);
const testDuration = __ENV.TEST_DURATION || "1m";
const coolDown = __ENV.COOLDOWN || "15s";
const thinkTimeMs = Number(__ENV.THINK_TIME_MS || 0);
const level = Number(__ENV.CONCURRENCY || 100);

const throughput = new Counter("donate_ops_total");
const failures = new Rate("donate_failed_rate");
const appLatency = new Trend("donate_app_latency_ms", true);

export const options = {
  scenarios: {
    donate_qr: {
      executor: "constant-vus",
      vus: level,
      duration: testDuration,
      gracefulStop: coolDown,
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1000", "p(99)<2000"],
    donate_failed_rate: ["rate<0.01"],
  },
  summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

function buildPayload(iteration) {
  return JSON.stringify({
    streamerId,
    donorName: `k6-user-${__VU}-${iteration}`,
    amount,
    message: `k6 donation ${__VU}-${iteration}`,
    methodId,
  });
}

export default function () {
  const payload = buildPayload(__ITER);
  const params = {
    headers: {
      "Content-Type": "application/json",
    },
    tags: {
      endpoint: "donate_qr",
      concurrency: String(level),
    },
    timeout: "30s",
  };

  const response = http.post(`${baseUrl}${donatePath}`, payload, params);

  const ok = check(response, {
    "status is 200": (r) => r.status === 200,
    "has donation id": (r) => {
      if (!r.body) return false;
      try {
        const body = JSON.parse(r.body);
        return Boolean(body.donationId || body.id);
      } catch (_) {
        return false;
      }
    },
  });

  throughput.add(1);
  failures.add(!ok);
  appLatency.add(response.timings.duration);

  if (thinkTimeMs > 0) {
    sleep(thinkTimeMs / 1000);
  }
}

export function handleSummary(data) {
  const totalOps = data.metrics.donate_ops_total
    ? data.metrics.donate_ops_total.count
    : 0;
  const durationMs = data.state.testRunDurationMs || 0;
  const opsPerSec = durationMs > 0 ? totalOps / (durationMs / 1000) : 0;

  return {
    stdout: [
      "",
      "=== k6 donate summary ===",
      `scenario_concurrency=${level}`,
      `total_ops=${totalOps}`,
      `test_duration_ms=${durationMs.toFixed(0)}`,
      `ops_per_sec=${opsPerSec.toFixed(2)}`,
      `http_p50_ms=${data.metrics.http_req_duration.values.med.toFixed(2)}`,
      `http_p95_ms=${data.metrics.http_req_duration.values["p(95)"].toFixed(2)}`,
      `http_p99_ms=${data.metrics.http_req_duration.values["p(99)"].toFixed(2)}`,
      `http_failed_rate=${data.metrics.http_req_failed.values.rate.toFixed(4)}`,
      "",
    ].join("\n"),
  };
}
