package pl.kuba6000.gtnhextranerf.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable summary of all executions of one definition. */
public final class AuditResult {

    private final String id;
    private final String expectation;
    private final boolean active;
    private final boolean accepted;
    private final AuditCounts counts;
    private final int successfulRuns;
    private final int failedRuns;
    private final int skippedRuns;
    private final long exclusiveNanos;
    private final List<String> diagnostics;

    AuditResult(AuditDefinition definition, AuditCounts counts, int successes, int failures, int skips, long nanos,
        List<String> diagnostics) {
        id = definition.id();
        active = definition.isActive();
        AuditExpectation contract = active ? definition.expectation() : null;
        expectation = active ? contract.describe() : "inactive";
        this.counts = counts;
        successfulRuns = successes;
        failedRuns = failures;
        skippedRuns = skips;
        exclusiveNanos = nanos;
        this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
        accepted = failures == 0 && (!active || (successes > 0 && skips == 0 && contract.accepts(counts)));
    }

    public String id() {
        return id;
    }

    public String expectation() {
        return expectation;
    }

    public boolean active() {
        return active;
    }

    public boolean accepted() {
        return accepted;
    }

    public AuditCounts counts() {
        return counts;
    }

    public int successfulRuns() {
        return successfulRuns;
    }

    public int failedRuns() {
        return failedRuns;
    }

    public int skippedRuns() {
        return skippedRuns;
    }

    public long exclusiveNanos() {
        return exclusiveNanos;
    }

    public List<String> diagnostics() {
        return diagnostics;
    }

    public AuditStatus status() {
        if (failedRuns > 0) return AuditStatus.FAILED;
        if (successfulRuns + skippedRuns == 0) return active ? AuditStatus.NOT_RUN : AuditStatus.SKIPPED;
        if (skippedRuns > 0) return AuditStatus.SKIPPED;
        return accepted ? AuditStatus.SUCCESS : AuditStatus.FAILED;
    }
}
