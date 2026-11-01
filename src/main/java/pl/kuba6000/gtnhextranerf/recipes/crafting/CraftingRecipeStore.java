package pl.kuba6000.gtnhextranerf.recipes.crafting;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.crafting.CraftingManager;

import pl.kuba6000.gtnhextranerf.mixins.early.MixinCraftingManagerAccessor;
import pl.kuba6000.gtnhextranerf.recipes.RecipeStore;

public final class CraftingRecipeStore implements RecipeStore<CraftingRecipe> {

    private final Map<Object, CraftingRecipe> handles = new IdentityHashMap<>();

    @Override
    public String name() {
        return "minecraft crafting";
    }

    @Override
    public List<CraftingRecipe> snapshot() {
        List<CraftingRecipe> result = new ArrayList<>();
        for (Object recipe : recipeList()) {
            CraftingRecipe known = handles.get(recipe);
            if (known == null || !known.hasCurrentOutput()) {
                known = new CraftingRecipe(recipe);
                handles.put(recipe, known);
            }
            result.add(known);
        }
        return result;
    }

    @Override
    public void replaceAll(List<CraftingRecipe> recipes) {
        List<Object> replacement = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            if (!recipe.hasCurrentOutput()) recipe.publishOutput();
            handles.put(recipe.handle(), recipe);
            replacement.add(recipe.handle());
        }
        List<Object> live = recipeList();
        live.clear();
        live.addAll(replacement);
        if (!live.equals(replacement)) throw new IllegalStateException("Could not publish crafting recipes");
    }

    private static List<Object> recipeList() {
        return ((MixinCraftingManagerAccessor) CraftingManager.getInstance()).gtnhextranerf$getRecipes();
    }
}
