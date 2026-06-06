package com.lw.OpenFile.integration.tconstruct;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ToolPart;

import java.util.List;

public class SoulgeHeartPart extends ToolPart {

    public SoulgeHeartPart(int cost) {
        super(cost);
    }

    @Override
    public boolean canUseMaterial(Material material) {
        return super.canUseMaterial(material) && ItemSoulge.hasSoulgeHeartStats(material.getIdentifier());
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        Material material = getMaterial(stack);
        if (!ItemSoulge.hasSoulgeHeartStats(material.getIdentifier())) {
            return;
        }
        ItemSoulge.SoulgeHeartStats stats = ItemSoulge.getSoulgeHeartStats(material.getIdentifier());
        tooltip.add(TextFormatting.WHITE + "检测范围 " + TextFormatting.GREEN + "+" + Math.round(stats.detectionRange));
        tooltip.add(TextFormatting.WHITE + "施加印记层数 " + TextFormatting.GREEN + "+" + stats.exertTimes);
        tooltip.add(TextFormatting.WHITE + "攻击间隔 " + TextFormatting.GREEN + "+" + stats.attackInterval);
        tooltip.add(TextFormatting.WHITE + "斩杀线 " + TextFormatting.GREEN + "+" + trimPercent(stats.executeThreshold));
    }

    private static String trimPercent(float value) {
        int percent = Math.round(value * 100.0F);
        if (percent == 0) {
            return "0";
        }
        if (percent % 100 == 0) {
            return Integer.toString(percent / 100);
        }
        return Float.toString(value);
    }
}
