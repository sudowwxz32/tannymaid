package com.totallywaifu.item;

import com.totallywaifu.data.Collection;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Vara de Transformación.
 * <ul>
 *   <li>Clic derecho a una mesa de crafteo, horno, cofre o mesa de encantamientos: se transforma en su chica (y la desbloqueas).</li>
 *   <li>Clic derecho a esa chica: vuelve a ser el bloque (con lo que tenía dentro).</li>
 *   <li>Clic derecho a un animal o monstruo: se transforma en chica PARA SIEMPRE.</li>
 * </ul>
 */
public class WandItem extends Item {
    public WandItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isFoil(ItemStack st) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        WaifuKind kind = WaifuKind.fromBlock(state.getBlock());
        if (kind == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(ctx.getPlayer() instanceof ServerPlayer p)) return InteractionResult.FAIL;
        ServerLevel lv = (ServerLevel) level;
        if (kind == WaifuKind.DINA) return tnt(lv, p, pos);
        if (Collection.active(p, kind)) {
            p.displayClientMessage(Component.literal("§dYa tienes a " + kind.display + ". Destransfórmala (vara sobre ella) o búscala en ♥ Mis Waifus."), true);
            return InteractionResult.FAIL;
        }
        WaifuEntity w = ModRegistry.TYPES.get(kind).get().create(lv);
        if (w == null) return InteractionResult.FAIL;
        CompoundTag saved = Collection.saved(p, kind);
        if (saved != null) {                          // vuelve con su mochila y todas sus cosas
            w.load(saved);
            w.setUUID(UUID.randomUUID());
            w.setHealth(w.getMaxHealth());
        }
        BlockPos at = pos;
        if (kind == WaifuKind.CAMA) {
            BedPart part = state.getValue(BedBlock.PART);
            Direction facing = state.getValue(BedBlock.FACING);
            BlockPos other = part == BedPart.FOOT ? pos.relative(facing) : pos.relative(facing.getOpposite());
            at = part == BedPart.FOOT ? pos : other;
            w.getPersistentData().putInt("TW_BedColor", ((BedBlock) state.getBlock()).getColor().getId());
            level.setBlock(other, Blocks.AIR.defaultBlockState(), 2 | 16);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractFurnaceBlockEntity f) {
                for (int i = 0; i < 3; i++) w.furnace.setItem(i, f.getItem(i).copy());
                f.clearContent();
            } else if (be instanceof Container c && kind == WaifuKind.KURA) {
                for (int i = 0; i < c.getContainerSize() && i < w.chest.getContainerSize(); i++) {
                    ItemStack s = c.getItem(i);
                    if (s.isEmpty()) continue;
                    ItemStack rest = w.chest.addItem(s.copy());
                    if (!rest.isEmpty()) w.spawnAtLocation(rest);
                }
                c.clearContent();
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        place(lv, p, w, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p.getYRot() + 180);
        w.say(p, saved != null ? "¡Volví! Y todavía tengo todas mis cosas ♥" : "¡¿Eh?! ¡Puedo moverme! Soy " + kind.display + ", tu " + kind.role.toLowerCase() + ". ¡Cuenta conmigo!");
        tips(p, kind, true);
        return InteractionResult.CONSUME;
    }

    /** Un bloque de TNT: ¡aparecen las hermanas Dina y Mita! (para siempre) */
    private static InteractionResult tnt(ServerLevel lv, ServerPlayer p, BlockPos pos) {
        lv.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        float yaw = p.getYRot() + 180;
        double fx = Math.cos(Math.toRadians(yaw)), fz = Math.sin(Math.toRadians(yaw));
        WaifuEntity a = ModRegistry.TYPES.get(WaifuKind.DINA).get().create(lv), b = ModRegistry.TYPES.get(WaifuKind.MITA).get().create(lv);
        if (a == null || b == null) return InteractionResult.FAIL;
        place(lv, p, a, pos.getX() + 0.5 + fx * 0.5, pos.getY(), pos.getZ() + 0.5 + fz * 0.5, yaw);
        place(lv, p, b, pos.getX() + 0.5 - fx * 0.5, pos.getY(), pos.getZ() + 0.5 - fz * 0.5, yaw);
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
        lv.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.2F, 1.3F);
        a.say(p, "¡BOOM! Soy Dina, la mayor. ¡Explotamos más fuerte que nadie! ♥");
        b.say(p, "¡Y yo soy Mita! ¡Les lanzamos explosivos a los monstruos!");
        tips(p, WaifuKind.DINA, false);
        return InteractionResult.CONSUME;
    }

    /** Transforma una criatura en chica (para siempre) o devuelve a una chica de mesa a su bloque. */
    public static boolean useOnEntity(ServerPlayer p, Entity target) {
        ServerLevel lv = p.serverLevel();
        if (target instanceof WaifuEntity w) {
            if (!w.kind.station()) {
                p.displayClientMessage(Component.literal(w.kind.color + w.kind.display + " §7ya no puede volver a su forma anterior: se queda así para siempre ♥"), true);
                return true;
            }
            if (!w.isOwner(p)) {
                p.displayClientMessage(Component.literal("§7Solo su dueño puede destransformarla."), true);
                return true;
            }
            revert(lv, p, w);
            return true;
        }
        WaifuKind kind = WaifuKind.fromEntity(target);
        if (kind == null || !(target instanceof Mob mob) || !mob.isAlive()) return false;
        if (kind.surprise() && !Collection.surprisesUnlocked(p)) {
            lv.sendParticles(ParticleTypes.ENCHANT, mob.getX(), mob.getY() + 1, mob.getZ(), 20, 0.4, 0.4, 0.4, 0.5);
            p.displayClientMessage(Component.literal("§d✦ Esta criatura guarda un secreto... completa tu colección de ♥ Mis Waifus para despertarla."), true);
            return true;
        }
        if (kind.legend() && !Collection.extrasComplete(p)) {
            lv.sendParticles(ParticleTypes.ENCHANT, mob.getX(), mob.getY() + 1, mob.getZ(), 30, 0.5, 0.5, 0.5, 0.6);
            p.displayClientMessage(Component.literal("§6☀ Esta criatura es una LEYENDA... completa los extras de ♥ Mis Waifus para despertarla."), true);
            return true;
        }
        WaifuEntity w = ModRegistry.TYPES.get(kind).get().create(lv);
        if (w == null) return false;
        if (mob instanceof Villager v) {
            MerchantOffers o = v.getOffers();
            if (!o.isEmpty()) w.overrideOffers(new MerchantOffers(o.createTag()));
        }
        if (mob.hasCustomName()) w.setCustomName(mob.getCustomName());
        double x = mob.getX(), y = mob.getY(), z = mob.getZ();
        float yaw = mob.getYRot();
        mob.discard();
        place(lv, p, w, x, y, z, yaw);
        lv.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y + 1, z, 40, 0.4, 0.8, 0.4, 0.3);
        w.say(p, "¡¿Qué me pasó?! ...Bueno, me gusta. Soy " + kind.display + " ♥");
        tips(p, kind, false);
        return true;
    }

    private static void place(ServerLevel lv, ServerPlayer p, WaifuEntity w, double x, double y, double z, float yaw) {
        w.moveTo(x, y, z, yaw, 0);
        w.setYHeadRot(yaw);
        w.setYBodyRot(yaw);
        w.setOwner(p.getUUID());
        lv.addFreshEntity(w);
        Collection.track(p, w);
        lv.sendParticles(ParticleTypes.HEART, x, y + 1.4, z, 14, 0.6, 0.7, 0.6, 0);
        lv.sendParticles(ParticleTypes.END_ROD, x, y + 1, z, 40, 0.4, 0.9, 0.4, 0.08);
        lv.sendParticles(ParticleTypes.POOF, x, y + 0.5, z, 20, 0.4, 0.4, 0.4, 0.02);
        lv.playSound(null, w.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.5F, 1.2F);
        lv.playSound(null, w.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.0F, 1.4F);
        w.cheer(100);
    }

    private static void tips(ServerPlayer p, WaifuKind k, boolean station) {
        p.sendSystemMessage(Component.literal("§7  " + k.job + "."));
        p.sendSystemMessage(Component.literal(station
                ? "§7  Clic derecho: §f" + k.role + "§7 · Shift + clic: §fsu panel§7 · Vara sobre ella: §fvuelve a ser bloque§7 · Inventario → §d♥ Mis Waifus"
                : "§7  Clic derecho: §fsu panel§7 (o su habilidad) · Inventario → §d♥ Mis Waifus§7 para llamarla o guardarla"));
    }

    /** La chica de mesa vuelve a ser su bloque. Lo que lleva en su mochila se queda CON ELLA (vuelve cuando la despiertes). */
    private static void revert(ServerLevel lv, ServerPlayer p, WaifuEntity w) {
        BlockPos at = free(lv, w.blockPosition()), second = null;
        if (at == null) {
            p.displayClientMessage(Component.literal("§7No hay espacio libre para el bloque aquí."), true);
            return;
        }
        if (w.kind == WaifuKind.CAMA) {
            Direction dir = null;
            Direction look = Direction.fromYRot(w.getYRot());
            for (Direction d : new Direction[]{look, look.getClockWise(), look.getCounterClockWise(), look.getOpposite()}) {
                BlockState st = lv.getBlockState(at.relative(d));
                if (st.isAir() || st.canBeReplaced()) { dir = d; break; }
            }
            if (dir == null) {
                p.displayClientMessage(Component.literal("§7Una cama necesita dos bloques libres."), true);
                return;
            }
            DyeColor color = DyeColor.byId(w.getPersistentData().contains("TW_BedColor") ? w.getPersistentData().getInt("TW_BedColor") : DyeColor.PINK.getId());
            Block bed = BED_BY_COLOR.getOrDefault(color, Blocks.PINK_BED);
            second = at.relative(dir);
            BlockState foot = bed.defaultBlockState().setValue(BedBlock.FACING, dir).setValue(BedBlock.PART, BedPart.FOOT);
            lv.setBlock(at, foot, 3);
            lv.setBlock(second, foot.setValue(BedBlock.PART, BedPart.HEAD), 3);
        } else {
            BlockState bs = w.kind.block.defaultBlockState();
            if (bs.hasProperty(net.minecraft.world.level.block.GrindstoneBlock.FACE))
                bs = bs.setValue(net.minecraft.world.level.block.GrindstoneBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR);
            if (bs.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING))
                bs = bs.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, p.getDirection().getOpposite());
            lv.setBlock(at, bs, 3);
            BlockEntity be = lv.getBlockEntity(at);
            if (be instanceof AbstractFurnaceBlockEntity f) {
                for (int i = 0; i < 3; i++) f.setItem(i, w.furnace.getItem(i).copy());
                w.furnace.clearContent();
            } else if (be instanceof Container c && w.kind == WaifuKind.KURA) {
                for (int i = 0; i < c.getContainerSize(); i++) {        // lo que no quepa sigue en el cofre de ella
                    c.setItem(i, w.chest.getItem(i).copy());
                    w.chest.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        if (w.storedXp > 0) {
            ExperienceOrb.award(lv, w.position(), w.storedXp);
            w.storedXp = 0;
        }
        lv.sendParticles(ParticleTypes.POOF, w.getX(), w.getY() + 0.8, w.getZ(), 25, 0.4, 0.6, 0.4, 0.02);
        lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 1.2, w.getZ(), 6, 0.4, 0.4, 0.4, 0);
        lv.playSound(null, at, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.2F, 0.9F);
        w.say(p, "¡Hasta pronto! Vuelvo a ser tu " + w.kind.role.toLowerCase() + ". Mis cosas se quedan conmigo ♥");
        Collection.markBlock(p, w, at, second);
        w.discard();
    }

    private static final java.util.Map<DyeColor, Block> BED_BY_COLOR = new java.util.EnumMap<>(DyeColor.class);

    static {
        for (Block b : new Block[]{Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED,
                Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED})
            BED_BY_COLOR.put(((BedBlock) b).getColor(), b);
    }

    @Nullable
    private static BlockPos free(ServerLevel lv, BlockPos c) {
        for (BlockPos q : new BlockPos[]{c, c.above(), c.north(), c.south(), c.east(), c.west()}) {
            BlockState st = lv.getBlockState(q);
            if (st.isAir() || st.canBeReplaced()) return q;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack st, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§dClic derecho a una mesa de crafteo, horno, cofre, mesa de encantamientos,"));
        tip.add(Component.literal("§dcama, yunque, afiladora, telar, cortapiedras, mesa de herrería, mesa de"));
        tip.add(Component.literal("§dcartografía, ahumador o alto horno: ¡se transforma en chica!"));
        tip.add(Component.literal("§7Sobre ella otra vez: vuelve a ser el bloque."));
        tip.add(Component.literal("§5Sobre animales y monstruos: se transforman en chicas PARA SIEMPRE."));
        tip.add(Component.literal("§8Oveja, vaca, cerdo, gallina, perro, gato, gólem, aldeano, zombi,"));
        tip.add(Component.literal("§8esqueleto, creeper, araña, bruja, enderman, slime, conejo, panda,"));
        tip.add(Component.literal("§8caballo, abeja, piglin, esqueleto del Nether, blaze, ghast, warden..."));
        tip.add(Component.literal("§c¡Y sobre un bloque de TNT aparecen las hermanas Dina y Mita!"));
        tip.add(Component.literal("§8Nunca la pierdes: si mueres, se queda contigo."));
    }
}
