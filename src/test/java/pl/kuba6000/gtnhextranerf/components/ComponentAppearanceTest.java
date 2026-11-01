package pl.kuba6000.gtnhextranerf.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import org.junit.jupiter.api.Test;

class ComponentAppearanceTest {

    @Test
    void appearanceUsesTheSourceMetadataIconsColorsAndRenderPassesWithoutSharingStacks() {
        IIcon firstIcon = new TextureAtlasSprite("first") {};
        IIcon secondIcon = new TextureAtlasSprite("second") {};
        Item source = new Item() {

            @Override
            public IIcon getIcon(ItemStack stack, int pass) {
                return stack.getItemDamage() == 23 ? (pass == 0 ? firstIcon : secondIcon) : null;
            }

            @Override
            public int getColorFromItemStack(ItemStack stack, int pass) {
                return stack.getItemDamage() == 23 ? (pass == 0 ? 0x123456 : 0xABCDEF) : 0;
            }

            @Override
            public boolean requiresMultipleRenderPasses() {
                return true;
            }

            @Override
            public int getRenderPasses(int metadata) {
                return metadata == 23 ? 3 : 1;
            }

            @Override
            public int getSpriteNumber() {
                return 0;
            }

            @Override
            public boolean hasEffect(ItemStack stack, int pass) {
                return stack.getItemDamage() == 23 && pass == 1;
            }
        };
        ItemStack prototype = new ItemStack(source, 1, 23);
        ComponentRegistry registry = new ComponentRegistry((tier, type) -> prototype);
        ItemStack componentStack = registry.stack(ComponentTier.UQV, ComponentType.SENSOR, 1);
        ComponentItem component = (ComponentItem) componentStack.getItem();

        assertSame(firstIcon, component.getIcon(componentStack, 0));
        assertSame(secondIcon, component.getIcon(componentStack, 1));
        assertEquals(0x123456, component.getColorFromItemStack(componentStack, 0));
        assertEquals(0xABCDEF, component.getColorFromItemStack(componentStack, 1));
        assertTrue(component.requiresMultipleRenderPasses());
        assertEquals(3, component.getRenderPasses(0));
        assertEquals(0, component.getSpriteNumber());
        assertTrue(component.hasEffect(componentStack, 1));
        assertNull(component.getCreativeTab());

        ItemStack appearance = component.appearance();
        assertNotSame(prototype, appearance);
        assertSame(source, appearance.getItem());
        assertEquals(23, appearance.getItemDamage());
        appearance.setItemDamage(99);
        assertEquals(
            23,
            component.appearance()
                .getItemDamage());
    }
}
