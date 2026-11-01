package pl.kuba6000.gtnhextranerf.recipes.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import gregtech.api.recipe.RecipeCategory;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackendProperties;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import pl.kuba6000.gtnhextranerf.recipes.RecipeEditor;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

public final class MachineRecipes {

    private final RecipeMap<?> map;
    private final RecipeEditor<GTRecipe> editor;

    public MachineRecipes(RecipeMap<?> map, RecipeEditor<GTRecipe> editor) {
        this.map = map;
        this.editor = editor;
    }

    public void modifyOne(Predicate<GTRecipe> signature, RecipeChange mutation) {
        modifyKnownOne(editor.requireExactlyOne(signature), mutation);
    }

    /** For callers that already selected an original from this map. Changes remain staged until commit. */
    public void modifyKnownOne(GTRecipe original, RecipeChange mutation) {
        GTRecipe replacement = MachineRecipeCopies.copy(original);
        mutation.accept(replacement);
        editor.replaceKnownOne(original, replacement);
    }

    /** Adds a variant while retaining upstream metadata, chances and alternative inputs. */
    public void addCopyOne(GTRecipe original, RecipeChange mutation, Predicate<GTRecipe> signature) {
        GTRecipe variant = copyForMap(original);
        mutation.accept(variant);
        editor.addOne(variant, signature);
    }

    /** Copies a recipe into this map, retaining its data and assigning this map's category when moving it. */
    public void addCopyOne(GTRecipe original, Predicate<GTRecipe> signature) {
        editor.addOne(copyForMap(original), signature);
    }

    private GTRecipe copyForMap(GTRecipe original) {
        RecipeCategory category = original.getRecipeCategory();
        if (category != null && category.recipeMap != map) category = map.getDefaultRecipeCategory();
        return MachineRecipeCopies.copy(original, category);
    }

    public void removeExactly(Predicate<GTRecipe> signature, int expectedCount) {
        editor.removeExactly(signature, expectedCount);
    }

    public GTRecipe requireExactlyOne(Predicate<GTRecipe> signature) {
        return editor.requireExactlyOne(signature);
    }

    public void retainExactly(Predicate<GTRecipe> scope, Predicate<GTRecipe> retained, int expectedRetained,
        int expectedRemoved) {
        editor.retainExactly(scope, retained, expectedRetained, expectedRemoved);
    }

    /** Uses upstream emitters/transformers without registering anything during planning. */
    public void addOne(GTRecipeBuilder builder, Predicate<GTRecipe> signature) {
        RecipeMapBackendProperties properties = map.getBackend()
            .getProperties();
        GTRecipeBuilder prepared = builder.copy();
        properties.transformBuilder(prepared);
        List<GTRecipe> emitted = new ArrayList<>();
        for (GTRecipe recipe : properties.recipeEmitter.apply(prepared)) emitted.add(recipe);
        if (emitted.size() != 1) {
            throw new RecipeValidationException(
                map.unlocalizedName + ": expected one emitted recipe, observed " + emitted.size());
        }
        GTRecipe recipe = emitted.get(0);
        if (recipe.mInputs.length < properties.minItemInputs
            || recipe.mFluidInputs.length < properties.minFluidInputs) {
            throw new RecipeValidationException(map.unlocalizedName + ": addition has insufficient inputs");
        }
        properties.transformRecipe(recipe);
        if (recipe.getRecipeCategory() != null && recipe.getRecipeCategory().recipeMap != map) {
            throw new RecipeValidationException(map.unlocalizedName + ": addition belongs to another map");
        }
        if (prepared.isCheckForCollision() && GTRecipeBuilder.ENABLE_COLLISION_CHECK) {
            // Match against the staged recipes: a removed original must not collide with its replacement.
            editor.expectExactly(
                existing -> existing.mEnabled && !existing.mFakeRecipe
                    && (!properties.specialSlotSensitive || existing.mSpecialItems == null)
                    && existing.isRecipeInputEqual(false, true, recipe.mFluidInputs, recipe.mInputs),
                0);
        }
        editor.addOne(recipe, signature);
    }
}
