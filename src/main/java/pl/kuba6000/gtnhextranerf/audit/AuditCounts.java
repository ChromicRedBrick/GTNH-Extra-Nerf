package pl.kuba6000.gtnhextranerf.audit;

/** Immutable operation counts; counts are observations, not a proof of recipe correctness. */
public final class AuditCounts {

    public static final AuditCounts ZERO = new AuditCounts(0, 0, 0);
    private final int modified;
    private final int added;
    private final int removed;

    public AuditCounts(int modified, int added, int removed) {
        if (modified < 0 || added < 0 || removed < 0)
            throw new IllegalArgumentException("Audit counts cannot be negative");
        this.modified = modified;
        this.added = added;
        this.removed = removed;
    }

    public int modified() {
        return modified;
    }

    public int added() {
        return added;
    }

    public int removed() {
        return removed;
    }

    public AuditCounts plus(AuditCounts other) {
        return new AuditCounts(
            Math.addExact(modified, other.modified),
            Math.addExact(added, other.added),
            Math.addExact(removed, other.removed));
    }

    @Override
    public String toString() {
        return "modified=" + modified + ", added=" + added + ", removed=" + removed;
    }
}
