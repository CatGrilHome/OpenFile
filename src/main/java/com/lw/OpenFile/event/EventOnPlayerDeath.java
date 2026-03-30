package com.lw.OpenFile.event;

import com.OpenFile.open_file.Tags;
import com.lw.OpenFile.OpenFileConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EventOnPlayerDeath {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {

            String OpenFile = OpenFileConfig.pathOpenFile;
            File file = new File(OpenFile);

            if (file.exists()) {
                try {
                    Process proc = new ProcessBuilder(OpenFile).start();
                } catch (IOException e) {
                    e.setStackTrace(null);
                }
            }
        }
    }
}
