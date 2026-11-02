package com.bdsz.companion;

import com.bdsz.registry.ModRegistry;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashSet;
import java.util.Set;

/**
 * Las cápsulas-casa de Kaori. La choza está disponible desde el principio; las demás se desbloquean pasando tiempo
 * con ella. Al usar la cápsula otra vez, la casa se guarda (lo de los cofres pasa a su mochila).
 * <ol start="0">
 *   <li>Choza (7×7): cama, cofre, horno, ahumador, barril, mesa de crafteo.</li>
 *   <li>Casa segura de piedra (9×9): puerta de hierro con botones, 2 camas, yunque, mesa de herrería, alto horno,
 *       caldero, soporte de pociones.</li>
 *   <li>Casa del Guerrero Z (11×11): Generador Protector con energía, huerto de Semillas del Ermitaño, tazones de arroz.</li>
 *   <li>Base Cápsula (13×13): todo lo anterior más mesa de encantamientos con librerías y huerto de trigo.</li>
 * </ol>
 */
public final class HutBuilder {
    private HutBuilder() {}

    public static final String[] NAMES = {"Choza", "Casa segura de piedra", "Casa del Guerrero Z", "Base Cápsula"};

    private static int half(int type) { return 3 + type; }

    private static int wallH(int type) { return type == 0 ? 3 : 4; }

    public static boolean unlocked(ServerPlayer p, int type) {
        return type >= 0 && type < NAMES.length && KaoriEntity.bond(p) >= KaoriEntity.HOUSE_BOND[type];
    }

    public static void toggle(ServerPlayer p, KaoriEntity k, int type) {
        if (k.hutPos != null) {
            pack(p, k);
            return;
        }
        type = Math.max(0, Math.min(NAMES.length - 1, type));
        if (!unlocked(p, type)) {
            long left = KaoriEntity.HOUSE_BOND[type] - KaoriEntity.bond(p);
            k.say(p, "¿La " + NAMES[type] + "? Todavía no. Pasa más tiempo conmigo: " + KaoriActions.time(left) + " más.");
            return;
        }
        ServerLevel lv = (ServerLevel) k.level();
        int h = half(type);
        Direction front = p.getDirection().getOpposite();               // la puerta mira hacia ti
        BlockPos c = p.blockPosition().relative(p.getDirection(), h + 3);
        int y = lv.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, c.getX(), c.getZ()) - 1;
        BlockPos center = new BlockPos(c.getX(), y, c.getZ());
        if (Math.abs(y - p.getBlockY()) > 6 || !fits(lv, center, type)) {
            k.say(p, "Aquí no cabe la " + NAMES[type] + ". Busca un lugar más plano y despejado, por favor. No hago milagros... casi.");
            return;
        }
        build(lv, center, front, type);
        k.hutPos = center;
        k.hutDir = front.get2DDataValue();
        k.hutType = type;
        k.hutDim = lv.dimension().location().toString();
        k.swing(InteractionHand.MAIN_HAND);
        BlockPos mid = center.above(2);
        lv.sendParticles(ParticleTypes.CLOUD, mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5, 160 + type * 60, h, 2.0, h, 0.04);
        lv.sendParticles(ParticleTypes.EXPLOSION, mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5, 3 + type, h * 0.5, 1, h * 0.5, 0);
        lv.playSound(null, mid, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.5F, 0.8F);
        lv.playSound(null, mid, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.4F, 1.8F);
        k.say(p, switch (type) {
            case 1 -> "¡Cápsula, va! *¡POP!* Casa segura de piedra: puerta de hierro, nada entra sin permiso. Ni los zombis.";
            case 2 -> "¡Cápsula, va! *¡POP!* La Casa del Guerrero Z: con Generador Protector encendido y huerto de semillas. Impresionante, lo sé.";
            case 3 -> "¡Cápsula, va! *¡POP!* Mi obra maestra: la Base Cápsula. Encantamientos, pociones, huertos... todo. Aplaude.";
            default -> "¡Cápsula, va! *¡POP!* Una choza con cama, cofre, horno y mesa de crafteo. Impresionante, ¿verdad? Lo sé.";
        });
    }

    private static BlockPos at(BlockPos center, Direction front, int lx, int ly, int lz) {
        Direction fwd = front.getOpposite();                // de la puerta hacia el fondo
        Direction right = fwd.getClockWise();
        return center.relative(right, lx).relative(fwd, lz).above(ly);
    }

    private static boolean fits(ServerLevel lv, BlockPos center, int type) {
        int h = half(type), solid = 0;
        for (int dx = -h; dx <= h; dx++) {
            for (int dz = -h; dz <= h; dz++) {
                for (int dy = 1; dy <= wallH(type) + 2; dy++) {
                    BlockState s = lv.getBlockState(center.offset(dx, dy, dz));
                    if (!s.isAir() && !s.canBeReplaced() && !(s.getBlock() instanceof LeavesBlock)) solid++;
                }
            }
        }
        return solid <= 2 + type * 2;
    }

    private static void set(ServerLevel lv, BlockPos p, BlockState s) {
        lv.setBlock(p, s, Block.UPDATE_ALL);
    }

    private record Mat(BlockState floor, BlockState wall, BlockState corner, BlockState roof, BlockState slab, BlockState window, BlockState trim) {}

    private static Mat mat(int type) {
        return switch (type) {
            case 1 -> new Mat(Blocks.POLISHED_ANDESITE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.STONE_BRICK_SLAB.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            case 2 -> new Mat(Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.WHITE_CONCRETE.defaultBlockState(),
                    Blocks.ORANGE_CONCRETE.defaultBlockState(), Blocks.ORANGE_CONCRETE.defaultBlockState(),
                    Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(), Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), Blocks.BLUE_CONCRETE.defaultBlockState());
            case 3 -> new Mat(Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Blocks.WHITE_CONCRETE.defaultBlockState(),
                    Blocks.RED_CONCRETE.defaultBlockState(), Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
                    Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(), Blocks.ORANGE_CONCRETE.defaultBlockState());
            default -> new Mat(Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(),
                    Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(),
                    Blocks.SPRUCE_SLAB.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState());
        };
    }

    private static void build(ServerLevel lv, BlockPos c, Direction front, int type) {
        Direction fwd = front.getOpposite();
        Mat m = mat(type);
        int h = half(type), wh = wallH(type);
        BlockState slab = m.slab.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        // limpiar y cimientos
        for (int lx = -h - 1; lx <= h + 1; lx++) {
            for (int lz = -h - 1; lz <= h + 1; lz++) {
                boolean in = Math.abs(lx) <= h && Math.abs(lz) <= h;
                for (int ly = 1; ly <= wh + 3; ly++) {
                    BlockPos q = at(c, front, lx, ly, lz);
                    if (in || !lv.getBlockState(q).isAir() && lv.getBlockState(q).canBeReplaced()) set(lv, q, Blocks.AIR.defaultBlockState());
                }
                if (!in) continue;
                for (int ly = -1; ly >= -5; ly--) {
                    BlockPos q = at(c, front, lx, ly, lz);
                    if (lv.getBlockState(q).isAir() || lv.getBlockState(q).canBeReplaced()) set(lv, q, Blocks.COBBLESTONE.defaultBlockState());
                    else break;
                }
                boolean edge = Math.abs(lx) == h || Math.abs(lz) == h;
                set(lv, at(c, front, lx, 0, lz), edge && type > 0 ? m.trim : m.floor);
            }
        }
        // paredes, esquinas, franja y ventanas
        for (int ly = 1; ly <= wh; ly++) {
            for (int lx = -h; lx <= h; lx++) {
                for (int lz = -h; lz <= h; lz++) {
                    boolean ex = Math.abs(lx) == h, ez = Math.abs(lz) == h;
                    if (!ex && !ez) continue;
                    BlockPos q = at(c, front, lx, ly, lz);
                    int along = ex ? lz : lx;
                    if (ex && ez) set(lv, q, m.corner);
                    else if ((ly == 2 || (wh >= 4 && ly == 3)) && Math.abs(along) % 2 == 1 && Math.abs(along) < h - 0
                            && !(lz == -h && Math.abs(lx) <= 1)) set(lv, q, m.window);
                    else if (type >= 2 && ly == wh) set(lv, q, m.trim);
                    else set(lv, q, m.wall);
                }
            }
        }
        // techo con alero y segundo nivel
        for (int lx = -h - 1; lx <= h + 1; lx++) {
            for (int lz = -h - 1; lz <= h + 1; lz++) {
                boolean rim = Math.abs(lx) == h + 1 || Math.abs(lz) == h + 1;
                set(lv, at(c, front, lx, wh + 1, lz), rim ? slab : m.roof);
            }
        }
        for (int lx = -h + 1; lx <= h - 1; lx++) for (int lz = -h + 1; lz <= h - 1; lz++) set(lv, at(c, front, lx, wh + 2, lz), slab);
        // faroles en las esquinas del techo y en la entrada
        for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) set(lv, at(c, front, sx * (h - 1), wh + 3, sz * (h - 1)), Blocks.LANTERN.defaultBlockState());
        // puerta
        if (type == 0) {
            BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, fwd);
            set(lv, at(c, front, 0, 1, -h), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(lv, at(c, front, 0, 2, -h), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        } else {
            // puerta de hierro: solo se abre con los botones (los monstruos no pueden entrar)
            BlockState door = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, fwd);
            set(lv, at(c, front, 0, 1, -h), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(lv, at(c, front, 0, 2, -h), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            BlockState btn = Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.WALL);
            set(lv, at(c, front, 1, 2, -h - 1), btn.setValue(ButtonBlock.FACING, front));       // afuera
            set(lv, at(c, front, -1, 2, -h + 1), btn.setValue(ButtonBlock.FACING, fwd));        // adentro
        }
        set(lv, at(c, front, 0, wh, -h - 1), Blocks.LANTERN.defaultBlockState());
        // luces colgantes
        for (int lx = -h + 2; lx <= h - 2; lx += 3) for (int lz = -h + 2; lz <= h - 2; lz += 3)
            set(lv, at(c, front, lx, wh, lz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        furnish(lv, c, front, type);
    }

    private static void bed(ServerLevel lv, BlockPos c, Direction front, int lx, int lz, Block bed) {
        Direction fwd = front.getOpposite();
        BlockState b = bed.defaultBlockState().setValue(BedBlock.FACING, fwd);
        set(lv, at(c, front, lx, 1, lz - 1), b.setValue(BedBlock.PART, BedPart.FOOT));
        set(lv, at(c, front, lx, 1, lz), b.setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static void chest(ServerLevel lv, BlockPos q, Direction facing, ItemStack... items) {
        set(lv, q, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
        if (lv.getBlockEntity(q) instanceof Container ct) for (int i = 0; i < items.length; i++) ct.setItem(i, items[i]);
    }

    private static void furnish(ServerLevel lv, BlockPos c, Direction front, int type) {
        Direction fwd = front.getOpposite();
        int in = half(type) - 1;           // última fila/columna interior
        if (type == 0) {
            bed(lv, c, front, -2, 2, Blocks.ORANGE_BED);
            set(lv, at(c, front, 2, 1, 2), Blocks.CRAFTING_TABLE.defaultBlockState());
            set(lv, at(c, front, 1, 1, 2), Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, front));
            chest(lv, at(c, front, 2, 1, 0), fwd.getCounterClockWise(), new ItemStack(Items.BREAD, 8), new ItemStack(Items.TORCH, 16), new ItemStack(Items.COAL, 8));
            set(lv, at(c, front, 2, 1, -1), Blocks.BARREL.defaultBlockState());
            set(lv, at(c, front, 0, 1, 2), Blocks.SMOKER.defaultBlockState().setValue(FurnaceBlock.FACING, front));
            for (int lx = -1; lx <= 1; lx++) for (int lz = -1; lz <= 1; lz++) set(lv, at(c, front, lx, 1, lz), Blocks.ORANGE_CARPET.defaultBlockState());
            set(lv, at(c, front, -2, 1, -2), Blocks.POTTED_RED_TULIP.defaultBlockState());
            return;
        }
        // comunes a las casas grandes: camas, cocina y taller en la pared del fondo, cofres a la derecha
        bed(lv, c, front, -in, in, Blocks.RED_BED);
        bed(lv, c, front, -in + 2, in, Blocks.ORANGE_BED);
        set(lv, at(c, front, in, 1, in), Blocks.CRAFTING_TABLE.defaultBlockState());
        set(lv, at(c, front, in - 1, 1, in), Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, front));
        set(lv, at(c, front, in - 2, 1, in), Blocks.BLAST_FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, front));
        set(lv, at(c, front, in - 3, 1, in), Blocks.SMOKER.defaultBlockState().setValue(FurnaceBlock.FACING, front));
        Direction toCenter = fwd.getCounterClockWise();
        chest(lv, at(c, front, in, 1, in - 2), toCenter, new ItemStack(Items.BREAD, 16), new ItemStack(Items.TORCH, 32), new ItemStack(Items.COAL, 16));
        chest(lv, at(c, front, in, 1, in - 3), toCenter);
        set(lv, at(c, front, in, 1, in - 4), Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, fwd));
        set(lv, at(c, front, in, 1, in - 5), Blocks.SMITHING_TABLE.defaultBlockState());
        if (type == 3) {
            set(lv, at(c, front, -3, 1, -in), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            set(lv, at(c, front, -2, 1, -in), Blocks.BREWING_STAND.defaultBlockState());
        } else {
            set(lv, at(c, front, -in, 1, -in + 1), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            set(lv, at(c, front, -in, 1, -in), Blocks.BREWING_STAND.defaultBlockState());
        }
        for (int lx = -1; lx <= 1; lx++) for (int lz = -1; lz <= 1; lz++)
            set(lv, at(c, front, lx, 1, lz), (type == 1 ? Blocks.GRAY_CARPET : Blocks.ORANGE_CARPET).defaultBlockState());
        if (type == 1) return;

        // Casa del Guerrero Z y Base Cápsula: Generador Protector encendido y huerto de Semillas del Ermitaño
        BlockPos gen = at(c, front, 0, 1, in);
        set(lv, gen, ModRegistry.SHIELD_GENERATOR.get().defaultBlockState());
        if (lv.getBlockEntity(gen) instanceof com.bdsz.block.ShieldGeneratorBlockEntity g) {
            g.addFuel(type == 3 ? 6 : 3);
            g.power(true);
        }
        int gx = -in + 1, gz = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos soil = at(c, front, gx + dx, 0, gz + dz);
                if (dx == 0 && dz == 0) { set(lv, soil, Blocks.WATER.defaultBlockState()); continue; }
                set(lv, soil, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
                set(lv, soil.above(), ModRegistry.SENZU_CROP.get().defaultBlockState());
            }
        }
        chest(lv, at(c, front, in, 1, -in + 1), toCenter, new ItemStack(ModRegistry.RICE_BOWL.get(), 8), new ItemStack(Items.COPPER_INGOT, 8),
                new ItemStack(Items.GOLDEN_CARROT, 8));
        if (type == 2) return;

        // Base Cápsula: mesa de encantamientos con librerías, otro huerto (trigo) y más cofres
        int ex = 2, ez = -2;
        set(lv, at(c, front, ex, 1, ez), Blocks.ENCHANTING_TABLE.defaultBlockState());
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) != 2 && Math.abs(dz) != 2) continue;
                if (dz == -2 && Math.abs(dx) <= 1) continue;              // paso libre
                set(lv, at(c, front, ex + dx, 1, ez + dz), Blocks.BOOKSHELF.defaultBlockState());
            }
        }
        int wx = -in + 1, wz = -in + 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos soil = at(c, front, wx + dx, 0, wz + dz);
                if (dx == 0 && dz == 0) { set(lv, soil, Blocks.WATER.defaultBlockState()); continue; }
                set(lv, soil, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
                set(lv, soil.above(), Blocks.WHEAT.defaultBlockState());
            }
        }
        chest(lv, at(c, front, in, 1, -in + 2), toCenter, new ItemStack(Items.LAPIS_LAZULI, 32), new ItemStack(Items.EXPERIENCE_BOTTLE, 16),
                new ItemStack(Items.GLASS_BOTTLE, 6), new ItemStack(Items.NETHER_WART, 4));
        set(lv, at(c, front, -4, 1, -in), Blocks.LOOM.defaultBlockState());
        set(lv, at(c, front, -5, 1, -in), Blocks.GRINDSTONE.defaultBlockState());
    }

    private static final Set<Block> HOUSE_BLOCKS = new HashSet<>();

    static {
        for (Block b : new Block[]{Blocks.OAK_PLANKS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.GLASS_PANE, Blocks.SPRUCE_SLAB,
                Blocks.SPRUCE_DOOR, Blocks.LANTERN, Blocks.ORANGE_BED, Blocks.RED_BED, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.CHEST,
                Blocks.BARREL, Blocks.SMOKER, Blocks.ORANGE_CARPET, Blocks.GRAY_CARPET, Blocks.POTTED_RED_TULIP, Blocks.POLISHED_ANDESITE,
                Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICKS, Blocks.SMOOTH_QUARTZ,
                Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.BLUE_CONCRETE,
                Blocks.POLISHED_DEEPSLATE, Blocks.RED_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.SMOOTH_STONE_SLAB, Blocks.IRON_DOOR,
                Blocks.STONE_BUTTON, Blocks.BLAST_FURNACE, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.SMITHING_TABLE,
                Blocks.WATER_CAULDRON, Blocks.CAULDRON, Blocks.BREWING_STAND, Blocks.ENCHANTING_TABLE, Blocks.BOOKSHELF, Blocks.FARMLAND,
                Blocks.WHEAT, Blocks.WATER, Blocks.LOOM, Blocks.GRINDSTONE}) HOUSE_BLOCKS.add(b);
    }

    private static boolean isHouseBlock(BlockState s) {
        return HOUSE_BLOCKS.contains(s.getBlock()) || s.is(ModRegistry.SHIELD_GENERATOR.get()) || s.is(ModRegistry.SENZU_CROP.get());
    }

    private static void pack(ServerPlayer p, KaoriEntity k) {
        ServerLevel lv = (ServerLevel) k.level();
        if (!lv.dimension().location().toString().equals(k.hutDim) || k.hutPos.distSqr(k.blockPosition()) > 48 * 48) {
            k.say(p, "Mi casa está lejos. Llévame cerca de ella para guardarla en la cápsula.");
            return;
        }
        BlockPos c = k.hutPos;
        Direction front = Direction.from2DDataValue(k.hutDir);
        int type = k.hutType, h = half(type) + 1, moved = 0;
        for (int ly = wallH(type) + 3; ly >= 0; ly--) {
            for (int lx = -h; lx <= h; lx++) {
                for (int lz = -h; lz <= h; lz++) {
                    BlockPos q = at(c, front, lx, ly, lz);
                    BlockState s = lv.getBlockState(q);
                    if (!isHouseBlock(s)) continue;
                    BlockEntity be = lv.getBlockEntity(q);
                    if (be instanceof Container ct && !(be instanceof com.bdsz.block.ShieldGeneratorBlockEntity)) {
                        for (int i = 0; i < ct.getContainerSize(); i++) {
                            ItemStack it = ct.removeItemNoUpdate(i);
                            if (!it.isEmpty()) { k.storeOrDrop(it); moved++; }
                        }
                    }
                    // el piso de la casa vuelve a ser tierra (no quedan huecos)
                    BlockState rest = ly == 0 ? Blocks.DIRT.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    lv.setBlock(q, rest, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
        BlockPos mid = c.above(2);
        lv.sendParticles(ParticleTypes.CLOUD, mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5, 140, h, 2.0, h, 0.04);
        lv.playSound(null, mid, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.2F, 1.4F);
        k.hutPos = null;
        k.swing(InteractionHand.MAIN_HAND);
        k.say(p, NAMES[type] + " guardada en la cápsula." + (moved > 0 ? " Lo que había en los cofres está en mi mochila." : "") + " Ordenado, como debe ser.");
    }
}
