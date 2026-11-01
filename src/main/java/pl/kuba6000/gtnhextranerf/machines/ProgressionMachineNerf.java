package pl.kuba6000.gtnhextranerf.machines;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidStack;

import bartworks.common.tileentities.multis.MTEManualTrafo;
import bartworks.common.tileentities.tiered.MTEEnergyDistributor;
import bartworks.system.material.WerkstoffLoader;
import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import bwcrossmod.galacticgreg.VoidMinerUtility;
import ggfab.mte.MTEAdvAssLine;
import goodgenerator.blocks.tileEntity.MTECoolantTower;
import goodgenerator.blocks.tileEntity.base.MTELargeFusionComputer;
import goodgenerator.loader.Loaders;
import goodgenerator.util.ItemRefer;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.MetaTileEntityIDs;
import gregtech.api.enums.Mods;
import gregtech.api.interfaces.ISecondaryDescribable;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTETransformer;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.api.util.GTLanguageManager;
import gregtech.common.tileentities.generators.MTELightningRod;
import gregtech.common.tileentities.machines.basic.MTEMassfabricator;
import gregtech.common.tileentities.machines.multi.MTEAssemblyLine;
import gregtech.common.tileentities.machines.multi.MTEFusionComputer;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;
import tectech.thing.metaTileEntity.single.MTEBuckConverter;
import toxiceverglades.dimension.DimensionEverglades;
import toxiceverglades.world.WorldProviderMod;

/** Startup verification of targeted native machine changes. */
public final class ProgressionMachineNerf {

    public static final int OZONE_LIMIT = 1_000_000;
    public static final long LIGHTNING_ROD_BUFFER = 10_000_000L;
    public static final String PLASMA_COIL_ERROR_KEY = "gtnhextranerf.structure_error.stinky_coils";
    public static final String SPATIAL_ASSEMBLY_ERROR_KEY = "gtnhextranerf.structure_error.unfluix_world";
    public static final RecipeMap<RecipeMapBackend> COOLANT_RECIPES = RecipeMapBuilder
        .of("gtnhextranerf.recipe.coolant_tower")
        .maxIO(0, 1, 1, 2)
        .minInputs(0, 1)
        .neiHandlerInfo(info -> info.setDisplayStack(ItemRefer.Coolant_Tower.get(1)))
        .build();

    public interface CoolantTowerHook {
    }

    public interface MassFabricatorHook {
    }

    public interface EvergladesSafetyHook {
    }

    public interface TransformerPortHook {
    }

    public interface VoidMinerFluidHook {
    }

    public interface LightningRodHook {
    }

    public interface SpatialAssemblyHook {
    }

    public static void registerRecipeMaps() {
        // Create the map before GT/NEI enumerate categories and controller catalysts.
        GTLanguageManager.addStringLocalization(COOLANT_RECIPES.unlocalizedName, "Coolant Tower");
    }

    public interface PlasmaCoilHook {
    }

    public interface OzoneSafetyHook {
    }

    public interface OzoneExplosionHook {
    }

    private ProgressionMachineNerf() {}

    public static void verifySpatialAssemblyLines() {
        AuditManager.run(AuditScope.VERIFY_ASSEMBLY_LINE_SPATIAL_DIMENSION, audit -> {
            check(
                SpatialAssemblyHook.class.isAssignableFrom(MTEAssemblyLine.class)
                    && SpatialAssemblyHook.class.isAssignableFrom(MTEAdvAssLine.class),
                "Assembly Line Spatial hooks are missing");
            boolean normal = false, advanced = false;
            for (IMetaTileEntity prototype : GregTechAPI.METATILEENTITIES) {
                if (!(prototype instanceof MTEAssemblyLine) && !(prototype instanceof MTEAdvAssLine)) continue;
                check(prototype instanceof SpatialAssemblyHook, "Registered Assembly Line lacks its Spatial hook");
                normal |= prototype instanceof MTEAssemblyLine;
                advanced |= prototype instanceof MTEAdvAssLine;
            }
            check(normal && advanced, "Normal or Advanced Assembly Line registration is missing");
            verifyGuiText(SPATIAL_ASSEMBLY_ERROR_KEY);
            audit.success();
        });
    }

    public static void verifyLightningRods() {
        AuditManager.run(AuditScope.VERIFY_LIGHTNING_RODS, audit -> {
            check(
                LightningRodHook.class.isAssignableFrom(MTELightningRod.class),
                "Lightning Rod generation hook is missing");
            int rods = 0;
            for (IMetaTileEntity prototype : GregTechAPI.METATILEENTITIES) {
                if (!(prototype instanceof MTELightningRod rod)) continue;
                check(rod instanceof LightningRodHook, "Registered Lightning Rod is missing the generation hook");
                check(rod.maxEUStore() == LIGHTNING_ROD_BUFFER, "Lightning Rod storage was not reduced");
                check(
                    rod.maxAmperesOut() == 512 && rod.maxEUOutput() == GTValues.V[rod.mTier],
                    "Lightning Rod output rating changed");
                rods++;
            }
            check(rods == 3, "Expected three native Lightning Rod registrations");
            audit.success();
        });
    }

    public static void verifyVoidMinerFluids() {
        AuditManager.run(AuditScope.VERIFY_VOID_MINER_GAS_USAGE, audit -> {
            check(
                VoidMinerFluidHook.class.isAssignableFrom(MTEVoidMinerBase.class),
                "Void Miner gas consumption hook is missing");
            int miners = 0;
            for (IMetaTileEntity prototype : GregTechAPI.METATILEENTITIES) {
                if (!(prototype instanceof MTEVoidMinerBase<?>)) continue;
                check(prototype instanceof VoidMinerFluidHook, "Registered Void Miner lacks its gas consumption hook");
                miners++;
            }
            check(miners >= 3, "Native Void Miner registrations are missing");
            FluidStack[] expected = { WerkstoffLoader.Neon.getFluidOrGas(1), WerkstoffLoader.Krypton.getFluidOrGas(1),
                WerkstoffLoader.Xenon.getFluidOrGas(1), WerkstoffLoader.Oganesson.getFluidOrGas(1) };
            check(VoidMinerUtility.NOBLE_GASSES.length == expected.length, "Native Void Miner gas list changed");
            for (int gas = 0; gas < expected.length; gas++) check(
                expected[gas].isFluidEqual(VoidMinerUtility.NOBLE_GASSES[gas]),
                "Native Void Miner gas order changed");
            audit.success();
        });
    }

    public static boolean isDisabledTransformer(IMetaTileEntity machine) {
        return machine instanceof MTEBuckConverter
            || machine instanceof MTETransformer && !(machine instanceof MTEEnergyDistributor);
    }

    public static void verifyTransformerPorts() {
        AuditManager.run(AuditScope.VERIFY_SINGLEBLOCK_TRANSFORMER_PORTS, audit -> {
            check(
                TransformerPortHook.class.isAssignableFrom(BaseMetaTileEntity.class),
                "Singleblock transformer port hook is missing");
            int transformers = 0, converters = 0;
            for (IMetaTileEntity prototype : GregTechAPI.METATILEENTITIES) {
                if (!isDisabledTransformer(prototype)) continue;
                if (prototype instanceof MTEBuckConverter) converters++;
                else transformers++;
            }
            check(transformers > 0 && converters > 0, "Native transformer or buck-converter registrations are missing");
            IMetaTileEntity manual = GregTechAPI.METATILEENTITIES[MetaTileEntityIDs.ManualTrafo.ID];
            check(
                manual instanceof MTEManualTrafo && !isDisabledTransformer(manual),
                "Manual Transformer is missing or included in the singleblock ban");
            audit.success();
        });
    }

    public static void verifyEverglades() {
        AuditManager.run(AuditScope.VERIFY_EVERGLADES_MACHINE_BAN, audit -> {
            check(
                EvergladesSafetyHook.class.isAssignableFrom(BaseMetaTileEntity.class),
                "Everglades machine tick hook is missing");
            if (Mods.GTPlusPlusEverglades.isModLoaded()) {
                check(
                    DimensionManager.isDimensionRegistered(DimensionEverglades.DIMID),
                    "Everglades dimension is not registered");
                check(
                    DimensionManager.createProviderFor(DimensionEverglades.DIMID) instanceof WorldProviderMod,
                    "Everglades runtime dimension does not use its native provider");
            }
            audit.success();
        });
    }

    public static void verifyMassFabricators() {
        AuditManager.run(AuditScope.VERIFY_MASS_FABRICATOR_AMPLIFIER, audit -> {
            check(MTEMassfabricator.sUUAperUUM > 0, "Native amplifier consumption must be positive");
            int found = 0;
            for (IMetaTileEntity prototype : GregTechAPI.METATILEENTITIES) {
                if (!(prototype instanceof MTEMassfabricator)) continue;
                found++;
                check(prototype instanceof MassFabricatorHook, "Mass Fabricator amplifier guard is missing");
            }
            check(found > 0, "No native Mass Fabricator prototypes registered");
            audit.success();
        });
    }

    public static void verifyCoolantTower() {
        AuditManager.run(AuditScope.VERIFY_COOLANT_TOWER, audit -> {
            IMetaTileEntity prototype = prototype(ItemRefer.Coolant_Tower.get(1));
            check(prototype instanceof CoolantTowerHook, "Coolant Tower processing hook is missing");
            check(
                ((MTECoolantTower) prototype).getRecipeMap() == COOLANT_RECIPES,
                "Coolant Tower recipe map is missing");
            MTECoolantTower tower = (MTECoolantTower) prototype;
            check(
                tower.protectsExcessItem() && tower.protectsExcessFluid(),
                "Mapped cooling can void required outputs");
            check(
                COOLANT_RECIPES.getFrontend()
                    .getNEIProperties().registerNEI,
                "Coolant Tower NEI registration is disabled");
            audit.success();
        });
    }

    public static void verifyPlasmaForge() {
        AuditManager.run(AuditScope.VERIFY_PLASMA_FORGE_COILS, audit -> {
            check(
                prototype(ItemList.Machine_Multi_PlasmaForge.get(1)) instanceof PlasmaCoilHook,
                "Plasma Forge coil gate hook is missing");
            // Native errors send the translation key to the client; don't invoke client-only translate/render APIs.
            InputStream resource = ProgressionMachineNerf.class
                .getResourceAsStream("/assets/gtnhextranerf/lang/en_US.lang");
            check(resource != null, "Missing default controller error localization");
            try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                Properties text = new Properties();
                text.load(reader);
                check(text.getProperty(PLASMA_COIL_ERROR_KEY) != null, "Missing Plasma Forge coil error text");
            } catch (IOException failure) {
                throw new RecipeValidationException("Could not load controller error localization", failure);
            }
            audit.success();
        });
    }

    public static void verifyOzone() {
        AuditManager.run(AuditScope.VERIFY_OZONE_SAFETY, audit -> {
            IMetaTileEntity machine = prototype(ItemList.Machine_Multi_PurificationUnitOzonation.get(1));
            check(machine instanceof OzoneSafetyHook, "Ozonator safety hook is missing");
            check(machine instanceof OzoneExplosionHook, "Ozonator explosion safety hook is missing");
            // getDescription() polls the client's Shift key. The primary page is safe on a dedicated server.
            String tooltip = String.join("\n", ((ISecondaryDescribable) machine).getPrimaryDescription());
            check(tooltip.contains(formatNumber(OZONE_LIMIT)), "Ozonator tooltip still has its old safety limit");
            check(!tooltip.contains("every doubling of"), "Ozonator tooltip still promises exact doubling");
            audit.success();
        });
    }

    public static void verifyFusion() {
        AuditManager.run(AuditScope.VERIFY_LEGACY_FUSION_COILS, audit -> {
            check(GregTechAPI.sBlockCasingsBEC != null, "Missing BEC casing block");
            ItemStack[] controllers = { ItemList.FusionComputer_ZPMV.get(1), ItemList.FusionComputer_UV.get(1),
                GregtechItemList.FusionComputer_UV2.get(1), GregtechItemList.FusionComputer_UV3.get(1) };
            ItemStack[] casings = { ItemList.Casing_Fusion.get(1), ItemList.Casing_Fusion2.get(1),
                GregtechItemList.Casing_Fusion_External.get(1), GregtechItemList.Casing_Fusion_External2.get(1) };
            for (int i = 0; i < controllers.length; i++) {
                IMetaTileEntity prototype = prototype(controllers[i]);
                check(prototype instanceof MTEFusionComputer, "Missing legacy fusion prototype");
                MTEFusionComputer fusion = (MTEFusionComputer) prototype;
                check(
                    fusion.getFusionCoil() == GregTechAPI.sBlockCasingsBEC && fusion.getFusionCoilMeta() == 1,
                    "Legacy fusion MK" + (i + 2) + " did not acquire BEC isolation coils");
                String coilName = new ItemStack(GregTechAPI.sBlockCasingsBEC, 1, 1).getDisplayName();
                check(
                    String.join("\n", fusion.getSecondaryDescription())
                        .contains(coilName),
                    "Legacy fusion structure tooltip does not name its actual coil block");
                check(
                    fusion.getCasing() == Block.getBlockFromItem(casings[i].getItem())
                        && fusion.getCasingMeta() == casings[i].getItemDamage(),
                    "Legacy fusion MK" + (i + 2) + " ordinary casing changed");
            }
            MTEFusionComputer mk1 = (MTEFusionComputer) prototype(ItemList.FusionComputer_LuV.get(1));
            check(
                mk1.getFusionCoil() == GregTechAPI.sBlockCasings1 && mk1.getFusionCoilMeta() == 15,
                "MK1 fusion coil changed");
            ItemStack[] compact = { ItemRefer.Compact_Fusion_MK1.get(1), ItemRefer.Compact_Fusion_MK2.get(1),
                ItemRefer.Compact_Fusion_MK3.get(1), ItemRefer.Compact_Fusion_MK4.get(1),
                ItemRefer.Compact_Fusion_MK5.get(1) };
            for (int i = 0; i < compact.length; i++) {
                IMetaTileEntity prototype = prototype(compact[i]);
                check(prototype instanceof MTELargeFusionComputer, "Missing Compact fusion prototype");
                MTELargeFusionComputer fusion = (MTELargeFusionComputer) prototype;
                check(
                    fusion.getCoilBlock() == Loaders.compactFusionCoil && fusion.getCoilMeta() == i,
                    "Compact fusion MK" + (i + 1) + " coil changed");
            }
            audit.success();
        });
    }

    private static IMetaTileEntity prototype(ItemStack controller) {
        if (controller == null || controller.getItemDamage() < 0
            || controller.getItemDamage() >= GregTechAPI.METATILEENTITIES.length)
            throw new RecipeValidationException("Invalid machine controller registry entry");
        IMetaTileEntity prototype = GregTechAPI.METATILEENTITIES[controller.getItemDamage()];
        if (prototype == null) throw new RecipeValidationException("Missing machine prototype");
        return prototype;
    }

    private static void verifyGuiText(String key) {
        Properties text = new Properties();
        try (InputStream source = ProgressionMachineNerf.class
            .getResourceAsStream("/assets/gtnhextranerf/lang/en_US.lang")) {
            check(source != null, "Missing machine error localization");
            text.load(new InputStreamReader(source, StandardCharsets.UTF_8));
            String value = text.getProperty(key);
            check(
                value != null && !value.trim()
                    .isEmpty(),
                "Missing machine GUI error text: " + key);
        } catch (IOException failure) {
            throw new RecipeValidationException("Cannot read machine GUI error localization: " + key, failure);
        }
    }

    private static void check(boolean valid, String message) {
        if (!valid) throw new RecipeValidationException(message);
    }
}
