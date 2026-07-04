const { spawn } = require("child_process");
const path = require("path");

const TEST_SCRIPT = path.join(__dirname, "donate-concurrency-test.cjs");

const START_USERS = Number(process.env.START_USERS || 100);
const MAX_USERS = Number(process.env.MAX_USERS || 5000);
const MULTIPLIER = Number(process.env.MULTIPLIER || 2);
const SEARCH_PRECISION = Number(process.env.SEARCH_PRECISION || 25);

const MAX_FAILURE_RATE = Number(process.env.MAX_FAILURE_RATE || 0.01);
const MAX_P95_MS = Number(process.env.MAX_P95_MS || 1000);
const MAX_P99_MS = Number(process.env.MAX_P99_MS || 2000);
const MAX_EXIT_CODE = Number(process.env.MAX_EXIT_CODE || 0);

function extractNumber(output, label) {
    const escaped = label.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    const match = output.match(new RegExp(`${escaped}:\\s+([0-9.]+)`));
    return match ? Number(match[1]) : null;
}

function parseMetrics(output) {
    const total = extractNumber(output, "TOTAL");
    const success = extractNumber(output, "SUCCESS");
    const failed = extractNumber(output, "FAILED");
    const totalDurationMs = extractNumber(output, "TOTAL_DURATION_MS");

    return {
        total,
        success,
        failed,
        avgMs: extractNumber(output, "AVG_MS"),
        minMs: extractNumber(output, "MIN_MS"),
        maxMs: extractNumber(output, "MAX_MS"),
        p50Ms: extractNumber(output, "P50_MS"),
        p95Ms: extractNumber(output, "P95_MS"),
        p99Ms: extractNumber(output, "P99_MS"),
        totalDurationMs,
        successRate:
            total && success !== null
                ? success / total
                : null,
        failureRate:
            total && failed !== null
                ? failed / total
                : null,
        opsPerSec:
            total && totalDurationMs
                ? total / (totalDurationMs / 1000)
                : null
    };
}

function formatNumber(value, digits = 2) {
    return value === null || value === undefined || Number.isNaN(value)
        ? "-"
        : value.toFixed(digits);
}

function isStable(result) {
    const { metrics, exitCode } = result;

    if (exitCode > MAX_EXIT_CODE) {
        return false;
    }

    if (metrics.failureRate !== null && metrics.failureRate > MAX_FAILURE_RATE) {
        return false;
    }

    if (metrics.p95Ms !== null && metrics.p95Ms > MAX_P95_MS) {
        return false;
    }

    if (metrics.p99Ms !== null && metrics.p99Ms > MAX_P99_MS) {
        return false;
    }

    return true;
}

function printResult(label, result) {
    const { totalUsers, metrics, exitCode } = result;
    const stable = isStable(result) ? "PASS" : "FAIL";

    console.log(
        [
            `${label}: users=${totalUsers}`,
            `stable=${stable}`,
            `successRate=${metrics.successRate === null ? "-" : `${(metrics.successRate * 100).toFixed(2)}%`}`,
            `failureRate=${metrics.failureRate === null ? "-" : `${(metrics.failureRate * 100).toFixed(2)}%`}`,
            `p95=${formatNumber(metrics.p95Ms)}ms`,
            `p99=${formatNumber(metrics.p99Ms)}ms`,
            `ops/sec=${formatNumber(metrics.opsPerSec)}`,
            `exit=${exitCode}`
        ].join(" | ")
    );
}

function runLevel(totalUsers) {
    return new Promise((resolve) => {
        const child = spawn(
            process.execPath,
            [TEST_SCRIPT],
            {
                cwd: __dirname,
                env: {
                    ...process.env,
                    TOTAL_USERS: String(totalUsers)
                },
                stdio: ["ignore", "pipe", "pipe"]
            }
        );

        let stdout = "";
        let stderr = "";

        child.stdout.on("data", (chunk) => {
            const text = chunk.toString();
            stdout += text;
            process.stdout.write(text);
        });

        child.stderr.on("data", (chunk) => {
            const text = chunk.toString();
            stderr += text;
            process.stderr.write(text);
        });

        child.on("close", (code) => {
            resolve({
                totalUsers,
                exitCode: code ?? 1,
                stdout,
                stderr,
                metrics: parseMetrics(stdout)
            });
        });
    });
}

async function findUpperBound() {
    let currentUsers = START_USERS;
    let lastPassing = null;
    let firstFailing = null;

    while (currentUsers <= MAX_USERS) {
        console.log(`\n===== Capacity probe: ${currentUsers} users =====`);
        const result = await runLevel(currentUsers);
        printResult("Probe", result);

        if (isStable(result)) {
            lastPassing = result;
            currentUsers = Math.min(MAX_USERS, Math.max(currentUsers + 1, Math.floor(currentUsers * MULTIPLIER)));

            if (lastPassing.totalUsers === MAX_USERS) {
                return { lastPassing, firstFailing: null };
            }

            continue;
        }

        firstFailing = result;
        break;
    }

    return { lastPassing, firstFailing };
}

async function binarySearchCapacity(lowPass, highFail) {
    let bestPass = lowPass;
    let low = lowPass.totalUsers;
    let high = highFail.totalUsers;

    while (high - low > SEARCH_PRECISION) {
        const mid = Math.floor((low + high) / 2);
        console.log(`\n===== Capacity refine: ${mid} users =====`);
        const result = await runLevel(mid);
        printResult("Refine", result);

        if (isStable(result)) {
            bestPass = result;
            low = mid;
        } else {
            high = mid;
        }
    }

    return {
        bestPass,
        failBoundary: highFail,
        low,
        high
    };
}

function printThresholds() {
    console.log("=== Capacity thresholds ===");
    console.log(`MAX_FAILURE_RATE: ${(MAX_FAILURE_RATE * 100).toFixed(2)}%`);
    console.log(`MAX_P95_MS: ${MAX_P95_MS}`);
    console.log(`MAX_P99_MS: ${MAX_P99_MS}`);
    console.log(`MAX_EXIT_CODE: ${MAX_EXIT_CODE}`);
    console.log(`START_USERS: ${START_USERS}`);
    console.log(`MAX_USERS: ${MAX_USERS}`);
    console.log(`MULTIPLIER: ${MULTIPLIER}`);
    console.log(`SEARCH_PRECISION: ${SEARCH_PRECISION}`);
}

async function main() {
    if (!Number.isFinite(START_USERS) || START_USERS <= 0) {
        throw new Error("START_USERS must be a positive number.");
    }

    if (!Number.isFinite(MAX_USERS) || MAX_USERS < START_USERS) {
        throw new Error("MAX_USERS must be >= START_USERS.");
    }

    printThresholds();

    const { lastPassing, firstFailing } = await findUpperBound();

    if (!lastPassing) {
        console.log("\n=== Capacity result ===");
        console.log("No passing level found. The system failed the very first probe.");
        process.exit(1);
    }

    if (!firstFailing) {
        console.log("\n=== Capacity result ===");
        console.log(`No failing level found up to ${MAX_USERS} users.`);
        console.log(`Current verified stable capacity: at least ${lastPassing.totalUsers} concurrent users.`);
        return;
    }

    const refined = await binarySearchCapacity(lastPassing, firstFailing);

    console.log("\n=== Capacity result ===");
    console.log(`Stable capacity estimate: ${refined.bestPass.totalUsers} concurrent users.`);
    console.log(`First failing boundary observed: ${refined.failBoundary.totalUsers} concurrent users.`);
    console.log(
        `Search window after refinement: ${refined.low}..${refined.high} users (precision ${SEARCH_PRECISION}).`
    );
    console.log(
        `Best stable metrics: p95=${formatNumber(refined.bestPass.metrics.p95Ms)}ms, p99=${formatNumber(refined.bestPass.metrics.p99Ms)}ms, failureRate=${refined.bestPass.metrics.failureRate === null ? "-" : `${(refined.bestPass.metrics.failureRate * 100).toFixed(2)}%`}, ops/sec=${formatNumber(refined.bestPass.metrics.opsPerSec)}`
    );
}

main().catch((error) => {
    console.error("Donate capacity test crashed:", error);
    process.exit(1);
});
