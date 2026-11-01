package pl.kuba6000.gtnhextranerf.client;

import java.util.Objects;
import java.util.function.BiFunction;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pl.kuba6000.gtnhextranerf.components.ComponentItem;

/** Delegates to the source item's renderer, including GregTech's material effects. */
@SideOnly(Side.CLIENT)
public final class ComponentItemRenderer implements IItemRenderer {

    private final ComponentItem component;
    private final BiFunction<ItemStack, ItemRenderType, IItemRenderer> renderers;

    public ComponentItemRenderer(ComponentItem component) {
        this(component, MinecraftForgeClient::getItemRenderer);
    }

    public ComponentItemRenderer(ComponentItem component,
        BiFunction<ItemStack, ItemRenderType, IItemRenderer> renderers) {
        this.component = Objects.requireNonNull(component, "Component item");
        this.renderers = Objects.requireNonNull(renderers, "Source renderers");
    }

    private ItemStack appearanceFor(ItemStack item) {
        ItemStack source = component.appearance();
        source.stackSize = item.stackSize;
        return source;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        ItemStack source = appearanceFor(item);
        IItemRenderer renderer = renderers.apply(source, type);
        return renderer != null && renderer.handleRenderType(source, type);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        ItemStack source = appearanceFor(item);
        IItemRenderer renderer = renderers.apply(source, type);
        return renderer != null && renderer.shouldUseRenderHelper(type, source, helper);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        ItemStack source = appearanceFor(item);
        IItemRenderer renderer = Objects.requireNonNull(renderers.apply(source, type), "Missing source renderer");
        renderer.renderItem(type, source, data);
    }
}
