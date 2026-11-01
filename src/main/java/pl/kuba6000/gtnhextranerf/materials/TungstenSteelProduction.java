package pl.kuba6000.gtnhextranerf.materials;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.audit.AuditDefinition;
import pl.kuba6000.gtnhextranerf.audit.AuditExpectation;
import pl.kuba6000.gtnhextranerf.audit.AuditReport;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.audit.AuditSession;
import pl.kuba6000.gtnhextranerf.recipes.RecipeContext;
import pl.kuba6000.gtnhextranerf.recipes.RecipeModule;
import pl.kuba6000.gtnhextranerf.recipes.RecipeRegistry;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;
import pl.kuba6000.gtnhextranerf.recipes.machine.MachineRecipeStore;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeChange;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeMatch;
import tectech.recipe.TecTechRecipeMaps;

/** Fresh alloy production is raw; finished-material recipes remain usable for recycling old stock. */
public final class TungstenSteelProduction implements RecipeModule {

    private static final int BLAST_VARIANTS = 9;
    private static final AuditDefinition MOLTEN_AUDIT = new AuditDefinition() {

        @Override
        public String id() {
            return "VERIFY_TUNGSTEN_STEEL_FOTG";
        }

        @Override
        public AuditExpectation expectation() {
            return AuditExpectation.exact()
                .build();
        }
    };

    @Override
    public void register(RecipeRegistry registry) {
        registry.register(AuditScope.REROUTE_TUNGSTEN_STEEL_PRODUCTION, TungstenSteelProduction::reroute);
    }

    private static void reroute(RecipeContext recipes) {
        ItemStack finishedDust = item(OrePrefixes.dust, Materials.TungstenSteel, 1);
        ItemStack rawDust = item(OrePrefixes.dust, NerfMaterials.rawTungstenSteel(), 1);
        ItemStack rawHot = item(OrePrefixes.ingotHot, NerfMaterials.rawTungstenSteel(), 1);
        for (RecipeMap<?> mixer : new RecipeMap<?>[] { RecipeMaps.mixerRecipes, RecipeMaps.mixerNonCellRecipes }) {
            recipes.machine(mixer)
                .modifyOne(
                    RecipeMatch.builder()
                        .itemInputs(
                            item(OrePrefixes.dust, Materials.Steel, 1),
                            item(OrePrefixes.dust, Materials.Tungsten, 1),
                            GTUtility.getIntegratedCircuit(1))
                        .itemOutputs(item(OrePrefixes.dust, Materials.TungstenSteel, 2))
                        .fluidInputs()
                        .fluidOutputs()
                        .build(),
                    RecipeChange.builder()
                        .itemOutputs(item(OrePrefixes.dust, NerfMaterials.rawTungstenSteel(), 2))
                        .build());
        }
        for (ItemStack[] inputs : quantumInputs()) {
            recipes.machine(quantumMap())
                .modifyOne(
                    RecipeMatch.builder()
                        .itemInputs(inputs)
                        .build(),
                    RecipeChange.builder()
                        .replaceItemOutput(
                            item(OrePrefixes.dust, Materials.TungstenSteel, 64),
                            item(OrePrefixes.dust, NerfMaterials.rawTungstenSteel(), 64))
                        .build());
        }
        RecipeMatch original = RecipeMatch.builder()
            .itemOutputs(item(OrePrefixes.ingotHot, Materials.TungstenSteel, 1))
            .build();
        recipes.edit(new MachineRecipeStore(RecipeMaps.blastFurnaceRecipes))
            .expectExactly(original, BLAST_VARIANTS);
        for (GTRecipe recipe : RecipeMaps.blastFurnaceRecipes.getAllRecipes()) {
            if (!original.test(recipe)) continue;
            ItemStack[] inputs = Arrays.stream(recipe.mInputs)
                .map(stack -> stack.isItemEqual(finishedDust) ? rawDust.copy() : stack.copy())
                .toArray(ItemStack[]::new);
            recipes.machine(RecipeMaps.blastFurnaceRecipes)
                .addCopyOne(
                    recipe,
                    RecipeChange.builder()
                        .replaceItemInput(finishedDust, rawDust)
                        .itemOutputs(rawHot)
                        .build(),
                    RecipeMatch.builder()
                        .itemInputs(inputs)
                        .itemOutputs(rawHot)
                        .fluidInputs(recipe.mFluidInputs)
                        .fluidOutputs(recipe.mFluidOutputs)
                        .build());
        }
    }

    public static void verifyPublished() {
        RecipeMatch mixed = RecipeMatch.builder()
            .itemInputs(
                item(OrePrefixes.dust, Materials.Steel, 1),
                item(OrePrefixes.dust, Materials.Tungsten, 1),
                GTUtility.getIntegratedCircuit(1))
            .itemOutputs(item(OrePrefixes.dust, NerfMaterials.rawTungstenSteel(), 2))
            .build();
        for (RecipeMap<?> mixer : new RecipeMap<?>[] { RecipeMaps.mixerRecipes, RecipeMaps.mixerNonCellRecipes }) {
            if (mixer.getAllRecipes()
                .stream()
                .filter(mixed)
                .count() != 1)
                throw new RecipeValidationException(
                    "Tungstensteel primary production is not routed through raw material in " + mixer.unlocalizedName);
        }
        if (rawBlastRecipes().size() != BLAST_VARIANTS)
            throw new RecipeValidationException("Raw Tungstensteel blast furnace variants are missing");
        ItemStack finishedDust = item(OrePrefixes.dust, Materials.TungstenSteel, 1);
        ItemStack rawDust = item(OrePrefixes.dust, NerfMaterials.rawTungstenSteel(), 1);
        for (ItemStack[] inputs : quantumInputs()) {
            RecipeMatch signature = RecipeMatch.builder()
                .itemInputs(inputs)
                .build();
            List<GTRecipe> matches = quantumMap().getAllRecipes()
                .stream()
                .filter(signature)
                .collect(Collectors.toList());
            if (matches.size() != 1 || has(matches.get(0).mOutputs, finishedDust)
                || !has(matches.get(0).mOutputs, rawDust))
                throw new RecipeValidationException("Quantum Force tungstensteel production bypasses Brainrotter");
        }
        ItemStack steel = item(OrePrefixes.dust, Materials.Steel, 1);
        ItemStack tungsten = item(OrePrefixes.dust, Materials.Tungsten, 1);
        for (RecipeMap<?> map : RecipeMap.ALL_RECIPE_MAPS.values()) for (GTRecipe recipe : map.getAllRecipes()) {
            if (!has(recipe.mInputs, steel) || !has(recipe.mInputs, tungsten)) continue;
            if (has(recipe.mOutputs, finishedDust)
                || has(recipe.mOutputs, item(OrePrefixes.ingot, Materials.TungstenSteel, 1))
                || has(recipe.mOutputs, item(OrePrefixes.ingotHot, Materials.TungstenSteel, 1))
                || Arrays.stream(recipe.mFluidOutputs)
                    .anyMatch(fluid -> fluid.isFluidEqual(Materials.TungstenSteel.getMolten(1))))
                throw new RecipeValidationException(
                    "Fresh Steel/Tungsten alloy bypasses Brainrotter in " + map.unlocalizedName);
        }
    }

    private static boolean has(ItemStack[] stacks, ItemStack item) {
        return Arrays.stream(stacks)
            .anyMatch(stack -> stack != null && stack.isItemEqual(item));
    }

    private static ItemStack[][] quantumInputs() {
        return new ItemStack[][] {
            { Materials.Lead.getDust(16), Materials.Bauxite.getDust(32), Materials.Tungstate.getDust(16) },
            { Materials.Rutile.getDust(32), Materials.Scheelite.getDust(16), Materials.Ilmenite.getDust(16) } };
    }

    private static RecipeMap<?> quantumMap() {
        RecipeMap<?> map = RecipeMap.ALL_RECIPE_MAPS.get("gt.recipe.quantumforcesmelter");
        if (map == null) throw new RecipeValidationException("Quantum Force recipe map is missing");
        return map;
    }

    private static List<GTRecipe> rawBlastRecipes() {
        RecipeMatch rawHot = RecipeMatch.builder()
            .itemOutputs(item(OrePrefixes.ingotHot, NerfMaterials.rawTungstenSteel(), 1))
            .build();
        return RecipeMaps.blastFurnaceRecipes.getAllRecipes()
            .stream()
            .filter(rawHot)
            .collect(Collectors.toList());
    }

    /** FoTG derives this map on world entry, after the startup audit has already finished. */
    public static void auditMolten() {
        AuditSession session = new AuditSession(Collections.singletonList(MOLTEN_AUDIT));
        try {
            session.run(MOLTEN_AUDIT, audit -> {
                verifyPublished();
                for (GTRecipe blast : rawBlastRecipes()) {
                    RecipeMatch expected = RecipeMatch.builder()
                        .itemInputs(blast.mInputs)
                        .fluidInputs(blast.mFluidInputs)
                        .itemOutputs()
                        .fluidOutputs(
                            NerfMaterials.rawTungstenSteel()
                                .getMolten(144))
                        .duration(blast.mDuration)
                        .eut(blast.mEUt)
                        .specialValue(blast.mSpecialValue)
                        .build();
                    if (TecTechRecipeMaps.godforgeMoltenRecipes.getAllRecipes()
                        .stream()
                        .filter(expected)
                        .count() != 1)
                        throw new RecipeValidationException(
                            "FoTG did not derive a raw tungstensteel molten recipe from EBF");
                }
                audit.success();
            });
        } catch (RuntimeException | Error failure) {
            AuditReport report = session.finish();
            GTNHExtraNerf.LOG.warn(report.failureReport(), failure);
            if (!GTNHExtraNerf.isDevelopmentEnvironment()) throw new IllegalStateException(
                "GTNH Extra Nerf integrity check failed: FoTG tungstensteel production\n" + report.failureReport(),
                failure);
            return;
        }
        GTNHExtraNerf.LOG.info(
            "FoTG tungstensteel production: {}",
            session.finish()
                .summary());
    }

    private static ItemStack item(OrePrefixes prefix, Materials material, int amount) {
        return GTOreDictUnificator.get(prefix, material, amount);
    }
}
