package pl.kuba6000.gtnhextranerf.mixins.late;

import java.util.Arrays;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import goodgenerator.blocks.tileEntity.MTECoolantTower;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;

@Mixin(value = MTECoolantTower.class, remap = false)
public abstract class MixinCoolantTower extends TTMultiblockBase implements ProgressionMachineNerf.CoolantTowerHook {

    protected MixinCoolantTower(String name) {
        super(name);
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return ProgressionMachineNerf.COOLANT_RECIPES;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic();
    }

    @Override
    public boolean protectsExcessItem() {
        return true;
    }

    @Override
    public boolean protectsExcessFluid() {
        return true;
    }

    @Override
    protected long getPowerFlow() {
        // Normal GT ProcessingLogic stores long power in lEUt; TT's legacy default reads the int field instead.
        return lEUt;
    }

    @ModifyArg(
        method = "getStructure_EM",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/HatchElementBuilder;atLeast([Lgregtech/api/interfaces/IHatchElement;)Lgregtech/api/util/HatchElementBuilder;"),
        index = 0,
        require = 1)
    private IHatchElement<?>[] gtnhextranerf$allowMappedHatches(IHatchElement<?>[] original) {
        IHatchElement<?>[] hatches = Arrays.copyOf(original, original.length + 2);
        hatches[original.length] = gregtech.api.enums.HatchElement.Energy;
        hatches[original.length + 1] = gregtech.api.enums.HatchElement.OutputBus;
        return hatches;
    }

    @Inject(method = "checkProcessing_EM", at = @At("HEAD"), cancellable = true, require = 1)
    private void gtnhextranerf$processMappedCooling(CallbackInfoReturnable<CheckRecipeResult> callback) {
        if (getStoredFluids().stream()
            .anyMatch(ProgressionMachineNerf.COOLANT_RECIPES::containsInput)) {
            eAmpereFlow = 1;
            callback.setReturnValue(super.checkProcessing_EM());
        } else {
            // Original Steam condensation remains free, including after a powered recipe completes.
            lEUt = 0;
            mEUt = 0;
            eAmpereFlow = 1;
        }
    }

    @Inject(method = "onRunningTick", at = @At("HEAD"), cancellable = true, require = 1)
    private void gtnhextranerf$consumeMappedPower(ItemStack controller, CallbackInfoReturnable<Boolean> callback) {
        if (lEUt < 0) callback.setReturnValue(super.onRunningTick(controller));
    }
}
