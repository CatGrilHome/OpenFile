package com.lw.OpenFile;

import com.OpenFile.open_file.Tags;
import com.lw.OpenFile.event.EventOnPlayerDeath;
import com.lw.OpenFile.event.EventOnPlayerRespawn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class OpenFile {

    public static final String MOD_ID = "OpenFile";
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventOnPlayerDeath());
        MinecraftForge.EVENT_BUS.register(new EventOnPlayerRespawn());
    }

}
