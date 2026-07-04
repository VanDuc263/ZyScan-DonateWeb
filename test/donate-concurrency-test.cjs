const http = require("http");
const https = require("https");

const API_BASE_URL = process.env.API_BASE_URL || "http://localhost:8080";
const TOTAL_USERS = Number(process.env.TOTAL_USERS || 100);
const STREAMER_ID = Number(process.env.STREAMER_ID || 1);
const METHOD_ID = Number(process.env.METHOD_ID || 1);
const AMOUNT = Number(process.env.AMOUNT || 1000);
const REQUEST_TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS || 30000);
const DONATE_MODE = (process.env.DONATE_MODE || "qr").toLowerCase();
const DONATE_PATH = process.env.DONATE_PATH || "";
const DONOR_NAME_PREFIX = process.env.DONOR_NAME_PREFIX || "PerfUser";
const MESSAGE_PREFIX = process.env.MESSAGE_PREFIX || "Concurrent donate test";
const DONOR_ID_START = Number(process.env.DONOR_ID_START || 1);
const TRIGGER_WEBHOOK = process.env.TRIGGER_WEBHOOK === "true";
const WEBHOOK_PATH = process.env.WEBHOOK_PATH || "/api/webhooks/sepay";

function toUrl(pathOrUrl) {
    return pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")
        ? new URL(pathOrUrl)
        : new URL(pathOrUrl, API_BASE_URL);
}

function postJson(pathOrUrl, payload, timeoutMs = REQUEST_TIMEOUT_MS) {
    const url = toUrl(pathOrUrl);
    const body = JSON.stringify(payload);
    const transport = url.protocol === "https:" ? https : http;

    return new Promise((resolve, reject) => {
        const request = transport.request(
            {
                method: "POST",
                hostname: url.hostname,
                port: url.port || (url.protocol === "https:" ? 443 : 80),
                path: `${url.pathname}${url.search}`,
                headers: {
                    "Content-Type": "application/json",
                    "Content-Length": Buffer.byteLength(body)
                },
                timeout: timeoutMs
            },
            (response) => {
                let responseBody = "";

                response.setEncoding("utf8");
                response.on("data", (chunk) => {
                    responseBody += chunk;
                });
                response.on("end", () => {
                    let parsed = null;

                    try {
                        parsed = responseBody ? JSON.parse(responseBody) : null;
                    } catch (_) {
                        parsed = responseBody;
                    }

                    if (response.statusCode < 200 || response.statusCode >= 300) {
                        reject(
                            new Error(
                                `POST ${url} failed with ${response.statusCode}: ${
                                    typeof parsed === "string" ? parsed : JSON.stringify(parsed)
                                }`
                            )
                        );
                        return;
                    }

                    resolve(parsed);
                });
            }
        );

        request.on("timeout", () => {
            request.destroy(new Error(`Request timeout after ${timeoutMs}ms`));
        });
        request.on("error", reject);
        request.write(body);
        request.end();
    });
}

function percentile(values, p) {
    if (!values.length) {
        return 0;
    }

    const sorted = [...values].sort((a, b) => a - b);
    const index = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
    return sorted[Math.max(0, index)];
}

function resolveDonatePath() {
    if (DONATE_PATH) {
        return DONATE_PATH;
    }

    if (DONATE_MODE === "wallet") {
        return "/api/donate/wallet";
    }

    if (DONATE_MODE === "bank-qr") {
        return "/api/donate/bank-qr";
    }

    return "/api/donate/qr";
}

function buildDonatePayload(index) {
    const payload = {
        streamerId: STREAMER_ID,
        donorName: `${DONOR_NAME_PREFIX}-${index}`,
        amount: AMOUNT,
        message: `${MESSAGE_PREFIX} #${index}`
    };

    if (DONATE_MODE === "wallet") {
        payload.donorId = DONOR_ID_START + index - 1;
    } else {
        payload.methodId = METHOD_ID;
    }

    return payload;
}

async function sendDonateRequest(index, donatePath) {
    const payload = buildDonatePayload(index);
    const startedAt = process.hrtime.bigint();

    try {
        const response = await postJson(donatePath, payload);
        const latencyMs = Number(process.hrtime.bigint() - startedAt) / 1e6;

        return {
            index,
            ok: true,
            latencyMs,
            payload,
            response
        };
    } catch (error) {
        const latencyMs = Number(process.hrtime.bigint() - startedAt) / 1e6;

        return {
            index,
            ok: false,
            latencyMs,
            payload,
            error: error.message
        };
    }
}

async function triggerWebhook(result) {
    const addInfo = result.response?.addInfo;
    const amount = Number(result.response?.amount || result.payload.amount);

    if (!addInfo) {
        return {
            index: result.index,
            ok: false,
            latencyMs: 0,
            error: "Missing addInfo in donation response"
        };
    }

    const startedAt = process.hrtime.bigint();

    try {
        const response = await postJson(WEBHOOK_PATH, {
            transferType: "in",
            content: addInfo,
            transferAmount: Math.round(amount),
            referenceCode: `DONATE-PERF-${result.index}-${Date.now()}`
        });

        const latencyMs = Number(process.hrtime.bigint() - startedAt) / 1e6;

        return {
            index: result.index,
            ok: true,
            latencyMs,
            response
        };
    } catch (error) {
        const latencyMs = Number(process.hrtime.bigint() - startedAt) / 1e6;

        return {
            index: result.index,
            ok: false,
            latencyMs,
            error: error.message
        };
    }
}

function summarize(label, results) {
    const succeeded = results.filter((item) => item.ok);
    const failed = results.filter((item) => !item.ok);
    const latencies = succeeded.map((item) => item.latencyMs);
    const avgLatencyMs = latencies.length
        ? latencies.reduce((sum, value) => sum + value, 0) / latencies.length
        : 0;

    console.log(`=== ${label} summary ===`);
    console.log(`TOTAL: ${results.length}`);
    console.log(`SUCCESS: ${succeeded.length}`);
    console.log(`FAILED: ${failed.length}`);
    console.log(`AVG_MS: ${avgLatencyMs.toFixed(2)}`);
    console.log(`MIN_MS: ${(latencies.length ? Math.min(...latencies) : 0).toFixed(2)}`);
    console.log(`MAX_MS: ${(latencies.length ? Math.max(...latencies) : 0).toFixed(2)}`);
    console.log(`P50_MS: ${percentile(latencies, 50).toFixed(2)}`);
    console.log(`P95_MS: ${percentile(latencies, 95).toFixed(2)}`);
    console.log(`P99_MS: ${percentile(latencies, 99).toFixed(2)}`);

    if (failed.length) {
        console.log(`=== ${label} failures ===`);
        failed.slice(0, 20).forEach((item) => {
            console.log(`#${item.index}: ${item.error}`);
        });

        if (failed.length > 20) {
            console.log(`... and ${failed.length - 20} more failures`);
        }
    }
}

async function main() {
    const donatePath = resolveDonatePath();
    const startedAt = process.hrtime.bigint();

    console.log(`Starting donate concurrency test for streamer ${STREAMER_ID} with ${TOTAL_USERS} concurrent users...`);
    console.log(`DONATE_MODE: ${DONATE_MODE}`);
    console.log(`DONATE_PATH: ${donatePath}`);

    const donateResults = await Promise.all(
        Array.from({ length: TOTAL_USERS }, (_, index) => sendDonateRequest(index + 1, donatePath))
    );

    console.log("=== Donate test context ===");
    console.log(`API_BASE_URL: ${API_BASE_URL}`);
    console.log(`STREAMER_ID: ${STREAMER_ID}`);
    console.log(`AMOUNT: ${AMOUNT}`);
    console.log(`REQUEST_TIMEOUT_MS: ${REQUEST_TIMEOUT_MS}`);

    summarize("Donate", donateResults);

    if (TRIGGER_WEBHOOK && DONATE_MODE !== "wallet") {
        const successfulDonations = donateResults.filter((item) => item.ok);
        console.log(`Triggering webhook concurrently for ${successfulDonations.length} successful pending donations...`);
        const webhookResults = await Promise.all(successfulDonations.map((item) => triggerWebhook(item)));
        summarize("Webhook settle", webhookResults);
    }

    const totalDurationMs = Number(process.hrtime.bigint() - startedAt) / 1e6;
    console.log(`TOTAL_DURATION_MS: ${totalDurationMs.toFixed(2)}`);

    process.exit(donateResults.some((item) => !item.ok) ? 1 : 0);
}

main().catch((error) => {
    console.error("Donate concurrency test crashed:", error);
    process.exit(1);
});
