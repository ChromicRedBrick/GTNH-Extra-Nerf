package pl.kuba6000.gtnhextranerf.progression;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Shared server-tick revocation, persistence and notification for observed native accidents. */
final class CertificationIncidents {

    private CertificationIncidents() {}

    static void record(World world, ProgressionTier tier, String incident, int x, int y, int z) {
        if (world == null || world.isRemote) return;
        ProgressionServerTasks.enqueue(() -> {
            if (!MachineProgression.revoke(world, tier)) return;
            GTNHExtraNerf.LOG.warn(
                "{} certification revoked after {} in dimension {} at {},{},{}",
                tier.id(),
                incident,
                world.provider.dimensionId,
                x,
                y,
                z);
            ChatComponentTranslation message = new ChatComponentTranslation(
                "gtnhextranerf.progression." + tier.id() + ".revoked",
                new ChatComponentTranslation("gtnhextranerf.progression.incident." + incident));
            MinecraftServer server = MinecraftServer.getServer();
            if (server != null) for (Object player : server.getConfigurationManager().playerEntityList)
                ((EntityPlayerMP) player).addChatMessage(message.createCopy());
        });
    }
}
