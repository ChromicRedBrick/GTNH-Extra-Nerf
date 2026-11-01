package pl.kuba6000.gtnhextranerf.mixins.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.enums.SmallOres;
import gregtech.common.SmallOreBuilder;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

@Mixin(value = SmallOres.class, remap = false)
public class MixinSmallOres {

    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/common/SmallOreBuilder;amount(I)Lgregtech/common/SmallOreBuilder;"))
    private static SmallOreBuilder modifyNaquadahAmount(SmallOreBuilder builder, int original,
        Operation<SmallOreBuilder> operation) {
        return AuditManager.auditResult(AuditScope.SET_SMALL_NAQ_ORE_TO_1, unit -> {
            if ("ore.small.naquadah".equals(builder.smallOreName)) {
                unit.recipeModified();
                return operation.call(builder, 1);
            }
            return operation.call(builder, original);
        });
    }
}
