package pl.kuba6000.gtnhextranerf.client;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
import com.gtnewhorizon.gtnhlib.client.event.RenderTooltipEvent;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.event.TooltipEnricherEvent;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Materials;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Adds the material image through the installed tooltip renderer's public client events. */
@SideOnly(Side.CLIENT)
public final class BrainrotTooltips {

    private static final ResourceLocation SAHUR = new ResourceLocation(
        GTNHExtraNerf.MODID,
        "textures/gui/tooltips/tung_tung_tung_sahur.png");
    private static final UITexture SAHUR_TEXTURE = UITexture.fullImage(SAHUR);

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new BrainrotTooltips());
        if (Loader.isModLoaded("chromatictooltips")) MinecraftForge.EVENT_BUS.register(new Chromatic());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void render(RenderTooltipEvent event) {
        if (event.alternativeRenderer != null || !isFinishedMaterial(event.itemStack)) return;
        event.alternativeRenderer = lines -> draw(event, lines);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderModular(RichTooltipEvent.Pre event) {
        if (!isFinishedMaterial(event.getItemStack()) || !(event.getTooltip() instanceof RichText text)) return;
        int width = Math.max(16, Math.min(320, event.getScreenWidth() - 16));
        event.setMaxWidth(Math.min(event.getMaxWidth(), width));
        int textHeight = 0;
        for (String line : text.getAsStrings()) {
            textHeight += Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(line, event.getMaxWidth())
                .size() * 10;
        }
        int size = Math.min(Math.min(128, width), event.getScreenHeight() - textHeight - 24);
        // RichTooltip has no paging; leave expanded text intact when it already fills the screen.
        if (size >= 24) event.getTooltip()
            .spaceLine()
            .addLine(
                SAHUR_TEXTURE.asIcon()
                    .size(size));
    }

    private static boolean isFinishedMaterial(ItemStack stack) {
        if (stack == null) return false;
        ItemData data = GTOreDictUnificator.getAssociation(stack);
        return data != null && data.mMaterial != null && data.mMaterial.mMaterial == Materials.TungstenSteel;
    }

    private static void draw(RenderTooltipEvent event, List<String> original) {
        List<String> lines = new ArrayList<>();
        int maxWidth = Math.max(16, Math.min(320, event.gui.width - 16));
        int textHeight = -2, width = 0;
        for (String line : original) {
            GuiDraw.ITooltipLineHandler custom = GuiDraw.getTipLine(line);
            if (custom != null) {
                lines.add(line);
                textHeight += custom.getSize().height;
                width = Math.max(width, custom.getSize().width);
                continue;
            }
            boolean gap = line.endsWith(GuiDraw.TOOLTIP_LINESPACE);
            List<String> wrapped = event.font.listFormattedStringToWidth(
                gap ? line.substring(0, line.length() - GuiDraw.TOOLTIP_LINESPACE.length()) : line,
                maxWidth);
            for (int i = 0; i < wrapped.size(); i++) {
                String text = wrapped.get(i);
                width = Math.max(width, event.font.getStringWidth(text));
                boolean last = gap && i == wrapped.size() - 1;
                lines.add(last ? text + GuiDraw.TOOLTIP_LINESPACE : text);
                textHeight += last ? 12 : 10;
            }
        }
        int height = Math.max(16, Math.min(128, event.gui.height - 28));
        int available = event.gui.height - textHeight - 24;
        if (available >= 48) height = Math.min(height, available);
        int imageSize = Math.min(maxWidth, height);
        int lineWidth = Math.max(width, imageSize);
        // IDs are cleared by GuiDraw after drawing: register the image inside the renderer on every frame.
        lines.add(GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(new GuiDraw.ITooltipLineHandler() {

            @Override
            public Dimension getSize() {
                return new Dimension(lineWidth, imageSize + 4);
            }

            @Override
            public void draw(int x, int y) {
                drawImage(x + (lineWidth - imageSize) / 2, y + 2, imageSize);
            }
        }));
        GuiContainerManager.drawPagedTooltip(event.font, event.x + 12, event.y - 12, lines);
    }

    private static void drawImage(int x, int y, int size) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(SAHUR);
            Gui.func_152125_a(x, y, 0, 0, 1, 1, size, size, 1, 1);
        } finally {
            GL11.glPopAttrib();
        }
    }

    /** Kept separate so Forge can register the legacy listener without loading optional Chromatic types. */
    @SideOnly(Side.CLIENT)
    public static final class Chromatic {

        private static final String SECTION = GTNHExtraNerf.MODID + ".sahur";

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void enrich(TooltipEnricherEvent event) {
            TooltipContext context = event.context;
            if (!isFinishedMaterial(context.getItem()) || context.getSection(SECTION) != null) return;
            Minecraft mc = Minecraft.getMinecraft();
            int viewport = Math.min(mc.displayWidth, mc.displayHeight) / Math.max(1, context.getScaleFactor());
            int size = Math.max(16, Math.min(128, viewport - 32));
            // Let Chromatic move the whole image to another page instead of shrinking it to leftover space.
            context.addSection(SECTION, Collections.singletonList(image(size)));
        }

        private static ITooltipComponent image(int size) {
            return new ITooltipComponent() {

                @Override
                public int getWidth() {
                    return size;
                }

                @Override
                public int getHeight() {
                    return size;
                }

                @Override
                public int getSpacing() {
                    return 4;
                }

                @Override
                public void draw(int x, int y, int availableWidth, TooltipContext context) {
                    drawImage(x + Math.max(0, (availableWidth - size) / 2), y, size);
                }
            };
        }
    }
}
