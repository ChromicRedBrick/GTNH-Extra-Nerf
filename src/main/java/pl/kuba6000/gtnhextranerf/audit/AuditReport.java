package pl.kuba6000.gtnhextranerf.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Stable evidence retained after session closure; no references to mutable session state. */
public final class AuditReport {

    private final List<AuditResult> results;

    AuditReport(List<AuditResult> results) {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    public List<AuditResult> results() {
        return results;
    }

    public AuditResult result(String id) {
        return results.stream()
            .filter(
                result -> result.id()
                    .equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown audit definition: " + id));
    }

    public boolean accepted() {
        return results.stream()
            .allMatch(AuditResult::accepted);
    }

    public AuditCounts counts() {
        AuditCounts total = AuditCounts.ZERO;
        for (AuditResult result : results) total = total.plus(result.counts());
        return total;
    }

    public String summary() {
        long nanos = 0;
        int failures = 0;
        int skipped = 0;
        int missing = 0;
        for (AuditResult result : results) {
            nanos += result.exclusiveNanos();
            if (result.status() == AuditStatus.FAILED) failures++;
            if (result.status() == AuditStatus.SKIPPED) skipped++;
            if (result.status() == AuditStatus.NOT_RUN) missing++;
        }
        return String.format(
            Locale.ROOT,
            "[Extra Nerf] Audit %s: observed %s; failed scopes=%d, skipped scopes=%d, not run=%d; exclusive time=%.3f ms",
            accepted() ? "PASSED" : "FAILED",
            counts(),
            failures,
            skipped,
            missing,
            nanos / 1_000_000.0);
    }

    public String failureReport() {
        StringBuilder text = new StringBuilder("GTNH Extra Nerf integrity check failed:");
        for (AuditResult result : results) {
            if (result.accepted()) continue;
            text.append("\n- ")
                .append(result.id())
                .append(": ")
                .append(result.status())
                .append("; expected ")
                .append(result.expectation())
                .append("; observed ")
                .append(result.counts())
                .append("; successful runs=")
                .append(result.successfulRuns())
                .append(", failed runs=")
                .append(result.failedRuns())
                .append(", skipped runs=")
                .append(result.skippedRuns());
            for (String diagnostic : result.diagnostics()) text.append("\n  ")
                .append(diagnostic);
        }
        return text.toString();
    }
}
