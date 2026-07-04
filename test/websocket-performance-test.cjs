const http = require("http");
const https = require("https");
const SockJS = require("../donate-web/node_modules/sockjs-client");
const { Client } = require("../donate-web/node_modules/@stomp/stompjs/bundles/stomp.umd.js");

const WS_URL = process.env.WS_URL || "http://localhost:8080/ws";
const API_BASE_URL = process.env.API_BASE_URL || "http://localhost:8080";
const TOTAL_CONNECTIONS = Number(process.env.TOTAL_CONNECTIONS || 500);
const CONNECT_TIMEOUT_MS = Number(30000);
const HOLD_MS = Number(process.env.HOLD_MS || 10000);
const SUBSCRIPTION_READY_DELAY_MS = Number(process.env.SUBSCRIPTION_READY_DELAY_MS || 500);

const STREAMER_ID = Number(process.env.STREAMER_ID || 1);
const METHOD_ID = Number(process.env.METHOD_ID || 1);
const DONOR_NAME = process.env.DONOR_NAME || "PerfTest";
const DONOR_ID = process.env.DONOR_ID ? Number(process.env.DONOR_ID) : null;
const AMOUNT = Number(process.env.AMOUNT || 1000);
const MESSAGE = process.env.MESSAGE || "WebSocket performance test";

const EXISTING_DONATION_ID = process.env.DONATION_ID || "";
const EXISTING_ADD_INFO = process.env.ADD_INFO || "";
const CREATE_DONATION_PATH = process.env.CREATE_DONATION_PATH || "/api/donate/qr";
const WEBHOOK_PATH = process.env.WEBHOOK_PATH || "/api/webhooks/sepay";
const SUBSCRIBE_DESTINATION = process.env.SUBSCRIBE_DESTINATION || "";
const SKIP_BROADCAST_TEST = process.env.SKIP_BROADCAST_TEST === "true";

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function toUrl(pathOrUrl) {
    return pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")
        ? new URL(pathOrUrl)
        : new URL(pathOrUrl, API_BASE_URL);
}

function postJson(pathOrUrl, payload) {
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
                }
            },
            (response) => {
                let responseBody = "";

                response.setEncoding("utf8");
                response.on("data", (chunk) => {
                    responseBody += chunk;
                });
                response.on("end", () => {
                    const isOk = response.statusCode >= 200 && response.statusCode < 300;
                    let parsed = null;

                    try {
                        parsed = responseBody ? JSON.parse(responseBody) : null;
                    } catch (_) {
                        parsed = responseBody;
                    }

                    if (!isOk) {
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

        request.on("error", reject);
        request.write(body);
        request.end();
    });
}

async function createPendingDonation() {
    if (EXISTING_DONATION_ID && EXISTING_ADD_INFO) {
        return {
            donationId: String(EXISTING_DONATION_ID),
            addInfo: EXISTING_ADD_INFO,
            amount: AMOUNT
        };
    }

    const payload = {
        streamerId: STREAMER_ID,
        donorName: DONOR_NAME,
        amount: AMOUNT,
        message: MESSAGE,
        methodId: METHOD_ID
    };

    if (DONOR_ID !== null && Number.isFinite(DONOR_ID)) {
        payload.donorId = DONOR_ID;
    }

    const response = await postJson(CREATE_DONATION_PATH, payload);

    if (!response?.donationId || !response?.addInfo) {
        throw new Error(`Create donation response is missing donationId/addInfo: ${JSON.stringify(response)}`);
    }

    return {
        donationId: String(response.donationId),
        addInfo: response.addInfo,
        amount: Number(response.amount || AMOUNT)
    };
}

async function triggerPaymentWebhook(addInfo, amount) {
    const payload = {
        transferType: "in",
        content: addInfo,
        transferAmount: Math.round(amount),
        referenceCode: `PERF-${Date.now()}`
    };

    return postJson(WEBHOOK_PATH, payload);
}

function percentile(values, p) {
    if (!values.length) {
        return 0;
    }

    const sorted = [...values].sort((a, b) => a - b);
    const index = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
    return sorted[Math.max(0, index)];
}

function createConnection(index, destination) {
    return new Promise((resolve) => {
        let settled = false;
        let subscription = null;
        const state = {
            connectCompletedAt: 0,
            messageReceived: false,
            receivedAt: 0,
            lastMessageBody: null
        };

        const startedAt = process.hrtime.bigint();
        const timeout = setTimeout(() => {
            finalize("timeout", new Error(`Connection ${index} timed out after ${CONNECT_TIMEOUT_MS}ms`));
        }, CONNECT_TIMEOUT_MS);

        const client = new Client({
            webSocketFactory: () => new SockJS(WS_URL),
            reconnectDelay: 0,
            debug: () => {}
        });

        function finalize(status, error) {
            if (settled) {
                return;
            }

            settled = true;
            clearTimeout(timeout);

            const finishedAt = process.hrtime.bigint();
            const connectDurationMs = Number(finishedAt - startedAt) / 1e6;

            resolve({
                index,
                status,
                error: error ? error.message : null,
                connectDurationMs,
                state,
                subscription,
                client
            });
        }

        client.onConnect = () => {
            state.connectCompletedAt = Date.now();

            if (destination) {
                subscription = client.subscribe(destination, (message) => {
                    state.messageReceived = true;
                    state.receivedAt = Date.now();
                    state.lastMessageBody = message.body;
                });
            }

            finalize("connected");
        };

        client.onStompError = (frame) => {
            const message = frame.headers?.message || frame.body || `STOMP error on connection ${index}`;
            finalize("stomp_error", new Error(message));
        };

        client.onWebSocketError = (event) => {
            const message = event?.message || `WebSocket error on connection ${index}`;
            finalize("websocket_error", new Error(message));
        };

        client.onWebSocketClose = (event) => {
            if (!settled) {
                const message = `WebSocket closed before connect on connection ${index} (code=${event?.code ?? "unknown"})`;
                finalize("closed_early", new Error(message));
            }
        };

        client.activate();
    });
}

async function waitForBroadcast(results, expectedCount, triggerStartedAt) {
    const pending = results.filter((item) => item.status === "connected");
    const startedAt = Date.now();
    const deadline = startedAt + CONNECT_TIMEOUT_MS;

    while (Date.now() < deadline) {
        const received = pending.filter((item) => item.state.messageReceived);
        if (received.length >= expectedCount) {
            const latencies = received.map((item) => item.state.receivedAt - triggerStartedAt);
            return {
                receivedCount: received.length,
                missingCount: expectedCount - received.length,
                latencies
            };
        }

        await wait(50);
    }

    const received = pending.filter((item) => item.state.messageReceived);
    const latencies = received.map((item) => item.state.receivedAt - triggerStartedAt);

    return {
        receivedCount: received.length,
        missingCount: expectedCount - received.length,
        latencies
    };
}

async function deactivateClients(results) {
    await Promise.allSettled(
        results
            .filter((item) => item.status === "connected")
            .map(async (item) => {
                try {
                    item.subscription?.unsubscribe();
                } catch (_) {
                    // Ignore cleanup errors in the load script.
                }

                return item.client.deactivate();
            })
    );
}

function printSummary(summary) {
    console.log("=== WebSocket performance test summary ===");
    console.log(`WS_URL: ${WS_URL}`);
    console.log(`API_BASE_URL: ${API_BASE_URL}`);
    console.log(`TOPIC: ${summary.destination}`);
    console.log(`TOTAL_CONNECTIONS: ${TOTAL_CONNECTIONS}`);
    console.log(`CONNECTED: ${summary.connectedCount}`);
    console.log(`FAILED: ${summary.failedCount}`);
    console.log(`TOTAL_DURATION_MS: ${summary.totalDurationMs.toFixed(2)}`);
    console.log(`AVG_CONNECT_MS: ${summary.avgConnectMs.toFixed(2)}`);
    console.log(`MIN_CONNECT_MS: ${summary.minConnectMs.toFixed(2)}`);
    console.log(`MAX_CONNECT_MS: ${summary.maxConnectMs.toFixed(2)}`);

    if (summary.broadcast) {
        console.log("=== Broadcast summary ===");
        console.log(`BROADCAST_RECEIVED: ${summary.broadcast.receivedCount}`);
        console.log(`BROADCAST_MISSING: ${summary.broadcast.missingCount}`);
        console.log(`AVG_BROADCAST_MS: ${summary.broadcast.avgLatencyMs.toFixed(2)}`);
        console.log(`MIN_BROADCAST_MS: ${summary.broadcast.minLatencyMs.toFixed(2)}`);
        console.log(`MAX_BROADCAST_MS: ${summary.broadcast.maxLatencyMs.toFixed(2)}`);
        console.log(`P50_BROADCAST_MS: ${summary.broadcast.p50LatencyMs.toFixed(2)}`);
        console.log(`P95_BROADCAST_MS: ${summary.broadcast.p95LatencyMs.toFixed(2)}`);
        console.log(`P99_BROADCAST_MS: ${summary.broadcast.p99LatencyMs.toFixed(2)}`);
    }

    if (summary.failures.length) {
        console.log("=== Failed connections ===");
        summary.failures.slice(0, 20).forEach((item) => {
            console.log(`#${item.index} ${item.status}: ${item.error}`);
        });

        if (summary.failures.length > 20) {
            console.log(`... and ${summary.failures.length - 20} more failures`);
        }
    }
}

async function main() {
    const startedAt = process.hrtime.bigint();

    const donationContext = SUBSCRIBE_DESTINATION
        ? { donationId: "custom", addInfo: "", amount: AMOUNT }
        : await createPendingDonation();
    const destination = SUBSCRIBE_DESTINATION || `/topic/payment/${donationContext.donationId}`;

    console.log(`Starting websocket performance test with ${TOTAL_CONNECTIONS} concurrent connections...`);
    console.log(`Subscribing all clients to ${destination}`);

    const results = await Promise.all(
        Array.from({ length: TOTAL_CONNECTIONS }, (_, index) => createConnection(index + 1, destination))
    );

    const connected = results.filter((item) => item.status === "connected");
    const failures = results.filter((item) => item.status !== "connected");

    let broadcast = null;

    if (!SKIP_BROADCAST_TEST && !SUBSCRIBE_DESTINATION) {
        console.log(`Waiting ${SUBSCRIPTION_READY_DELAY_MS}ms for broker subscriptions to settle...`);
        await wait(SUBSCRIPTION_READY_DELAY_MS);

        console.log(`Triggering payment success webhook for donation ${donationContext.donationId}...`);
        const triggerStartedAt = Date.now();
        await triggerPaymentWebhook(donationContext.addInfo, donationContext.amount);

        broadcast = await waitForBroadcast(results, connected.length, triggerStartedAt);

        const latencies = broadcast.latencies;
        const avgLatencyMs = latencies.length
            ? latencies.reduce((sum, value) => sum + value, 0) / latencies.length
            : 0;

        broadcast = {
            ...broadcast,
            avgLatencyMs,
            minLatencyMs: latencies.length ? Math.min(...latencies) : 0,
            maxLatencyMs: latencies.length ? Math.max(...latencies) : 0,
            p50LatencyMs: percentile(latencies, 50),
            p95LatencyMs: percentile(latencies, 95),
            p99LatencyMs: percentile(latencies, 99)
        };
    } else if (connected.length) {
        console.log(`Holding ${connected.length} active connections for ${HOLD_MS}ms...`);
        await wait(HOLD_MS);
    }

    await deactivateClients(results);

    const totalDurationMs = Number(process.hrtime.bigint() - startedAt) / 1e6;
    const connectTimes = connected.map((item) => item.connectDurationMs);
    const avgConnectMs = connectTimes.length
        ? connectTimes.reduce((sum, value) => sum + value, 0) / connectTimes.length
        : 0;

    printSummary({
        destination,
        connectedCount: connected.length,
        failedCount: failures.length,
        totalDurationMs,
        avgConnectMs,
        minConnectMs: connectTimes.length ? Math.min(...connectTimes) : 0,
        maxConnectMs: connectTimes.length ? Math.max(...connectTimes) : 0,
        broadcast,
        failures
    });

    const hasFailures = failures.length > 0;
    const hasMissingBroadcast = broadcast && broadcast.missingCount > 0;
    process.exit(hasFailures || hasMissingBroadcast ? 1 : 0);
}

main().catch((error) => {
    console.error("WebSocket performance test crashed:", error);
    process.exit(1);
});
