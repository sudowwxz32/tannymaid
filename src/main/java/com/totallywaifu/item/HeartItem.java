package com.totallywaifu.item;

import com.totallywaifu.data.Collection;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Corazón de una chica (Kozue, Homura, Kura o Shiori): úsalo en cualquier lugar para que despierte.
 * Cada chica se consigue UNA sola vez y queda en tu colección (♥ Mis Waifus, en el inventario).
 * El "Corazón de Waifu" genérico solo aparece cuando una chica se guarda en un corazón (por ejemplo, si cae sin su dueño).
 */
public class HeartItem extends Item {
    private static final String KEY = "Waifu", KIND = "WaifuKind";
    @Nullable private final WaifuKind kind;

    public HeartItem(@Nullable WaifuKind kind, Properties props) {
        super(props);
        this.kind = kind;
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
    private WaifuKind kindOf(ItemStack st) {
        if (kind != null) return kind;
        return st.hasTag() && st.getTag().contains(KEY) ? WaifuKind.byId(st.getTag().getInt(KIND)) : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
        return awaken(ctx.getLevel(), ctx.getPlayer(), ctx.getItemInHand(), Vec3.atBottomCenterOf(at)) ? InteractionResult.CONSUME
                : ctx.getLevel().isClientSide ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        ItemStack st = p.getItemInHand(hand);
        Vec3 at = p.position().add(p.getLookAngle().x * 1.6, 0, p.getLookAngle().z * 1.6);
        return awaken(level, p, st, at) ? InteractionResultHolder.consume(st) : InteractionResultHolder.pass(st);
    }

    private boolean awaken(Level level, @Nullable Player p, ItemStack st, Vec3 at) {
        WaifuKind k = kindOf(st);
        if (k == null || level.isClientSide || !(p instanceof ServerPlayer sp)) return false;
        boolean stored = st.hasTag() && st.getTag().contains(KEY);
        if (!stored && Collection.has(sp, k)) {
            sp.displayClientMessage(Component.literal("§dYa tienes a " + k.display + ": búscala en ♥ Mis Waifus (pestaña del inventario)."), true);
            return false;
        }
        ServerLevel lv = (ServerLevel) level;
        WaifuEntity w = ModRegistry.TYPES.get(k).get().create(lv);
        if (w == null) return false;
        if (stored) {
            w.load(st.getTag().getCompound(KEY));
            w.setUUID(UUID.randomUUID());
        }
        float yaw = sp.getYRot() + 180;
        w.moveTo(at.x, at.y, at.z, yaw, 0);
        w.setYHeadRot(yaw);
        w.setYBodyRot(yaw);
        w.setHealth(w.getMaxHealth());
        w.setOwner(sp.getUUID());
        lv.addFreshEntity(w);
        Collection.track(sp, w);
        lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 1.4, w.getZ(), 14, 0.6, 0.7, 0.6, 0);
        lv.sendParticles(ParticleTypes.END_ROD, w.getX(), w.getY() + 1, w.getZ(), 30, 0.4, 0.9, 0.4, 0.08);
        lv.playSound(null, w.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.5F, 1.2F);
        lv.playSound(null, w.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.6F);
        w.cheer(100);
        if (stored) {
            w.say(sp, "¡Ya volví! ♥");
        } else {
            w.say(sp, "¡Hola! Soy " + k.display + ", tu " + k.role.toLowerCase() + ". ¡Cuenta conmigo!");
            sp.sendSystemMessage(Component.literal("§7  " + k.job + "."));
            sp.sendSystemMessage(Component.literal("§7  Clic derecho: §f" + k.role + "§7 · Shift + clic derecho: §fsu panel§7 · Inventario → §d♥ Mis Waifus§7 para llamarla o guardarla."));
        }
        if (!sp.getAbilities().instabuild) st.shrink(1);
        return true;
    }

    @Override
    public boolean isFoil(ItemStack st) {
        return st.hasTag() && st.getTag().contains(KEY);
    }

    @Override
    public void appendHoverText(ItemStack st, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        WaifuKind k = kindOf(st);
        if (k == null) return;
        if (st.hasTag() && st.getTag().contains(KEY)) {
            tip.add(Component.literal("§dGuarda a " + k.display + " (con su mochila y todo). Úsalo para que vuelva."));
            return;
        }
        tip.add(Component.literal(k.color + k.display + " §7· " + k.role));
        tip.add(Component.literal("§dÚsalo en cualquier lugar para despertarla (solo una vez)."));
        tip.add(Component.literal("§7" + k.job + "."));
    }
}
