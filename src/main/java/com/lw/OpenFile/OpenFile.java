package com.lw.OpenFile;

import com.OpenFile.open_file.Tags;
import com.lw.OpenFile.event.EventOnPlayerDeath;
import com.lw.OpenFile.event.EventOnPlayerRespawn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
     dependencies = "required-after:appliedenergistics2;after:mixinbooter")
public class OpenFile implements ILateMixinLoader {

    public static final String MOD_ID = "OpenFile";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventOnPlayerDeath());
        MinecraftForge.EVENT_BUS.register(new EventOnPlayerRespawn());
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.open_file.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }
}
