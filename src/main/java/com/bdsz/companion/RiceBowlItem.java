package com.bdsz.companion;

import com.bdsz.data.SaiyanData;
import com.bdsz.data.SaiyanState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Tazón de arroz de Kaori: recupera mucha vida, ki y hambre, y el hambre tarda en volver. */
public class RiceBowlItem extends Item {
    public RiceBowlItem(Properties props) {
        super(props);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity e) {
        if (!level.isClientSide && e instanceof ServerPlayer p) {
            p.heal(10.0F);
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            p.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, false, false));
            // el hambre tarda en volver: 5 minutos sin gastar comida
            KaoriActions.data(p).putLong("KaoriFullUntil", level.getGameTime() + 6000);
            SaiyanState s = SaiyanData.get(p);
            if (s.saiyan) {
                s.ki = Math.min(s.maxKi(), s.ki + Math.max(100, s.maxKi() / 2));
                SaiyanData.set(p, s);
            }
        }
        ItemStack rest = super.finishUsingItem(stack, level, e);
        if (e instanceof Player p && !p.getAbilities().instabuild) {
            if (rest.isEmpty()) return new ItemStack(Items.BOWL);
            if (!p.getInventory().add(new ItemStack(Items.BOWL))) p.drop(new ItemStack(Items.BOWL), false);
        }
        return rest;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7Cocinado por Kaori"));
        tip.add(Component.literal("§a+10 de vida, regeneración y la mitad de tu ki"));
        tip.add(Component.literal("§6El hambre tarda mucho en volver"));
    }
}
