package pl.kuba6000.gtnhextranerf.recipes.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

/** Immutable crafting description shared by target matching and native construction. */
public final class CraftingDefinition {

    final CraftingRecipeBuilder.Kind kind;
    final List<String> pattern;
    final Map<Character, CraftingIngredient> ingredients;
    final List<CraftingIngredient> inputs;
    private final ItemStack output;
    final boolean removable;
    final boolean mirrored;
    final CraftingRecipeType matchType;

    CraftingDefinition(CraftingRecipeBuilder builder) {
        kind = builder.kind;
        pattern = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(builder.pattern)));
        ingredients = Collections.unmodifiableMap(new LinkedHashMap<>(builder.ingredients));
        inputs = Collections.unmodifiableList(new ArrayList<>(builder.inputs));
        output = builder.output.copy();
        removable = builder.removable;
        mirrored = builder.mirrored;
        matchType = builder.matchType;
    }

    public CraftingRecipeBuilder toBuilder() {
        return new CraftingRecipeBuilder(this);
    }

    ItemStack output() {
        return output.copy();
    }

    public ItemStack[] probe() {
        CraftingIngredient[] grid = grid();
        ItemStack[] result = new ItemStack[9];
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] != null) result[i] = grid[i].probe();
        }
        return result;
    }

    CraftingIngredient[] grid() {
        CraftingIngredient[] grid = new CraftingIngredient[9];
        if (kind.isShapeless()) {
            for (int i = 0; i < inputs.size(); i++) grid[i] = inputs.get(i);
        } else {
            for (int y = 0; y < pattern.size(); y++) {
                for (int x = 0; x < pattern.get(y)
                    .length(); x++) {
                    grid[y * 3 + x] = ingredients.get(
                        pattern.get(y)
                            .charAt(x));
                }
            }
        }
        return grid;
    }

    CraftingTarget target() {
        CraftingTarget target = CraftingTarget.matching(probe(), output);
        return matchType == null ? target : target.ofType(matchType);
    }

    CraftingRecipe create() {
        return CraftingRecipeFactory.create(this);
    }
}
