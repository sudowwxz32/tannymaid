package com.bdsz.command;

import com.bdsz.data.PowerLogic;
import com.bdsz.data.SaiyanData;
import com.bdsz.data.SaiyanState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /raza sayajin · /raza humano · /raza info */
public final class RazaCommand {
    private RazaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("raza")
                .executes(RazaCommand::info)
                .then(Commands.literal("sayajin").executes(RazaCommand::saiyan))
                .then(Commands.literal("saiyajin").executes(RazaCommand::saiyan))
                .then(Commands.literal("saiyan").executes(RazaCommand::saiyan))
                .then(Commands.literal("humano").executes(RazaCommand::human))
                .then(Commands.literal("info").executes(RazaCommand::info)));
    }

    private static int saiyan(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        SaiyanState s = SaiyanData.get(p);
        if (s.saiyan) {
            p.sendSystemMessage(Component.literal("§6Ya eres saiyajin. §7K = menú, P = transformarte, U = aura."));
            return 1;
        }
        s.saiyan = true;
        SaiyanData.set(p, s);
        PowerLogic.burst(p, s, false);
        p.sendSystemMessage(Component.literal("§6§l¡Ahora eres un guerrero saiyajin!"));
        p.sendSystemMessage(Component.literal("§eK§7 abre el menú de fases y Kaioken · §eP§7 te transforma · §eU§7 activa el aura"));
        return 1;
    }

    private static int human(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        SaiyanData.set(p, new SaiyanState());
        p.sendSystemMessage(Component.literal("§7Vuelves a ser humano."));
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) {
            p.sendSystemMessage(Component.literal("§7Raza: humano. Usa §e/raza sayajin§7 para despertar tu sangre saiyajin."));
            return 1;
        }
        p.sendSystemMessage(Component.literal(String.format("§6Raza: saiyajin §7| Estado: %s%s §7| Daño x%.1f | Resistencia %d%% | Aura: %s",
                s.activeForm().display, s.activeKaioken() > 0 ? " + Kaioken x" + s.activeKaioken() : "",
                s.damageMultiplier(), Math.round(s.resistance() * 100), s.aura ? "sí" : "no")));
        return 1;
    }
}
