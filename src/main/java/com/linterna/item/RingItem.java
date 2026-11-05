package com.linterna.item;

import com.linterna.data.Holding;
import com.linterna.data.LanternData;
import com.linterna.data.LanternLogic;
import com.linterna.data.LanternState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * El Anillo Verde. Clic derecho sobre algo: rayo de selección que lo levanta (otro clic lo lanza).
 * Clic derecho o izquierdo al aire: pequeños proyectiles de energía. Muestra su energía en la barra.
 */
public class RingItem extends Item {
    public static final String CHARGE = "LinternaCarga";

    public RingItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) Holding.use(sp);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity e, int slot, boolean selected) {
        if (!level.isClientSide && e instanceof ServerPlayer p && p.tickCount % 20 == 0) {
            int pct = LanternLogic.pct(LanternData.get(p));
            if (stack.getOrCreateTag().getInt(CHARGE) != pct) stack.getOrCreateTag().putInt(CHARGE, pct);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(CHARGE);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * Math.min(100, stack.getOrCreateTag().getInt(CHARGE)) / 100F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x4CFF6A;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, net.minecraft.world.entity.item.ItemEntity entity) {
        entity.setUnlimitedLifetime();        // el anillo tirado nunca desaparece
        if (entity.getY() < entity.level().getMinBuildHeight() + 2) entity.setPos(entity.getX(), entity.level().getMinBuildHeight() + 70, entity.getZ());
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains(CHARGE)) tip.add(Component.literal("§aEnergía: §f" + stack.getTag().getInt(CHARGE) + "%"));
        tip.add(Component.literal("§aClic derecho sobre algo: rayo de selección (lo levanta; otro clic lo lanza)"));
        tip.add(Component.literal("§aClic al aire: dispara proyectiles de energía"));
        tip.add(Component.literal("§2Siempre te protege: fuego, caídas, ahogo y golpes mortales"));
        tip.add(Component.literal("§7Póntelo en la §fRanura del anillo§7 (catálogo M o botón ◎ del inventario)"));
        tip.add(Component.literal("§7L volar · Y armadura · Alt+P pico · C intangible"));
        tip.add(Component.literal("§7V armas · Alt+M martillo · Alt+T traje · M catálogo"));
        tip.add(Component.literal("§2Recárgalo con la Lámpara de Poder"));
    }

    public static int max() { return LanternState.MAX_CHARGE; }
}
