package pl.kuba6000.gtnhextranerf.recipes.machine;

import java.util.ArrayList;

import gregtech.api.recipe.RecipeCategory;
import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

/** GTRecipe.copy() drops metadata/category and shares chance arrays in the pinned GT version. */
final class MachineRecipeCopies {

    private MachineRecipeCopies() {}

    static GTRecipe copy(GTRecipe original) {
        return copy(original, original.getRecipeCategory());
    }

    static GTRecipe copy(GTRecipe original, RecipeCategory category) {
        if (original.getClass() != GTRecipe.class && original.getClass() != GTRecipe.GTRecipe_WithAlt.class) {
            throw new RecipeValidationException(
                "Unsupported recipe subtype: " + original.getClass()
                    .getName());
        }
        GTRecipe detached = original.copy();
        GTRecipeBuilder builder = GTRecipeBuilder.builder()
            .itemInputsUnified(detached.mInputs, copy(original.mInputChances))
            .itemOutputs(detached.mOutputs, copy(original.mOutputChances))
            .fluidInputs(detached.mFluidInputs, copy(original.mFluidInputChances))
            .fluidOutputs(detached.mFluidOutputs, copy(original.mFluidOutputChances))
            .duration(original.mDuration)
            .eut(original.mEUt)
            .special(original.mSpecialItems)
            .specialValue(original.mSpecialValue)
            .recipeCategory(category);
        original.getMetadataStorage()
            .getEntries()
            .forEach(entry -> copyMetadata(builder, entry.getKey(), entry.getValue()));
        if (!original.mEnabled) builder.disabled();
        if (original.mHidden) builder.hidden();
        if (original.mFakeRecipe) builder.fake();
        if (!original.mCanBeBuffered) builder.noBuffer();
        if (original.mNeedsEmptyOutput) builder.needsEmptyOutput();
        if (original.isNBTSensitive) builder.nbtSensitive();
        if (original.getNeiDesc() != null) builder.setNEIDesc(
            original.getNeiDesc()
                .clone());

        GTRecipe result;
        if (detached instanceof GTRecipe.GTRecipe_WithAlt) {
            GTRecipe.GTRecipe_WithAlt source = (GTRecipe.GTRecipe_WithAlt) detached;
            GTRecipe.GTRecipe_WithAlt target = builder.forceOreDictInput()
                .buildWithAlt()
                .orElseThrow(() -> new RecipeValidationException("Could not copy recipe with alternative inputs"));
            target.mInputs = source.mInputs;
            target.mOreDictAlt = source.mOreDictAlt;
            target.mOreDictIds = source.mOreDictIds;
            target.mAltFluidInputs = source.mAltFluidInputs;
            result = target;
        } else {
            result = builder.build()
                .orElseThrow(() -> new RecipeValidationException("Could not copy recipe"));
        }
        // Builder decoration may derive the special value from metadata; preserve the effective upstream value.
        result.mSpecialValue = original.mSpecialValue;
        result.owners = original.owners == null ? null : new ArrayList<>(original.owners);
        result.stackTraces = original.stackTraces == null ? null : new ArrayList<>(original.stackTraces);
        return result;
    }

    private static int[] copy(int[] values) {
        return values == null ? null : values.clone();
    }

    private static <T> void copyMetadata(GTRecipeBuilder builder, RecipeMetadataKey<T> key, Object value) {
        builder.metadata(key, key.cast(value));
    }
}
