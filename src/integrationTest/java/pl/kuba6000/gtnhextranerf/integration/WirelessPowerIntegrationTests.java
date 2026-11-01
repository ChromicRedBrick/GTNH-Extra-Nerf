package pl.kuba6000.gtnhextranerf.integration;

import java.util.ArrayList;
import java.util.List;

import appeng.api.config.PowerUnits;
import appeng.tile.networking.TileWirelessBase;
import appeng.tile.networking.TileWirelessConnector;
import appeng.tile.networking.TileWirelessHub;

/** Exercises the public power calculation against AE2's real proxy and configured energy units. */
final class WirelessPowerIntegrationTests {

    private WirelessPowerIntegrationTests() {}

    static void verify() {
        Connector first = new Connector(0, 0, 0);
        Connector second = new Connector(2, 0, 0);
        first.links.add(second);
        second.links.add(first);
        checkPair(first, second, 16);
        second.xCoord = 32;
        checkPair(first, second, 1_048_576);
        second.xCoord = 3;
        second.yCoord = 4;
        checkPair(first, second, 625);

        second.yCoord = 0;
        second.xCoord = 56;
        checkPair(first, second, 9_834_496);
        second.xCoord = 57;
        checkPair(first, second, 10_000_000);

        // Coordinate subtraction/squaring must not overflow integer arithmetic.
        first.xCoord = -30_000_000;
        second.xCoord = 30_000_000;
        second.yCoord = 0;
        checkPair(first, second, 10_000_000);
        first.xCoord = first.yCoord = first.zCoord = Integer.MIN_VALUE;
        second.xCoord = second.yCoord = second.zCoord = Integer.MAX_VALUE;
        checkPair(first, second, 10_000_000);

        first.links.clear();
        second.links.clear();
        checkPair(first, second, 0);

        Hub hub = new Hub();
        Connector near = new Connector(2, 0, 0);
        Connector far = new Connector(4, 0, 0);
        hub.links.add(near);
        hub.links.add(far);
        near.links.add(hub);
        far.links.add(hub);
        check(powerEu(hub), 136, "hub must sum its half of each link");
        check(powerEu(hub) + powerEu(near) + powerEu(far), 272, "hub network total");
        hub.links.remove(far);
        far.links.clear();
        check(powerEu(hub) + powerEu(near) + powerEu(far), 16, "removed hub link must stop drawing power");
    }

    private static void checkPair(TileWirelessBase first, TileWirelessBase second, double expected) {
        check(powerEu(first), expected / 2, "first endpoint");
        check(powerEu(second), expected / 2, "second endpoint");
    }

    private static double powerEu(TileWirelessBase tile) {
        tile.setConnectionsPowerDraw();
        return PowerUnits.AE.convertTo(PowerUnits.EU, tile.getPowerUsage());
    }

    private static void check(double actual, double expected, String detail) {
        if (!Double.isFinite(actual) || Math.abs(actual - expected) > Math.max(1e-9, expected * 1e-12))
            throw new AssertionError("Wireless power " + detail + ": expected " + expected + " EU/t, got " + actual);
    }

    private static final class Connector extends TileWirelessConnector {

        private final List<TileWirelessBase> links = new ArrayList<>();

        private Connector(int x, int y, int z) {
            xCoord = x;
            yCoord = y;
            zCoord = z;
        }

        @Override
        public List<TileWirelessBase> getConnectedTiles() {
            return links;
        }
    }

    private static final class Hub extends TileWirelessHub {

        private final List<TileWirelessBase> links = new ArrayList<>();

        @Override
        public List<TileWirelessBase> getConnectedTiles() {
            return links;
        }
    }
}
