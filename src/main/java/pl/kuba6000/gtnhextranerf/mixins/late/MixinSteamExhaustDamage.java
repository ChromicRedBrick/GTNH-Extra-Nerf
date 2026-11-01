package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.metatileentity.implementations.MTEBasicMachineBronze;
import pl.kuba6000.gtnhextranerf.progression.SteamSafety;

@Mixin(value = MTEBasicMachineBronze.class, remap = false)
public abstract class MixinSteamExhaustDamage implements SteamSafety.ExhaustDamageHook {

    @WrapOperation(
        method = "allowToCheckRecipe",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTUtility;applyHeatDamage(Lnet/minecraft/entity/EntityLivingBase;F)Z"),
        require = 1,
        allow = 1)
    private boolean gtnhextranerf$exhaust(EntityLivingBase victim, float damage, Operation<Boolean> original) {
        float health = victim.getHealth(), absorption = victim.getAbsorptionAmount();
        boolean accepted = original.call(victim, damage);
        SteamSafety.observeDamage(
            victim,
            health,
            absorption,
            accepted,
            SteamSafety.Incident.EXHAUST_DAMAGE,
            ((MTEBasicMachineBronze) (Object) this).getBaseMetaTileEntity());
        return accepted;
    }
}
