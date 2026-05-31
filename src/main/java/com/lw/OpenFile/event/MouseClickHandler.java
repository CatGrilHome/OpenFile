package com.lw.OpenFile.event;

import com.OpenFile.open_file.Tags;
import com.lw.OpenFile.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 客户端事件处理器：监听鼠标左键点击，累计10次后向服务端发送随机传送请求。
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MOD_ID)
public class MouseClickHandler {

    private static int clickCount = 0;

    @SubscribeEvent
    public static void onMouseClick(MouseEvent event) {
        // 只统计左键按下（不是松开），按键编号 0 = 左键
        if (event.getButton() != 0 || !event.isButtonstate()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        // 玩家为空、已死亡、或 GUI 打开时不计数
        if (mc.player == null || mc.player.isDead || mc.currentScreen != null) {
            return;
        }

        clickCount++;

        if (clickCount >= 10) {
            clickCount = 0;

            // 发送传送请求到服务端
            PacketHandler.INSTANCE.sendToServer(new PacketHandler.TeleportRequest());
        }
    }
}
