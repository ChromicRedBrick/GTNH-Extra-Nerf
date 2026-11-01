package pl.kuba6000.gtnhextranerf.recipes.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipe;
import pl.kuba6000.gtnhextranerf.recipes.RecipeStore;

/** Writes only the changed recipes through the backend, which rebuilds machine lookup indexes. */
public final class MachineRecipeStore implements RecipeStore<GTRecipe> {

    private final RecipeMap<?> map;

    public MachineRecipeStore(RecipeMap<?> map) {
        this.map = map;
    }

    @Override
    public String name() {
        return map.unlocalizedName;
    }

    @Override
    public List<GTRecipe> snapshot() {
        return new ArrayList<>(map.getAllRecipes());
    }

    @Override
    public void replaceAll(List<GTRecipe> recipes) {
        List<GTRecipe> current = snapshot();
        Set<GTRecipe> retained = identities(recipes);
        Set<GTRecipe> existing = identities(current);
        List<GTRecipe> removed = new ArrayList<>();
        for (GTRecipe recipe : current) if (!retained.contains(recipe)) removed.add(recipe);
        if (!removed.isEmpty()) map.getBackend()
            .removeRecipes(removed);
        for (GTRecipe recipe : recipes) {
            if (!existing.contains(recipe) && map.getBackend()
                .compileRecipe(recipe) != recipe) {
                throw new IllegalStateException(name() + ": backend rejected a recipe");
            }
        }
        List<GTRecipe> actual = snapshot();
        if (actual.size() != recipes.size() || !identities(actual).equals(retained)) {
            throw new IllegalStateException(name() + ": backend did not publish the planned recipes");
        }
    }

    private static Set<GTRecipe> identities(List<GTRecipe> recipes) {
        Set<GTRecipe> result = Collections.newSetFromMap(new IdentityHashMap<>());
        result.addAll(recipes);
        return result;
    }
}
