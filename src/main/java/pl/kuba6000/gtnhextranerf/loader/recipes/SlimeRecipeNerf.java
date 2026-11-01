package pl.kuba6000.gtnhextranerf.loader.recipes;

import static com.dreammaster.scripts.IngredientFactory.getModItem;
import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.recipe.RecipeMaps.centrifugeNonCellRecipes;
import static gregtech.api.recipe.RecipeMaps.centrifugeRecipes;
import static gregtech.api.recipe.RecipeMaps.extractorRecipes;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeBuilder.WILDCARD;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.recipes.RecipeContext;
import pl.kuba6000.gtnhextranerf.recipes.RecipeModule;
import pl.kuba6000.gtnhextranerf.recipes.RecipeRegistry;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;
import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingDefinition;
import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingRecipeBuilder;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeMatch;

public final class SlimeRecipeNerf implements RecipeModule {

    private static final Materials[] RESISTOR_CARBONS = { Materials.Coal, Materials.Carbon, Materials.Charcoal,
        Materials.Lignite };

    @Override
    public void register(RecipeRegistry registry) {
        registry.register(AuditScope.REMOVE_SLIME_EXTRACTOR_RECIPES, SlimeRecipeNerf::removeSlimeExtractorRecipes);
        registry.register(AuditScope.REMOVE_SLIME_CENTRIFUGE_RECIPES, SlimeRecipeNerf::removeSlimeCentrifugeRecipes);
        registry.register(
            AuditScope.REMOVE_SLIME_MULTIBLOCK_CENTRIFUGE_RECIPES,
            SlimeRecipeNerf::removeSlimeMultiblockCentrifugeRecipes);
        registry.register(
            AuditScope.REPLACE_SLIME_STICKY_PISTON_ASSEMBLER_RECIPE,
            SlimeRecipeNerf::replaceStickyPistonAssemblerRecipe);
        registry.register(
            AuditScope.REPLACE_SLIME_STICKY_PISTON_HAND_CRAFTING,
            SlimeRecipeNerf::replaceStickyPistonHandCrafting);
        registry.register(
            AuditScope.REPLACE_SLIME_COATED_BOARD_HAND_CRAFTING,
            SlimeRecipeNerf::replaceCoatedBoardHandCrafting);
        registry
            .register(AuditScope.REPLACE_SLIME_RESISTOR_HAND_CRAFTING, SlimeRecipeNerf::replaceResistorHandCrafting);
    }

    private static void removeSlimeExtractorRecipes(RecipeContext recipes) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(new ItemStack(Items.slime_ball, 1, 0));
        if (Mods.TinkerConstruct.isModLoaded()) {
            inputs.add(tinkers("strangeFood", 0));
            inputs.add(tinkers("slime.gel", 0));
            inputs.add(tinkers("slime.gel", 1));
            inputs.add(tinkers("slime.leaves", 0));
        }
        recipes.machine(extractorRecipes)
            .removeExactly(hasAnyInput(inputs), inputs.size());
    }

    private static void removeSlimeCentrifugeRecipes(RecipeContext recipes) {
        List<ItemStack> inputs = centrifugeSlimeInputs();
        recipes.machine(centrifugeRecipes)
            .removeExactly(hasAnyInput(inputs), inputs.size());
    }

    private static void removeSlimeMultiblockCentrifugeRecipes(RecipeContext recipes) {
        List<ItemStack> inputs = centrifugeSlimeInputs();
        recipes.machine(centrifugeNonCellRecipes)
            .removeExactly(hasAnyInput(inputs), inputs.size());
    }

    private static List<ItemStack> centrifugeSlimeInputs() {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(new ItemStack(Items.slime_ball, 1, 0));
        inputs.add(tinkers("strangeFood", 0));
        inputs.add(tinkers("slime.gel", 0));
        inputs.add(tinkers("slime.gel", 1));
        inputs.add(tinkers("slime.gel", 2));
        inputs.add(tinkers("CraftedSoil", 0));
        return inputs;
    }

    private static void replaceStickyPistonAssemblerRecipe(RecipeContext recipes) {
        ItemStack piston = new ItemStack(Blocks.piston, 1, WILDCARD);
        ItemStack output = new ItemStack(Blocks.sticky_piston, 1, 0);
        RecipeMatch oldRecipe = RecipeMatch.builder()
            .itemInputs(piston, new ItemStack(Items.slime_ball, 1, WILDCARD))
            .itemOutputs(output)
            .fluidInputs()
            .fluidOutputs()
            .duration(5 * SECONDS)
            .eut(4)
            .build();
        recipes.machine(assemblerRecipes)
            .removeExactly(oldRecipe, 1);

        ItemStack glue = emtGlue();
        RecipeMatch replacement = RecipeMatch.builder()
            .itemInputs(piston, glue)
            .itemOutputs(output)
            .fluidInputs()
            .fluidOutputs()
            .duration(5 * SECONDS)
            .eut(4)
            .build();
        recipes.machine(assemblerRecipes)
            .addOne(
                GTValues.RA.stdBuilder()
                    .itemInputs(piston, glue)
                    .itemOutputs(output)
                    .duration(5 * SECONDS)
                    .eut(4),
                replacement);
    }

    private static void replaceStickyPistonHandCrafting(RecipeContext recipes) {
        CraftingDefinition original = CraftingRecipeBuilder.universalShaped()
            .pattern(" M ", " G ", " P ")
            .ingredient('M', ore("craftingToolSoftMallet"))
            .ingredient('G', new ItemStack(Items.slime_ball))
            .ingredient('P', new ItemStack(Blocks.piston))
            .output(new ItemStack(Blocks.sticky_piston))
            .build();
        recipes.crafting()
            .removeOne(original);
        recipes.crafting()
            .addOne(
                original.toBuilder()
                    .ingredient('G', emtGlue())
                    .build());
    }

    private static void replaceCoatedBoardHandCrafting(RecipeContext recipes) {
        ItemStack plate = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Wood, 1);
        for (ItemStack slime : craftingSlimeBalls()) {
            recipes.crafting()
                .removeOne(singleCoatedBoard(plate, slime));
            recipes.crafting()
                .removeOne(tripleCoatedBoard(plate, slime));
        }
        recipes.crafting()
            .addOne(singleCoatedBoard(plate, emtGlue()));
        recipes.crafting()
            .addOne(tripleCoatedBoard(plate, emtGlue()));
    }

    private static CraftingDefinition singleCoatedBoard(ItemStack plate, ItemStack binder) {
        return CraftingRecipeBuilder.shapeless()
            .inputs(plate, binder, binder)
            .output(ItemList.Circuit_Board_Coated.get(1))
            .build();
    }

    private static CraftingDefinition tripleCoatedBoard(ItemStack plate, ItemStack binder) {
        return CraftingRecipeBuilder.shaped()
            .pattern("RRR", "PPP", "RRR")
            .ingredient('P', plate)
            .ingredient('R', binder)
            .output(ItemList.Circuit_Board_Coated.get(3))
            .build();
    }

    private static void replaceResistorHandCrafting(RecipeContext recipes) {
        for (Materials carbon : RESISTOR_CARBONS) {
            CraftingRecipeBuilder resistor = CraftingRecipeBuilder.shaped()
                .pattern("RPR", "FCF", " P ")
                .ingredient('F', GTOreDictUnificator.get(OrePrefixes.wireGt01, Materials.Copper, 1))
                .ingredient('P', GTOreDictUnificator.get(OrePrefixes.wireFine, Materials.Copper, 1))
                .ingredient('C', GTOreDictUnificator.get(OrePrefixes.dust, carbon, 1))
                .output(ItemList.Circuit_Parts_Resistor.get(1));
            for (ItemStack slime : craftingSlimeBalls()) {
                recipes.crafting()
                    .removeOne(
                        resistor.ingredient('R', slime)
                            .build());
            }
            recipes.crafting()
                .addOne(
                    resistor.ingredient('R', emtGlue())
                        .build());
        }
    }

    private static List<ItemStack> craftingSlimeBalls() {
        List<ItemStack> slimeBalls = new ArrayList<>();
        slimeBalls.add(new ItemStack(Items.slime_ball, 1, 0));
        if (Mods.TinkerConstruct.isModLoaded()) {
            slimeBalls.add(tinkers("materials", 36));
            slimeBalls.add(tinkers("strangeFood", 0));
            slimeBalls.add(tinkers("strangeFood", 1));
        }
        return slimeBalls;
    }

    private static Predicate<GTRecipe> hasAnyInput(List<ItemStack> targets) {
        return recipe -> {
            if (recipe.mInputs == null) return false;
            for (ItemStack input : recipe.mInputs) {
                if (input == null) continue;
                for (ItemStack target : targets) {
                    if (sameItem(input, target)) return true;
                }
            }
            return false;
        };
    }

    private static boolean sameItem(ItemStack actual, ItemStack expected) {
        return GTUtility.areStacksEqual(actual, expected, true) && actual.getItemDamage() == expected.getItemDamage();
    }

    private static ItemStack ore(String name) {
        ItemStack result = GTOreDictUnificator.getFirstOre(name, 1);
        if (result == null) {
            throw new RecipeValidationException("Could not resolve crafting ingredient " + name);
        }
        return result;
    }

    private static ItemStack emtGlue() {
        return getModItem(Mods.ElectroMagicTools.ID, "EMTItems", 1, 8);
    }

    private static ItemStack tinkers(String name, int meta) {
        return getModItem(Mods.TinkerConstruct.ID, name, 1, meta);
    }
}
