package com.linterna.command;

import com.linterna.data.Abduction;
import com.linterna.data.LanternData;
import com.linterna.data.LanternState;
import com.linterna.registry.ModRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** /linterna llamar · reiniciar · recargar · anillo · estado */
public final class LinternaCommand {
    private LinternaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("linterna")
                .then(Commands.literal("estado").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    LanternState s = LanternData.get(p);
                    String st = switch (s.stage) { case 1 -> "tuvo miedo (sin anillo)"; case 2 -> "portador del anillo"; default -> "esperando la luz verde"; };
                    p.sendSystemMessage(Component.literal("§aLinterna: §f" + st + (s.bearer() ? " · energía " + s.charge * 100 / LanternState.MAX_CHARGE
                            + "% · uso " + s.useTicks / 1200 + " min" + (s.suitUnlocked() ? " · traje desbloqueado" : "") : "")));
                    return 1;
                }))
                .then(Commands.literal("llamar").requires(src -> src.hasPermission(2)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    LanternState s = LanternData.get(p);
                    s.stage = 0;
                    Abduction.begin(p, s);
                    return 1;
                }))
                .then(Commands.literal("reiniciar").requires(src -> src.hasPermission(2)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    LanternState s = new LanternState();
                    LanternData.set(p, s);
                    p.sendSystemMessage(Component.literal("§aLa luz verde volverá la próxima noche."));
                    return 1;
                }))
                .then(Commands.literal("recargar").requires(src -> src.hasPermission(2)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    LanternState s = LanternData.get(p);
                    s.charge = LanternState.MAX_CHARGE;
                    LanternData.set(p, s);
                    return 1;
                }))
                .then(Commands.literal("anillo").requires(src -> src.hasPermission(2)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    LanternState s = LanternData.get(p);
                    s.stage = 2;
                    if (s.charge <= 0) s.charge = LanternState.MAX_CHARGE;
                    LanternData.set(p, s);
                    if (!com.linterna.data.RingSlot.worn(p)) com.linterna.data.RingSlot.set(p, new ItemStack(ModRegistry.RING.get()));
                    else p.getInventory().add(new ItemStack(ModRegistry.RING.get()));
                    p.getInventory().add(new ItemStack(ModRegistry.CANDELABRA_ITEM.get()));
                    return 1;
                })));
    }
}
