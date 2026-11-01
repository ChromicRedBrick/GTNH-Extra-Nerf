package pl.kuba6000.gtnhextranerf.progression.exam;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

/** Common MUI2 shell; drawing and Minecraft font access are provided only by the client proxy. */
public final class ExamGui implements IGuiHolder<GuiData> {

    public static final int WIDTH = 300;
    public static final int HEIGHT = 224;
    public static final int CONTENT_VERTICAL_PADDING = 86;
    private static final UITexture PAPER = UITexture.builder()
        .location(GTNHExtraNerf.MODID, "gui/lv_exam")
        .imageSize(WIDTH, HEIGHT)
        .adaptable(10, 30, 10, 56)
        .build();
    private final ExamDefinition definition;
    private final ProgressionTier tier;

    public ExamGui(ExamDefinition definition) {
        this(definition.tier(), definition);
    }

    public ExamGui(ProgressionTier tier, ExamDefinition definition) {
        this.tier = Objects.requireNonNull(tier);
        this.definition = definition;
    }

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings()
            .disable();
        ExamSyncHandler exam = syncManager.isClient() ? new ExamSyncHandler(tier)
            : new ExamSyncHandler((EntityPlayerMP) data.getPlayer(), tier, definition);
        syncManager.syncValue(ExamSyncHandler.SYNC_ID, exam);
        VerticalScrollData scrollData = new VerticalScrollData();
        ScrollWidget<?> scroll = new ScrollWidget<>(scrollData).pos(10, 30)
            .size(280, 138)
            .heightRelOffset(1, -CONTENT_VERTICAL_PADDING)
            .showScrollShadows(false);
        scroll.child(GTNHExtraNerf.proxy.createExamCanvas(exam));
        ModularPanel panel = ModularPanel.defaultPanel(tier.id() + "_exam", WIDTH, HEIGHT)
            .background(PAPER);
        panel.child(
            IKey.lang("gtnhextranerf.exam." + tier.id() + ".title")
                .asWidget()
                .pos(10, 8)
                .size(200, 12)
                .color(0xFF40362A));
        panel.child(
            IKey.dynamic(() -> pageLabel(exam))
                .asWidget()
                .pos(215, 8)
                .size(75, 12)
                .color(0xFF40362A));
        panel.child(scroll);
        panel.child(
            new ButtonWidget<>().left(10)
                .bottom(32)
                .size(24, 18)
                .setEnabledIf(widget -> exam.admitted())
                .overlay(IKey.str("<"))
                .onMousePressed(button -> {
                    if (button != 0 || !syncManager.isClient()) return false;
                    exam.changePage(-1);
                    scrollData.scrollTo(scroll.getScrollArea(), 0);
                    return true;
                }));
        panel.child(
            new ButtonWidget<>().left(38)
                .bottom(32)
                .size(24, 18)
                .setEnabledIf(widget -> exam.admitted())
                .overlay(IKey.str(">"))
                .onMousePressed(button -> {
                    if (button != 0 || !syncManager.isClient()) return false;
                    exam.changePage(1);
                    scrollData.scrollTo(scroll.getScrollArea(), 0);
                    return true;
                }));
        panel.child(
            new ButtonWidget<>().left(195)
                .bottom(32)
                .size(95, 18)
                .setEnabledIf(widget -> exam.admitted())
                .overlay(IKey.lang("gtnhextranerf.exam.submit"))
                .onMousePressed(button -> button == 0 && syncManager.isClient() && exam.submit()));
        panel.child(
            IKey.dynamic(() -> selectionLabel(exam))
                .asWidget()
                .left(68)
                .bottom(32)
                .size(122, 18)
                .color(0xFF40362A));
        panel.child(
            new ButtonWidget<>().left(10)
                .bottom(32)
                .size(280, 18)
                .setEnabledIf(widget -> !exam.admitted() && exam.canPay())
                .overlay(IKey.lang("gtnhextranerf.certification." + (tier.hasExam() ? "pay_exam" : "pay_certificate")))
                .onMousePressed(button -> button == 0 && syncManager.isClient() && exam.pay()));
        panel.child(
            IKey.dynamic(() -> statusLabel(exam))
                .asWidget()
                .left(10)
                .bottom(7)
                .size(280, 18)
                .color(0xFF40362A));
        return panel;
    }

    private static String pageLabel(ExamSyncHandler exam) {
        if (!exam.admitted()) return "";
        if (exam.finished()) return StatCollector.translateToLocal("gtnhextranerf.exam.result");
        if (exam.page() < 0) return StatCollector.translateToLocal("gtnhextranerf.exam.instructions");
        return exam.paper() == null ? "..."
            : (exam.page() + 1) + " / "
                + exam.paper()
                    .questions()
                    .size();
    }

    private static String selectionLabel(ExamSyncHandler exam) {
        return exam.paper() == null ? ""
            : StatCollector.translateToLocalFormatted(
                "gtnhextranerf.exam.answered",
                exam.answeredCount(),
                exam.paper()
                    .questions()
                    .size());
    }

    private static String statusLabel(ExamSyncHandler exam) {
        if (!exam.admitted()) return StatCollector.translateToLocal(
            exam.waiting() ? "gtnhextranerf.certification.waiting" : "gtnhextranerf.certification.status");
        if (exam.paper() == null) return StatCollector.translateToLocal("gtnhextranerf.exam.loading");
        if (exam.waiting()) return StatCollector.translateToLocal("gtnhextranerf.exam.waiting");
        ExamAttempt.Result result = exam.result();
        return StatCollector.translateToLocalFormatted(
            "gtnhextranerf.exam.status." + result.status()
                .name()
                .toLowerCase(java.util.Locale.ROOT),
            result.correct(),
            result.total());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(GuiData data, ModularPanel mainPanel) {
        return new ModularScreen(GTNHExtraNerf.MODID, mainPanel).pausesGame(false);
    }
}
