package pl.kuba6000.gtnhextranerf.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class ComponentRegistryTest {

    @Test
    void eachComponentHasItsOwnIdentityAndRequestsReturnIndependentStacks() {
        Item realComponent = new Item();
        ComponentRegistry registry = new ComponentRegistry((tier, type) -> new ItemStack(realComponent));
        Set<Item> identities = new HashSet<>();
        for (ComponentType type : ComponentType.values()) {
            ItemStack first = registry.stack(ComponentTier.UQV, type, 2);
            ItemStack second = registry.stack(ComponentTier.UQV, type, 5);
            assertNotSame(realComponent, first.getItem());
            assertSame(first.getItem(), second.getItem());
            assertNotSame(first, second);
            first.stackSize = 0;
            assertEquals(5, second.stackSize);
            identities.add(second.getItem());
        }
        assertEquals(ComponentType.values().length, identities.size());
        assertThrows(
            UnsupportedOperationException.class,
            () -> registry.items()
                .clear());
    }
}
