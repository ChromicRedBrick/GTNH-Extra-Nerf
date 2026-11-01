package pl.kuba6000.gtnhextranerf.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;

import org.junit.jupiter.api.Test;

import pl.kuba6000.gtnhextranerf.components.ComponentItem;
import pl.kuba6000.gtnhextranerf.components.ComponentRegistry;
import pl.kuba6000.gtnhextranerf.components.ComponentTier;
import pl.kuba6000.gtnhextranerf.components.ComponentType;

class ComponentItemRendererTest {

    @Test
    void rendersTheSourceComponentAndPreservesItsMetadataAndTheRequestedStackSize() {
        Item sourceItem = new Item();
        ItemStack prototype = new ItemStack(sourceItem, 1, 23);
        ComponentRegistry registry = new ComponentRegistry((tier, type) -> prototype);
        ItemStack component = registry.stack(ComponentTier.UQV, ComponentType.SENSOR, 4);
        SourceRenderer sourceRenderer = new SourceRenderer(sourceItem);
        ComponentItemRenderer renderer = new ComponentItemRenderer(
            (ComponentItem) component.getItem(),
            (stack, type) -> sourceRenderer);

        assertTrue(renderer.handleRenderType(component, ItemRenderType.INVENTORY));
        assertTrue(
            renderer.shouldUseRenderHelper(ItemRenderType.INVENTORY, component, ItemRendererHelper.INVENTORY_BLOCK));
        renderer.renderItem(ItemRenderType.INVENTORY, component);

        assertSame(sourceItem, sourceRenderer.rendered.getItem());
        assertEquals(23, sourceRenderer.rendered.getItemDamage());
        assertEquals(4, sourceRenderer.rendered.stackSize);
        assertNotSame(sourceItem, component.getItem());
        assertEquals(1, prototype.stackSize);
    }

    private static final class SourceRenderer implements IItemRenderer {

        private final Item source;
        private ItemStack rendered;

        private SourceRenderer(Item source) {
            this.source = source;
        }

        @Override
        public boolean handleRenderType(ItemStack item, ItemRenderType type) {
            return item.getItem() == source && item.getItemDamage() == 23;
        }

        @Override
        public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
            return handleRenderType(item, type) && helper == ItemRendererHelper.INVENTORY_BLOCK;
        }

        @Override
        public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
            rendered = item;
        }
    }
}
