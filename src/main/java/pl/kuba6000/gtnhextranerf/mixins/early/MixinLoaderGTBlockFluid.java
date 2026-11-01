package pl.kuba6000.gtnhextranerf.mixins.early;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.api.items.ItemBreederCell;
import gregtech.api.items.ItemRadioactiveCellIC;
import gregtech.loaders.preload.LoaderGTBlockFluid;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

@Mixin(value = LoaderGTBlockFluid.class, remap = false)
public class MixinLoaderGTBlockFluid {

    @Redirect(method = "run", at = @At(value = "NEW", target = "gregtech/api/items/ItemBreederCell"))
    private static ItemBreederCell redirectBreederCell(String aUnlocalized, String aEnglish, String aEnglishTooltip,
        int aHeatBonusStep, int aHeatBonusMultiplier, int aRequiredPulse, Supplier<ItemStack> aProduct) {
        return AuditManager.auditResult(AuditScope.SET_GLOWSTONE_BREEDER_REQUIRED_PULSE_TO_1000, unit -> {
            int pulse = aRequiredPulse;
            if ("rodGlowstone".equals(aUnlocalized)) {
                unit.recipeModified();
                pulse = 1000;
            }
            return new ItemBreederCell(
                aUnlocalized,
                aEnglish,
                aEnglishTooltip,
                aHeatBonusStep,
                aHeatBonusMultiplier,
                pulse,
                aProduct);
        });
    }

    @Redirect(method = "run", at = @At(value = "NEW", target = "gregtech/api/items/ItemRadioactiveCellIC"))
    private static ItemRadioactiveCellIC redirectRadioactiveCell(String aUnlocalized, String aEnglish, int aCellcount,
        int maxDamage, float aEnergy, int aRadiation, float aHeat, ItemStack aDepleted, boolean aMox,
        float aHeatBonus) {
        return AuditManager.auditResult(AuditScope.REDUCE_THORIUM_URANIUM_MOX_CELL_LIFETIME, unit -> {
            int dmg = maxDamage;
            if (aUnlocalized.contains("Thorium") || aUnlocalized.contains("Uranium")
                || aUnlocalized.contains("MOX")
                || aUnlocalized.contains("Mox")) {
                unit.recipeModified();
                dmg = maxDamage / 10;
            }
            return new ItemRadioactiveCellIC(
                aUnlocalized,
                aEnglish,
                aCellcount,
                dmg,
                aEnergy,
                aRadiation,
                aHeat,
                aDepleted,
                aMox,
                aHeatBonus);
        });
    }
}
