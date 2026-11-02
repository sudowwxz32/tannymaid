package com.bdsz.companion;

import com.bdsz.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Kaori: acompañante mecánica e inventora. Amable y muy educada, pero altanera, ruda y fuerte.
 * No puede morir: si su vida llega a cero toma una Semilla del Ermitaño de su mochila o descansa hasta recuperarse.
 */
public class KaoriEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WEAK = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TALK = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_COLLECT = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.BOOLEAN);

    public static final double BASE_HEALTH = 30.0D;

    private final SimpleContainer bag = new SimpleContainer(27);
    /** 0 casco, 1 pechera, 2 grebas, 3 botas, 4 mano principal, 5 mano secundaria */
    private final SimpleContainer equip = new SimpleContainer(6);
    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos anchor;
    @Nullable private KaoriResource gather;
    private int gatherLeft, gatherDone;
    private final Set<BlockPos> gatherSkip = new HashSet<>();
    private long lastCombatSpeech, lastHurtSpeech;
    // choza de la cápsula
    @Nullable BlockPos hutPos;
    int hutDir;
    String hutDim = "";

    public KaoriEntity(EntityType<? extends KaoriEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCanPickUpLoot(false);
        xpReward = 0;
        setCustomName(Component.literal("Kaori"));
        if (getNavigation() instanceof GroundPathNavigation nav) nav.setCanOpenDoors(true);
        equip.addListener(c -> applyEquipment());
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)        // un poco más que un jugador
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_MODE, KaoriMode.FOLLOW.ordinal());
        entityData.define(DATA_WEAK, false);
        entityData.define(DATA_TALK, -1000);
        entityData.define(DATA_COLLECT, true);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new KaoriGoals.Rest(this));
        goalSelector.addGoal(2, new KaoriGoals.Combat(this));
        goalSelector.addGoal(3, new KaoriGoals.Gather(this));
        goalSelector.addGoal(3, new KaoriGoals.Follow(this, 1.15D, 5.0F, 2.4F));
        goalSelector.addGoal(3, new KaoriGoals.Stay(this));
        goalSelector.addGoal(4, new KaoriGoals.Farm(this));
        goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        goalSelector.addGoal(5, new KaoriGoals.Collect(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new KaoriGoals.OwnerHurtBy(this));
        targetSelector.addGoal(2, new KaoriGoals.OwnerHurt(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(4, new KaoriGoals.Defend(this));
    }

    // ------------------------------------------------------------------ dueño, modo y estado

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }

    public void setOwner(Player p) { ownerUUID = p.getUUID(); }

    public boolean isOwner(Entity e) { return e != null && e.getUUID().equals(ownerUUID); }

    @Nullable
    public ServerPlayer getOwnerPlayer() {
        if (ownerUUID == null || !(level() instanceof ServerLevel sl)) return null;
        return sl.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    public KaoriMode getMode() { return KaoriMode.byId(entityData.get(DATA_MODE)); }

    public void setMode(KaoriMode m) {
        entityData.set(DATA_MODE, m.ordinal());
        if (!m.follows()) anchor = blockPosition();
        if (m != KaoriMode.FARM && gather == null) holdBest();
    }

    public boolean isWeakened() { return entityData.get(DATA_WEAK); }

    public boolean isCollecting() { return entityData.get(DATA_COLLECT); }

    public boolean isTalking() { return tickCount - entityData.get(DATA_TALK) < 50; }

    @Nullable public BlockPos getAnchor() { return anchor; }

    public SimpleContainer getBag() { return bag; }

    public SimpleContainer getEquip() { return equip; }

    public boolean isGathering() { return gather != null; }

    @Nullable public KaoriResource getGather() { return gather; }

    public boolean skipped(BlockPos p) { return gatherSkip.contains(p); }

    public void skip(BlockPos p) { gatherSkip.add(p.immutable()); }

    /** Habla (al chat del dueño) y pone cara contenta unos segundos. */
    public void say(@Nullable Player to, String text) {
        entityData.set(DATA_TALK, tickCount);
        Player p = to != null ? to : getOwnerPlayer();
        if (p != null) p.sendSystemMessage(Component.literal("§6[Kaori] §f" + text));
    }

    public String pick(String... lines) {
        return lines[getRandom().nextInt(lines.length)];
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (isWeakened()) {
            // descansa sentada y se recupera poco a poco
            if (tickCount % 10 == 0) heal(1.0F);
            if (tickCount % 30 == 0 && level() instanceof ServerLevel sl)
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.2, getZ(), 2, 0.3, 0.3, 0.3, 0);
            if (getHealth() >= getMaxHealth()) recover();
        } else if (tickCount % 80 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);                                    // regeneración natural (es fuerte)
        }
        if (getY() < level().getMinBuildHeight() + 2 || isInLava()) teleportToSafety();
        if (tickCount % 100 == 0 && KaoriActions.isStale(this)) {
            // el dueño invocó otra Kaori: esta se despide y deja sus cosas
            for (int i = 0; i < bag.getContainerSize(); i++) spawnAtLocation(bag.removeItemNoUpdate(i));
            for (int i = 0; i < equip.getContainerSize(); i++) spawnAtLocation(equip.removeItemNoUpdate(i));
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 30, 0.5, 0.8, 0.5, 0.05);
            discard();
        }
    }

    // ------------------------------------------------------------------ vida: nunca muere

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return false;
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) { teleportToSafety(); return false; }
        if (isWeakened()) return false;
        Entity attacker = source.getEntity();
        if (attacker != null && isOwner(attacker)) return false;
        if (isInvulnerableTo(source)) return false;
        if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE)) amount *= 0.85F;       // es dura
        if (amount >= getHealth()) {
            if (eatSenzu()) return true;
            becomeWeakened();
            return true;
        }
        if (amount > 4 && level().getGameTime() - lastHurtSpeech > 300) {
            lastHurtSpeech = level().getGameTime();
            say(null, pick("¡Tch! ¿Eso es todo lo que tienes?", "¡Auch! Ya me las pagarás...", "No te preocupes por mí, ¡concéntrate!"));
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && !isRemoved()) {
            dead = false;
            if (!eatSenzu()) becomeWeakened();
            return;
        }
        super.die(source);
    }

    /** Si tiene una Semilla del Ermitaño en la mochila, se la come y queda como nueva. */
    private boolean eatSenzu() {
        for (int i = 0; i < bag.getContainerSize(); i++) {
            if (bag.getItem(i).is(ModRegistry.SENZU_BEAN.get())) {
                bag.getItem(i).shrink(1);
                bag.setChanged();
                setHealth(getMaxHealth());
                clearFire();
                removeAllEffects();
                level().playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.3F);
                if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1, getZ(), 30, 0.4, 0.6, 0.4, 0.3);
                say(null, "*ñam* ¡Ja! Con una Semilla del Ermitaño quedo como nueva. ¿Seguimos?");
                return true;
            }
        }
        return false;
    }

    public void becomeWeakened() {
        setHealth(1.0F);
        entityData.set(DATA_WEAK, true);
        setTarget(null);
        setAggressive(false);
        getNavigation().stop();
        clearFire();
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 15, 0.4, 0.4, 0.4, 0.02);
        say(null, "Uff... necesito descansar un momento. No es que esté cansada, ¿eh? Solo... descanso.");
    }

    public void recover() {
        entityData.set(DATA_WEAK, false);
        setHealth(getMaxHealth());
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.8, getZ(), 5, 0.4, 0.3, 0.4, 0.02);
        say(null, "¡Listo! Kaori está de vuelta. ¿Me extrañaste? Claro que sí.");
    }

    private void teleportToSafety() {
        clearFire();
        setDeltaMovement(0, 0, 0);
        fallDistance = 0;
        ServerPlayer o = getOwnerPlayer();
        if (o != null && o.level() == level()) moveTo(o.getX(), o.getY(), o.getZ(), getYRot(), getXRot());
        else if (level() instanceof ServerLevel sl) {
            BlockPos s = sl.getSharedSpawnPos();
            moveTo(s.getX() + 0.5, sl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, s.getX(), s.getZ()), s.getZ() + 0.5);
        }
    }

    @Override
    public boolean canAttack(LivingEntity t) {
        if (t == this || isOwner(t) || t instanceof KaoriEntity) return false;
        if (t instanceof Player p && (p.isCreative() || p.isSpectator() || ownerUUID != null)) return false;
        return super.canAttack(t);
    }

    @Override
    public void setTarget(@Nullable LivingEntity t) {
        if (t != null && (isWeakened() || !canAttack(t))) return;
        LivingEntity prev = getTarget();
        super.setTarget(t);
        if (t != null && prev == null && !level().isClientSide && level().getGameTime() - lastCombatSpeech > 400) {
            lastCombatSpeech = level().getGameTime();
            say(null, pick("¡Hmph! Déjamelo a mí.", "¿Te atreves a tocar a mi compañero? Error.", "Con permiso... ¡voy a romperle la cara!",
                    "Quédate atrás, yo me encargo."));
        }
    }

    public boolean isDefendTarget(Mob m) {
        if (!m.isAlive() || !(m instanceof Enemy)) return false;
        if (m instanceof NeutralMob n && !n.isAngry()) return false;
        return canAttack(m);
    }

    /** Golpe fuerte: con arma usa su daño; sin arma golpea con ki. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean ok = super.doHurtTarget(target);
        if (ok && getMainHandItem().isEmpty() && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 8, 0.3, 0.3, 0.3, 0.2);
            if (target instanceof LivingEntity le) le.knockback(0.6, getX() - le.getX(), getZ() - le.getZ());
        }
        if (ok && !getMainHandItem().isEmpty() && getMainHandItem().isDamageableItem()) {
            getMainHandItem().hurtAndBreak(1, this, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            equip.setItem(4, getMainHandItem());
        }
        return ok;
    }

    /** Ráfaga de ki contra enemigos lejanos. */
    public void kiBlast(LivingEntity target) {
        com.bdsz.entity.KiBlastEntity blast = new com.bdsz.entity.KiBlastEntity(ModRegistry.KI_BLAST.get(), level());
        blast.setOwner(this);
        net.minecraft.world.phys.Vec3 dir = target.getEyePosition().subtract(getEyePosition()).normalize();
        blast.setPos(getX() + dir.x * 0.8, getEyeY() - 0.2, getZ() + dir.z * 0.8);
        blast.setup(0xFF9A3C, 0.8F, 9.0F);
        blast.shoot(dir.x, dir.y, dir.z, 1.6F, 0.0F);
        level().addFreshEntity(blast);
        swing(InteractionHand.MAIN_HAND, true);
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.7F, 1.6F);
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }

    @Override public boolean requiresCustomPersistence() { return true; }

    // ------------------------------------------------------------------ interacción

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        ServerPlayer sp = (ServerPlayer) player;
        if (ownerUUID == null) {
            setOwner(player);
            KaoriActions.data(sp).putUUID("KaoriUUID", getUUID());
        }
        if (!isOwner(player)) {
            say(sp, "Disculpa, pero ya tengo compañero. Búscate tu propia mecánica genio.");
            return InteractionResult.CONSUME;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEdible() && getHealth() < getMaxHealth()) {
            FoodProperties food = stack.getFoodProperties(this);
            heal(food == null ? 4 : food.getNutrition() * 1.5F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            level().playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.2F);
            say(sp, pick("Gracias. No tenía hambre... pero estaba rico.", "Muy amable de tu parte. ¡Mmm!"));
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) openInventory(sp);
        else KaoriActions.openMenu(sp, this);
        return InteractionResult.CONSUME;
    }

    public void openInventory(ServerPlayer p) {
        NetworkHooks.openScreen(p, new SimpleMenuProvider((id, inv, pl) -> new KaoriMenu(id, inv, bag, equip, this),
                Component.literal("Mochila de Kaori")), buf -> buf.writeVarInt(getId()));
    }

    // ------------------------------------------------------------------ equipo

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    public void applyEquipment() {
        if (level().isClientSide) return;
        for (int i = 0; i < 6; i++) {
            if (i == 4 && (isGathering() || getMode() == KaoriMode.FARM)) continue;   // con la herramienta del trabajo
            setItemSlot(SLOTS[i], equip.getItem(i));
        }
    }

    /** Vuelve a llevar su arma (después de trabajar). */
    public void holdBest() {
        setItemSlot(EquipmentSlot.MAINHAND, equip.getItem(4));
    }

    /** La mejor herramienta de su mochila o equipo para romper un bloque. */
    public ItemStack bestTool(net.minecraft.world.level.block.state.BlockState state) {
        ItemStack best = ItemStack.EMPTY;
        float speed = 1.0F;
        for (int i = 0; i < bag.getContainerSize() + 1; i++) {
            ItemStack s = i < bag.getContainerSize() ? bag.getItem(i) : equip.getItem(4);
            if (s.isEmpty() || !(s.getItem() instanceof DiggerItem)) continue;
            float sp = s.getDestroySpeed(state);
            if (sp > speed) { speed = sp; best = s; }
        }
        return best;
    }

    public boolean canStore(ItemStack stack) { return bag.canAddItem(stack); }

    public void storeOrDrop(ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack rest = bag.addItem(stack);
        if (!rest.isEmpty()) spawnAtLocation(rest);
    }

    public boolean consumeItem(Item item) {
        for (int i = 0; i < bag.getContainerSize(); i++) {
            if (bag.getItem(i).is(item)) {
                bag.getItem(i).shrink(1);
                bag.setChanged();
                return true;
            }
        }
        return false;
    }

    public void pickup(net.minecraft.world.entity.item.ItemEntity it, int count) {
        take(it, count);
    }

    // ------------------------------------------------------------------ órdenes

    public void startGather(ServerPlayer p, KaoriResource r, int amount) {
        gather = r;
        gatherLeft = amount;
        gatherDone = 0;
        gatherSkip.clear();
        say(p, pick("Hmph, recolectar " + r.display + "... está bien, pero solo porque me lo pides con educación.",
                "¿" + amount + " de " + r.display + "? Pan comido. Observa y aprende."));
    }

    public void onGathered() {
        gatherDone++;
        if (--gatherLeft <= 0 || !bag.canAddItem(new ItemStack(Items.COBBLESTONE))) finishGather(gatherLeft <= 0 ? "ok" : "full");
    }

    public void finishGather(String why) {
        KaoriResource r = gather;
        gather = null;
        holdBest();
        if (r == null) return;
        switch (why) {
            case "ok" -> say(null, "Listo: " + gatherDone + " de " + r.display + " en mi mochila. De nada.");
            case "full" -> say(null, "Mi mochila está llena. ¡Vacíala, por favor! (" + gatherDone + " de " + r.display + ")");
            case "tool" -> say(null, "Para " + r.display + " necesito " + r.toolName() + " en mi mochila. No pienso picar con las uñas.");
            case "stop" -> say(null, "Está bien, lo dejo. Junté " + gatherDone + " de " + r.display + ".");
            default -> say(null, "No encuentro más " + r.display + " por aquí. Junté " + gatherDone + ".");
        }
    }

    public void setCollecting(boolean b) { entityData.set(DATA_COLLECT, b); }

    public void teleportToOwner(ServerPlayer o) {
        if (o.level() != level()) {
            teleportTo((ServerLevel) o.level(), o.getX(), o.getY(), o.getZ(), EnumSet.noneOf(RelativeMovement.class), getYRot(), getXRot());
            return;
        }
        teleportNear(o);
    }

    public void teleportNear(LivingEntity t) {
        BlockPos base = t.blockPosition();
        for (int i = 0; i < 14; i++) {
            int dx = getRandom().nextInt(5) - 2, dz = getRandom().nextInt(5) - 2;
            if (Math.abs(dx) < 1 && Math.abs(dz) < 1) continue;
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos pos = base.offset(dx, dy, dz);
                if (canStandAt(pos)) {
                    moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, getYRot(), getXRot());
                    getNavigation().stop();
                    return;
                }
            }
        }
        moveTo(t.getX(), t.getY(), t.getZ(), getYRot(), getXRot());
        getNavigation().stop();
    }

    private boolean canStandAt(BlockPos pos) {
        if (WalkNodeEvaluator.getBlockPathTypeStatic(level(), pos.mutable()) != BlockPathTypes.WALKABLE) return false;
        BlockPos d = pos.subtract(blockPosition());
        return level().noCollision(this, getBoundingBox().move(d.getX(), d.getY(), d.getZ()));
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putInt("Mode", getMode().ordinal());
        tag.putBoolean("Weak", isWeakened());
        tag.putBoolean("Collect", isCollecting());
        if (anchor != null) tag.putLong("Anchor", anchor.asLong());
        tag.put("Bag", save(bag));
        tag.put("Equip", save(equip));
        if (hutPos != null) {
            tag.putLong("Hut", hutPos.asLong());
            tag.putInt("HutDir", hutDir);
            tag.putString("HutDim", hutDim);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) ownerUUID = tag.getUUID("Owner");
        entityData.set(DATA_MODE, tag.getInt("Mode"));
        entityData.set(DATA_WEAK, tag.getBoolean("Weak"));
        entityData.set(DATA_COLLECT, !tag.contains("Collect") || tag.getBoolean("Collect"));
        anchor = tag.contains("Anchor") ? BlockPos.of(tag.getLong("Anchor")) : null;
        load(bag, tag.getList("Bag", 10));
        load(equip, tag.getList("Equip", 10));
        hutPos = tag.contains("Hut") ? BlockPos.of(tag.getLong("Hut")) : null;
        hutDir = tag.getInt("HutDir");
        hutDim = tag.getString("HutDim");
        applyEquipment();
    }

    private static ListTag save(SimpleContainer c) {
        ListTag l = new ListTag();
        for (int i = 0; i < c.getContainerSize(); i++) {
            if (c.getItem(i).isEmpty()) continue;
            CompoundTag t = new CompoundTag();
            t.putByte("Slot", (byte) i);
            c.getItem(i).save(t);
            l.add(t);
        }
        return l;
    }

    private static void load(SimpleContainer c, ListTag l) {
        for (int i = 0; i < c.getContainerSize(); i++) c.setItem(i, ItemStack.EMPTY);
        for (int i = 0; i < l.size(); i++) {
            CompoundTag t = l.getCompound(i);
            int s = t.getByte("Slot") & 255;
            if (s < c.getContainerSize()) c.setItem(s, ItemStack.of(t));
        }
    }
}
