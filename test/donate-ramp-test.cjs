const { spawn } = require("child_process");
const path = require("path");

const LEVELS = (process.env.RAMP_LEVELS || "50,100,200,500")
    .split(",")
    .map((value) => Number(value.trim()))
    .filter((value) => Number.isFinite(value) && value > 0);

const TEST_SCRIPT = path.join(__dirname, "donate-concurrency-test.cjs");

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

function extractNumber(output, label) {
    const escaped = label.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    const match = output.match(new RegExp(`${escaped}:\\s+([0-9.]+)`));
    return match ? Number(match[1]) : null;
}

function parseMetrics(output) {
    return {
        total: extractNumber(output, "TOTAL"),
        success: extractNumber(output, "SUCCESS"),
        failed: extractNumber(output, "FAILED"),
        avgMs: extractNumber(output, "AVG_MS"),
        minMs: extractNumber(output, "MIN_MS"),
        maxMs: extractNumber(output, "MAX_MS"),
        p50Ms: extractNumber(output, "P50_MS"),
        p95Ms: extractNumber(output, "P95_MS"),
        p99Ms: extractNumber(output, "P99_MS"),
        totalDurationMs: extractNumber(output, "TOTAL_DURATION_MS")
    };
}

function formatNumber(value, digits = 2) {
    return value === null || Number.isNaN(value) ? "-" : value.toFixed(digits);
}

function formatInt(value) {
    return value === null || Number.isNaN(value) ? "-" : String(Math.round(value));
}

function printSummaryTable(results) {
    const headers = [
        "Users",
        "OK",
        "Fail",
        "Success%",
        "Avg ms",
        "P95 ms",
        "P99 ms",
        "Max ms",
        "Total ms",
        "Exit"
    ];

    const rows = results.map((result) => {
        const { metrics } = result;
        const total = metrics.total ?? result.totalUsers;
        const success = metrics.success;
        const failed = metrics.failed;
        const successRate = total && success !== null ? (success / total) * 100 : null;

        return [
            String(result.totalUsers),
            formatInt(success),
            formatInt(failed),
            successRate === null ? "-" : `${successRate.toFixed(1)}%`,
            formatNumber(metrics.avgMs),
            formatNumber(metrics.p95Ms),
            formatNumber(metrics.p99Ms),
            formatNumber(metrics.maxMs),
            formatNumber(metrics.totalDurationMs),
            String(result.exitCode)
        ];
    });

    const widths = headers.map((header, index) =>
        Math.max(header.length, ...rows.map((row) => row[index].length))
    );

    const formatRow = (columns) =>
        columns.map((column, index) => column.padEnd(widths[index], " ")).join(" | ");

    const separator = widths.map((width) => "-".repeat(width)).join("-|-");

    console.log("\n=== Donate ramp summary ===");
    console.log(formatRow(headers));
    console.log(separator);
    rows.forEach((row) => {
        console.log(formatRow(row));
    });
}

async function main() {
    if (!LEVELS.length) {
        throw new Error("No valid RAMP_LEVELS provided.");
    }

    console.log(`Running donate ramp test for levels: ${LEVELS.join(", ")}`);

    const results = [];

    for (const level of LEVELS) {
        console.log(`\n===== Level ${level} users =====`);
        const result = await runLevel(level);
        results.push(result);
    }

    printSummaryTable(results);
}

main().catch((error) => {
    console.error("Donate ramp test crashed:", error);
    process.exit(1);
});
