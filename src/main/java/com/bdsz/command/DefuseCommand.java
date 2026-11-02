package com.bdsz.command;

import com.bdsz.data.FusionLogic;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** /desfusionar — sale de cualquier fusión, incluso de la fusión permanente con el dragón. */
public final class DefuseCommand {
    private DefuseCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("desfusionar").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            return FusionLogic.forceDefuse(p) ? 1 : 0;
        }));
    }
}
