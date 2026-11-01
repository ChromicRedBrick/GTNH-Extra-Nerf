package pl.kuba6000.gtnhextranerf.progression.exam;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.value.sync.SyncHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pl.kuba6000.gtnhextranerf.progression.CertificationCost;
import pl.kuba6000.gtnhextranerf.progression.MachineProgression;
import pl.kuba6000.gtnhextranerf.progression.ProgressionCertificates;
import pl.kuba6000.gtnhextranerf.progression.ProgressionServerTasks;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

/** Panel-scoped exam session. Its volatile client data is consumed only by the GUI thread. */
public final class ExamSyncHandler extends SyncHandler<ExamSyncHandler> {

    public static final String SYNC_ID = "gtnhextranerf.exam";
    public static final int PAPER = 0;
    public static final int RESULT = 1;
    public static final int SUBMIT = 2;
    public static final int PAY = 3;
    public static final int ADMISSION = 4;

    public enum Admission {
        OFFERED,
        INSUFFICIENT,
        ADMITTED,
        CERTIFIED,
        ALREADY_CERTIFIED,
        UNAVAILABLE
    }

    private final EntityPlayerMP serverPlayer;
    private final ProgressionTier tier;
    private final ExamDefinition definition;
    private ExamAttempt attempt;
    private final AtomicBoolean queued = new AtomicBoolean();
    private volatile Admission admission = Admission.OFFERED;
    private volatile ItemStack feeMaterial;
    private volatile int feeAmount;
    private volatile ExamPaper paper;
    private volatile ExamAttempt.Result result = new ExamAttempt.Result(ExamAttempt.Status.READY, 0, 0);
    private volatile boolean waiting;
    private int page = -1;
    private int[] choices = new int[0];

    public ExamSyncHandler() {
        this(ProgressionTier.LV);
    }

    public ExamSyncHandler(ProgressionTier tier) {
        allowC2S();
        this.tier = tier;
        definition = null;
        serverPlayer = null;
    }

    public ExamSyncHandler(EntityPlayerMP player, ExamDefinition definition) {
        this(player, definition.tier(), definition);
    }

    public ExamSyncHandler(EntityPlayerMP player, ProgressionTier tier, ExamDefinition definition) {
        allowC2S();
        this.tier = tier;
        this.definition = definition;
        serverPlayer = player;
        if (tier.hasExam() && (definition == null || definition.tier() != tier))
            throw new IllegalArgumentException("Missing or mismatched exam for " + tier);
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (!init || serverPlayer == null) return;
        if (MachineProgression.isCertified(serverPlayer.worldObj, tier)) admission = Admission.ALREADY_CERTIFIED;
        else if (tier.hasExam() && !definition.enabled()) admission = Admission.UNAVAILABLE;
        sendAdmission();
    }

    @Override
    public void readOnServer(int id, PacketBuffer buffer) throws IOException {
        if (serverPlayer == null || (id != SUBMIT && id != PAY)) return;
        int[] submitted = id == SUBMIT ? ExamProtocol.readChoices(buffer) : null;
        if (!queued.compareAndSet(false, true)) return;
        ProgressionServerTasks.enqueue(() -> {
            try {
                if (!isValid() || getSyncManager().getModularSyncManager()
                    .isClosed()
                    || serverPlayer.openContainer != getSyncManager().getModularSyncManager()
                        .getContainer())
                    return;
                if (id == PAY) admit();
                else if (attempt != null) {
                    result = attempt.submit(serverPlayer.getUniqueID(), submitted);
                    sendResult();
                } else sendAdmission();
            } finally {
                queued.set(false);
            }
        });
    }

    private void admit() {
        // All admission and inventory work runs in the server tick, including requests from different players.
        if (admission == Admission.ADMITTED || admission == Admission.CERTIFIED) {
            sendAdmission();
            return;
        }
        if (MachineProgression.isCertified(serverPlayer.worldObj, tier)) admission = Admission.ALREADY_CERTIFIED;
        else if (tier.hasExam() && !definition.enabled()) admission = Admission.UNAVAILABLE;
        else if (!CertificationCost.take(serverPlayer, tier)) admission = Admission.INSUFFICIENT;
        else if (!tier.hasExam()) {
            MachineProgression.certify(serverPlayer.worldObj, tier);
            admission = Admission.CERTIFIED;
        } else {
            attempt = new ExamAttempt(serverPlayer.getUniqueID(), definition, () -> {
                ItemStack reward = ProgressionCertificates.issue(serverPlayer.worldObj, tier);
                if (!serverPlayer.inventory.addItemStackToInventory(reward)) serverPlayer.entityDropItem(reward, 0);
                serverPlayer.inventoryContainer.detectAndSendChanges();
            });
            paper = definition.paper();
            admission = Admission.ADMITTED;
            syncToClient(PAPER, buffer -> ExamProtocol.writePaper(buffer, paper));
        }
        sendAdmission();
    }

    private void sendAdmission() {
        Admission snapshot = admission;
        ItemStack material = CertificationCost.material(tier);
        int amount = CertificationCost.amount(tier);
        syncToClient(ADMISSION, buffer -> {
            buffer.writeByte(snapshot.ordinal());
            buffer.writeItemStackToBuffer(material);
            buffer.writeVarIntToBuffer(amount);
        });
    }

    private void sendResult() {
        ExamAttempt.Result snapshot = result;
        syncToClient(RESULT, buffer -> {
            buffer.writeByte(
                snapshot.status()
                    .ordinal());
            buffer.writeByte(snapshot.correct());
            buffer.writeByte(snapshot.total());
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readOnClient(int id, PacketBuffer buffer) throws IOException {
        if (id == ADMISSION) {
            int status = buffer.readUnsignedByte();
            ItemStack material = buffer.readItemStackFromBuffer();
            int amount = buffer.readVarIntFromBuffer();
            if (status >= Admission.values().length || material == null || amount <= 0 || amount > 2304)
                throw new IOException("Invalid certification fee");
            feeMaterial = material;
            feeAmount = amount;
            admission = Admission.values()[status];
            waiting = false;
        } else if (id == PAPER) paper = ExamProtocol.readPaper(buffer);
        else if (id == RESULT) {
            int status = buffer.readUnsignedByte(), correct = buffer.readUnsignedByte(),
                total = buffer.readUnsignedByte();
            if (status >= ExamAttempt.Status.values().length || total > ExamDefinition.MAX_QUESTIONS || correct > total)
                throw new IOException("Invalid exam result");
            result = new ExamAttempt.Result(ExamAttempt.Status.values()[status], correct, total);
            waiting = false;
        }
    }

    public ExamPaper paper() {
        return paper;
    }

    public ExamAttempt.Result result() {
        return result;
    }

    public int page() {
        return page;
    }

    public void changePage(int delta) {
        if (finished()) return;
        ExamPaper snapshot = paper;
        if (snapshot != null) page = Math.max(
            -1,
            Math.min(
                snapshot.questions()
                    .size() - 1,
                page + delta));
    }

    public void choose(int question, int mask) {
        ExamPaper snapshot = paper;
        if (snapshot == null) return;
        if (choices.length != snapshot.questions()
            .size())
            choices = new int[snapshot.questions()
                .size()];
        choices[question] = mask;
    }

    public int answeredCount() {
        int count = 0;
        for (int choice : choices) if (Integer.bitCount(choice) == 1) count++;
        return count;
    }

    @SideOnly(Side.CLIENT)
    public boolean submit() {
        ExamPaper snapshot = paper;
        if (!admitted() || snapshot == null || waiting || finished()) return false;
        if (choices.length != snapshot.questions()
            .size())
            choices = new int[snapshot.questions()
                .size()];
        int[] submission = choices.clone();
        waiting = true;
        syncToServer(SUBMIT, buffer -> ExamProtocol.writeChoices(buffer, submission));
        return true;
    }

    public boolean waiting() {
        return waiting;
    }

    public boolean admitted() {
        return admission == Admission.ADMITTED;
    }

    public Admission admission() {
        return admission;
    }

    public boolean canPay() {
        return feeMaterial != null && !waiting
            && (admission == Admission.OFFERED || admission == Admission.INSUFFICIENT);
    }

    @SideOnly(Side.CLIENT)
    public boolean pay() {
        if (!canPay()) return false;
        waiting = true;
        syncToServer(PAY, buffer -> {});
        return true;
    }

    @SideOnly(Side.CLIENT)
    public String paymentText() {
        ItemStack material = feeMaterial;
        if (material == null) return StatCollector.translateToLocal("gtnhextranerf.exam.loading");
        String cost = StatCollector
            .translateToLocalFormatted("gtnhextranerf.certification.cost", feeAmount, material.getDisplayName());
        String tierName = tier.id()
            .toUpperCase(java.util.Locale.ROOT);
        return cost + "\n\n"
            + StatCollector.translateToLocalFormatted(
                "gtnhextranerf.certification." + (tier.hasExam() ? "exam_fee" : "payment_only"),
                tierName)
            + "\n\n"
            + StatCollector.translateToLocalFormatted(
                "gtnhextranerf.certification.admission." + admission.name()
                    .toLowerCase(java.util.Locale.ROOT),
                tierName);
    }

    public boolean finished() {
        ExamAttempt.Status status = result.status();
        return status == ExamAttempt.Status.PASSED || status == ExamAttempt.Status.FAILED;
    }
}
