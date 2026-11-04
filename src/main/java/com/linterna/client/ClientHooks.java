package com.linterna.client;

import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {}

    public static void ask() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AskScreen)) mc.setScreen(new AskScreen());
    }
}
