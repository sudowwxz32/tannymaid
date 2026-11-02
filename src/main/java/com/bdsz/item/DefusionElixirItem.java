package com.bdsz.item;

import com.bdsz.data.FusionLogic;
import com.bdsz.data.SaiyanData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Elixir de Separación: al beberlo sales de la fusión (incluida la fusión permanente con el dragón). */
public class DefusionElixirItem extends Item {
    public DefusionElixirItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            var s = SaiyanData.get(sp);
            if (s.fusion <= 0 && s.fusing <= 0) {
                sp.displayClientMessage(Component.literal("§7No estás fusionado."), true);
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity e) {
        if (!level.isClientSide && e instanceof ServerPlayer p && FusionLogic.forceDefuse(p)) {
            if (!p.getAbilities().instabuild) {
                stack.shrink(1);
                if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
                if (!p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) p.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        return stack;
    }

    @Override public int getUseDuration(ItemStack stack) { return 32; }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§dBébelo para salir de la fusión"));
        tip.add(Component.literal("§7Funciona incluso con la fusión permanente del dragón"));
    }
}
