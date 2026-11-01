package pl.kuba6000.gtnhextranerf.mixins.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class RingRecipeTransformerTest {

    @Test
    void leavesUnrelatedCraftingPatternsUntouched() {
        ItemStack input = new ItemStack(new Item());
        Object[] recipe = { "X", 'X', input };

        assertFalse(RingRecipeTransformer.replaceStick(new ItemStack(new Item()), recipe));
        assertSame(input, recipe[2]);
    }
}
