package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.tile.networking.TileWirelessBase;

@Mixin(value = TileWirelessBase.class, remap = false)
public abstract class MixinWirelessConnectionPower {

    @Unique
    private static final double MAX_LINK_EU_PER_TICK = 10_000_000;

    @Unique
    private static final double MAX_UNCAPPED_DISTANCE_SQUARED = Math.sqrt(MAX_LINK_EU_PER_TICK);

    @WrapMethod(method = "setConnectionsPowerDraw", require = 1)
    private void gtnhextranerf$setDistancePower(Operation<Void> original) {
        // Replace the pricing calculation entirely; running the original would restore AE2's old formula.
        TileWirelessBase self = (TileWirelessBase) (Object) this;
        double euPerTick = 0;
        for (TileWirelessBase other : self.getConnectedTiles()) {
            double dx = (double) self.xCoord - other.xCoord;
            double dy = (double) self.yCoord - other.yCoord;
            double dz = (double) self.zCoord - other.zCoord;
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            double linkEuPerTick = distanceSquared >= MAX_UNCAPPED_DISTANCE_SQUARED ? MAX_LINK_EU_PER_TICK
                : distanceSquared * distanceSquared;
            // Both endpoints charge their share; a hub accumulates its share of every link.
            euPerTick += linkEuPerTick / 2;
        }
        self.setPowerDraw(PowerMultiplier.CONFIG.divide(PowerUnits.EU.convertTo(PowerUnits.AE, euPerTick)));
    }
}
