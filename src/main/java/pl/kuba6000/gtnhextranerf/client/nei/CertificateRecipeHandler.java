package pl.kuba6000.gtnhextranerf.client.nei;

import java.awt.Point;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.modularui.GTUITextures;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.progression.CertificationCost;
import pl.kuba6000.gtnhextranerf.progression.ProgressionCertificates;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

/** Informational certificate pages with an entry point into the server-owned exam. */
@SideOnly(Side.CLIENT)
public final class CertificateRecipeHandler extends TemplateRecipeHandler {

    public static final String ID = "gtnhextranerf.certification";
    public static final int WIDTH = 166;
    public static final int HEIGHT = 208;
    private static final int BUTTON_Y = HEIGHT - 23;

    @Override
    public CertificateRecipeHandler newInstance() {
        return new CertificateRecipeHandler();
    }

    @Override
    public String getRecipeName() {
        if (!arecipes.isEmpty() && ((CertificatePage) arecipes.get(0)).tier != ProgressionTier.LV)
            return StatCollector.translateToLocal(
                "gtnhextranerf.progression." + ((CertificatePage) arecipes.get(0)).tier.id() + ".nei.title");
        return StatCollector.translateToLocal("gtnhextranerf.progression.nei.title");
    }

    @Override
    public String getGuiTexture() {
        return "gregtech:textures/gui/background/nei_single_recipe.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (!ID.equals(outputId)) {
            super.loadCraftingRecipes(outputId, results);
            return;
        }
        if (results.length != 1) return;
        for (ProgressionTier tier : ProgressionTier.values()) if (tier.id()
            .equals(results[0])) addPage(tier);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        addPage(ProgressionCertificates.tierOf(result));
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        addPage(ProgressionCertificates.tierOf(ingredient));
    }

    private void addPage(ProgressionTier tier) {
        if (tier != null) arecipes.add(new CertificatePage(tier));
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GTUITextures.BACKGROUND_NEI_SINGLE_RECIPE.draw(0, 0, WIDTH, HEIGHT);
        GTUITextures.SLOT_DARK_GRAY.draw(74, 31, 18, 18);
        GTUITextures.BUTTON_STANDARD.draw(10, BUTTON_Y, WIDTH - 20, 20);
    }

    @Override
    public void drawForeground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        CertificatePage page = (CertificatePage) arecipes.get(recipe);
        drawLines(font, page.result.item.getDisplayName(), 8);
        int y = 58;
        y = drawLines(
            font,
            StatCollector.translateToLocal("gtnhextranerf.progression." + page.tier.id() + ".guide"),
            y);
        y = drawLines(
            font,
            StatCollector.translateToLocal("gtnhextranerf.progression." + page.tier.id() + ".acquisition_exam"),
            y + 7);
        ItemStack fee = CertificationCost.material(page.tier);
        drawLines(
            font,
            StatCollector.translateToLocalFormatted(
                "gtnhextranerf.certification.cost",
                CertificationCost.amount(page.tier),
                fee == null ? StatCollector.translateToLocal("gtnhextranerf.certification.missing_fee")
                    : fee.getDisplayName()),
            y + 7);
        String label = StatCollector.translateToLocal("gtnhextranerf.exam." + page.tier.id() + ".open");
        font.drawString(label, (WIDTH - font.getStringWidth(label)) / 2, BUTTON_Y + 6, 0x404040);
    }

    @Override
    public boolean mouseClicked(GuiRecipe<?> gui, int button, int recipe) {
        if (button != 0) return false;
        Point mouse = GuiDraw.getMousePosition(), offset = gui.getRecipePosition(recipe);
        GuiContainerAccessor container = (GuiContainerAccessor) gui;
        int x = mouse.x - container.getGuiLeft() - offset.x, y = mouse.y - container.getGuiTop() - offset.y;
        if (x < 10 || x >= WIDTH - 10 || y < BUTTON_Y || y >= BUTTON_Y + 20) return false;
        GTNHExtraNerf.proxy.openExam(((CertificatePage) arecipes.get(recipe)).tier);
        return true;
    }

    private static int drawLines(FontRenderer font, String text, int y) {
        for (String line : font.listFormattedStringToWidth(text, WIDTH - 20)) {
            font.drawString(line, 10, y, 0x404040);
            y += font.FONT_HEIGHT;
        }
        return y;
    }

    private final class CertificatePage extends CachedRecipe {

        private final ProgressionTier tier;
        private final PositionedStack result;

        private CertificatePage(ProgressionTier tier) {
            this.tier = tier;
            result = new PositionedStack(ProgressionCertificates.stack(tier), 75, 32, false);
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return Collections.emptyList();
        }
    }
}
