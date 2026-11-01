package pl.kuba6000.gtnhextranerf.loader.recipes;

import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static pl.kuba6000.gtnhextranerf.recipes.RecipeItems.requireModItem;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMap;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.components.ComponentTier;
import pl.kuba6000.gtnhextranerf.components.ComponentType;
import pl.kuba6000.gtnhextranerf.loader.ComponentLoader;
import pl.kuba6000.gtnhextranerf.recipes.RecipeContext;
import pl.kuba6000.gtnhextranerf.recipes.RecipeModule;
import pl.kuba6000.gtnhextranerf.recipes.RecipeRegistry;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;
import pl.kuba6000.gtnhextranerf.recipes.machine.MachineRecipes;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeChange;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeMatch;

public final class IntergalacticRecipeNerf implements RecipeModule {

    private static final String SPACE_ASSEMBLER_MAP = "gt.recipe.spaceAssembler";

    @Override
    public void register(RecipeRegistry registry) {
        registry.register(
            AuditScope.MODIFY_CRAFTING_INPUT_PROXY_RECIPE,
            IntergalacticRecipeNerf::requireQuestionableSensor);
    }

    private static void requireQuestionableSensor(RecipeContext recipes) {
        RecipeMap<?> map = RecipeMap.ALL_RECIPE_MAPS.get(SPACE_ASSEMBLER_MAP);
        if (map == null) throw new RecipeValidationException("Missing recipe map: " + SPACE_ASSEMBLER_MAP);

        // Use the same solder fallback as NHCore's SpaceAssemblerRecipes.
        Fluid solder = FluidRegistry.getFluid("molten.mutatedlivingsolder");
        if (solder == null) solder = FluidRegistry.getFluid("molten.solderingalloy");
        if (solder == null) throw new RecipeValidationException("Missing Crafting Input Proxy solder fluid");

        ItemStack proxy = ItemList.Hatch_CraftingInput_Bus_Slave.get(1);
        ItemStack sensor = ItemList.Sensor_UEV.get(1);
        MachineRecipes assembler = recipes.machine(map);
        assembler.requireExactlyOne(
            RecipeMatch.builder()
                .itemOutputs(proxy)
                .build());
        assembler.modifyOne(
            RecipeMatch.builder()
                .itemInputs(
                    ItemList.Hatch_CraftingInput_Bus_ME.get(1),
                    requireModItem(Mods.AppliedEnergistics2, "tile.BlockAdvancedCraftingUnit", 1, 0),
                    requireModItem(Mods.AppliedEnergistics2, "item.ItemMultiMaterial", 8, 60),
                    requireModItem(Mods.AE2FluidCraft, "fluid_part", 8, 7),
                    requireModItem(Mods.AppliedEnergistics2, "tile.BlockWirelessConnector", 2, 0),
                    sensor,
                    ItemList.EnergisedTesseract.get(1))
                .itemOutputs(proxy)
                .fluidInputs(new FluidStack(solder, 2_304), Materials.DimensionallyShiftedSuperfluid.getFluid(4_000))
                .fluidOutputs()
                .duration(15 * SECONDS)
                .eut(TierEU.RECIPE_UIV)
                .build(),
            RecipeChange.builder()
                .replaceItemInput(
                    sensor,
                    ComponentLoader.components()
                        .stack(ComponentTier.UQV, ComponentType.SENSOR, 1))
                .build());
    }

}
