package com.tanny.tannymaid.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannyLookup;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

/** /tanny follow | stay | home | sethome | tp | organize | farm | defend | inventory | lucario | collect | status | dismiss */
public final class TannyCommand {

    private TannyCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tanny")
                .executes(TannyCommand::help)
                .then(Commands.literal("help").executes(TannyCommand::help))
                .then(Commands.literal("follow").executes(ctx -> run(ctx, TannyEntity::commandFollow)))
                .then(Commands.literal("stay").executes(ctx -> run(ctx, TannyEntity::commandStay)))
                .then(Commands.literal("home").executes(ctx -> run(ctx, TannyEntity::commandHome)))
                .then(Commands.literal("sethome").executes(ctx -> run(ctx, TannyEntity::commandSetHome)))
                .then(Commands.literal("tp").executes(ctx -> run(ctx, TannyEntity::commandTp)))
                .then(Commands.literal("organize").executes(ctx -> run(ctx, TannyEntity::commandOrganize)))
                .then(Commands.literal("farm").executes(ctx -> run(ctx, TannyEntity::commandFarm)))
                .then(Commands.literal("defend").executes(ctx -> run(ctx, TannyEntity::commandDefend)))
                .then(Commands.literal("inventory").executes(ctx -> run(ctx, (t, p) -> t.openInventory(p))))
                .then(Commands.literal("inv").executes(ctx -> run(ctx, (t, p) -> t.openInventory(p))))
                .then(Commands.literal("lucario").executes(ctx -> run(ctx, (t, p) -> t.getLucario().toggleManual(p))))
                .then(Commands.literal("collect").executes(ctx -> run(ctx, TannyEntity::commandCollect)))
                .then(Commands.literal("status").executes(ctx -> run(ctx, TannyEntity::commandStatus)))
                .then(Commands.literal("dismiss").executes(ctx -> run(ctx, TannyEntity::commandDismiss))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, BiConsumer<TannyEntity, ServerPlayer> action)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TannyEntity tanny = TannyLookup.find(player);
        if (tanny == null) {
            String last = TannyLookup.describeLastPos(player);
            if (last != null) {
                TannySpeech.system(player, "Tanny no está cargada ahora. Su última posición conocida: §f" + last
                        + "§7. Acércate a esa zona y vuelve a intentarlo.");
            } else {
                TannySpeech.system(player, "Aún no tienes a Tanny. Craftea un §dContrato de Tanny Maid§7 "
                        + "(lingote de oro + mapa) y úsalo.");
            }
            return 0;
        }
        action.accept(tanny, player);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        TannySpeech.system(p, "§dÓrdenes de Tanny:");
        TannySpeech.system(p, "§f/tanny follow§7 – te sigue (y te defiende)");
        TannySpeech.system(p, "§f/tanny stay§7 – se queda en el punto actual");
        TannySpeech.system(p, "§f/tanny sethome§7 – marca tu posición como casa");
        TannySpeech.system(p, "§f/tanny home§7 – va a casa y se queda ahí");
        TannySpeech.system(p, "§f/tanny tp§7 – se teletransporta a ti");
        TannySpeech.system(p, "§f/tanny organize§7 – ordena cofres y barriles cercanos a ella");
        TannySpeech.system(p, "§f/tanny farm§7 – cosecha y siembra en el área donde está");
        TannySpeech.system(p, "§f/tanny defend§7 – modo defensa activa (radio amplio)");
        TannySpeech.system(p, "§f/tanny inventory§7 – abre su inventario");
        TannySpeech.system(p, "§f/tanny lucario§7 – saca o guarda a su Lucario");
        TannySpeech.system(p, "§f/tanny collect§7 – activa/desactiva recoger objetos del suelo");
        TannySpeech.system(p, "§f/tanny status§7 – vida, modo y posición");
        TannySpeech.system(p, "§f/tanny dismiss§7 – la despide y te devuelve el contrato");
        TannySpeech.system(p, "Clic derecho: inventario · Shift+clic derecho: cambiar modo · Clic con comida/poción: curarla");
        return 1;
    }
}
