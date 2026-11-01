package pl.kuba6000.gtnhextranerf.progression;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachineBronze;
import gregtech.api.metatileentity.implementations.MTEFluidPipe;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

/** Only an actual server-player injury from a pipe or Steam exhaust counts as a pressure incident. */
public final class SteamSafety {

    public interface PipeDamageHook {
    }

    public interface ExhaustDamageHook {
    }

    public enum Incident {

        PIPE_DAMAGE("pipe_damage"),
        EXHAUST_DAMAGE("exhaust_damage");

        private final String id;

        Incident(String id) {
            this.id = id;
        }
    }

    private SteamSafety() {}

    public static void observeDamage(EntityLivingBase victim, float healthBefore, float absorptionBefore,
        boolean accepted, Incident incident, IGregTechTileEntity source) {
        if (!accepted || !(victim instanceof EntityPlayerMP)
            || victim.worldObj.isRemote
            || source == null
            || source.getWorld() != victim.worldObj
            || (victim.getHealth() >= healthBefore && victim.getAbsorptionAmount() >= absorptionBefore)) return;
        CertificationIncidents.record(
            victim.worldObj,
            ProgressionTier.STEAM,
            incident.id,
            source.getXCoord(),
            source.getYCoord(),
            source.getZCoord());
    }

    public static void verify() {
        AuditManager.run(AuditScope.VERIFY_STEAM_SAFETY_HOOKS, audit -> {
            if (!PipeDamageHook.class.isAssignableFrom(MTEFluidPipe.class)
                || !ExhaustDamageHook.class.isAssignableFrom(MTEBasicMachineBronze.class))
                throw new IllegalStateException("Steam injury hooks are missing");
            audit.success();
        });
    }
}
