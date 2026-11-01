package pl.kuba6000.gtnhextranerf.mixins.support;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;

/** Recognizes the Werkstoff hand-crafted ring before replacing its rod ingredient. */
public final class RingRecipeTransformer {

    private RingRecipeTransformer() {}

    public static boolean replaceStick(ItemStack output, Object[] recipe) {
        if (recipe == null || recipe.length != 4
            || !"h ".equals(recipe[0])
            || !"fX".equals(recipe[1])
            || !Character.valueOf('X')
                .equals(recipe[2])
            || !(recipe[3] instanceof ItemStack stick)) return false;

        ItemData result = GTOreDictUnificator.getAssociation(output);
        if (result == null || result.mPrefix != OrePrefixes.ring
            || result.mMaterial == null
            || !OrePrefixes.stick.contains(stick)) return false;

        ItemStack longStick = GTOreDictUnificator.get(OrePrefixes.stickLong, result.mMaterial.mMaterial, 1);
        if (longStick == null) return false;

        recipe[3] = longStick;
        return true;
    }
}
