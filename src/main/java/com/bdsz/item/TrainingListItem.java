package com.bdsz.item;

import com.bdsz.data.PowerLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Lista de Entrenamiento: clic izquierdo (o derecho) con ella en la mano para desbloquear un bloque de fases. */
public class TrainingListItem extends Item {
    /** Bloque que desbloquea (2, 3 o 4). */
    public final int tier;

    public TrainingListItem(int tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    /** Intenta desbloquear; si lo logra, consume la lista. */
    public static void use(ServerPlayer p, ItemStack stack) {
        long now = p.level().getGameTime();
        if (now - p.getPersistentData().getLong("bdsz_last_list") < 10) return;   // evita doble uso por un solo clic
        p.getPersistentData().putLong("bdsz_last_list", now);
        if (stack.getItem() instanceof TrainingListItem list && PowerLogic.unlockTier(p, list.tier)) {
            if (!p.isCreative()) stack.shrink(1);
            p.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) use(sp, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        String forms = switch (tier) {
            case 2 -> "Super Saiyajin Falso y Fase 1";
            case 3 -> "Fase 2, Fase 3 y Dios";
            case 4 -> "Blue, Blue Evolución y Ultra Instinto";
            case 5 -> "Super Saiyajin Fase 4 y Ultra Ego";
            default -> "tiempo infinito en las transformaciones";
        };
        if (tier == 6) {
            tip.add(Component.literal("§d§lDominio Total: §r§e" + forms));
            tip.add(Component.literal("§dDesbloquea: Gohan Bestia, Legendario (3 fases), Rosé y Modo Ozaru"));
        }
        else tip.add(Component.literal("§6Desbloquea el " + (tier == 5 ? "Bloque Especial" : "Bloque " + tier) + ": §e" + forms));
        tip.add(Component.literal("§7Haz clic izquierdo con ella en la mano."));
    }
}
