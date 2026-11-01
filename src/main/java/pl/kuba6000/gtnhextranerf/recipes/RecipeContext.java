package pl.kuba6000.gtnhextranerf.recipes;

import java.util.IdentityHashMap;
import java.util.Map;

import gregtech.api.recipe.RecipeMap;
import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingRecipeStore;
import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingRecipes;
import pl.kuba6000.gtnhextranerf.recipes.machine.MachineRecipeStore;
import pl.kuba6000.gtnhextranerf.recipes.machine.MachineRecipes;

public final class RecipeContext {

    private final RecipeTransaction transaction;
    private final Map<RecipeMap<?>, MachineRecipes> machines = new IdentityHashMap<>();
    private CraftingRecipes crafting;

    RecipeContext(RecipeTransaction transaction) {
        this.transaction = transaction;
    }

    public <T> RecipeEditor<T> edit(RecipeStore<T> store) {
        return transaction.edit(store);
    }

    public MachineRecipes machine(RecipeMap<?> map) {
        return machines.computeIfAbsent(map, key -> new MachineRecipes(key, edit(new MachineRecipeStore(key))));
    }

    public CraftingRecipes crafting() {
        if (crafting == null) crafting = new CraftingRecipes(edit(new CraftingRecipeStore()));
        return crafting;
    }
}
