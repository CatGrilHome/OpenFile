package com.lw.OpenFile.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.misc.ItemEncodedPattern;
import com.lw.OpenFile.integration.jei.OpenFileJeiPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PatternMachineDetector {

    private PatternMachineDetector() {}

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

        if (details.isCraftable()) {
            return "ME分子装配室";
        }

        IAEItemStack[] outputs = details.getOutputs();
        if (outputs == null || outputs.length == 0) {
            return null;
        }

        return resolveFromJei(outputs);
    }

    private static String resolveFromJei(IAEItemStack[] outputs) {
        try {
            IJeiRuntime runtime = OpenFileJeiPlugin.getRuntime();
            if (runtime == null) {
                return null;
            }

            IRecipeRegistry registry = runtime.getRecipeRegistry();
            Set<String> titles = new LinkedHashSet<>();

            for (IAEItemStack output : outputs) {
                if (output == null || output.getStackSize() <= 0) {
                    continue;
                }
                ItemStack outputStack = output.createItemStack();
                if (outputStack.isEmpty()) {
                    continue;
                }

                IFocus<ItemStack> focus = registry.createFocus(IFocus.Mode.OUTPUT, outputStack);
                for (IRecipeCategory<?> category : registry.getRecipeCategories(focus)) {
                    String title = category.getTitle();
                    if (title != null && !title.trim().isEmpty()) {
                        titles.add(title);
                    }
                }
            }

            return titles.isEmpty() ? null : String.join(", ", titles);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
