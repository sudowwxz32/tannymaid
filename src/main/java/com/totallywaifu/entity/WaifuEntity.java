package com.totallywaifu.entity;

import com.totallywaifu.item.HeartItem;
import com.totallywaifu.menu.*;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Una waifu: nació de una mesa de crafteo, un horno, un cofre o una mesa de encantamientos.
 * Clic derecho abre el menú de su mesa; Shift + clic derecho abre su panel (mochila y modos).
 */
public class WaifuEntity extends PathfinderMob implements net.minecraft.world.item.trading.Merchant {
    public static final int FOLLOW = 0, STAY = 1, WORK = 2;
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(WaifuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HAPPY = SynchedEntityData.defineId(WaifuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LIT = SynchedEntityData.defineId(WaifuEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BUSY = SynchedEntityData.defineId(WaifuEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(WaifuEntity.class, EntityDataSerializers.BOOLEAN);
    /** Yume: quién duerme en sus piernas ahora mismo. */
    @Nullable public UUID sleeper;
    int modeBeforeSleep = FOLLOW;

    public final WaifuKind kind;
    /** Mochila (27). La primera fila es su reserva: Kura no se la lleva. */
    public final SimpleContainer bag = new Store(27);
    /** Cofre doble de Kura. */
    public final SimpleContainer chest = new Store(54);
    /** Horno de Homura: entrada, combustible, resultado. */
    public final SimpleContainer furnace = new Store(3);
    /** Pedido de Kozue: el objeto que quieres que fabrique. */
    public final SimpleContainer order = new Store(1);
    public final int[] furnaceData = new int[4];
    public final ContainerData furnaceSync = new ContainerData() {
        @Override public int get(int i) { return furnaceData[i]; }
        @Override public void set(int i, int v) { furnaceData[i] = v; }
        @Override public int getCount() { return 4; }
    };
    public int storedXp;
    public float furnaceXp;
    @Nullable private UUID owner;
    @Nullable public BlockPos anchor;
    final WaifuWork work = new WaifuWork(this);

    public WaifuEntity(EntityType<? extends WaifuEntity> type, Level level) {
        super(type, level);
        kind = ModRegistry.kindOf(type);
        setPersistenceRequired();
        setCanPickUpLoot(false);
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0.0F);
        if (kind == WaifuKind.CABALLO || kind == WaifuKind.ENDERMAN || kind == WaifuKind.WARDEN || kind == WaifuKind.GOLEM) setMaxUpStep(1.1F);
        if (kind == WaifuKind.PIGLIN) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.GOLDEN_SWORD));
        if (kind == WaifuKind.WITHER) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.STONE_SWORD));
    }

    public static AttributeSupplier.Builder attributes(WaifuKind k) {
        AttributeSupplier.Builder b = PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, k.health).add(Attributes.MOVEMENT_SPEED, k.speed)
                .add(Attributes.ATTACK_DAMAGE, k.attack).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.ARMOR, k.armor);
        if (k == WaifuKind.GOLEM || k == WaifuKind.WARDEN) b.add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ATTACK_KNOCKBACK, 1.5);
        if (k == WaifuKind.PANDA || k == WaifuKind.SLIME) b.add(Attributes.ATTACK_KNOCKBACK, 1.2);
        return b;
    }

    // ------------------------------------------------------------------ comercio (Elena, la aldeana)
    @Nullable private Player trader;
    private net.minecraft.world.item.trading.MerchantOffers offers;
    private long restockDay = -1;
    private int merchantXp;

    @Override public void setTradingPlayer(@Nullable Player p) { trader = p; }
    @Nullable @Override public Player getTradingPlayer() { return trader; }
    @Override public void overrideOffers(net.minecraft.world.item.trading.MerchantOffers o) { offers = o; }
    @Override public void notifyTradeUpdated(ItemStack st) {}
    @Override public int getVillagerXp() { return merchantXp; }
    @Override public void overrideXp(int xp) { merchantXp = xp; }
    @Override public boolean showProgressBar() { return false; }
    @Override public net.minecraft.sounds.SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return level().isClientSide; }

    @Override
    public void notifyTrade(net.minecraft.world.item.trading.MerchantOffer o) {
        o.increaseUses();
        merchantXp += o.getXp();
        cheer(40);
        playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.3F);
        if (o.shouldRewardExp()) level().addFreshEntity(new ExperienceOrb(level(), getX(), getY() + 0.5, getZ(), 3 + random.nextInt(4)));
    }

    @Override
    public net.minecraft.world.item.trading.MerchantOffers getOffers() {
        if (offers == null) offers = new net.minecraft.world.item.trading.MerchantOffers();
        if (offers.isEmpty()) defaultOffers();
        long day = level().getDayTime() / 24000L;
        if (day != restockDay) {                    // se reabastece cada día
            restockDay = day;
            offers.forEach(net.minecraft.world.item.trading.MerchantOffer::resetUses);
        }
        return offers;
    }

    private void defaultOffers() {
        net.minecraft.world.item.Item E = net.minecraft.world.item.Items.EMERALD;
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(net.minecraft.world.item.Items.WHEAT, 20), new ItemStack(E), 16, 2, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(net.minecraft.world.item.Items.PAPER, 24), new ItemStack(E), 16, 2, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(E), new ItemStack(net.minecraft.world.item.Items.BREAD, 6), 16, 1, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(E, 2), new ItemStack(net.minecraft.world.item.Items.GOLDEN_CARROT, 3), 12, 3, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(E, 3), new ItemStack(net.minecraft.world.item.Items.BOOKSHELF), 12, 3, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(E, 8), new ItemStack(net.minecraft.world.item.Items.NAME_TAG), 6, 5, 0.05F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MODE, FOLLOW);
        entityData.define(HAPPY, 0);
        entityData.define(LIT, false);
        entityData.define(BUSY, false);
        entityData.define(SITTING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true) {
            @Override public boolean canUse() {
                return !kind.ranged() && getMode() != STAY && !isSitting() && !isVehicle() && super.canUse();
            }
        });
        goalSelector.addGoal(3, new WaifuGoals.Work(this));
        goalSelector.addGoal(4, new WaifuGoals.Follow(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override public boolean canUse() { return getMode() == WORK && !isBusy() && super.canUse(); }
        });
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, WaifuEntity.class));
        targetSelector.addGoal(2, new WaifuGoals.Protect(this));
        targetSelector.addGoal(3, new WaifuGoals.Hunt(this));
    }

    // ------------------------------------------------------------------ estado

    public int getMode() { return entityData.get(MODE); }

    public void setMode(int m) {
        entityData.set(MODE, m);
        if (m == WORK) anchor = blockPosition();
        if (m == STAY) getNavigation().stop();
        work.reset();
    }


    public boolean isHappy() { return entityData.get(HAPPY) > 0; }
    public void cheer(int ticks) { entityData.set(HAPPY, ticks); }
    public boolean isLit() { return entityData.get(LIT); }
    public boolean isSitting() { return entityData.get(SITTING); }
    public void setSitting(boolean b) { entityData.set(SITTING, b); }
    public boolean isBusy() { return entityData.get(BUSY); }
    void setBusy(boolean b) { if (b != isBusy()) entityData.set(BUSY, b); }

    @Nullable public UUID getOwner() { return owner; }
    public void setOwner(@Nullable UUID u) { owner = u; }

    @Nullable
    public Player ownerPlayer() {
        return owner == null ? null : level().getPlayerByUUID(owner);
    }

    public boolean isOwner(Entity e) {
        return e instanceof Player p && owner != null && owner.equals(p.getUUID());
    }

    public Component name() {
        return Component.literal(kind.color + kind.display);
    }

    public void say(Player p, String text) {
        p.sendSystemMessage(Component.literal(kind.color + "§l" + kind.display + "§r§7: " + text));
    }

    /** Guarda objetos donde corresponde (Kura en su cofre, las demás en la mochila). Devuelve lo que sobró. */
    public ItemStack store(ItemStack st) {
        SimpleContainer c = kind == WaifuKind.KURA ? chest : bag;
        ItemStack rest = c.addItem(st);
        if (!rest.isEmpty() && kind == WaifuKind.KURA) rest = bag.addItem(rest);
        return rest;
    }

    // ------------------------------------------------------------------ vida

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (kind == WaifuKind.HOMURA && isLit() && random.nextInt(4) == 0) {
                level().addParticle(ParticleTypes.FLAME, getX() + (random.nextDouble() - 0.5) * 0.6, getY() + 1.0 + random.nextDouble() * 0.4,
                        getZ() + (random.nextDouble() - 0.5) * 0.6, 0, 0.01, 0);
            }
            if (kind == WaifuKind.SHIORI && random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.ENCHANT, getX() + (random.nextDouble() - 0.5) * 1.6, getY() + 1.6 + random.nextDouble() * 0.6,
                        getZ() + (random.nextDouble() - 0.5) * 1.6, 0, -0.4, 0);
            }
            if (kind == WaifuKind.ENDERMAN && random.nextInt(2) == 0) {
                level().addParticle(ParticleTypes.PORTAL, getRandomX(0.5), getRandomY() - 0.25, getRandomZ(0.5), (random.nextDouble() - 0.5) * 2, -random.nextDouble(),
                        (random.nextDouble() - 0.5) * 2);
            }
            if (kind == WaifuKind.BRUJA && random.nextInt(10) == 0) level().addParticle(ParticleTypes.WITCH, getRandomX(0.5), getY() + 2.2, getRandomZ(0.5), 0, 0, 0);
            if (kind == WaifuKind.BLAZE && random.nextInt(3) == 0) {
                level().addParticle(random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.SMOKE, getRandomX(0.8), getY() + random.nextDouble() * getBbHeight() * 0.7, getRandomZ(0.8), 0, 0.02, 0);
            }
            if (kind == WaifuKind.WARDEN && random.nextInt(8) == 0) level().addParticle(ParticleTypes.SCULK_SOUL, getRandomX(0.6), getY() + getBbHeight() * 0.6, getRandomZ(0.6), 0, 0.03, 0);
            if (kind == WaifuKind.ALLAY && random.nextInt(4) == 0) level().addParticle(ParticleTypes.END_ROD, getRandomX(0.6), getY() + random.nextDouble() * 1.4, getRandomZ(0.6), 0, 0.01, 0);
            if ((kind == WaifuKind.DINA || kind == WaifuKind.MITA) && random.nextInt(6) == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY() + getBbHeight() + 0.25, getZ(), 0, 0.03, 0);
            if (kind == WaifuKind.SLIME && random.nextInt(14) == 0) level().addParticle(ParticleTypes.ITEM_SLIME, getRandomX(0.5), getY() + 0.2, getRandomZ(0.5), 0, 0, 0);
            if (kind == WaifuKind.CAMA && isSitting() && random.nextInt(20) == 0) level().addParticle(ParticleTypes.NOTE, getX(), getY() + 1.6, getZ(), random.nextDouble(), 0, 0);
            if (isHappy() && random.nextInt(12) == 0) level().addParticle(ParticleTypes.HEART, getX(), getY() + 2.1, getZ(), 0, 0.05, 0);
            return;
        }
        int h = entityData.get(HAPPY);
        if (h > 0) entityData.set(HAPPY, h - 1);
        if (tickCount % (kind == WaifuKind.ZOMBIE ? 15 : 40) == 0 && getHealth() < getMaxHealth()) heal(1.0F);
        if (kind == WaifuKind.HOMURA) WaifuWork.furnaceTick(this);
        if (sleeper != null || isSitting()) Sleep.tick(this);
        work.always();
        if (tickCount % 40 == 0 && ownerPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) com.totallywaifu.data.Collection.refresh(sp, this);
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (isOwner(src.getEntity()) || src.getEntity() instanceof WaifuEntity) return false;
        if ((kind == WaifuKind.DINA || kind == WaifuKind.MITA || kind == WaifuKind.CREEPER || kind == WaifuKind.WARDEN)
                && src.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return false;
        if (src.getDirectEntity() instanceof WaifuShot) return false;
        return super.hurt(src, amount);
    }

    @Override
    public boolean canAttack(LivingEntity t) {
        if (isOwner(t) || t instanceof WaifuEntity || t instanceof Player) return false;
        if (t instanceof TamableAnimal ta && owner != null && owner.equals(ta.getOwnerUUID())) return false;
        return super.canAttack(t);
    }

    @Override
    public void die(DamageSource src) {
        if (!level().isClientSide) {
            // no muere de verdad: se refugia en un corazón con todas sus cosas
            setHealth(getMaxHealth());
            if (!com.totallywaifu.data.Collection.onDefeat(this)) spawnAtLocation(HeartItem.withWaifu(this));
            ((ServerLevel) level()).sendParticles(ParticleTypes.HEART, getX(), getY() + 1, getZ(), 12, 0.5, 0.6, 0.5, 0);
            discard();
            return;
        }
        super.die(src);
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level lv) {
        return ModRegistry.kindOf(getType()) == WaifuKind.ARANA ? new net.minecraft.world.entity.ai.navigation.WallClimberNavigation(this, lv) : super.createNavigation(lv);
    }

    @Override
    public boolean onClimbable() {
        return super.onClimbable() || (kind == WaifuKind.ARANA && horizontalCollision);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        if (kind.noFall()) return false;
        return super.causeFallDamage(dist, mult, src);
    }

    @Override
    public boolean doHurtTarget(Entity t) {
        boolean hit = super.doHurtTarget(t);
        if (hit && kind == WaifuKind.GOLEM) {                 // ¡por los aires!
            t.setDeltaMovement(t.getDeltaMovement().add(0, 0.7, 0));
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.2F);
        }
        if (hit) MobGirls.onHit(this, t);
        return hit;
    }

    @Override
    public boolean isPushable() {
        return !isSitting() && super.isPushable();
    }

    // ------------------------------------------------------------------ Brisa se puede montar
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (kind == WaifuKind.CABALLO && getFirstPassenger() instanceof Player p) return p;
        return super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player p, net.minecraft.world.phys.Vec3 in) {
        super.tickRidden(p, in);
        setRot(p.getYRot(), p.getXRot() * 0.5F);
        yRotO = yBodyRot = yHeadRot = getYRot();
    }

    @Override
    protected net.minecraft.world.phys.Vec3 getRiddenInput(Player p, net.minecraft.world.phys.Vec3 in) {
        float side = p.xxa * 0.5F, fwd = p.zza;
        if (fwd <= 0) fwd *= 0.35F;
        return new net.minecraft.world.phys.Vec3(side, 0, fwd);
    }

    @Override
    protected float getRiddenSpeed(Player p) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.6F;
    }

    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() * 0.72;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource src, int looting, boolean hit) {}

    // ------------------------------------------------------------------ interacción

    @Override
    protected InteractionResult mobInteract(Player p, InteractionHand hand) {
        ItemStack held = p.getItemInHand(hand);
        if (held.is(ModRegistry.WAND.get())) return InteractionResult.PASS;     // la vara se encarga
        if (level().isClientSide) return InteractionResult.SUCCESS;
        ServerPlayer sp = (ServerPlayer) p;
        if (owner == null) {
            owner = p.getUUID();
            com.totallywaifu.data.Collection.track(sp, this);
            say(p, "¡Hola! Soy " + kind.display + ". Desde ahora te ayudo. §8(Shift + clic: mi panel)");
            cheer(80);
            return InteractionResult.CONSUME;
        }
        // regalo: una flor la pone feliz y la cura
        if (held.is(ItemTags.FLOWERS) && hand == InteractionHand.MAIN_HAND) {
            if (!p.getAbilities().instabuild) held.shrink(1);
            setHealth(getMaxHealth());
            cheer(120);
            say(p, "¡Para mí? ¡Gracias! ♥");
            playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.4F);
            return InteractionResult.CONSUME;
        }
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!p.isShiftKeyDown() && isOwner(p)) {
            InteractionResult r = WaifuWork.interact(this, sp, held);
            if (r != null) return r;
        }
        if (p.isShiftKeyDown() || !kind.station()) {
            if (!isOwner(p)) {
                say(p, "Solo mi dueño puede ver mi mochila.");
                return InteractionResult.CONSUME;
            }
            NetworkHooks.openScreen(sp, new SimpleMenuProvider((id, inv, pl) -> new WaifuMenu(id, inv, this),
                    Component.literal(kind.display + " · " + kind.role)), buf -> {
                buf.writeVarInt(getId());
                buf.writeVarInt(kind.ordinal());
            });
            return InteractionResult.CONSUME;
        }
        openStation(sp);
        return InteractionResult.CONSUME;
    }

    /** El menú de su mesa (crafteo, horno, cofre o encantamientos). */
    public void openStation(ServerPlayer p) {
        getNavigation().stop();
        getLookControl().setLookAt(p);
        cheer(40);
        ContainerLevelAccess access = ContainerLevelAccess.create(level(), blockPosition());
        Component title = Component.literal(kind.display);
        switch (kind) {
            case KOZUE -> p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new WaifuCraftingMenu(id, inv, access, this), title));
            case HOMURA -> p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new FurnaceMenu(id, inv, furnace, furnaceSync), title));
            case KURA -> {
                playSound(SoundEvents.CHEST_OPEN, 0.8F, 1.1F);
                p.openMenu(new SimpleMenuProvider((id, inv, pl) -> ChestMenu.sixRows(id, inv, chest), title));
            }
            case SHIORI -> p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new WaifuEnchantMenu(id, inv, access, this), title));
            case CAMA -> Sleep.start(p, this);
            default -> { }
        }
    }

    /** Se guarda dentro de un Corazón de Waifu (con su mochila y todo) para llevarla contigo. */
    public void packIntoHeart(Player p) {
        ItemStack heart = HeartItem.withWaifu(this);
        if (!p.getInventory().add(heart)) p.drop(heart, false);
        playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.6F);
        ((ServerLevel) level()).sendParticles(ParticleTypes.HEART, getX(), getY() + 1, getZ(), 10, 0.4, 0.6, 0.4, 0);
        discard();
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putInt("Mode", getMode());
        if (offers != null && !offers.isEmpty()) t.put("Offers", offers.createTag());
        t.putInt("MerchantXp", merchantXp);
        if (owner != null) t.putUUID("Owner", owner);
        if (anchor != null) t.putLong("Anchor", anchor.asLong());
        t.put("Bag", save(bag));
        t.put("Chest", save(chest));
        t.put("Furnace", save(furnace));
        t.put("Order", save(order));
        t.putIntArray("FurnaceData", furnaceData);
        t.putInt("Xp", storedXp);
        t.putFloat("FurnaceXp", furnaceXp);
        t.putInt("ModeBeforeSleep", modeBeforeSleep);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        entityData.set(MODE, t.getInt("Mode"));
        if (t.contains("Offers")) offers = new net.minecraft.world.item.trading.MerchantOffers(t.getCompound("Offers"));
        merchantXp = t.getInt("MerchantXp");
        owner = t.hasUUID("Owner") ? t.getUUID("Owner") : null;
        anchor = t.contains("Anchor") ? BlockPos.of(t.getLong("Anchor")) : null;
        load(bag, t.getList("Bag", Tag.TAG_COMPOUND));
        load(chest, t.getList("Chest", Tag.TAG_COMPOUND));
        load(furnace, t.getList("Furnace", Tag.TAG_COMPOUND));
        load(order, t.getList("Order", Tag.TAG_COMPOUND));
        int[] fd = t.getIntArray("FurnaceData");
        System.arraycopy(fd, 0, furnaceData, 0, Math.min(4, fd.length));
        storedXp = t.getInt("Xp");
        furnaceXp = t.getFloat("FurnaceXp");
        modeBeforeSleep = t.getInt("ModeBeforeSleep");
        if (sleeper == null) setSitting(false);
    }

    private static ListTag save(Container c) {
        ListTag l = new ListTag();
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack s = c.getItem(i);
            if (s.isEmpty()) continue;
            CompoundTag e = new CompoundTag();
            e.putShort("Slot", (short) i);
            s.save(e);
            l.add(e);
        }
        return l;
    }

    private static void load(Container c, ListTag l) {
        c.clearContent();
        for (int i = 0; i < l.size(); i++) {
            CompoundTag e = l.getCompound(i);
            int s = e.getShort("Slot");
            if (s >= 0 && s < c.getContainerSize()) c.setItem(s, ItemStack.of(e));
        }
    }

    void setLit(boolean b) { if (b != isLit()) entityData.set(LIT, b); }

    /** Contenedor que solo sirve mientras estés cerca de ella. */
    public class Store extends SimpleContainer {
        public Store(int n) { super(n); }

        @Override
        public boolean stillValid(Player p) {
            return isAlive() && distanceToSqr(p) < 8 * 8;
        }

        @Override
        public void stopOpen(Player p) {
            if (this == chest && !level().isClientSide) playSound(SoundEvents.CHEST_CLOSE, 0.8F, 1.1F);
        }
    }
}
