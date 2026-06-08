package com.lw.OpenFile.integration.tconstruct;

import com.OpenFile.open_file.Tags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ToolPart;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class OpenFileTinkerTools {

    public static final ToolPart soulgeHeart = new SoulgeHeartPart(864);
    public static final ItemSoulge soulge = new ItemSoulge();

    private OpenFileTinkerTools() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        registerPart(event, soulgeHeart, "soulge_heart");
        registerTool(event, soulge, "soulge");

        TinkerRegistry.registerToolPart(soulgeHeart);
        TinkerRegistry.registerTool(soulge);
        TinkerRegistry.addPatternForItem(soulgeHeart);
        TinkerRegistry.addCastForItem(soulgeHeart);
        registerSoulgeHeartCasting();
        TinkerRegistry.registerToolStationCrafting(soulge);
        TinkerRegistry.registerToolForgeCrafting(soulge);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelRegisterUtil.registerPartModel(soulgeHeart);
        ModelRegisterUtil.registerToolModel(soulge);
        ToolBuildGuiInfo info = new ToolBuildGuiInfo(soulge);
        info.addSlotPosition(45, 46);
        info.addSlotPosition(25, 46);
        info.addSlotPosition(45, 26);
        info.addSlotPosition(25, 26);
        info.addSlotPosition(7, 62);
        TinkerRegistryClient.addToolBuilding(info);
    }

    private static void registerSoulgeHeartCasting() {
        Material gold = TinkerRegistry.getMaterial("gold");
        if (gold.hasFluid()) {
            TinkerRegistry.registerTableCasting(new ItemStack(soulgeHeart), new ItemStack(soulgeHeart), gold.getFluid(), soulgeHeart.getCost());
        }
    }

    private static void registerPart(RegistryEvent.Register<Item> event, ToolPart part, String name) {
        part.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        part.setTranslationKey(Tags.MOD_ID + "." + name);
        event.getRegistry().register(part);
    }

    private static void registerTool(RegistryEvent.Register<Item> event, Item item, String name) {
        item.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        item.setTranslationKey(Tags.MOD_ID + "." + name);
        event.getRegistry().register(item);
    }
}
