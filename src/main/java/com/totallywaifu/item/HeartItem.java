package com.totallywaifu.item;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Corazón de Waifu. Úsalo sobre una mesa de crafteo, un horno, un cofre o una mesa de encantamientos colocados:
 * el bloque despierta como una waifu (con lo que tenía dentro). Si el corazón ya guarda a una waifu, úsalo en el suelo para que vuelva.
 */
public class HeartItem extends Item {
    private static final String KEY = "Waifu", KIND = "WaifuKind";

    public HeartItem(Properties props) {
        super(props);
    }

    public static ItemStack withWaifu(WaifuEntity w) {
        ItemStack st = new ItemStack(ModRegistry.HEART.get());
        CompoundTag data = new CompoundTag();
        w.saveWithoutId(data);
        st.getOrCreateTag().put(KEY, data);
        st.getOrCreateTag().putInt(KIND, w.kind.ordinal());
        st.setHoverName(Component.literal(w.kind.color + "Corazón de " + w.kind.display));
        return st;
    }

    @Nullable
    private static WaifuKind stored(ItemStack st) {
        return st.hasTag() && st.getTag().contains(KEY) ? WaifuKind.byId(st.getTag().getInt(KIND)) : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player p = ctx.getPlayer();
        ItemStack st = ctx.getItemInHand();
        BlockPos pos = ctx.getClickedPos();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ServerLevel lv = (ServerLevel) level;
        WaifuKind saved = stored(st);
        if (saved != null) {
            // vuelve la waifu guardada, con su mochila y todo
            BlockPos at = pos.relative(ctx.getClickedFace());
            WaifuEntity w = ModRegistry.TYPES.get(saved).get().create(lv);
            if (w == null) return InteractionResult.FAIL;
            w.load(st.getTag().getCompound(KEY));
            w.setUUID(UUID.randomUUID());
            w.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p == null ? 0 : p.getYRot() + 180, 0);
            w.setHealth(w.getMaxHealth());
            if (w.getOwner() == null && p != null) w.setOwner(p.getUUID());
            lv.addFreshEntity(w);
            awaken(lv, w, p, false);
            if (p == null || !p.getAbilities().instabuild) st.shrink(1);
            return InteractionResult.CONSUME;
        }
        BlockState bs = level.getBlockState(pos);
        WaifuKind kind = WaifuKind.fromBlock(bs.getBlock());
        if (kind == null) {
            if (p != null) p.displayClientMessage(Component.literal("§dÚsalo sobre una mesa de crafteo, un horno, un cofre o una mesa de encantamientos."), true);
            return InteractionResult.FAIL;
        }
        WaifuEntity w = ModRegistry.TYPES.get(kind).get().create(lv);
        if (w == null) return InteractionResult.FAIL;
        // lo que había dentro del bloque pasa a ella
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractFurnaceBlockEntity f) {
            for (int i = 0; i < 3; i++) w.furnace.setItem(i, f.getItem(i).copy());
            f.clearContent();
        } else if (be instanceof Container c && kind == WaifuKind.KURA) {
            for (int i = 0; i < c.getContainerSize() && i < w.chest.getContainerSize(); i++) w.chest.setItem(i, c.getItem(i).copy());
            c.clearContent();
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        float yaw = p == null ? 0 : p.getYRot() + 180;
        w.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        w.setYHeadRot(yaw);
        w.setYBodyRot(yaw);
        if (p != null) w.setOwner(p.getUUID());
        lv.addFreshEntity(w);
        awaken(lv, w, p, true);
        if (p == null || !p.getAbilities().instabuild) st.shrink(1);
        return InteractionResult.CONSUME;
    }

    private static void awaken(ServerLevel lv, WaifuEntity w, @Nullable Player p, boolean first) {
        lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 1.2, w.getZ(), 14, 0.6, 0.7, 0.6, 0);
        lv.sendParticles(ParticleTypes.END_ROD, w.getX(), w.getY() + 1, w.getZ(), 30, 0.4, 0.9, 0.4, 0.08);
        lv.sendParticles(ParticleTypes.POOF, w.getX(), w.getY() + 0.5, w.getZ(), 20, 0.4, 0.4, 0.4, 0.02);
        lv.playSound(null, w.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.5F, 1.2F);
        lv.playSound(null, w.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.6F);
        w.cheer(100);
        if (p != null) {
            if (first) {
                w.say(p, "¡¿Eh?! ¡Puedo moverme! Soy " + w.kind.display + ", tu " + w.kind.role.toLowerCase() + ". ¡Cuenta conmigo!");
                p.sendSystemMessage(Component.literal("§7  " + w.kind.job + "."));
                p.sendSystemMessage(Component.literal("§7  Clic derecho: §f" + w.kind.role + "§7 · Shift + clic derecho: §fsu panel§7 (mochila y modos Seguir / Quieta / Trabajar)"));
            } else {
                w.say(p, "¡Ya volví! ♥");
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack st) {
        return stored(st) != null;
    }

    @Override
    public void appendHoverText(ItemStack st, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        WaifuKind k = stored(st);
        if (k != null) {
            tip.add(Component.literal("§dGuarda a " + k.display + " (con su mochila y todo)."));
            tip.add(Component.literal("§7Úsalo en el suelo para que vuelva."));
        } else {
            tip.add(Component.literal("§dÚsalo sobre una mesa de crafteo, un horno, un cofre"));
            tip.add(Component.literal("§do una mesa de encantamientos: ¡despertará como waifu!"));
        }
    }
}
