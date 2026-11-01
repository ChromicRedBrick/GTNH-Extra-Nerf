package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import bwcrossmod.galacticgreg.VoidMinerUtility;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;

/** Change only gas admission/payment; native selection, boosts and unboosted fallback remain intact. */
@Mixin(value = MTEVoidMinerBase.class, remap = false)
public abstract class MixinVoidMinerFluids implements ProgressionMachineNerf.VoidMinerFluidHook {

    // Columns follow GT's noble-gas array: Neon, Krypton, Xenon, Oganesson.
    @Unique
    private static final int[][] gtnhextranerf$gasCosts = { { 10, 1_000, 100_000, 10_000_000 },
        { 100, 10, 1_000, 100_000 }, { 1_000, 100, 10, 10 } };

    @Shadow
    @Final
    protected byte TIER_MULTIPLIER;

    @ModifyExpressionValue(
        method = "consumeNobleGas",
        at = @At(value = "FIELD", target = "Lbwcrossmod/galacticgreg/MTEVoidMinerBase;batchMultiplier:I"),
        require = 2,
        allow = 2)
    private int gtnhextranerf$gasCost(int original, FluidStack gasToConsume) {
        // Both the availability check and deduction must use the same expanded cost.
        for (int gas = 0; gas < VoidMinerUtility.NOBLE_GASSES.length; gas++) {
            if (gasToConsume.isFluidEqual(VoidMinerUtility.NOBLE_GASSES[gas]))
                return Math.multiplyExact(original, gtnhextranerf$gasCosts[TIER_MULTIPLIER - 1][gas]);
        }
        return original;
    }
}
