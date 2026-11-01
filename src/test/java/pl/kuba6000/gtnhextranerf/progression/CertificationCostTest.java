package pl.kuba6000.gtnhextranerf.progression;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class CertificationCostTest {

    @Test
    void consumesAnExactPaymentAcrossSlotsAndLeavesOtherMaterialsAlone() {
        Item item = new Item();
        ItemStack[] inventory = { new ItemStack(item, 3, 2), new ItemStack(item, 64, 3), new ItemStack(item, 4, 2),
            null, new ItemStack(item, 5, 2) };
        assertTrue(CertificationCost.consume(inventory, new ItemStack(item, 1, 2), 9));
        assertNull(inventory[0]);
        assertEquals(64, inventory[1].stackSize);
        assertNull(inventory[2]);
        assertEquals(3, inventory[4].stackSize);
    }

    @Test
    void insufficientOrWrongItemsNeverCauseAPartialPayment() {
        Item requested = new Item();
        ItemStack[] inventory = { new ItemStack(requested, 4, 2), new ItemStack(requested, 64, 3),
            new ItemStack(new Item(), 64, 2), new ItemStack(requested, 3, 2) };
        assertFalse(CertificationCost.consume(inventory, new ItemStack(requested, 1, 2), 8));
        assertEquals(4, inventory[0].stackSize);
        assertEquals(64, inventory[1].stackSize);
        assertEquals(64, inventory[2].stackSize);
        assertEquals(3, inventory[3].stackSize);
    }
}
