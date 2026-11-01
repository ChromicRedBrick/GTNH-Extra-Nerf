package pl.kuba6000.gtnhextranerf.progression;

import net.minecraft.world.World;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.BaseTileEntity;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

/** Native accident observations are queued; only the logical server tick mutates certification. */
public final class ElectricalSafety {

    public interface CableFireHook {
    }

    public interface MachineExplosionHook {
    }

    public enum Incident {

        CABLE_FIRE("cable_fire"),
        MACHINE_EXPLOSION("machine_explosion");

        private final String id;

        Incident(String id) {
            this.id = id;
        }
    }

    private ElectricalSafety() {}

    public static void record(World world, Incident incident, int x, int y, int z) {
        CertificationIncidents.record(world, ProgressionTier.LV, incident.id, x, y, z);
    }

    public static void verify() {
        AuditManager.run(AuditScope.VERIFY_ELECTRICAL_SAFETY_HOOKS, audit -> {
            if (!CableFireHook.class.isAssignableFrom(BaseTileEntity.class)
                || !MachineExplosionHook.class.isAssignableFrom(BaseMetaTileEntity.class))
                throw new IllegalStateException("Electrical accident hooks are missing");
            audit.success();
        });
    }
}
