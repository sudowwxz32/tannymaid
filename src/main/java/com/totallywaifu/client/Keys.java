package com.totallywaifu.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.network.CollectionAction;
import com.totallywaifu.network.Net;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Tecla para abrir ♥ Mis Waifus (por defecto K; se cambia en Opciones → Controles → Totally Waifu?). */
public final class Keys {
    private Keys() {}

    public static final KeyMapping COLLECTION = new KeyMapping("key.totallywaifu.coleccion", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.totallywaifu");

    /** Inari: chica ↔ zorro dios gigante (por defecto V). */
    public static final KeyMapping INARI = new KeyMapping("key.totallywaifu.inari", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.totallywaifu");

    @Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Register {
        private Register() {}

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent e) {
            e.register(COLLECTION);
            e.register(INARI);
        }
    }

    @Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, value = Dist.CLIENT)
    public static final class Press {
        private Press() {}

        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            while (COLLECTION.consumeClick()) {
                if (mc.player != null && mc.screen == null) Net.CHANNEL.sendToServer(new CollectionAction(0, CollectionAction.OPEN));
            }
            while (INARI.consumeClick()) {
                if (mc.player != null && mc.screen == null && !Ascend.active)
                    Net.CHANNEL.sendToServer(new CollectionAction(com.totallywaifu.entity.WaifuKind.DIOSA.ordinal(), CollectionAction.INARI));
            }
        }
    }
}
