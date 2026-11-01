package pl.kuba6000.gtnhextranerf.loot;

import java.util.Objects;

/** Registry identity and exact metadata; global limits ignore the separate reward NBT payload. */
public final class ItemKey {

    private final String registryName;
    private final int metadata;

    private ItemKey(String registryName, int metadata) {
        Objects.requireNonNull(registryName, "Registry name");
        int separator = registryName.indexOf(':');
        if (separator <= 0 || separator == registryName.length() - 1 || registryName.indexOf(':', separator + 1) >= 0)
            throw new IllegalArgumentException("Expected modid:item registry name: " + registryName);
        if (metadata < 0 || metadata == 32767) throw new IllegalArgumentException("Expected exact item metadata");
        this.registryName = registryName;
        this.metadata = metadata;
    }

    public static ItemKey of(String registryName, int metadata) {
        return new ItemKey(registryName, metadata);
    }

    public String registryName() {
        return registryName;
    }

    public int metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ItemKey)) return false;
        ItemKey key = (ItemKey) other;
        return metadata == key.metadata && registryName.equals(key.registryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryName, metadata);
    }

    @Override
    public String toString() {
        return registryName + ":" + metadata;
    }
}
