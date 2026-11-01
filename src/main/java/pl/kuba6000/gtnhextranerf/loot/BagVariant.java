package pl.kuba6000.gtnhextranerf.loot;

import java.util.Objects;

/** An already resolved bag/Fortune variant. This type does not merge native trash tables. */
public final class BagVariant {

    private final int groupId;
    private final int fortune;

    private BagVariant(int groupId, int fortune) {
        if (groupId < 0 || groupId > 32767) throw new IllegalArgumentException("Invalid bag group ID");
        if (fortune < 0 || fortune > 3) throw new IllegalArgumentException("Fortune must be between 0 and 3");
        this.groupId = groupId;
        this.fortune = fortune;
    }

    public static BagVariant of(int groupId, int fortune) {
        return new BagVariant(groupId, fortune);
    }

    public int groupId() {
        return groupId;
    }

    public int fortune() {
        return fortune;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BagVariant)) return false;
        BagVariant bag = (BagVariant) other;
        return groupId == bag.groupId && fortune == bag.fortune;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, fortune);
    }

    @Override
    public String toString() {
        return "bag " + groupId + ", Fortune " + fortune;
    }
}
