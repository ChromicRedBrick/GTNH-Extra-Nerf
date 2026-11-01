package pl.kuba6000.gtnhextranerf.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import net.minecraft.item.ItemStack;

/** Public catalog used by recipe modules and client integration. */
public final class ComponentRegistry {

    private final Map<ComponentTier, Map<ComponentType, ComponentItem>> tiers = new EnumMap<>(ComponentTier.class);
    private final Collection<ComponentItem> items;

    public ComponentRegistry() {
        this(ComponentTier::appearance);
    }

    /** The appearance provider is lazy: constructing the catalog never reads GregTech's item registry. */
    public ComponentRegistry(BiFunction<ComponentTier, ComponentType, ItemStack> appearances) {
        Objects.requireNonNull(appearances, "Component appearances");
        List<ComponentItem> entries = new ArrayList<>();
        for (ComponentTier tier : ComponentTier.values()) {
            Map<ComponentType, ComponentItem> components = new EnumMap<>(ComponentType.class);
            for (ComponentType type : ComponentType.values()) {
                ComponentItem item = new ComponentItem(tier, type, () -> appearances.apply(tier, type));
                components.put(type, item);
                entries.add(item);
            }
            tiers.put(tier, components);
        }
        items = Collections.unmodifiableList(entries);
    }

    public ItemStack stack(ComponentTier tier, ComponentType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Component amount must not be negative");
        return new ItemStack(
            tiers.get(Objects.requireNonNull(tier, "Component tier"))
                .get(Objects.requireNonNull(type, "Component type")),
            amount);
    }

    public Collection<ComponentItem> items() {
        return items;
    }
}
