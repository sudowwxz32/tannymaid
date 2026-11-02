package com.bdsz.companion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Set;

/**
 * La cápsula de Kaori: la lanza y aparece una choza pequeña de 7×7 amueblada (cama, cofre, horno, mesa de crafteo,
 * farol y alfombra). Si la vuelves a usar, la choza se guarda otra vez en la cápsula (lo del cofre pasa a su mochila).
 */
public final class HutBuilder {
    private HutBuilder() {}

    private static final int HALF = 3, HEIGHT = 5;

    public static void toggle(ServerPlayer p, KaoriEntity k) {
        if (k.hutPos != null) {
            pack(p, k);
            return;
        }
        ServerLevel lv = (ServerLevel) k.level();
        Direction front = p.getDirection().getOpposite();               // la puerta mira hacia ti
        BlockPos c = p.blockPosition().relative(p.getDirection(), 6);
        int y = lv.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, c.getX(), c.getZ()) - 1;
        BlockPos center = new BlockPos(c.getX(), y, c.getZ());
        if (Math.abs(y - p.getBlockY()) > 6 || !fits(lv, center)) {
            k.say(p, "Aquí no cabe la choza. Busca un lugar más plano y despejado, por favor. No hago milagros... casi.");
            return;
        }
        build(lv, center, front);
        k.hutPos = center;
        k.hutDir = front.get2DDataValue();
        k.hutDim = lv.dimension().location().toString();
        k.swing(InteractionHand.MAIN_HAND);
        BlockPos mid = center.above(2);
        lv.sendParticles(ParticleTypes.CLOUD, mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5, 160, 2.6, 2.0, 2.6, 0.04);
        lv.sendParticles(ParticleTypes.EXPLOSION, mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5, 3, 1.5, 1, 1.5, 0);
        lv.playSound(null, mid, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.5F, 0.8F);
        lv.playSound(null, mid, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.4F, 1.8F);
        k.say(p, "¡Cápsula, va! *¡POP!* Una choza con cama, cofre, horno y mesa de crafteo. Impresionante, ¿verdad? Lo sé.");
    }

    private static BlockPos at(BlockPos center, Direction front, int lx, int ly, int lz) {
        Direction fwd = front.getOpposite();                // de la puerta hacia el fondo
        Direction right = fwd.getClockWise();
        return center.relative(right, lx).relative(fwd, lz).above(ly);
    }

    private static boolean fits(ServerLevel lv, BlockPos center) {
        int solid = 0;
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                for (int dy = 1; dy <= HEIGHT; dy++) {
                    BlockPos q = center.offset(dx, dy, dz);
                    BlockState s = lv.getBlockState(q);
                    if (!s.isAir() && !s.canBeReplaced() && !(s.getBlock() instanceof LeavesBlock)) solid++;
                }
            }
        }
        return solid <= 2;
    }

    private static void set(ServerLevel lv, BlockPos p, BlockState s) {
        lv.setBlock(p, s, Block.UPDATE_ALL);
    }

    private static void build(ServerLevel lv, BlockPos c, Direction front) {
        Direction fwd = front.getOpposite();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState(), log = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),
                roof = Blocks.SPRUCE_PLANKS.defaultBlockState(), glass = Blocks.GLASS_PANE.defaultBlockState(),
                slab = Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        // limpiar y cimientos
        for (int lx = -HALF; lx <= HALF; lx++) {
            for (int lz = -HALF; lz <= HALF; lz++) {
                for (int ly = 1; ly <= HEIGHT + 1; ly++) set(lv, at(c, front, lx, ly, lz), Blocks.AIR.defaultBlockState());
                for (int ly = -1; ly >= -4; ly--) {
                    BlockPos q = at(c, front, lx, ly, lz);
                    if (lv.getBlockState(q).isAir() || lv.getBlockState(q).canBeReplaced()) set(lv, q, Blocks.COBBLESTONE.defaultBlockState());
                    else break;
                }
                set(lv, at(c, front, lx, 0, lz), planks);
            }
        }
        // paredes, esquinas y ventanas
        for (int ly = 1; ly <= 3; ly++) {
            for (int lx = -HALF; lx <= HALF; lx++) {
                for (int lz = -HALF; lz <= HALF; lz++) {
                    boolean edgeX = Math.abs(lx) == HALF, edgeZ = Math.abs(lz) == HALF;
                    if (!edgeX && !edgeZ) continue;
                    BlockPos q = at(c, front, lx, ly, lz);
                    if (edgeX && edgeZ) set(lv, q, log);
                    else if (ly == 2 && (Math.abs(lx) == 1 || Math.abs(lz) == 1) && !(lz == -HALF && lx == 0)) {
                        set(lv, q, glass);
                    } else set(lv, q, planks);
                }
            }
        }
        // techo con borde
        for (int lx = -HALF - 1; lx <= HALF + 1; lx++) {
            for (int lz = -HALF - 1; lz <= HALF + 1; lz++) {
                boolean rim = Math.abs(lx) == HALF + 1 || Math.abs(lz) == HALF + 1;
                set(lv, at(c, front, lx, 4, lz), rim ? slab : roof);
            }
        }
        for (int lx = -HALF + 1; lx <= HALF - 1; lx++) for (int lz = -HALF + 1; lz <= HALF - 1; lz++) set(lv, at(c, front, lx, 5, lz), slab);
        // puerta
        BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, fwd);
        set(lv, at(c, front, 0, 1, -HALF), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(lv, at(c, front, 0, 2, -HALF), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        set(lv, at(c, front, 0, 3, -HALF - 1), Blocks.LANTERN.defaultBlockState());
        set(lv, at(c, front, 0, 0, -HALF - 1), Blocks.SPRUCE_SLAB.defaultBlockState());
        // muebles
        BlockState bed = Blocks.ORANGE_BED.defaultBlockState().setValue(BedBlock.FACING, fwd);
        set(lv, at(c, front, -2, 1, 1), bed.setValue(BedBlock.PART, BedPart.FOOT));
        set(lv, at(c, front, -2, 1, 2), bed.setValue(BedBlock.PART, BedPart.HEAD));
        set(lv, at(c, front, 2, 1, 2), Blocks.CRAFTING_TABLE.defaultBlockState());
        set(lv, at(c, front, 1, 1, 2), Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, front));
        set(lv, at(c, front, 2, 1, 0), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, fwd.getCounterClockWise()));
        set(lv, at(c, front, 2, 1, -1), Blocks.BARREL.defaultBlockState());
        set(lv, at(c, front, 0, 1, 2), Blocks.SMOKER.defaultBlockState().setValue(FurnaceBlock.FACING, front));
        for (int lx = -1; lx <= 1; lx++) for (int lz = -1; lz <= 1; lz++) set(lv, at(c, front, lx, 1, lz), Blocks.ORANGE_CARPET.defaultBlockState());
        set(lv, at(c, front, 0, 3, 0), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        set(lv, at(c, front, -2, 1, -2), Blocks.POTTED_RED_TULIP.defaultBlockState());
        // regalo en el cofre: un poco de pan y antorchas
        if (lv.getBlockEntity(at(c, front, 2, 1, 0)) instanceof Container chest) {
            chest.setItem(0, new ItemStack(net.minecraft.world.item.Items.BREAD, 8));
            chest.setItem(1, new ItemStack(net.minecraft.world.item.Items.TORCH, 16));
            chest.setItem(2, new ItemStack(net.minecraft.world.item.Items.COAL, 8));
        }
    }

    private static final Set<Block> HUT_BLOCKS = Set.of(Blocks.OAK_PLANKS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.GLASS_PANE,
            Blocks.SPRUCE_SLAB, Blocks.SPRUCE_DOOR, Blocks.LANTERN, Blocks.ORANGE_BED, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.CHEST,
            Blocks.BARREL, Blocks.SMOKER, Blocks.ORANGE_CARPET, Blocks.POTTED_RED_TULIP);

    private static void pack(ServerPlayer p, KaoriEntity k) {
        ServerLevel lv = (ServerLevel) k.level();
        if (!lv.dimension().location().toString().equals(k.hutDim) || k.hutPos.distSqr(k.blockPosition()) > 48 * 48) {
            k.say(p, "Mi choza está lejos. Llévame cerca de ella para guardarla en la cápsula.");
            return;
        }
        BlockPos c = k.hutPos;
        Direction front = Direction.from2DDataValue(k.hutDir);
        int moved = 0;
        for (int ly = 5; ly >= 0; ly--) {
            for (int lx = -HALF - 1; lx <= HALF + 1; lx++) {
                for (int lz = -HALF - 1; lz <= HALF + 1; lz++) {
                    BlockPos q = at(c, front, lx, ly, lz);
                    BlockState s = lv.getBlockState(q);
                    if (!HUT_BLOCKS.contains(s.getBlock())) continue;
                    BlockEntity be = lv.getBlockEntity(q);
                    if (be instanceof Container ct) {
                        for (int i = 0; i < ct.getContainerSize(); i++) {
                            ItemStack it = ct.removeItemNoUpdate(i);
                            if (!it.isEmpty()) { k.storeOrDrop(it); moved++; }
                        }
                    }
                    lv.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
        BlockPos mid = c.above(2);
        lv.sendParticles(ParticleTypes.CLOUD, mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5, 140, 2.6, 2.0, 2.6, 0.04);
        lv.playSound(null, mid, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.2F, 1.4F);
        k.hutPos = null;
        k.swing(InteractionHand.MAIN_HAND);
        k.say(p, "Choza guardada en la cápsula." + (moved > 0 ? " Lo que había en el cofre está en mi mochila." : "") + " Ordenado, como debe ser.");
    }
}
