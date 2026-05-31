package com.lw.OpenFile.util;

import appeng.api.features.IInscriberRecipe;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.items.misc.ItemEncodedPattern;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;

import java.util.Collection;

/**
 * Analyzes an encoded pattern's recipe to determine which machine
 * is intended to execute it.
 */
public final class PatternMachineDetector {

    private PatternMachineDetector() {}

    /**
     * Determine the target machine name for a pattern.
     *
     * @param patternStack the encoded pattern ItemStack
     * @param world        the current world (needed for recipe lookup)
     * @return human-readable machine name, or null if undetermined
     */
    public static String detectMachine(ItemStack patternStack, World world) {
        if (patternStack.isEmpty()
                || !(patternStack.getItem() instanceof ItemEncodedPattern)) {
            return null;
        }

        ItemEncodedPattern item = (ItemEncodedPattern) patternStack.getItem();
        ICraftingPatternDetails details = item.getPatternForItem(patternStack, world);
        if (details == null) {
            return null;
        }

        // Crafting patterns always go to Molecular Assembler
        if (details.isCraftable()) {
            return "ME分子装配室";
        }

        // Processing pattern — check known machine recipes
        IAEItemStack[] inputs = details.getInputs();
        IAEItemStack[] outputs = details.getOutputs();

        if (inputs == null || outputs == null || inputs.length == 0 || outputs.length == 0) {
            return "处理机器";
        }

        // Check furnace recipes
        if (matchesFurnace(inputs, outputs)) {
            return "熔炉";
        }

        // Check AE2 inscriber recipes
        if (matchesInscriber(inputs, outputs)) {
            return "压印器";
        }

        return "Processing Machine";
    }

    /** Check if inputs/outputs match a furnace smelting recipe. */
    private static boolean matchesFurnace(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        FurnaceRecipes furnace = FurnaceRecipes.instance();
        for (IAEItemStack input : inputs) {
            if (input == null || input.getStackSize() <= 0) continue;
            ItemStack smeltResult = furnace.getSmeltingResult(input.createItemStack());
            if (!smeltResult.isEmpty()) {
                for (IAEItemStack output : outputs) {
                    if (output != null && output.createItemStack().isItemEqual(smeltResult)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Check if inputs/outputs match an AE2 inscriber recipe. */
    private static boolean matchesInscriber(IAEItemStack[] inputs, IAEItemStack[] outputs) {
        try {
            Collection<IInscriberRecipe> recipes =
                    Api.INSTANCE.registries().inscriber().getRecipes();
            for (IInscriberRecipe recipe : recipes) {
                if (matchesInscriberRecipe(recipe, inputs, outputs)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Inscriber registry unavailable
        }
        return false;
    }

    private static boolean matchesInscriberRecipe(
            IInscriberRecipe recipe, IAEItemStack[] inputs, IAEItemStack[] outputs) {
        ItemStack recipeOutput = recipe.getOutput();
        if (recipeOutput.isEmpty()) return false;

        // Check if any pattern output matches the recipe output
        boolean outputMatches = false;
        for (IAEItemStack output : outputs) {
            if (output != null && output.createItemStack().isItemEqual(recipeOutput)) {
                outputMatches = true;
                break;
            }
        }
        if (!outputMatches) return false;

        // Check if at least one pattern input is an inscriber input
        for (ItemStack recipeInput : recipe.getInputs()) {
            if (recipeInput.isEmpty()) continue;
            for (IAEItemStack input : inputs) {
                if (input != null && input.createItemStack().isItemEqual(recipeInput)) {
                    return true;
                }
            }
        }

        return false;
    }
}
