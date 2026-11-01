package pl.kuba6000.gtnhextranerf.mixins.support;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import pl.kuba6000.gtnhextranerf.audit.AuditHandle;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

/** Semantic rules at the native recipe boundary; does not modify caller-owned arrays. */
public final class Ae2RecipeIngredients {

    private Ae2RecipeIngredients() {}

    public static boolean isAe2(ItemStack output) {
        if (output == null || output.getItem() == null) return false;
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(output.getItem());
        return id != null && Mods.AppliedEnergistics2.ID.equals(id.modId);
    }

    public static boolean isTitanium(ItemStack stack) {
        return titanium(GTOreDictUnificator.getAssociation(stack));
    }

    private static boolean titanium(ItemData data) {
        return data != null && data.mPrefix != null
            && data.mMaterial != null
            && data.mMaterial.mMaterial == Materials.Titanium;
    }

    public static ItemStack tpvItem(ItemStack original) {
        ItemStack replacement = GTOreDictUnificator
            .get(GTOreDictUnificator.getAssociation(original).mPrefix, Materials.TPV, original.stackSize);
        if (replacement == null) throw new RecipeValidationException("Missing TPV component for AE2 recipe");
        if (original.hasTagCompound()) replacement.setTagCompound(
            (net.minecraft.nbt.NBTTagCompound) original.getTagCompound()
                .copy());
        return replacement;
    }

    private static String tpvOre(OrePrefixes prefix) {
        String name = prefix.get(Materials.TPV)
            .toString();
        if (GTOreDictUnificator.getFirstOre(name, 1) == null)
            throw new RecipeValidationException("Missing TPV component: " + name);
        return name;
    }

    /** Null means the input is not Titanium; an unavailable corresponding TPV form is an error. */
    public static FluidStack tpvFluid(FluidStack input) {
        if (input == null || input.getFluid() == null) return null;
        Fluid original = input.getFluid();
        Fluid replacement;
        if (original == Materials.Titanium.mStandardMoltenFluid) replacement = Materials.TPV.mStandardMoltenFluid;
        else if (original == Materials.Titanium.mFluid) replacement = Materials.TPV.mFluid;
        else if (original == Materials.Titanium.mGas) replacement = Materials.TPV.mGas;
        else if (original == Materials.Titanium.mPlasma) replacement = Materials.TPV.mPlasma;
        else return null;
        if (replacement == null)
            throw new RecipeValidationException("Missing corresponding TPV fluid for " + original.getName());
        FluidStack result = new FluidStack(replacement, input.amount);
        if (input.tag != null) result.tag = (net.minecraft.nbt.NBTTagCompound) input.tag.copy();
        return result;
    }

    public static Object[] crafting(ItemStack output, Object[] inputs, AuditHandle audit) {
        if (!isAe2(output)) return inputs;
        Object[] replacement = null;
        for (int i = 0; i < inputs.length; i++) {
            Object input = inputs[i];
            Object changed = input;
            if (input instanceof String) {
                OrePrefixes prefix = OrePrefixes.getOrePrefix((String) input);
                if (prefix != null && prefix.get(Materials.Titanium)
                    .toString()
                    .equals(input)) changed = tpvOre(prefix);
            } else if (input instanceof ItemData && titanium((ItemData) input)) {
                changed = tpvOre(((ItemData) input).mPrefix);
            } else if (input instanceof ItemStack && isTitanium((ItemStack) input)) {
                changed = tpvItem((ItemStack) input);
            }
            if (changed != input) {
                if (replacement == null) replacement = inputs.clone();
                replacement[i] = changed;
            }
        }
        if (replacement == null) return inputs;
        audit.recipeModified();
        return replacement;
    }
}
