package com.totallywaifu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;

/** Los oficios de las waifus. */
public final class WaifuWork {
    private final WaifuEntity w;
    @Nullable private BlockPos block;
    @Nullable private LivingEntity prey;
    @Nullable private ItemEntity item;
    private int progress, need, stuck, think, cooldown;
    private final Map<BlockPos, Long> skip = new HashMap<>();

    WaifuWork(WaifuEntity w) {
        this.w = w;
    }

    public void reset() {
        if (block != null && w.level() instanceof ServerLevel lv) lv.destroyBlockProgress(w.getId(), block, -1);
        block = null;
        prey = null;
        item = null;
        progress = stuck = 0;
        w.setBusy(false);
    }

    private ServerLevel lv() {
        return (ServerLevel) w.level();
    }

    private BlockPos center() {
        return w.anchor != null ? w.anchor : w.blockPosition();
    }

    // ================================================================== cada tick (en cualquier modo)
    void always() {
        long t = w.tickCount;
        // recoge lo que tenga muy cerca
        if (t % 5 == 0) {
            double r = w.kind == WaifuKind.KURA || w.kind == WaifuKind.PERRO || w.kind == WaifuKind.ZORRO || w.kind == WaifuKind.ALLAY ? 3.5 : 2.0;
            for (ItemEntity ie : w.level().getEntitiesOfClass(ItemEntity.class, w.getBoundingBox().inflate(r, 1, r),
                    ie -> ie.isAlive() && !ie.hasPickUpDelay())) {
                pick(ie);
            }
        }
        switch (w.kind) {
            case KOZUE -> { if (t % 30 == 0) craftOrder(); }
            case HOMURA -> { if (t % 20 == 0) feedFurnace(); }
            case KURA -> {
                if (t % 100 == 0) collectFromOthers();
                if (t % 80 == 0) feedOwner();
                if (t % 2400 == 0) sortChest(w);
            }
            case SHIORI -> shioriAlways(t);
            case CAMA -> { }
            default -> MobGirls.always(w, this, t);
        }
        if (cooldown > 0) cooldown--;
    }

    private void pick(ItemEntity ie) {
        ItemStack rest = w.store(ie.getItem().copy());
        if (rest.getCount() == ie.getItem().getCount()) return;
        w.take(ie, ie.getItem().getCount() - rest.getCount());
        if (rest.isEmpty()) ie.discard(); else ie.setItem(rest);
        w.playSound(SoundEvents.ITEM_PICKUP, 0.3F, 1.4F + w.getRandom().nextFloat() * 0.4F);
    }

    int cooldown() { return cooldown; }
    void cooldown(int c) { cooldown = c; }

    /** Interacciones especiales con clic derecho (tijeras, cubo, hierro, comercio...). null = la normal. */
    static net.minecraft.world.InteractionResult interact(WaifuEntity w, net.minecraft.server.level.ServerPlayer p, ItemStack held) {
        return MobGirls.interact(w, p, held);
    }

    // ================================================================== modo Trabajar
    void tick() {
        if (++think >= 20 && block == null && prey == null && item == null) {
            think = 0;
            choose();
        }
        if (item != null) doItem();
        else if (prey != null) doHunt();
        else if (block != null) doMine();
        else w.setBusy(false);
    }

    private void choose() {
        // primero: objetos tirados cerca
        double r = w.kind == WaifuKind.KURA ? 16 : 9;
        item = w.level().getEntitiesOfClass(ItemEntity.class, w.getBoundingBox().inflate(r, 4, r), ie -> ie.isAlive() && !ie.hasPickUpDelay()
                        && ie.position().distanceToSqr(Vec3.atCenterOf(center())) < 20 * 20)
                .stream().min(Comparator.comparingDouble(w::distanceToSqr)).orElse(null);
        if (item != null) return;
        switch (w.kind) {
            case KOZUE -> {
                block = findBlock(s -> s.is(Tags.Blocks.ORES), true);
                if (block == null) block = findBlock(st -> st.is(BlockTags.LOGS), false);
            }
            case HOMURA -> {                         // menas de todo tipo y, si no hay, piedra
                block = findBlock(s -> s.is(Tags.Blocks.ORES), true);
                if (block == null && count(Items.COBBLESTONE) + count(Items.COBBLED_DEEPSLATE) < 192)
                    block = findBlock(s -> s.is(Tags.Blocks.STONE) || s.is(net.minecraft.world.level.block.Blocks.STONE), true);
            }
            case SHIORI -> block = findBlock(s -> s.is(Tags.Blocks.ORES_LAPIS), true);
            default -> { }
        }
        if (block != null) {
            BlockState st = w.level().getBlockState(block);
            need = Math.max(8, (int) (st.getDestroySpeed(w.level(), block) * 9));
            progress = 0;
            boolean log = st.is(BlockTags.LOGS);
            w.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(log ? Items.IRON_AXE : Items.IRON_PICKAXE));
        } else if (prey != null) {
            w.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        } else if (w.tickCount % 200 == 0 && w.anchor != null && w.distanceToSqr(Vec3.atCenterOf(w.anchor)) > 10 * 10) {
            w.getNavigation().moveTo(w.anchor.getX() + 0.5, w.anchor.getY(), w.anchor.getZ() + 0.5, 1.0);   // vuelve a su zona
        }
    }

    private void doItem() {
        if (!item.isAlive()) { item = null; return; }
        w.setBusy(true);
        if (w.tickCount % 10 == 0) w.getNavigation().moveTo(item, 1.15);
        if (w.distanceToSqr(item) < 2.0 * 2.0) {
            pick(item);
            item = null;
        } else if (++stuck > 160) {
            item = null;
            stuck = 0;
        }
    }

    private void doHunt() {
        if (!prey.isAlive() || prey.distanceToSqr(w) > 24 * 24) {
            prey = null;
            w.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return;
        }
        w.setBusy(true);
        w.getLookControl().setLookAt(prey, 30, 30);
        if (w.tickCount % 10 == 0) w.getNavigation().moveTo(prey, 1.2);
        if (w.distanceToSqr(prey) < 2.3 * 2.3 && cooldown == 0) {
            w.swing(InteractionHand.MAIN_HAND);
            prey.hurt(w.damageSources().mobAttack(w), 9.0F);
            cooldown = 12;
        }
        if (++stuck > 400) { prey = null; stuck = 0; }
    }

    private void doMine() {
        BlockState st = w.level().getBlockState(block);
        if (st.isAir() || st.getDestroySpeed(w.level(), block) < 0) { finishBlock(); return; }
        w.setBusy(true);
        Vec3 c = Vec3.atCenterOf(block);
        w.getLookControl().setLookAt(c.x, c.y, c.z);
        double dh = Math.sqrt(Math.pow(w.getX() - c.x, 2) + Math.pow(w.getZ() - c.z, 2));
        double dy = c.y - w.getY();
        boolean reach = dh < 3.2 && dy > -2.5 && dy < 6.5;
        if (!reach) {
            if (w.tickCount % 10 == 0) {
                BlockPos stand = standNear(block);
                w.getNavigation().moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 1.1);
            }
            if (++stuck > 140) {
                skip.put(block, w.level().getGameTime() + 2400);
                finishBlock();
            }
            return;
        }
        w.getNavigation().stop();
        if (w.tickCount % 5 == 0) {
            w.swing(InteractionHand.MAIN_HAND);
            w.level().playSound(null, block, st.getSoundType().getHitSound(), SoundSource.NEUTRAL, 0.5F, 0.8F);
        }
        progress++;
        lv().destroyBlockProgress(w.getId(), block, Math.min(9, progress * 10 / need));
        if (progress >= need) {
            ItemStack tool = new ItemStack(st.is(BlockTags.LOGS) ? Items.DIAMOND_AXE : Items.DIAMOND_PICKAXE);
            List<ItemStack> drops = Block.getDrops(st, lv(), block, w.level().getBlockEntity(block), w, tool);
            w.level().destroyBlock(block, false, w);
            if (w.kind == WaifuKind.KOZUE && st.is(BlockTags.LOGS)) replant(block);
            for (ItemStack d : drops) {
                ItemStack rest = w.store(d);
                if (!rest.isEmpty()) Block.popResource(w.level(), block, rest);
            }
            lv().sendParticles(ParticleTypes.HAPPY_VILLAGER, c.x, c.y, c.z, 5, 0.3, 0.3, 0.3, 0);
            finishBlock();
        }
    }

    private void finishBlock() {
        if (block != null) lv().destroyBlockProgress(w.getId(), block, -1);
        block = null;
        progress = stuck = 0;
        think = 15;            // busca el siguiente enseguida (el resto del árbol, la veta...)
    }

    /** Un bloque de aire junto al objetivo donde pararse. */
    private BlockPos standNear(BlockPos b) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos q = b.relative(d);
            for (int dy = -2; dy <= 1; dy++) {
                BlockPos s = q.above(dy);
                if (w.level().getBlockState(s).isAir() && w.level().getBlockState(s.above()).isAir()
                        && w.level().getBlockState(s.below()).isSolidRender(w.level(), s.below())) return s;
            }
        }
        return b;
    }

    /** Busca el bloque más cercano a ella dentro de su zona. exposed: tiene que tocar aire (no excava a ciegas). */
    @Nullable
    private BlockPos findBlock(java.util.function.Predicate<BlockState> ok, boolean exposed) {
        BlockPos c = center(), best = null;
        double bd = Double.MAX_VALUE;
        long now = w.level().getGameTime();
        skip.values().removeIf(v -> v < now);
        for (BlockPos q : BlockPos.betweenClosed(c.offset(-12, -6, -12), c.offset(12, 9, 12))) {
            BlockState st = w.level().getBlockState(q);
            if (st.isAir() || !ok.test(st) || skip.containsKey(q)) continue;
            double d = q.distSqr(w.blockPosition());
            if (d >= bd) continue;
            if (exposed && !touchesAir(q)) continue;
            if (st.is(BlockTags.LOGS) && !naturalTree(q)) continue;
            bd = d;
            best = q.immutable();
        }
        return best;
    }

    private boolean touchesAir(BlockPos q) {
        for (Direction d : Direction.values()) if (w.level().getBlockState(q.relative(d)).isAir()) return true;
        return false;
    }

    /** Tronco de un árbol natural (con hojas naturales encima o al lado): así no desarma tu casa. */
    private boolean naturalTree(BlockPos log) {
        for (BlockPos q : BlockPos.betweenClosed(log.offset(-3, 0, -3), log.offset(3, 8, 3))) {
            BlockState st = w.level().getBlockState(q);
            if (st.is(BlockTags.LEAVES) && st.hasProperty(LeavesBlock.PERSISTENT) && !st.getValue(LeavesBlock.PERSISTENT)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "WaifuWork";
    }

    // ================================================================== Kozue: pedidos de crafteo (cualquier objeto vanilla)
    /** Si el tronco era la base del árbol, planta un brote de su mochila. */
    private void replant(BlockPos log) {
        BlockState below = w.level().getBlockState(log.below());
        if (!below.is(BlockTags.DIRT)) return;
        for (int i = 0; i < w.bag.getContainerSize(); i++) {
            ItemStack s = w.bag.getItem(i);
            if (s.is(ItemTags.SAPLINGS) && s.getItem() instanceof net.minecraft.world.item.BlockItem bi) {
                w.level().setBlock(log, bi.getBlock().defaultBlockState(), 3);
                s.shrink(1);
                lv().sendParticles(ParticleTypes.HAPPY_VILLAGER, log.getX() + 0.5, log.getY() + 0.5, log.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0);
                return;
            }
        }
    }

    private static Map<net.minecraft.world.item.Item, List<CraftingRecipe>> byResult;
    private static RecipeManager cachedFor;

    private List<CraftingRecipe> recipesFor(net.minecraft.world.item.Item it) {
        RecipeManager rm = w.level().getRecipeManager();
        if (byResult == null || cachedFor != rm) {
            byResult = new HashMap<>();
            for (CraftingRecipe r : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
                if (r.isSpecial()) continue;
                ItemStack out = r.getResultItem(w.level().registryAccess());
                if (!out.isEmpty()) byResult.computeIfAbsent(out.getItem(), k -> new ArrayList<>()).add(r);
            }
            cachedFor = rm;
        }
        return byResult.getOrDefault(it, List.of());
    }

    /** Inventario simulado: su mochila + el cofre de Kura (si está cerca) + lo que va fabricando. */
    private static final class Pool {
        final List<net.minecraft.world.Container> src = new ArrayList<>();
        final List<Integer> slot = new ArrayList<>();
        final List<ItemStack> items = new ArrayList<>();
        final List<ItemStack> extra = new ArrayList<>();

        void add(net.minecraft.world.Container c) {
            for (int i = 0; i < c.getContainerSize(); i++) {
                src.add(c);
                slot.add(i);
                items.add(c.getItem(i).copy());
            }
        }

        List<ItemStack> snapshot() {
            List<ItemStack> l = new ArrayList<>();
            for (ItemStack s : items) l.add(s.copy());
            l.add(ItemStack.EMPTY);
            for (ItemStack s : extra) l.add(s.copy());
            return l;
        }

        void restore(List<ItemStack> l) {
            for (int i = 0; i < items.size(); i++) items.set(i, l.get(i));
            extra.clear();
            for (int i = items.size() + 1; i < l.size(); i++) extra.add(l.get(i));
        }

        boolean take(Ingredient ing) {
            for (List<ItemStack> l : List.of(extra, items)) {
                for (ItemStack s : l) {
                    if (!s.isEmpty() && ing.test(s)) {
                        ItemStack rem = s.getCraftingRemainingItem();
                        s.shrink(1);
                        if (!rem.isEmpty()) extra.add(rem);
                        return true;
                    }
                }
            }
            return false;
        }

        void commit(WaifuEntity w) {
            for (int i = 0; i < items.size(); i++) src.get(i).setItem(slot.get(i), items.get(i));
            for (ItemStack s : extra) {
                if (s.isEmpty()) continue;
                ItemStack rest = w.bag.addItem(s);
                if (!rest.isEmpty()) w.spawnAtLocation(rest);
            }
            for (net.minecraft.world.Container c : new HashSet<>(src)) c.setChanged();
        }
    }

    /** Intenta fabricar 'it' dentro del inventario simulado, fabricando antes lo que falte (hasta 'depth' niveles). */
    private boolean craft(Pool pool, net.minecraft.world.item.Item it, int depth, Set<net.minecraft.world.item.Item> busy) {
        if (depth < 0 || !busy.add(it)) return false;
        try {
            for (CraftingRecipe r : recipesFor(it)) {
                List<ItemStack> snap = pool.snapshot();
                boolean ok = true;
                for (Ingredient ing : r.getIngredients()) {
                    if (ing.isEmpty() || pool.take(ing)) continue;
                    boolean made = false;
                    if (depth > 0) {
                        ItemStack[] opts = ing.getItems();
                        for (int o = 0; o < opts.length && o < 4 && !made; o++) {
                            if (opts[o].is(it)) continue;
                            if (craft(pool, opts[o].getItem(), depth - 1, busy) && pool.take(ing)) made = true;
                        }
                    }
                    if (!made) { ok = false; break; }
                }
                if (ok) {
                    pool.extra.add(r.getResultItem(w.level().registryAccess()).copy());
                    return true;
                }
                pool.restore(snap);
            }
            return false;
        } finally {
            busy.remove(it);
        }
    }

    private void craftOrder() {
        ItemStack want = w.order.getItem(0);
        if (want.isEmpty()) return;
        Pool pool = new Pool();
        pool.add(w.bag);
        WaifuEntity kura = kura();
        if (kura != null) pool.add(kura.chest);
        if (!craft(pool, want.getItem(), 3, new HashSet<>())) {
            if (w.tickCount % 600 == 0 && w.ownerPlayer() != null && w.ownerPlayer().distanceToSqr(w) < 16 * 16)
                w.ownerPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("§6Kozue§7: me faltan materiales para " + want.getHoverName().getString()
                        + " (ponlos en mi mochila o en el cofre de Kura)"), true);
            return;
        }
        pool.commit(w);
        w.swing(InteractionHand.MAIN_HAND);
        w.playSound(SoundEvents.VILLAGER_WORK_TOOLSMITH, 0.6F, 1.3F);
        lv().sendParticles(ParticleTypes.HAPPY_VILLAGER, w.getX(), w.getY() + 1.2, w.getZ(), 4, 0.3, 0.3, 0.3, 0);
    }

    @Nullable
    private WaifuEntity kura() {
        if (w.getOwner() == null) return null;
        return w.level().getEntitiesOfClass(WaifuEntity.class, w.getBoundingBox().inflate(16),
                o -> o.kind == WaifuKind.KURA && w.getOwner().equals(o.getOwner())).stream().findFirst().orElse(null);
    }

    // ================================================================== Homura: horno, carbón y caza
    private int count(net.minecraft.world.item.Item it) {
        int c = 0;
        for (int i = 0; i < w.bag.getContainerSize(); i++) if (w.bag.getItem(i).is(it)) c += w.bag.getItem(i).getCount();
        return c;
    }

    private int foodCount() {
        int c = 0;
        for (int i = 0; i < w.bag.getContainerSize(); i++) if (w.bag.getItem(i).isEdible()) c += w.bag.getItem(i).getCount();
        ItemStack in = w.furnace.getItem(0), out = w.furnace.getItem(2);
        if (in.isEdible()) c += in.getCount();
        if (out.isEdible()) c += out.getCount();
        return c;
    }

    /** Animales adultos de granja, sin nombre, y solo si quedan más de dos de esa especie (nunca los extingue). */
    @Nullable
    private LivingEntity findPrey() {
        List<Animal> all = w.level().getEntitiesOfClass(Animal.class, new net.minecraft.world.phys.AABB(center()).inflate(14, 6, 14),
                a -> a.isAlive() && !a.isBaby() && !a.hasCustomName() && !a.isLeashed()
                        && (a instanceof Cow || a instanceof Pig || a instanceof Chicken || a instanceof Sheep || a instanceof Rabbit));
        Map<Class<?>, Integer> per = new HashMap<>();
        for (Animal a : all) per.merge(a.getClass(), 1, Integer::sum);
        return all.stream().filter(a -> per.get(a.getClass()) > 2).min(Comparator.comparingDouble(w::distanceToSqr)).orElse(null);
    }

    private void feedFurnace() {
        SimpleContainerHelper f = new SimpleContainerHelper(w.furnace);
        // saca lo cocinado a su mochila si se llena
        ItemStack out = w.furnace.getItem(2);
        if (out.getCount() >= 32) {
            ItemStack rest = w.bag.addItem(out.copy());
            w.furnace.setItem(2, rest);
        }
        // SOLO cocina lo que le pidas: su pedido (panel) saca eso de su mochila. Lo que pongas en su horno, también.
        ItemStack want = w.order.getItem(0);
        if (w.furnace.getItem(0).isEmpty() && !want.isEmpty()) {
            for (int i = 0; i < w.bag.getContainerSize(); i++) {
                ItemStack s = w.bag.getItem(i);
                if (!s.isEmpty() && s.is(want.getItem()) && f.smeltable(w.level(), s)) {
                    w.furnace.setItem(0, s.copy());
                    w.bag.setItem(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
        // y combustible
        if (w.furnace.getItem(1).isEmpty() && !w.furnace.getItem(0).isEmpty()) {
            int best = -1;
            for (int i = 0; i < w.bag.getContainerSize(); i++) {
                ItemStack s = w.bag.getItem(i);
                if (s.isEmpty() || ForgeHooks.getBurnTime(s, RecipeType.SMELTING) <= 0 || s.is(Items.BUCKET)) continue;
                if (best < 0 || s.is(Items.COAL) || s.is(Items.CHARCOAL)) best = i;
            }
            if (best >= 0) {
                w.furnace.setItem(1, w.bag.getItem(best).copy());
                w.bag.setItem(best, ItemStack.EMPTY);
            }
        }
    }

    private record SimpleContainerHelper(net.minecraft.world.SimpleContainer c) {
        boolean smeltable(net.minecraft.world.level.Level lv, ItemStack s) {
            return lv.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.SimpleContainer(s.copyWithCount(1)), lv).isPresent();
        }
    }

    /** El horno de Homura funciona siempre, incluso con el menú cerrado (y el doble de rápido). */
    static void furnaceTick(WaifuEntity w) {
        int[] d = w.furnaceData;
        net.minecraft.world.SimpleContainer c = w.furnace;
        boolean wasLit = d[0] > 0;
        if (d[0] > 0) d[0]--;
        ItemStack in = c.getItem(0), fuel = c.getItem(1), res = c.getItem(2);
        SmeltingRecipe r = in.isEmpty() ? null : w.level().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.SimpleContainer(in.copyWithCount(1)), w.level()).orElse(null);
        ItemStack result = r == null ? ItemStack.EMPTY : r.getResultItem(w.level().registryAccess());
        boolean can = r != null && !result.isEmpty() && (res.isEmpty() || (ItemStack.isSameItemSameTags(res, result)
                && res.getCount() + result.getCount() <= res.getMaxStackSize()));
        if (d[0] <= 0 && can) {
            int burn = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
            if (burn > 0) {
                d[0] = d[1] = burn;
                if (fuel.hasCraftingRemainingItem()) c.setItem(1, fuel.getCraftingRemainingItem());
                else fuel.shrink(1);
            }
        }
        if (d[0] > 0 && can) {
            d[3] = r.getCookingTime();
            d[2] += 2;
            if (d[2] >= d[3]) {
                d[2] = 0;
                if (res.isEmpty()) c.setItem(2, result.copy()); else res.grow(result.getCount());
                in.shrink(1);
                w.furnaceXp += r.getExperience();
                if (w.furnaceXp >= 1) {
                    int xp = (int) w.furnaceXp;
                    w.furnaceXp -= xp;
                    w.level().addFreshEntity(new ExperienceOrb(w.level(), w.getX(), w.getY() + 0.5, w.getZ(), xp));
                }
                c.setChanged();
            }
        } else {
            d[2] = Math.max(0, d[2] - 2);
        }
        w.setLit(d[0] > 0);
        if (d[0] > 0 && w.tickCount % 80 == 0) w.playSound(SoundEvents.FURNACE_FIRE_CRACKLE, 0.6F, 1.0F);
        if (wasLit != d[0] > 0) c.setChanged();
    }

    // ================================================================== Kura: vacía las mochilas de las demás
    private void collectFromOthers() {
        if (w.getOwner() == null) return;
        for (WaifuEntity o : w.level().getEntitiesOfClass(WaifuEntity.class, w.getBoundingBox().inflate(24),
                o -> o != w && o.kind != WaifuKind.KURA && w.getOwner().equals(o.getOwner()))) {
            int used = 0;
            for (int i = 0; i < o.bag.getContainerSize(); i++) if (!o.bag.getItem(i).isEmpty()) used++;
            if (used < 18) continue;
            boolean moved = false;
            for (int i = 9; i < o.bag.getContainerSize(); i++) {         // la primera fila es su reserva
                ItemStack s = o.bag.getItem(i);
                if (s.isEmpty()) continue;
                ItemStack rest = w.chest.addItem(s.copy());
                if (rest.getCount() != s.getCount()) moved = true;
                o.bag.setItem(i, rest);
            }
            if (moved) {
                lv().sendParticles(ParticleTypes.HEART, o.getX(), o.getY() + 2, o.getZ(), 3, 0.3, 0.2, 0.3, 0);
                w.playSound(SoundEvents.CHEST_CLOSE, 0.5F, 1.3F);
            }
        }
    }

    /** Si tienes hambre, Kura te pasa la mejor comida de su cofre. */
    private void feedOwner() {
        Player p = w.ownerPlayer();
        if (p == null || p.distanceToSqr(w) > 10 * 10 || p.getFoodData().getFoodLevel() > 12) return;
        int best = -1, bestN = 0;
        for (int i = 0; i < w.chest.getContainerSize(); i++) {
            ItemStack s = w.chest.getItem(i);
            if (!s.isEdible() || s.getItem().getFoodProperties(s, p) == null) continue;
            int n = s.getItem().getFoodProperties(s, p).getNutrition();
            if (s.is(Items.ROTTEN_FLESH) || s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO) || s.is(Items.PUFFERFISH)) continue;
            if (n > bestN) { best = i; bestN = n; }
        }
        if (best < 0) return;
        ItemStack food = w.chest.getItem(best).split(Math.min(4, w.chest.getItem(best).getCount()));
        if (!p.getInventory().add(food)) p.drop(food, false);
        w.say(p, "¡Tienes hambre! Toma, de mi cofre ♥");
        w.cheer(40);
    }

    /** Ordena el cofre de Kura: junta montones y los acomoda por tipo. */
    public static void sortChest(WaifuEntity w) {
        List<ItemStack> all = new ArrayList<>();
        for (int i = 0; i < w.chest.getContainerSize(); i++) {
            ItemStack s = w.chest.getItem(i);
            if (s.isEmpty()) continue;
            boolean merged = false;
            for (ItemStack a : all) {
                if (ItemStack.isSameItemSameTags(a, s) && a.getCount() < a.getMaxStackSize()) {
                    int m = Math.min(s.getCount(), a.getMaxStackSize() - a.getCount());
                    a.grow(m);
                    s.shrink(m);
                    if (s.isEmpty()) { merged = true; break; }
                }
            }
            if (!merged && !s.isEmpty()) all.add(s.copy());
        }
        all.sort(Comparator.comparing((ItemStack s) -> net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(s.getItem()).toString())
                .thenComparing(s -> -s.getCount()));
        w.chest.clearContent();
        for (int i = 0; i < all.size() && i < w.chest.getContainerSize(); i++) w.chest.setItem(i, all.get(i));
        w.chest.setChanged();
    }

    // ================================================================== Shiori: magia, experiencia y reparación
    private void shioriAlways(long t) {
        if (w.getMode() == WaifuEntity.STAY) return;
        // absorbe orbes de experiencia cercanos
        if (t % 4 == 0) {
            for (ExperienceOrb orb : w.level().getEntitiesOfClass(ExperienceOrb.class, w.getBoundingBox().inflate(6))) {
                w.storedXp += orb.getValue();
                orb.discard();
                w.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.3F, 1.2F);
            }
        }
        // magia contra monstruos
        if (cooldown == 0) {
            Mob target = w.level().getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(13, 6, 13),
                            m -> m instanceof Enemy && m.isAlive() && w.hasLineOfSight(m) && !(m instanceof Creeper && m.distanceToSqr(w) < 9))
                    .stream().min(Comparator.comparingDouble(w::distanceToSqr)).orElse(null);
            if (target != null) {
                bolt(target);
                cooldown = 22;
            }
        }
        // escudo mágico si estás en apuros
        if (t % 40 == 0) {
            Player p = w.ownerPlayer();
            if (p != null && p.distanceToSqr(w) < 12 * 12 && p.getHealth() < p.getMaxHealth() * 0.4F && !p.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION)) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 600, 1));
                lv().sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1, p.getZ(), 40, 0.5, 0.8, 0.5, 0.5);
                w.say(p, "¡Escudo mágico! ✦");
            }
        }
        // repara tu equipo con su experiencia (2 de durabilidad por punto)
        if (t % 20 == 0 && w.storedXp > 0) {
            Player p = w.ownerPlayer();
            if (p != null && p.distanceToSqr(w) < 10 * 10) {
                for (ItemStack s : repairable(p)) {
                    if (w.storedXp <= 0) break;
                    int fix = Math.min(s.getDamageValue(), Math.min(w.storedXp * 2, 20));
                    s.setDamageValue(s.getDamageValue() - fix);
                    w.storedXp -= Math.max(1, fix / 2);
                    Vec3 a = w.position().add(0, 1.4, 0), b = p.position().add(0, 1.0, 0);
                    for (int i = 0; i <= 8; i++) {
                        Vec3 q = a.lerp(b, i / 8.0);
                        lv().sendParticles(ParticleTypes.ENCHANT, q.x, q.y, q.z, 2, 0.05, 0.05, 0.05, 0);
                    }
                }
            }
        }
    }

    private static List<ItemStack> repairable(Player p) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : p.getInventory().armor) if (s.isDamaged()) out.add(s);
        for (ItemStack s : p.getInventory().offhand) if (s.isDamaged()) out.add(s);
        for (ItemStack s : p.getInventory().items) if (s.isDamaged()) out.add(s);
        return out;
    }

    private void bolt(Mob target) {
        w.swing(InteractionHand.MAIN_HAND);
        w.getLookControl().setLookAt(target, 30, 30);
        Vec3 a = w.position().add(0, 1.5, 0), b = target.position().add(0, target.getBbHeight() * 0.6, 0);
        for (int i = 0; i <= 14; i++) {
            Vec3 q = a.lerp(b, i / 14.0);
            lv().sendParticles(ParticleTypes.END_ROD, q.x, q.y, q.z, 1, 0.02, 0.02, 0.02, 0);
            lv().sendParticles(ParticleTypes.ENCHANT, q.x, q.y, q.z, 2, 0.1, 0.1, 0.1, 0.1);
        }
        boolean wasAlive = target.isAlive();
        target.hurt(w.damageSources().indirectMagic(w, w), 8.0F);
        w.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 0.7F, 1.5F);
        if (wasAlive && !target.isAlive()) w.storedXp += 5 + w.getRandom().nextInt(4);
    }
}
