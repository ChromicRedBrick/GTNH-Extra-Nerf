package pl.kuba6000.gtnhextranerf.fabrication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import pl.kuba6000.gtnhextranerf.components.ComponentType;

/** Catalog of the UFO category item and its unavailable ingredients; registers no machine recipes. */
public final class UfoAssemblyRegistry {

    private final FabricationItem assembly = new FabricationItem("ufo_assembly");
    private final Map<ComponentType, FabricationItem> ingredients = new EnumMap<>(ComponentType.class);
    private final Collection<FabricationItem> items;

    public UfoAssemblyRegistry() {
        List<FabricationItem> entries = new ArrayList<>();
        entries.add(assembly);
        for (FabricationIngredient definition : FabricationIngredient.values()) {
            FabricationItem item = new FabricationItem(definition.id());
            if (ingredients.put(definition.component(), item) != null)
                throw new IllegalStateException("Duplicate fabrication ingredient for " + definition.component());
            entries.add(item);
        }
        if (ingredients.size() != ComponentType.values().length)
            throw new IllegalStateException("Every component requires a fabrication ingredient");
        items = Collections.unmodifiableList(entries);
    }

    public ItemStack assembly() {
        return new ItemStack(assembly);
    }

    public ItemStack ingredient(ComponentType type) {
        return new ItemStack(ingredients.get(Objects.requireNonNull(type, "Component type")));
    }

    public boolean isAssembly(ItemStack stack) {
        return stack != null && stack.getItem() == assembly;
    }

    public Optional<ComponentType> componentForIngredient(ItemStack stack) {
        if (stack != null) {
            for (Map.Entry<ComponentType, FabricationItem> entry : ingredients.entrySet()) {
                if (stack.getItem() == entry.getValue()) return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public Collection<FabricationItem> items() {
        return items;
    }
}
