package com.lw.OpenFile.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public final class OpenFileJeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
