package pl.kuba6000.gtnhextranerf.integration;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.cleanroommc.modularui.api.RecipeViewerSettings;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.Unpooled;
import pl.kuba6000.gtnhextranerf.progression.CertificationCost;
import pl.kuba6000.gtnhextranerf.progression.MachineProgression;
import pl.kuba6000.gtnhextranerf.progression.ProgressionCertificates;
import pl.kuba6000.gtnhextranerf.progression.ProgressionServerTasks;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;
import pl.kuba6000.gtnhextranerf.progression.exam.ExamAttempt;
import pl.kuba6000.gtnhextranerf.progression.exam.ExamDefinition;
import pl.kuba6000.gtnhextranerf.progression.exam.ExamGui;
import pl.kuba6000.gtnhextranerf.progression.exam.ExamProtocol;
import pl.kuba6000.gtnhextranerf.progression.exam.ExamQuestion;
import pl.kuba6000.gtnhextranerf.progression.exam.ExamSyncHandler;

/** Actual MUI2 server container, network-thread submission and inventory reward. */
final class ExamIntegrationTests {

    private ExamIntegrationTests() {}

    static void verify(WorldServer world) throws Exception {
        verifyExam(world, ProgressionTier.STEAM);
        verifyExam(world, ProgressionTier.LV);
        for (ProgressionTier tier : ProgressionTier.values()) if (!tier.hasExam()) verifyPaymentOnly(world, tier);
    }

    private static ExamDefinition definition(ProgressionTier tier, boolean enabled) {
        return new ExamDefinition(
            "fixture",
            tier,
            Arrays.asList(
                new ExamQuestion("one", "One", "A", "B", "C", "D"),
                new ExamQuestion("two", "Two", "A", "B", "C", "D")),
            new int[] { 0, 3 },
            2,
            enabled);
    }

    private static FakePlayer player(WorldServer world) {
        return FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "ExamTest"));
    }

    private static void clear(FakePlayer player) {
        Arrays.fill(player.inventory.mainInventory, null);
    }

    private static void verifyExam(WorldServer world, ProgressionTier tier) throws Exception {
        MachineProgression.revoke(world, tier);
        FakePlayer player = player(world);
        ExamDefinition definition = definition(tier, true);
        try (Session session = new Session(player, tier, definition)) {
            ExamSyncHandler exam = session.exam;
            submitFromNetwork(exam, 1, 8);
            tick();
            check(certificates(player, tier) == 0, "unpaid submission issued a certificate");
            requestFromNetwork(exam, ExamSyncHandler.PAY);
            tick();
            check(!exam.admitted(), "empty inventory was admitted without paying");
            fillPayment(player, tier);
            player.inventory.mainInventory[0].stackSize--;
            int partial = materials(player, tier);
            requestFromNetwork(exam, ExamSyncHandler.PAY);
            tick();
            check(
                !exam.admitted() && materials(player, tier) == partial,
                "insufficient payment was partially consumed");
            fillPayment(player, tier);
            int before = materials(player, tier);
            requestFromNetwork(exam, ExamSyncHandler.PAY);
            check(materials(player, tier) == before, "network thread changed the inventory");
            tick();
            check(exam.admitted() && materials(player, tier) == 0, "paid exam was not admitted for the exact fee");
            fillPayment(player, tier);
            requestFromNetwork(exam, ExamSyncHandler.PAY);
            tick();
            check(materials(player, tier) == before, "payment replay charged the same attempt again");
            submitFromNetwork(exam, 0, 0);
            tick();
            check(
                exam.result()
                    .status() == ExamAttempt.Status.INCOMPLETE,
                "incomplete paper ended the paid attempt");
            submitFromNetwork(exam, 1, 8);
            check(certificates(player, tier) == 0, "network callback issued a reward before the server tick");
            tick();
            check(
                exam.result()
                    .status() == ExamAttempt.Status.PASSED,
                "server did not grade the valid paper");
            check(certificates(player, tier) == 1, "exam did not issue exactly one certificate of its tier");
            submitFromNetwork(exam, 1, 8);
            tick();
            check(certificates(player, tier) == 1, "duplicate submission duplicated the certificate");
            check(materials(player, tier) == before, "submitting answers charged another fee");
        }
        clear(player);
        fillPayment(player, tier);
        try (Session session = new Session(player, tier, definition)) {
            requestFromNetwork(session.exam, ExamSyncHandler.PAY);
            tick();
            submitFromNetwork(session.exam, 2, 2);
            tick();
            check(
                session.exam.result()
                    .status() == ExamAttempt.Status.FAILED,
                "wrong paper did not fail");
            check(materials(player, tier) == 0 && certificates(player, tier) == 0, "failed exam refunded or rewarded");
        }
        try (Session session = new Session(player, tier, definition)) {
            requestFromNetwork(session.exam, ExamSyncHandler.PAY);
            tick();
            check(!session.exam.admitted(), "reopened exam reused a previous payment");
            fillPayment(player, tier);
            requestFromNetwork(session.exam, ExamSyncHandler.PAY);
            tick();
            check(session.exam.admitted() && materials(player, tier) == 0, "new attempt did not take another fee");
            submitFromNetwork(session.exam, 1, 8);
            player.openContainer = player.inventoryContainer;
            tick();
            check(certificates(player, tier) == 0, "abandoned exam issued a certificate");
        }
        check(materials(player, tier) == 0, "closing a paid paper refunded the fee");
        fillPayment(player, tier);
        int before = materials(player, tier);
        try (Session session = new Session(player, tier, definition(tier, false))) {
            requestFromNetwork(session.exam, ExamSyncHandler.PAY);
            tick();
            check(!session.exam.admitted() && materials(player, tier) == before, "disabled exam took payment");
        }
        try (Session session = new Session(player, tier, definition)) {
            requestFromNetwork(session.exam, ExamSyncHandler.PAY);
            player.openContainer = player.inventoryContainer;
            tick();
            check(materials(player, tier) == before, "closed panel's queued payment changed inventory");
        }
        clear(player);
    }

    private static void verifyPaymentOnly(WorldServer world, ProgressionTier tier) throws Exception {
        MachineProgression.revoke(world, tier);
        FakePlayer first = player(world), second = player(world);
        fillPayment(first, tier);
        fillPayment(second, tier);
        int before = materials(first, tier);
        try (Session one = new Session(first, tier, null); Session two = new Session(second, tier, null)) {
            submitFromNetwork(one.exam, 1, 8);
            tick();
            check(!MachineProgression.isCertified(world, tier), "unpaid answers bypassed " + tier + " payment");
            requestFromNetwork(one.exam, ExamSyncHandler.PAY);
            requestFromNetwork(two.exam, ExamSyncHandler.PAY);
            check(materials(first, tier) == before, tier + " network callback consumed inventory");
            tick();
            check(MachineProgression.isCertified(world, tier), tier + " payment did not certify the world");
            check(materials(first, tier) == 0, "first " + tier + " payment did not take the exact fee");
            check(materials(second, tier) == before, "simultaneous player was charged for an already certified world");
            check(
                one.exam.paper() == null && certificates(first, tier) == 0,
                tier + " unexpectedly issued an exam or item reward");
            fillPayment(first, tier);
            requestFromNetwork(one.exam, ExamSyncHandler.PAY);
            requestFromNetwork(two.exam, ExamSyncHandler.PAY);
            tick();
            check(
                materials(first, tier) == before && materials(second, tier) == before,
                "repeated " + tier + " payment charged again");
        }
        clear(first);
        clear(second);
    }

    private static final class Session implements AutoCloseable {

        private final FakePlayer player;
        private final ModularSyncManager manager = new ModularSyncManager(false);
        private final ExamSyncHandler exam;

        private Session(FakePlayer player, ProgressionTier tier, ExamDefinition definition) {
            this.player = player;
            PanelSyncManager sync = new PanelSyncManager(manager, true);
            UISettings settings = new UISettings(RecipeViewerSettings.DUMMY);
            GuiData data = new GuiData(player);
            ModularPanel panel = new ExamGui(tier, definition).buildUI(data, sync, settings);
            WidgetTree.collectSyncValues(sync, panel);
            ModularContainer container = new ModularContainer();
            container.construct(player, manager, settings, panel.getName(), data);
            player.openContainer = container;
            manager.onOpen();
            exam = (ExamSyncHandler) sync.findSyncHandlerNullable(ExamSyncHandler.SYNC_ID, 0);
            check(exam != null && exam.isAllowC2S(), "panel did not register its client request handler");
        }

        @Override
        public void close() {
            player.openContainer = player.inventoryContainer;
            manager.onClose();
            manager.dispose();
        }
    }

    private static void tick() {
        new ProgressionServerTasks().onTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
    }

    private static void fillPayment(FakePlayer player, ProgressionTier tier) {
        int remaining = CertificationCost.amount(tier);
        for (int slot = 0; remaining > 0; slot++) {
            ItemStack stack = CertificationCost.material(tier);
            stack.stackSize = Math.min(64, remaining);
            player.inventory.setInventorySlotContents(slot, stack);
            remaining -= stack.stackSize;
        }
    }

    private static int materials(FakePlayer player, ProgressionTier tier) {
        ItemStack material = CertificationCost.material(tier);
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory)
            if (stack != null && stack.isItemEqual(material)) count += stack.stackSize;
        return count;
    }

    private static void requestFromNetwork(ExamSyncHandler exam, int id) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread network = new Thread(() -> {
            PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
            try {
                exam.readOnServer(id, packet);
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                packet.release();
            }
        }, "payment-network-fixture");
        network.start();
        network.join();
        if (failure.get() != null) throw new AssertionError("payment network callback failed", failure.get());
    }

    private static int certificates(FakePlayer player, ProgressionTier tier) {
        int count = 0;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack item = player.inventory.getStackInSlot(slot);
            if (ProgressionCertificates.tierOf(item) == tier) count += item.stackSize;
        }
        return count;
    }

    private static void submitFromNetwork(ExamSyncHandler exam, int... choices) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread network = new Thread(() -> {
            PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
            try {
                ExamProtocol.writeChoices(packet, choices);
                exam.readOnServer(ExamSyncHandler.SUBMIT, packet);
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                packet.release();
            }
        }, "exam-network-fixture");
        network.start();
        network.join();
        if (failure.get() != null) throw new AssertionError("exam network callback failed", failure.get());
    }

    private static void check(boolean success, String detail) {
        if (!success) throw new AssertionError(detail);
    }
}
