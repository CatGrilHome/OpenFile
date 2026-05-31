package com.lw.OpenFile.mixin.ae2;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;

import com.lw.OpenFile.util.PatternMachineDetector;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin for AE2's ItemEncodedPattern to display the target machine
 * in the pattern's tooltip, above the recipe info.
 */
@Mixin(value = ItemEncodedPattern.class, remap = false)
public abstract class MixinItemEncodedPattern {

    @Inject(method = "addCheckedInformation", at = @At("HEAD"), remap = false)
    private void addMachineTooltip(ItemStack stack, World world,
                                    List<String> tooltip, ITooltipFlag flag,
                                    CallbackInfo ci) {
        String targetMachine = PatternMachineDetector.detectMachine(stack, world);
        if (targetMachine != null && !targetMachine.isEmpty()) {
            tooltip.add(Math.min(1, tooltip.size()), "§7机器：" + targetMachine);
        }

        ItemEncodedPattern patternItem = (ItemEncodedPattern) stack.getItem();
        ICraftingPatternDetails craftDetails = patternItem.getPatternForItem(stack, world);
        if (craftDetails != null) {
            tooltip.add(Math.min(2, tooltip.size()), "§7配方类型：" + (craftDetails.isCraftable() ? "合成样板" : "处理样板"));
        }
    }

    @Inject(method = "addCheckedInformation", at = @At("TAIL"), remap = false)
    private void cleanupTooltip(ItemStack stack, World world,
                                 List<String> tooltip, ITooltipFlag flag,
                                 CallbackInfo ci) {
        // Remove any bare-number-only lines at the end of the tooltip
        for (int i = tooltip.size() - 1; i >= 0; i--) {
            String stripped = tooltip.get(i).replaceAll("§[0-9a-fk-or]", "").trim();
            if (stripped.matches("\\d+")) {
                tooltip.remove(i);
            }
        }
    }
}
