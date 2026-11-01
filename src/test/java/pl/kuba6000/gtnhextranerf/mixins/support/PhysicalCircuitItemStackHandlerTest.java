package pl.kuba6000.gtnhextranerf.mixins.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;

class PhysicalCircuitItemStackHandlerTest {

    @Test
    void storesOneRealIntegratedCircuitAndReturnsItOnExtraction() {
        ItemStackHandler inventory = new ItemStackHandler(3);
        Item circuitItem = new Item();
        PhysicalCircuitItemStackHandler handler = handler(inventory, 2, circuitItem);
        ItemStack circuits = new ItemStack(circuitItem, 3, 7);

        ItemStack remainder = handler.insertItem(0, circuits, false);

        assertEquals(1, inventory.getStackInSlot(2).stackSize);
        assertEquals(
            7,
            inventory.getStackInSlot(2)
                .getItemDamage());
        assertEquals(2, remainder.stackSize);
        assertEquals(1, handler.getSlotLimit(0));

        ItemStack extracted = handler.extractItem(0, 1, false);
        assertEquals(1, extracted.stackSize);
        assertEquals(7, extracted.getItemDamage());
        assertNull(inventory.getStackInSlot(2));
    }

    @Test
    void rejectsNonIntegratedItemsAndDoesNotOverwriteAnOccupiedSlot() {
        ItemStackHandler inventory = new ItemStackHandler(1);
        Item circuitItem = new Item();
        PhysicalCircuitItemStackHandler handler = handler(inventory, 0, circuitItem);
        ItemStack ordinaryItem = new ItemStack(new Item(), 1);
        ItemStack circuit = new ItemStack(circuitItem, 1, 4);

        assertFalse(handler.isItemValid(0, ordinaryItem));
        assertSame(ordinaryItem, handler.insertItem(0, ordinaryItem, false));
        assertNull(inventory.getStackInSlot(0));

        assertNull(handler.insertItem(0, circuit, false));
        ItemStack secondCircuit = new ItemStack(circuit.getItem(), 1, 5);
        assertSame(secondCircuit, handler.insertItem(0, secondCircuit, false));
        assertEquals(
            4,
            inventory.getStackInSlot(0)
                .getItemDamage());
    }

    @Test
    void rejectsZeroSizeInsertionAndInvalidDirectWrites() {
        ItemStackHandler inventory = new ItemStackHandler(1);
        Item circuitItem = new Item();
        PhysicalCircuitItemStackHandler handler = handler(inventory, 0, circuitItem);
        ItemStack zeroSizeCircuit = new ItemStack(circuitItem, 0, 9);
        ItemStack invalidItem = new ItemStack(new Item(), 1);

        assertSame(zeroSizeCircuit, handler.insertItem(0, zeroSizeCircuit, false));
        assertNull(inventory.getStackInSlot(0));

        assertThrows(IllegalArgumentException.class, () -> handler.setStackInSlot(0, invalidItem));
        assertNull(inventory.getStackInSlot(0));
    }

    private static PhysicalCircuitItemStackHandler handler(ItemStackHandler inventory, int circuitSlot,
        Item circuitItem) {
        return new PhysicalCircuitItemStackHandler(inventory, circuitSlot, stack -> stack.getItem() == circuitItem);
    }
}
