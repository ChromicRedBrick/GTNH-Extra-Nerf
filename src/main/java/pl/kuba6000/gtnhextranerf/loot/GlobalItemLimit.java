package pl.kuba6000.gtnhextranerf.loot;

import java.util.Objects;

/** One stable allowance, shared across every bag variant for the exact registry item and metadata. */
public final class GlobalItemLimit {

    private final String id;
    private final ItemKey item;
    private final long maxUnits;

    public GlobalItemLimit(String id, ItemKey item, long maxUnits) {
        this.id = Objects.requireNonNull(id, "Limit ID");
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Expected stable namespaced limit ID");
        this.item = Objects.requireNonNull(item, "Limited item");
        if (maxUnits <= 0) throw new IllegalArgumentException("Unit limit must be positive");
        this.maxUnits = maxUnits;
    }

    public String id() {
        return id;
    }

    public ItemKey item() {
        return item;
    }

    public long maxUnits() {
        return maxUnits;
    }
}
