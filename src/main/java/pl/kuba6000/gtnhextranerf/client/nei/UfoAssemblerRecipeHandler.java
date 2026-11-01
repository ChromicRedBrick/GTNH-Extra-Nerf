package pl.kuba6000.gtnhextranerf.client.nei;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IUsageHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.modularui.GTUITextures;
import pl.kuba6000.gtnhextranerf.components.ComponentItem;
import pl.kuba6000.gtnhextranerf.components.ComponentRegistry;
import pl.kuba6000.gtnhextranerf.components.ComponentTier;
import pl.kuba6000.gtnhextranerf.components.ComponentType;
import pl.kuba6000.gtnhextranerf.fabrication.UfoAssemblyRegistry;
import pl.kuba6000.gtnhextranerf.loader.ComponentLoader;

/** NEI-only UEV-to-UQV blueprints; no corresponding crafting or machine recipes are registered. */
@SideOnly(Side.CLIENT)
public final class UfoAssemblerRecipeHandler extends TemplateRecipeHandler {

    public static final int WIDTH = 166;
    public static final int HEIGHT = 140;
    private static final int SOURCE_X = 16;
    private static final int CENTER_X = 75;
    private static final int RESULT_X = 134;
    private static final int COMPONENT_Y = 76;
    private static final int UFO_Y = 14;

    private final ComponentRegistry components;
    private final UfoAssemblyRegistry fabrication;

    public UfoAssemblerRecipeHandler() {
        this(ComponentLoader.components(), ComponentLoader.fabrication());
    }

    public UfoAssemblerRecipeHandler(ComponentRegistry components, UfoAssemblyRegistry fabrication) {
        this.components = Objects.requireNonNull(components, "Component catalog");
        this.fabrication = Objects.requireNonNull(fabrication, "Fabrication catalog");
    }

    @Override
    public UfoAssemblerRecipeHandler newInstance() {
        return new UfoAssemblerRecipeHandler(components, fabrication);
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("gtnhextranerf.nei.ufo_assembler");
    }

    @Override
    public String getGuiTexture() {
        return "gregtech:textures/gui/background/nei_single_recipe.png";
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GTUITextures.BACKGROUND_NEI_SINGLE_RECIPE.draw(0, 0, WIDTH, HEIGHT);
        drawBeam();
        drawArrow(39, 68, COMPONENT_Y + 8);
        drawArrow(98, 129, COMPONENT_Y + 8);
        GL11.glColor4f(1, 1, 1, 1);
        AssemblyPage page = (AssemblyPage) arecipes.get(recipe);
        for (PositionedStack stack : page.getIngredients()) drawSlot(stack);
        drawSlot(page.getResult());
    }

    private static void drawSlot(PositionedStack stack) {
        GTUITextures.SLOT_DARK_GRAY.draw(stack.relx - 1, stack.rely - 1, 18, 18);
    }

    private void drawBeam() {
        int top = UFO_Y + 16;
        int bottom = COMPONENT_Y + 27;
        int pulse = top + Math.floorMod(cycleticks / 2, bottom - top);
        for (int y = top; y < bottom; y++) {
            int halfWidth = 4 + (y - top) * 20 / (bottom - top);
            int alpha = 64 - (y - top) * 40 / (bottom - top);
            if (Math.abs(y - pulse) < 2) alpha += 50;
            GuiDraw.drawRect(CENTER_X + 8 - halfWidth, y, halfWidth * 2, 1, alpha << 24 | 0x65E7DF);
        }
    }

    private static void drawArrow(int startX, int endX, int centerY) {
        int color = 0xFF71878B;
        GuiDraw.drawRect(startX, centerY - 1, endX - startX - 3, 2, color);
        for (int step = 0; step < 5; step++) {
            GuiDraw.drawRect(endX - 4 + step, centerY - 5 + step, 1, 10 - step * 2, color);
        }
    }

    @Override
    public void drawForeground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int y = 112;
        String title = StatCollector.translateToLocal("gtnhextranerf.tier.uqv.name");
        for (String line : font.listFormattedStringToWidth(title, WIDTH - 20)) {
            font.drawString(line, (WIDTH - font.getStringWidth(line)) / 2, y, 0x404040);
            y += font.FONT_HEIGHT;
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (fabrication.isAssembly(result)) loadAllComponents();
        else queriedComponent(result).ifPresent(type -> arecipes.add(new AssemblyPage(type)));
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (fabrication.isAssembly(ingredient)) {
            loadAllComponents();
            return;
        }
        Optional<ComponentType> type = queriedComponent(ingredient);
        if (!type.isPresent()) type = fabrication.componentForIngredient(ingredient);
        if (type.isPresent()) {
            arecipes.add(new AssemblyPage(type.get()));
        }
    }

    @Override
    public IUsageHandler getUsageAndCatalystHandler(String inputId, Object... ingredients) {
        // The UFO catalyst has explicit item routing; no transfer rectangle exposes a generic all-recipes query.
        return getUsageHandler(inputId, ingredients);
    }

    private void loadAllComponents() {
        for (ComponentType type : ComponentType.values()) arecipes.add(new AssemblyPage(type));
    }

    private static Optional<ComponentType> queriedComponent(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ComponentItem) {
            ComponentItem component = (ComponentItem) stack.getItem();
            if (component.tier() == ComponentTier.UQV) return Optional.of(component.type());
        }
        return Optional.empty();
    }

    private ItemStack sourceComponent(ComponentType type) {
        return ((ComponentItem) components.stack(ComponentTier.UQV, type, 1)
            .getItem()).appearance();
    }

    private final class AssemblyPage extends CachedRecipe {

        private final PositionedStack result;
        private final List<PositionedStack> inputs;
        private final List<PositionedStack> machines;

        private AssemblyPage(ComponentType type) {
            ItemStack output = components.stack(ComponentTier.UQV, type, 1);
            result = new PositionedStack(output, RESULT_X, COMPONENT_Y, false);
            inputs = Collections.unmodifiableList(
                Arrays.asList(
                    new PositionedStack(sourceComponent(type), SOURCE_X, COMPONENT_Y, false),
                    new PositionedStack(fabrication.ingredient(type), CENTER_X, COMPONENT_Y, false)));
            machines = Collections.singletonList(new PositionedStack(fabrication.assembly(), CENTER_X, UFO_Y, false));
        }

        @Override
        public PositionedStack getResult() {
            return result;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return inputs;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return machines;
        }
    }
}
