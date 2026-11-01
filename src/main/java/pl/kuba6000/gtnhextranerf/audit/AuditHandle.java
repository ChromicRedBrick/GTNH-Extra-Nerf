package pl.kuba6000.gtnhextranerf.audit;

/** A single execution, owned by its opening thread. Closing without an outcome is a failure. */
public final class AuditHandle implements AutoCloseable {

    final AuditSession session;
    final AuditDefinition definition;
    final Thread owner;
    final long started;
    long nestedNanos;
    int modified;
    int added;
    int removed;
    AuditStatus outcome;
    String reason;
    boolean closed;

    AuditHandle(AuditSession session, AuditDefinition definition, long started) {
        this.session = session;
        this.definition = definition;
        this.started = started;
        owner = Thread.currentThread();
    }

    public void record(AuditCounts changes) {
        java.util.Objects.requireNonNull(changes, "changes");
        session.record(this, changes.modified(), changes.added(), changes.removed());
    }

    public void recipeModified() {
        session.record(this, 1, 0, 0);
    }

    public void recipeAdded() {
        session.record(this, 0, 1, 0);
    }

    public void recipeRemoved() {
        session.record(this, 0, 0, 1);
    }

    public void skip(String reason) {
        if (reason == null || reason.trim()
            .isEmpty()) throw new IllegalArgumentException("A skipped execution needs a reason");
        session.outcome(this, AuditStatus.SKIPPED, reason);
    }

    public void success() {
        session.outcome(this, AuditStatus.SUCCESS, null);
    }

    public void failure(Throwable failure) {
        java.util.Objects.requireNonNull(failure, "failure");
        session.outcome(
            this,
            AuditStatus.FAILED,
            failure.getClass()
                .getName() + ": "
                + failure.getMessage());
    }

    @Override
    public void close() {
        session.close(this);
    }
}
