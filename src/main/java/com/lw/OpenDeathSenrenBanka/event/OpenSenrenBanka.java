package com.lw.OpenDeathSenrenBanka.event;

import com.OpenDeathSenrenBanka.open_death_senrenbanka.Tags;
import com.lw.OpenDeathSenrenBanka.OpenDeathSenrenBankaConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class OpenSenrenBanka {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {

            String pathSenrenBanka = OpenDeathSenrenBankaConfig.pathSenrenBanka;
            File file = new File(pathSenrenBanka);

            if (file.exists()) {
                try {
                    Process proc = new ProcessBuilder(pathSenrenBanka).start();
                } catch (IOException e) {
                    e.setStackTrace(null);
                }
            }
        }
    }
}
