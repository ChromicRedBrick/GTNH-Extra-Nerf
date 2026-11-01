package pl.kuba6000.gtnhextranerf.progression;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

/** Stable item identities, shared by registration, activation and NEI. */
public final class ProgressionCertificates {

    private static final Map<ProgressionTier, ProgressionCertificateItem> ITEMS = new EnumMap<>(ProgressionTier.class);
    private static boolean registered;
    private static final String ISSUED_EPOCH = "gtnhextranerf_certificate_epoch";

    static {
        for (ProgressionTier tier : ProgressionTier.values()) ITEMS.put(tier, new ProgressionCertificateItem(tier));
    }

    private ProgressionCertificates() {}

    public static void register() {
        if (registered) throw new IllegalStateException("Progression certificates already registered");
        for (ProgressionCertificateItem item : ITEMS.values()) GameRegistry.registerItem(item, item.registryName());
        registered = true;
    }

    public static Collection<ProgressionCertificateItem> items() {
        return Collections.unmodifiableCollection(ITEMS.values());
    }

    public static ItemStack stack(ProgressionTier tier) {
        return new ItemStack(ITEMS.get(Objects.requireNonNull(tier)), 1);
    }

    public static ItemStack issue(World world, ProgressionTier tier) {
        ItemStack certificate = stack(tier);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong(ISSUED_EPOCH, MachineProgression.certificateEpoch(world, tier));
        certificate.setTagCompound(tag);
        return certificate;
    }

    public static long issuedEpoch(ItemStack certificate) {
        return certificate.hasTagCompound() ? certificate.getTagCompound()
            .getLong(ISSUED_EPOCH) : 0;
    }

    public static ProgressionTier tierOf(ItemStack stack) {
        if (stack == null || stack.getItemDamage() != 0) return null;
        for (Map.Entry<ProgressionTier, ProgressionCertificateItem> entry : ITEMS.entrySet()) {
            if (stack.getItem() == entry.getValue()) return entry.getKey();
        }
        return null;
    }

    public static void verify() {
        AuditManager.run(AuditScope.VERIFY_PROGRESSION_CERTIFICATES, audit -> {
            if (!registered || !ProgressionMachine.class.isAssignableFrom(MTEBasicMachine.class))
                throw new IllegalStateException("Progression items or machine hooks are missing");
            for (ProgressionCertificateItem item : ITEMS.values()) {
                if (GameRegistry.findItem(GTNHExtraNerf.MODID, item.registryName()) != item)
                    throw new IllegalStateException("Missing certificate: " + item.registryName());
            }
            audit.success();
        });
    }
}
