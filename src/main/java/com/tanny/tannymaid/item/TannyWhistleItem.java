package com.tanny.tannymaid.item;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannyLookup;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Campanita de Tanny: clic derecho la llama a tu lado; Shift + clic derecho abre su menú desde donde sea. */
public class TannyWhistleItem extends Item {
    public TannyWhistleItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(stack);
        player.getCooldowns().addCooldown(this, 20);
        TannyEntity tanny = TannyLookup.find(sp);
        if (tanny == null) {
            TannySpeech.system(sp, "Tanny no está cargada ahora. Usa un §dContrato de Tanny Maid§7 o acércate a donde la dejaste.");
            return InteractionResultHolder.consume(stack);
        }
        if (player.isShiftKeyDown()) tanny.openInventory(sp);
        else tanny.commandTp(sp);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7Clic derecho: §fTanny viene a tu lado"));
        tip.add(Component.literal("§7Shift + clic derecho: §fabre su menú"));
        tip.add(Component.literal("§8Atajo: tecla G (configurable) abre su menú"));
    }
}
