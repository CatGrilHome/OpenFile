package com.lw.OpenFile;

import com.OpenFile.open_file.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = "OpenFile")
public class OpenFileConfig {

    @Config.Comment("你想启动程序的路径")
    public static String pathOpenFile = "D:\\SteamLibrary\\steamapps\\common\\SenrenBanka\\SenrenBanka.exe";

    @Config.Comment("你要打开的网址链接")
    public static String url = "https://www.mcmod.cn/";

}

