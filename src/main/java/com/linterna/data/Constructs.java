package com.linterna.data;

import com.linterna.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;

/** El catálogo del anillo (tecla M): constructos de energía verde que duran un rato. */
public final class Constructs {
    private Constructs() {}

    public static final String[] NAMES = {"Pared", "Barrera", "Domo de defensa", "Plataforma", "Jaula", "Bombas", "Espada de luz", "Esmeraldas falsas",
            "Espada", "Hacha", "Arco", "Escudo", "Lanza", "Mazo", "Cañón", "Guadaña"};
    public static final int[] COST = {300, 450, 900, 200, 400, 300, 300, 800,
            300, 300, 350, 250, 400, 450, 500, 450};
    /** Cuántos son constructos (el resto son armas). */
    public static final int CONSTRUCTS = 8;
    public static final String[] TIPS = {
            "Una pared sólida de 5×3 frente a ti", "Una barrera curva de 7×4 que te cubre de flechas y monstruos",
            "Un domo de defensa alrededor tuyo (radio 4)", "Una plataforma de 5×5 bajo tus pies (para volar o puentes)",
            "Encierra al monstruo más cercano en una jaula", "4 bombas de energía para lanzar (clic derecho)",
            "Una espada de energía (dura 3 minutos)", "16 esmeraldas que se desvanecen en 5 minutos",
            "Espada holograma verde: muy afilada y nunca se rompe", "Hacha holograma verde: tala al instante y pega fuerte",
            "Arco holograma verde: no necesita flechas, dispara flechas de luz", "Escudo holograma verde: bloquea todo y nunca se rompe",
            "Lanza holograma verde: clic derecho lanza una jabalina de luz que atraviesa enemigos", "Mazo holograma verde: golpe pesado; cayendo hace una onda expansiva",
            "Cañón de energía: clic derecho dispara ráfagas verdes", "Guadaña de energía: golpe amplio que corta a todos alrededor"};

    public static final String EXPIRE = "LinternaExpira";

    public static void create(ServerPlayer p, int what) {
        LanternState s = LanternData.get(p);
        if (!s.bearer() || what < 0 || what >= NAMES.length) return;
        if (!LanternLogic.hasRing(p)) { LanternLogic.msg(p, "§aNecesitas tener el Anillo Verde contigo."); return; }
        if (!LanternLogic.spend(p, s, COST[what])) return;
        ServerLevel lv = p.serverLevel();
        Direction f = p.getDirection();
        BlockPos base = p.blockPosition();
        long now = lv.getGameTime();
        switch (what) {
            case 0 -> { // pared
                BlockPos c = base.relative(f, 3);
                Direction r = f.getClockWise();
                for (int i = -2; i <= 2; i++) for (int y = 0; y < 3; y++) place(lv, c.relative(r, i).above(y));
            }
            case 1 -> { // barrera curva
                for (int i = -3; i <= 3; i++) {
                    BlockPos c = base.relative(f, 3 - Math.abs(i) / 2).relative(f.getClockWise(), i);
                    for (int y = 0; y < 4; y++) place(lv, c.above(y));
                }
            }
            case 2 -> { // domo
                int r = 4;
                for (BlockPos q : BlockPos.betweenClosed(base.offset(-r, -1, -r), base.offset(r, r, r))) {
                    double d = Math.sqrt(q.distSqr(base));
                    if (d >= r - 0.5 && d <= r + 0.5) place(lv, q.immutable());
                }
            }
            case 3 -> { // plataforma
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) place(lv, base.offset(dx, -1, dz));
            }
            case 4 -> { // jaula
                Mob m = lv.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(16), x -> x instanceof Enemy)
                        .stream().min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
                if (m == null) {
                    LanternLogic.msg(p, "§7No hay monstruos cerca para enjaular.");
                    s.charge += COST[what];
                    break;
                }
                BlockPos c = m.blockPosition();
                int w = (int) Math.ceil(m.getBbWidth() / 2) + 1, h = (int) Math.ceil(m.getBbHeight()) + 1;
                for (BlockPos q : BlockPos.betweenClosed(c.offset(-w, -1, -w), c.offset(w, h, w))) {
                    boolean shell = Math.abs(q.getX() - c.getX()) == w || Math.abs(q.getZ() - c.getZ()) == w || q.getY() == c.getY() - 1 || q.getY() == c.getY() + h;
                    if (shell) place(lv, q.immutable());
                }
            }
            case 5 -> give(p, new ItemStack(ModRegistry.ENERGY_BOMB.get(), 4));
            case 6 -> {
                ItemStack sw = new ItemStack(ModRegistry.ENERGY_SWORD.get());
                sw.getOrCreateTag().putLong(EXPIRE, now + 3600);
                give(p, sw);
            }
            case 8 -> weapon(p, new ItemStack(ModRegistry.ENERGY_SWORD.get()), null, now);
            case 9 -> weapon(p, new ItemStack(ModRegistry.ENERGY_AXE.get()), null, now);
            case 10 -> weapon(p, new ItemStack(ModRegistry.ENERGY_BOW.get()), null, now);
            case 11 -> weapon(p, new ItemStack(ModRegistry.ENERGY_SHIELD.get()), null, now);
            case 12 -> weapon(p, new ItemStack(ModRegistry.ENERGY_LANCE.get()), null, now);
            case 13 -> weapon(p, new ItemStack(ModRegistry.ENERGY_MACE.get()), null, now);
            case 14 -> weapon(p, new ItemStack(ModRegistry.ENERGY_CANNON.get()), null, now);
            case 15 -> weapon(p, new ItemStack(ModRegistry.ENERGY_SCYTHE.get()), null, now);
            case 7 -> {
                ItemStack em = new ItemStack(Items.EMERALD, 16);
                CompoundTag t = em.getOrCreateTag();
                t.putLong(EXPIRE, now + 6000);
                em.setHoverName(net.minecraft.network.chat.Component.literal("§aEsmeralda de energía (falsa)"));
                give(p, em);
            }
            default -> { }
        }
        lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.4F);
        lv.sendParticles(LanternLogic.GREEN, p.getX(), p.getY() + 1, p.getZ(), 40, 1.5, 1.0, 1.5, 0);
        LanternLogic.msg(p, "§a✦ Constructo: " + NAMES[what]);
        LanternData.set(p, s);
    }

    public static final String WEAPON = "LinternaArma";

    /** Arma de energía: irrompible, encantada, verde, no se puede tirar y se desvanece en 3 minutos. */
    private static void weapon(ServerPlayer p, ItemStack st, String name, long now, Object... ench) {
        CompoundTag t = st.getOrCreateTag();
        t.putLong(EXPIRE, now + 3600);
        t.putBoolean(WEAPON, true);
        if (st.isDamageableItem()) t.putBoolean("Unbreakable", true);
        if (name != null) st.setHoverName(net.minecraft.network.chat.Component.literal("§a" + name));
        for (int i = 0; i + 1 < ench.length; i += 2) {
            net.minecraft.world.item.enchantment.Enchantment en = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS
                    .getValue(new net.minecraft.resources.ResourceLocation((String) ench[i]));
            if (en != null) st.enchant(en, (Integer) ench[i + 1]);
        }
        give(p, st);
    }

    private static void place(ServerLevel lv, BlockPos q) {
        BlockState st = lv.getBlockState(q);
        if (!st.isAir() && !st.canBeReplaced()) return;
        lv.setBlock(q, ModRegistry.CONSTRUCT.get().defaultBlockState(), 3);
    }

    private static void give(ServerPlayer p, ItemStack st) {
        if (!p.getInventory().add(st)) p.drop(st, false);
    }

    /** Los objetos creados con el anillo se desvanecen cuando pasa su tiempo. */
    public static void expire(ServerPlayer p) {
        long now = p.level().getGameTime();
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.hasTag() && s.getTag().contains(EXPIRE) && s.getTag().getLong(EXPIRE) <= now) {
                p.getInventory().setItem(i, ItemStack.EMPTY);
                LanternLogic.msg(p, "§7Un constructo del anillo se desvaneció.");
            }
        }
    }
}
