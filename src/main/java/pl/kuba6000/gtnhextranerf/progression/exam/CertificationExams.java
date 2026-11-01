package pl.kuba6000.gtnhextranerf.progression.exam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;

import io.netty.buffer.Unpooled;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

/** Common registration. Clients display the server projection instead of using their local bank. */
public final class CertificationExams {

    public static final String FACTORY_ID = "gtnhextranerf.lv_exam";
    private static final Map<ProgressionTier, ExamDefinition> DEFINITIONS = new EnumMap<>(ProgressionTier.class);
    private static final Map<ProgressionTier, SimpleGuiFactory> FACTORIES = new EnumMap<>(ProgressionTier.class);
    private static RuntimeException loadFailure;

    private CertificationExams() {}

    public static void register(File configDirectory) {
        if (!FACTORIES.isEmpty()) throw new IllegalStateException("Certification exams already registered");
        for (ProgressionTier tier : ProgressionTier.values()) register(configDirectory, tier);
    }

    private static void register(File configDirectory, ProgressionTier tier) {
        if (!tier.hasExam()) {
            FACTORIES.put(tier, new SimpleGuiFactory(factoryId(tier), new ExamGui(tier, null)));
            return;
        }
        ExamDefinition definition;
        try (Reader reader = new InputStreamReader(template(tier), StandardCharsets.UTF_8)) {
            definition = ExamDefinition.read(reader, tier);
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read bundled " + tier.id() + " exam template", failure);
        }
        Path file = configDirectory.toPath()
            .resolve(GTNHExtraNerf.MODID)
            .resolve(tier.id() + "-exam.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                try (InputStream source = template(tier)) {
                    Files.copy(source, file);
                }
            }
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                definition = ExamDefinition.read(reader, tier);
            }
        } catch (IOException | RuntimeException failure) {
            RuntimeException invalid = new IllegalStateException("Invalid exam config: " + file, failure);
            if (loadFailure == null) loadFailure = invalid;
            else loadFailure.addSuppressed(invalid);
        }
        DEFINITIONS.put(tier, definition);
        FACTORIES.put(tier, new SimpleGuiFactory(factoryId(tier), new ExamGui(definition)));
    }

    private static InputStream template(ProgressionTier tier) {
        return Objects.requireNonNull(
            CertificationExams.class.getResourceAsStream("/assets/gtnhextranerf/exams/" + tier.id() + ".json"),
            "Missing " + tier.id() + " exam template");
    }

    public static ExamDefinition definition() {
        return definition(ProgressionTier.LV);
    }

    public static SimpleGuiFactory factory() {
        return factory(ProgressionTier.LV);
    }

    public static ExamDefinition definition(ProgressionTier tier) {
        return Objects.requireNonNull(DEFINITIONS.get(tier), "Exam has not been registered: " + tier);
    }

    public static SimpleGuiFactory factory(ProgressionTier tier) {
        return Objects.requireNonNull(FACTORIES.get(tier), "Exam has not been registered: " + tier);
    }

    private static String factoryId(ProgressionTier tier) {
        return GTNHExtraNerf.MODID + "." + tier.id() + "_exam";
    }

    public static void verify() {
        AuditManager.run(AuditScope.VERIFY_CERTIFICATION_EXAM, audit -> {
            if (loadFailure != null) throw loadFailure;
            for (ProgressionTier tier : ProgressionTier.values()) {
                if (GuiManager.getFactory(factoryId(tier)) != factory(tier))
                    throw new IllegalStateException(tier.id() + " exam GUI factory is missing");
                if (!tier.hasExam()) continue;
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                try {
                    ExamProtocol.writePaper(buffer, definition(tier).paper());
                    ExamProtocol.readPaper(buffer);
                } catch (IOException failure) {
                    throw new IllegalStateException(tier.id() + " exam cannot be synchronized", failure);
                } finally {
                    buffer.release();
                }
            }
            audit.success();
        });
    }
}
