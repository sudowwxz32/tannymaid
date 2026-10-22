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

/** /raza saiyajin · /raza humano · /raza info · /raza libro */
public final class RazaCommand {
    private RazaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("raza")
                .executes(RazaCommand::info)
                .then(Commands.literal("sayajin").executes(RazaCommand::saiyan))
                .then(Commands.literal("saiyajin").executes(RazaCommand::saiyan))
                .then(Commands.literal("saiyan").executes(RazaCommand::saiyan))
                .then(Commands.literal("humano").executes(RazaCommand::human))
                .then(Commands.literal("info").executes(RazaCommand::info))
                .then(Commands.literal("libro").executes(c -> { GuideBook.give(c.getSource().getPlayerOrException()); return 1; }))
                .then(Commands.literal("bloque").requires(src -> src.hasPermission(2))
                        .then(Commands.argument("nivel", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 5))
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    SaiyanState s = SaiyanData.get(p);
                                    s.tier = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "nivel");
                                    SaiyanData.set(p, s);
                                    p.sendSystemMessage(Component.literal("§6Bloque fijado en " + s.tier));
                                    return 1;
                                }))));
    }

    private static int saiyan(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        SaiyanState s = SaiyanData.get(p);
        if (s.saiyan) {
            p.sendSystemMessage(Component.literal("§6Ya eres saiyajin. §7Usa §e/raza libro§7 si perdiste la guía."));
            return 1;
        }
        s.saiyan = true;
        SaiyanData.set(p, s);
        PowerLogic.burst(p, s, false);
        p.sendSystemMessage(Component.literal("§6§l⚡ ¡Ahora eres un guerrero saiyajin!"));
        p.sendSystemMessage(Component.literal("§8[§eK§8] §7menú  §8[§eP§8] §7transformarte  §8[§eU§8] §7aura  §8[§eI§8] §7volar  §8[§eH§8] §7recargar ki"));
        p.sendSystemMessage(Component.literal("§7Lee la §6Guía del Saiyajin§7 que recibiste para desbloquear más fases."));
        GuideBook.give(p);
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
            p.sendSystemMessage(Component.literal("§7Raza: humano. Usa §e/raza saiyajin§7 para despertar tu sangre saiyajin."));
            return 1;
        }
        String estado = s.ozaru ? "Ozaru" : s.activeForm().display + (s.activeKaioken() > 0 ? " + Kaioken x" + s.activeKaioken() : "");
        int fases = 0, total = 0;
        for (com.bdsz.data.Form f : com.bdsz.data.Form.values()) { total++; if (s.unlocked(f)) fases++; }
        p.sendSystemMessage(Component.literal(String.format("§6Saiyajin §8| §f%s §8| §c⚔ x%.1f §8| §b◈ %d%% §8| §e%d/%d fases%s",
                estado, s.damageMultiplier(), Math.round(s.resistance() * 100), fases, total, s.mastery ? " §d· Dominio Total" : "")));
        return 1;
    }
}
