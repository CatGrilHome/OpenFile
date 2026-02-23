package com.lw.OpenDeathSenrenBanka;

import com.OpenDeathSenrenBanka.open_death_senrenbanka.Tags;
import com.lw.OpenDeathSenrenBanka.event.OpenSenrenBanka;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class OpenDeathSenrenBanka {

    public static final String MOD_ID = "OpenDeathSenrenBanka";
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new OpenSenrenBanka());
    }

}
