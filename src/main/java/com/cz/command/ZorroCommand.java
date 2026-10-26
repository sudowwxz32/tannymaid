package com.cz.command;

import com.cz.data.ZorroData;
import com.cz.data.ZorroState;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** /zorro despertar · /zorro sellar · /zorro info */
public final class ZorroCommand {
    private ZorroCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("zorro")
                .executes(c -> info(c.getSource().getPlayerOrException()))
                .then(Commands.literal("despertar").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    ZorroState s = ZorroData.get(p);
                    if (s.awakened) { p.sendSystemMessage(Component.literal("§6El zorro ya duerme dentro de ti.")); return 1; }
                    s.awakened = true;
                    s.chakra = ZorroState.MAX_CHAKRA;
                    ZorroData.set(p, s);
                    p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.5F, 0.5F);
                    p.sendSystemMessage(Component.literal("§c§lSientes un chakra ardiente despertar en tu interior..."));
                    p.sendSystemMessage(Component.literal("§8[§eN§8] §7elegir manto  §8[§eY§8] §7transformarse / volver  §8[§eJ§8] §7cargar chakra"));
                    return 1;
                }))
                .then(Commands.literal("sellar").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    ZorroData.set(p, new ZorroState());
                    p.refreshDimensions();
                    p.sendSystemMessage(Component.literal("§7El chakra del zorro queda sellado."));
                    return 1;
                }))
                .then(Commands.literal("info").executes(c -> info(c.getSource().getPlayerOrException()))));
    }

    private static int info(ServerPlayer p) {
        ZorroState s = ZorroData.get(p);
        if (!s.awakened) {
            p.sendSystemMessage(Component.literal("§7Aún no despertaste el chakra del zorro. Usa §e/zorro despertar"));
            return 1;
        }
        p.sendSystemMessage(Component.literal("§6Chakra: §e" + s.chakra + "/" + ZorroState.MAX_CHAKRA + " §8| §6Manto: §f" + s.active().display));
        return 1;
    }
}
