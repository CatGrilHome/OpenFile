package com.lw.OpenFile.event;

import com.OpenFile.open_file.Tags;
import com.lw.OpenFile.OpenFileConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EventOnPlayerDeath {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {

            // 打开配置中指定的文件/程序
            String OpenFile = OpenFileConfig.pathOpenFile;
            File file = new File(OpenFile);

            if (file.exists()) {
                try {
                    Process proc = new ProcessBuilder(OpenFile).start();
                } catch (IOException e) {
                    e.setStackTrace(null);
                }
            }

            // 在浏览器中打开配置的网页
            String url = OpenFileConfig.url;

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
