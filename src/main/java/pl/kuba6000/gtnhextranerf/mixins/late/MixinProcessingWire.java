package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IRecipeMap;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.loaders.oreprocessing.ProcessingWire;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.mixins.support.ProcessingWireRecipeTransformer;

@Mixin(value = ProcessingWire.class, remap = false)
public class MixinProcessingWire {

    @ModifyReceiver(
        method = "registerOre",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTRecipeBuilder;addTo(Lgregtech/api/interfaces/IRecipeMap;)Ljava/util/Collection;"),
        require = 1)
    private GTRecipeBuilder gtnhextranerf$addPpsToCableRecipe(GTRecipeBuilder builder, IRecipeMap map,
        OrePrefixes prefix, Materials material, String oreDictName, String modName, ItemStack wire) {
        return AuditManager.auditResult(
            AuditScope.ADD_PPS_FOIL_TO_CABLE_MACHINE_CRAFTING,
            auditor -> ProcessingWireRecipeTransformer.transform(builder, map, prefix, material, auditor));
    }
}
