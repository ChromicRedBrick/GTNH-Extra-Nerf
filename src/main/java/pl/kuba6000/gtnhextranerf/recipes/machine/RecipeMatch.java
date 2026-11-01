package pl.kuba6000.gtnhextranerf.recipes.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.util.GTRecipe;

/** Immutable recipe signature. Omitted fields are unrestricted; declared fields match exactly. */
public final class RecipeMatch implements Predicate<GTRecipe> {

    private final List<Predicate<GTRecipe>> criteria;

    private RecipeMatch(List<Predicate<GTRecipe>> criteria) {
        this.criteria = new ArrayList<>(criteria);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RecipeMatch anyOf(RecipeMatch... alternatives) {
        if (alternatives.length == 0) throw new IllegalArgumentException("At least one recipe alternative is required");
        RecipeMatch[] choices = alternatives.clone();
        for (RecipeMatch choice : choices) Objects.requireNonNull(choice, "Recipe alternative");
        return new RecipeMatch(Collections.singletonList(recipe -> {
            for (RecipeMatch choice : choices) if (choice.test(recipe)) return true;
            return false;
        }));
    }

    @Override
    public boolean test(GTRecipe recipe) {
        if (recipe == null) return false;
        for (Predicate<GTRecipe> criterion : criteria) {
            if (!criterion.test(recipe)) return false;
        }
        return true;
    }

    private enum Field {
        ITEM_INPUTS,
        ITEM_OUTPUTS,
        FLUID_INPUTS,
        FLUID_OUTPUTS,
        DURATION,
        EUT,
        SPECIAL_VALUE,
        OUTPUT_CHANCES
    }

    public static final class Builder {

        private final EnumMap<Field, Predicate<GTRecipe>> criteria = new EnumMap<>(Field.class);
        private ItemStack[] itemInputs = new ItemStack[0];
        private final Map<String, Integer> oreInputs = new LinkedHashMap<>();

        private Builder() {}

        public Builder itemInputs(ItemStack... inputs) {
            itemInputs = MachineRecipeSelector.copyItems(inputs);
            updateItemInputs();
            return this;
        }

        /** Requires an additional OreDict slot. Repeating a name replaces its amount. */
        public Builder oreDictInput(String name, int amount) {
            if (name == null || name.trim()
                .isEmpty() || amount <= 0) {
                throw new IllegalArgumentException("An OreDict input requires a name and positive amount");
            }
            oreInputs.put(name, amount);
            updateItemInputs();
            return this;
        }

        private void updateItemInputs() {
            criteria.put(
                Field.ITEM_INPUTS,
                oreInputs.isEmpty() ? MachineRecipeSelector.itemInputsExactly(itemInputs)
                    : MachineRecipeSelector.itemInputsExactlyWithOreDict(itemInputs, oreInputs));
        }

        public Builder itemOutputs(ItemStack... outputs) {
            criteria.put(Field.ITEM_OUTPUTS, MachineRecipeSelector.itemOutputsExactly(outputs));
            return this;
        }

        public Builder fluidInputs(FluidStack... inputs) {
            criteria.put(Field.FLUID_INPUTS, MachineRecipeSelector.fluidInputsExactly(inputs));
            return this;
        }

        /** Each argument is one complete alternative set of fluid inputs. */
        public Builder fluidInputsAnyOf(FluidStack[]... alternatives) {
            if (alternatives.length == 0)
                throw new IllegalArgumentException("At least one fluid alternative is required");
            List<Predicate<GTRecipe>> choices = new ArrayList<>();
            for (FluidStack[] alternative : alternatives)
                choices.add(MachineRecipeSelector.fluidInputsExactly(alternative));
            criteria.put(Field.FLUID_INPUTS, recipe -> {
                for (Predicate<GTRecipe> choice : choices) if (choice.test(recipe)) return true;
                return false;
            });
            return this;
        }

        public Builder fluidInputCount(int count) {
            if (count < 0) throw new IllegalArgumentException("Fluid input count cannot be negative");
            criteria
                .put(Field.FLUID_INPUTS, recipe -> recipe.mFluidInputs != null && recipe.mFluidInputs.length == count);
            return this;
        }

        public Builder fluidOutputs(FluidStack... outputs) {
            criteria.put(Field.FLUID_OUTPUTS, MachineRecipeSelector.fluidOutputsExactly(outputs));
            return this;
        }

        public Builder outputChances(int... chances) {
            int[] expected = chances.clone();
            criteria.put(Field.OUTPUT_CHANCES, recipe -> {
                if (recipe.mOutputs == null || recipe.mOutputs.length != expected.length) return false;
                for (int i = 0; i < expected.length; i++) {
                    // GT normalizes guaranteed output arrays to null; missing slots also mean 100%.
                    if (recipe.getOutputChance(i) != expected[i]) return false;
                }
                return true;
            });
            return this;
        }

        public Builder specialValue(int value) {
            criteria.put(Field.SPECIAL_VALUE, recipe -> recipe.mSpecialValue == value);
            return this;
        }

        public Builder duration(int ticks) {
            criteria.put(Field.DURATION, recipe -> recipe.mDuration == ticks);
            return this;
        }

        public Builder eut(long eut) {
            criteria.put(Field.EUT, recipe -> recipe.mEUt == eut);
            return this;
        }

        public RecipeMatch build() {
            if (criteria.isEmpty()) throw new IllegalStateException("A recipe match must declare at least one field");
            return new RecipeMatch(new ArrayList<>(criteria.values()));
        }
    }
}
