package com.lw.OpenFile.event;

import com.OpenFile.open_file.Tags;
import com.lw.OpenFile.OpenFileConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.awt.*;
import java.net.URI;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EventOnPlayerRespawn {
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {

        EntityPlayer player = event.player;
        if (player instanceof EntityPlayer) {

            if (event.player.world.isRemote) {
                return;
            }

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
