package pl.kuba6000.gtnhextranerf.mixins.support;

import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.items.ItemIntegratedCircuit;

public final class PhysicalCircuitItemStackHandler implements IItemHandlerModifiable {

    private final IItemHandlerModifiable inventory;
    private final int circuitSlot;
    private final Predicate<ItemStack> circuitPredicate;

    public PhysicalCircuitItemStackHandler(IMetaTileEntity machine, int circuitSlot) {
        this(machine.getInventoryHandler(), circuitSlot, stack -> stack.getItem() instanceof ItemIntegratedCircuit);
    }

    PhysicalCircuitItemStackHandler(IItemHandlerModifiable inventory, int circuitSlot,
        Predicate<ItemStack> circuitPredicate) {
        this.inventory = inventory;
        this.circuitSlot = circuitSlot;
        this.circuitPredicate = circuitPredicate;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @Nullable ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return inventory.getStackInSlot(circuitSlot);
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack) {
        validateSlot(slot);
        if (stack == null) {
            inventory.setStackInSlot(circuitSlot, null);
        } else if (isItemValid(slot, stack)) {
            ItemStack inserted = stack.copy();
            inserted.stackSize = 1;
            inventory.setStackInSlot(circuitSlot, inserted);
        } else {
            throw new IllegalArgumentException("Only Integrated Circuits can be inserted into this slot");
        }
    }

    @Override
    public @Nullable ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {
        validateSlot(slot);
        if (stack == null || stack.stackSize <= 0 || !isItemValid(slot, stack) || getStackInSlot(slot) != null) {
            return stack;
        }

        if (!simulate) setStackInSlot(slot, stack);
        if (stack.stackSize == 1) return null;

        ItemStack remainder = stack.copy();
        remainder.stackSize--;
        return remainder;
    }

    @Override
    public @Nullable ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlot(slot);
        if (amount <= 0) return null;

        ItemStack stored = getStackInSlot(slot);
        if (stored == null) return null;

        ItemStack extracted = stored.copy();
        extracted.stackSize = 1;
        if (!simulate) inventory.setStackInSlot(circuitSlot, null);
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @Nullable ItemStack stack) {
        validateSlot(slot);
        return stack != null && circuitPredicate.test(stack);
    }

    private static void validateSlot(int slot) {
        if (slot != 0) throw new IndexOutOfBoundsException("Slot index out of bounds: " + slot);
    }
}
