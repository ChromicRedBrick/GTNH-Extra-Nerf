package pl.kuba6000.gtnhextranerf.integration;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.me.GridNode;
import appeng.parts.AEBasePart;
import appeng.parts.networking.PartDenseCable;
import appeng.parts.p2p.PartP2PTunnelME;
import appeng.tile.networking.TileCableBus;

/** Real part construction and external network nodes; no blocks are placed in the test world. */
final class P2PChannelIntegrationTests {

    private P2PChannelIntegrationTests() {}

    static void verify(World world) throws Exception {
        List<AEBasePart> parts = new ArrayList<>();
        try {
            PartP2PTunnelME input = tunnel(world, parts);
            PartP2PTunnelME firstOutput = tunnel(world, parts);
            PartP2PTunnelME secondOutput = tunnel(world, parts);
            GridNode sharedInput = (GridNode) input.getExternalFacingNode();
            checkCapacity(sharedInput, 8, "ME P2P input");
            AEApi.instance()
                .createGridConnection(sharedInput, firstOutput.getExternalFacingNode());
            AEApi.instance()
                .createGridConnection(sharedInput, secondOutput.getExternalFacingNode());
            checkCapacity(sharedInput, 8, "shared input with multiple output branches");
            checkCapacity((GridNode) firstOutput.getExternalFacingNode(), 8, "first output");
            checkCapacity((GridNode) secondOutput.getExternalFacingNode(), 8, "second output");

            PartDenseCable dense = new PartDenseCable(
                AEApi.instance()
                    .definitions()
                    .parts()
                    .cableDense()
                    .stack(AEColor.Transparent, 1));
            attach(dense, world, parts);
            checkCapacity((GridNode) dense.getGridNode(ForgeDirection.UNKNOWN), 32, "Dense Cable");
        } finally {
            for (int i = parts.size() - 1; i >= 0; i--) parts.get(i)
                .removeFromWorld();
        }
    }

    private static PartP2PTunnelME tunnel(World world, List<AEBasePart> parts) {
        PartP2PTunnelME tunnel = new PartP2PTunnelME(
            AEApi.instance()
                .definitions()
                .parts()
                .p2PTunnelME()
                .maybeStack(1)
                .get());
        attach(tunnel, world, parts);
        return tunnel;
    }

    private static void attach(AEBasePart part, World world, List<AEBasePart> parts) {
        TileCableBus host = new TileCableBus();
        host.setWorldObj(world);
        host.xCoord = parts.size() * 4;
        host.yCoord = 300;
        part.setPartHostInfo(ForgeDirection.UP, host.getCableBus(), host);
        parts.add(part);
        part.addToWorld();
    }

    private static void checkCapacity(GridNode node, int expected, String detail) {
        if (node == null || node.getMaxChannels() != expected) throw new AssertionError(
            detail + " should support "
                + expected
                + " channels, got "
                + (node == null ? "no node" : node.getMaxChannels()));
    }
}
