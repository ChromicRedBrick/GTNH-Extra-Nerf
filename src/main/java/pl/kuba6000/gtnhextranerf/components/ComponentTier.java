package pl.kuba6000.gtnhextranerf.components;

import java.util.function.Function;

import net.minecraft.item.ItemStack;

/** Cosmetic component tiers; these do not participate in GregTech voltage or machine progression. */
public enum ComponentTier {

    UQV("uqv", "UQV", ComponentType::uevAppearance);

    private final String id;
    private final String abbreviation;
    private final Function<ComponentType, ItemStack> appearance;

    ComponentTier(String id, String abbreviation, Function<ComponentType, ItemStack> appearance) {
        this.id = id;
        this.abbreviation = abbreviation;
        this.appearance = appearance;
    }

    public String id() {
        return id;
    }

    public String abbreviation() {
        return abbreviation;
    }

    public ItemStack appearance(ComponentType type) {
        return appearance.apply(type);
    }
}
