package pl.kuba6000.gtnhextranerf.loot;

import java.util.Objects;

/** One unsplit, fixed-quantity reward; no Minecraft stack or world reference. */
public final class LootReward {

    private final ItemKey item;
    private final int amount;
    private final String nbt;
    private final int maxStackSize;

    public LootReward(ItemKey item, int amount) {
        this(item, amount, "", 64);
    }

    /** NBT is an immutable serialized compound; registry adapters must validate it before compilation. */
    public LootReward(ItemKey item, int amount, String nbt, int maxStackSize) {
        this.item = Objects.requireNonNull(item, "Reward item");
        if (amount <= 0) throw new IllegalArgumentException("Reward amount must be positive");
        if (maxStackSize <= 0) throw new IllegalArgumentException("Stack size must be positive");
        this.amount = amount;
        this.nbt = Objects.requireNonNull(nbt, "Reward NBT");
        this.maxStackSize = maxStackSize;
    }

    public ItemKey item() {
        return item;
    }

    public int amount() {
        return amount;
    }

    public String nbt() {
        return nbt;
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public LootReward withAmount(int amount) {
        return new LootReward(item, amount, nbt, maxStackSize);
    }

    public int stackCount() {
        return 1 + (amount - 1) / maxStackSize;
    }
}
