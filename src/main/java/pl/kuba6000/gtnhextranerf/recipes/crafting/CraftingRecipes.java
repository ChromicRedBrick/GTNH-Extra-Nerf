package pl.kuba6000.gtnhextranerf.recipes.crafting;

import net.minecraft.item.ItemStack;

import pl.kuba6000.gtnhextranerf.recipes.RecipeEditor;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

public final class CraftingRecipes {

    private final RecipeEditor<CraftingRecipe> editor;

    public CraftingRecipes(RecipeEditor<CraftingRecipe> editor) {
        this.editor = editor;
    }

    public void replaceOne(CraftingDefinition original, CraftingDefinition replacement) {
        CraftingRecipe recipe = replacement.create();
        if (!replacement.target()
            .test(recipe)) {
            throw new RecipeValidationException("Crafting replacement does not match its expected signature");
        }
        editor.replaceOne(original.target(), recipe);
    }

    public void outputAmount(CraftingDefinition definition, int amount) {
        CraftingRecipe original = editor.requireExactlyOne(definition.target());
        editor.replaceOne(recipe -> recipe == original, original.withOutputAmount(amount));
    }

    public void removeOne(CraftingDefinition definition) {
        editor.removeExactly(definition.target(), 1);
    }

    public void addOne(CraftingDefinition definition) {
        editor.addOne(definition.create(), definition.target());
    }

    public void expectOutputCount(ItemStack output, int count) {
        try {
            editor.expectExactly(
                recipe -> recipe.output() != null && recipe.output()
                    .isItemEqual(output),
                count);
        } catch (RecipeValidationException failure) {
            throw new RecipeValidationException(
                "Unexpected production recipes for " + output + ": " + failure.getMessage());
        }
    }
}
