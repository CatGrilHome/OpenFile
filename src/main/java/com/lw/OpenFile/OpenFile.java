package com.lw.OpenFile;

import com.OpenFile.open_file.Tags;
import net.minecraftforge.fml.common.Mod;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
     dependencies = "required-after:appliedenergistics2;after:mixinbooter")
public class OpenFile implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.open_file.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }
}
