package pl.kuba6000.gtnhextranerf.client;

import net.minecraftforge.client.event.GuiScreenEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lumien.custommainmenu.configuration.Alignment;
import lumien.custommainmenu.configuration.GuiConfig;
import lumien.custommainmenu.configuration.elements.Image;
import lumien.custommainmenu.configuration.elements.Text;
import lumien.custommainmenu.gui.GuiCustom;
import lumien.custommainmenu.lib.texts.TextString;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.Tags;

/** Adds native CMM elements before it builds its labels, including after a menu reload. */
@SideOnly(Side.CLIENT)
public final class MainMenuBranding {

    private static final String TITLE_ID = GTNHExtraNerf.MODID + "_branding";

    @SubscribeEvent
    public void initialize(GuiScreenEvent.InitGuiEvent.Pre event) {
        if (!(event.gui instanceof GuiCustom menu) || !"mainmenu".equals(menu.guiConfig.name)) return;
        GuiConfig config = menu.guiConfig;
        if (config.customTexts.stream()
            .anyMatch(text -> TITLE_ID.equals(text.name))) return;

        Alignment corner = config.getAlignment("top_right");
        Image logo = new Image(config, -120, 8, 32, 32, corner);
        logo.image = GuiConfig.getWantedTexture(GTNHExtraNerf.MODID + ":textures/items/ufo_assembly.png");
        config.customImages.add(logo);
        config.customTexts
            .add(new Text(config, TITLE_ID, new TextString("\u00a7lEXTRA NERF"), -82, 14, corner, 0xE8EFED));
        // Development build names include a branch and commit; keep the version line compact.
        config.customTexts.add(
            new Text(
                config,
                TITLE_ID + "_version",
                new TextString("v" + Tags.VERSION.split("[-+]", 2)[0]),
                -82,
                27,
                corner,
                0x92A3A0));
    }
}
