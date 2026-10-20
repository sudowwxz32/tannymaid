package com.laury.laurymod.entity;

import com.laury.laurymod.entity.goal.*;
import com.laury.laurymod.item.DragonMaceItem;
import com.laury.laurymod.item.DragonScytheItem;
import com.laury.laurymod.item.FireDragonSwordItem;
import com.laury.laurymod.item.ForceDragonSwordItem;
import com.laury.laurymod.menu.LauryMenu;
import com.laury.laurymod.registry.ModItems;
import com.laury.laurymod.util.LauryChat;
import com.laury.laurymod.util.LauryLookup;
import com.laury.laurymod.util.LaurySpeech;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Laury, mujer dragón. Vida de Warden (500), no muere (se debilita y se regenera),
 * se transforma en un dragón azul que se puede montar y volar, pelea, recolecta, craftea y conversa.
 */
public class LauryEntity extends PathfinderMob implements PlayerRideableJumping {

    private static final EntityDataAccessor<Boolean> DATA_DRAGON = SynchedEntityData.defineId(LauryEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_WEAKENED = SynchedEntityData.defineId(LauryEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TALK = SynchedEntityData.defineId(LauryEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(LauryEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BREATH = SynchedEntityData.defineId(LauryEntity.class, EntityDataSerializers.INT);

    public static final int MODE_FOLLOW = 0;
    public static final int MODE_STAY = 1;

    private static final EntityDimensions HUMAN_SIZE = EntityDimensions.scalable(0.6F, 1.8F);
    private static final EntityDimensions DRAGON_SIZE = EntityDimensions.scalable(3.0F, 2.8F);
    private static final UUID DRAGON_DAMAGE = UUID.fromString("5b2a3c1e-7a51-4c55-9a4a-1d7b6f0a2e01");
    private static final UUID DRAGON_ARMOR = UUID.fromString("5b2a3c1e-7a51-4c55-9a4a-1d7b6f0a2e02");
    private static final UUID DRAGON_SPEED = UUID.fromString("5b2a3c1e-7a51-4c55-9a4a-1d7b6f0a2e03");
    private static final float FLY_SPEED = 0.8F;
    private static final float MAX_TURN = 7.5F;          // grados por tick: el dragón gira con inercia

    // ---- estado de vuelo (cliente): inclinación, alabeo y aleteo, para animación y cámara
    public float flightPitch, prevFlightPitch;            // + = nariz abajo (picada)
    public float flightRoll, prevFlightRoll;              // + = ala derecha abajo (giro a la derecha)
    public float flapPhase, prevFlapPhase;
    public float flapStrength;                            // 0 = alas quietas/recogidas, 1 = aleteo completo
    public float diveAmount;                              // 0..1, alas pegadas al cuerpo en picada
    public float flightSpeed;                             // bloques por tick (suavizado)
    private float pendingBoost;
    private static final int TALK_TICKS = 50;

    // Barra de herramientas: 0 espada, 1 hacha, 2 pico, 3 pala, 4 azada, 5 arco, 6 escudo
    public static final int SLOT_SWORD = 0, SLOT_AXE = 1, SLOT_PICK = 2, SLOT_SHOVEL = 3, SLOT_HOE = 4, SLOT_BOW = 5, SLOT_SHIELD = 6;
    private static final EquipmentSlot[] ARMOR_ORDER = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final SimpleContainer inventory = new SimpleContainer(27);
    private final SimpleContainer tools = new SimpleContainer(LauryMenu.TOOL_COUNT);
    private final SimpleContainer armor = new SimpleContainer(4);

    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos anchorPos;
    private long lastCombatSpeech = -10000L;
    private int bowCooldown;
    private int breathCooldown;

    // recolección
    @Nullable private LauryResource gatherResource;
    private int gatherTarget;
    private int gatherDone;
    private final Set<BlockPos> gatherSkip = new HashSet<>();

    // conversación
    private record Pending(int at, String text) {}
    private final Deque<Pending> pending = new ArrayDeque<>();
    private final Map<String, Integer> lastPicks = new HashMap<>();
    private final List<String> likes = new ArrayList<>();
    @Nullable private String nickname;
    @Nullable private String expecting;
    private int expectTicks;
    private int chatWindow;
    private int worryCooldown;

    public LauryEntity(EntityType<? extends LauryEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCanPickUpLoot(false);
        xpReward = 0;
        setCustomName(Component.literal("Laury"));
        setCustomNameVisible(true);
        if (getNavigation() instanceof GroundPathNavigation nav) nav.setCanOpenDoors(true);
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
        tools.addListener(c -> updateEquipment());
        armor.addListener(c -> updateEquipment());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)               // igual que un Warden
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_DRAGON, false);
        entityData.define(DATA_WEAKENED, false);
        entityData.define(DATA_TALK, -1000);
        entityData.define(DATA_MODE, MODE_FOLLOW);
        entityData.define(DATA_BREATH, -1000);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new LauryWeakenedGoal(this));
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LauryMeleeGoal(this));
        goalSelector.addGoal(2, new LauryChatGoal(this));
        goalSelector.addGoal(3, new LauryGatherGoal(this));
        goalSelector.addGoal(3, new LauryFollowGoal(this));
        goalSelector.addGoal(3, new LauryStayGoal(this));
        goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new LauryOwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new LauryOwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(4, new LauryDefendGoal(this));
    }

    // ================================================================ estado

    public boolean isDragon() { return entityData.get(DATA_DRAGON); }
    public boolean isWeakened() { return entityData.get(DATA_WEAKENED); }
    public int getMode() { return entityData.get(DATA_MODE); }
    public void setMode(int mode) { entityData.set(DATA_MODE, mode); getNavigation().stop(); }
    public SimpleContainer getInventory() { return inventory; }
    public SimpleContainer getTools() { return tools; }
    public SimpleContainer getArmor() { return armor; }
    @Nullable public BlockPos getAnchor() { return anchorPos; }

    public boolean isTalking() {
        int now = (int) level().getGameTime();
        int start = entityData.get(DATA_TALK);
        return now - start >= 0 && now - start < TALK_TICKS;
    }

    public void markTalking() { entityData.set(DATA_TALK, (int) level().getGameTime()); }

    /** Ticks desde el último aliento de fuego (para la animación de la mandíbula). */
    public int ticksSinceBreath() { return (int) level().getGameTime() - entityData.get(DATA_BREATH); }

    public boolean isBusy() { return isGathering() || isVehicle(); }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwner(Player p) { ownerUUID = p.getUUID(); }
    public boolean isOwner(@Nullable Entity e) { return e != null && ownerUUID != null && ownerUUID.equals(e.getUUID()); }

    @Nullable
    public ServerPlayer getOwnerPlayer() {
        if (ownerUUID == null || !(level() instanceof ServerLevel sl)) return null;
        return sl.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    // ================================================================ tamaño y forma

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return isDragon() ? DRAGON_SIZE : HUMAN_SIZE;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dims) {
        return dims.height * 0.88F;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_DRAGON.equals(key)) refreshDimensions();
    }

    public void transform(boolean dragon, @Nullable ServerPlayer player) {
        if (dragon == isDragon()) {
            LaurySpeech.say(this, player, dragon ? "¡Jaja, ya soy un dragón, {p}! ¿No ves estas alas?" : "Ya estoy en forma humana, {p}.");
            return;
        }
        if (isWeakened()) {
            LaurySpeech.say(this, player, "Ahora no puedo... dame un momento para recuperarme.");
            return;
        }
        if (dragon) {
            AABB box = DRAGON_SIZE.makeBoundingBox(position());
            if (!level().noCollision(this, box)) {
                LaurySpeech.say(this, player, "¡Aquí no hay espacio para un dragón! Salgamos a un lugar abierto.");
                return;
            }
            if (isGathering()) finishGather("stopped");
        } else {
            ejectPassengers();
        }
        entityData.set(DATA_DRAGON, dragon);
        refreshDimensions();
        applyFormModifiers();
        updateEquipment();
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, getX(), getY() + 1, getZ(), 80, 1.2, 1.2, 1.2, 0.05);
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 3, 0.8, 0.8, 0.8, 0.0);
            sl.playSound(null, blockPosition(), dragon ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.ILLUSIONER_MIRROR_MOVE,
                    SoundSource.NEUTRAL, dragon ? 0.8F : 1.0F, dragon ? 1.3F : 1.0F);
        }
        LaurySpeech.say(this, player, dragon ? LaurySpeech.TO_DRAGON : LaurySpeech.TO_HUMAN);
    }

    private void applyFormModifiers() {
        boolean dragon = isDragon();
        setModifier(Attributes.ATTACK_DAMAGE, DRAGON_DAMAGE, "laury_dragon_damage", dragon ? 16.0D : 0.0D);
        setModifier(Attributes.ARMOR, DRAGON_ARMOR, "laury_dragon_armor", dragon ? 10.0D : 0.0D);
        setModifier(Attributes.MOVEMENT_SPEED, DRAGON_SPEED, "laury_dragon_speed", dragon ? 0.05D : 0.0D);
    }

    private void setModifier(net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0.0D) inst.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }

    // ================================================================ tick

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) serverTick();
        else clientFlightTick();
    }

    /** Calcula (en el cliente) la inclinación, el alabeo y el ritmo de aleteo a partir del movimiento real. */
    private void clientFlightTick() {
        prevFlightPitch = flightPitch;
        prevFlightRoll = flightRoll;
        prevFlapPhase = flapPhase;
        if (!isDragon() || isWeakened()) {
            flightPitch *= 0.8F;
            flightRoll *= 0.8F;
            flapStrength *= 0.8F;
            diveAmount *= 0.8F;
            flightSpeed = 0;
            return;
        }
        double dx = getX() - xo, dy = getY() - yo, dz = getZ() - zo;
        double hs = Math.sqrt(dx * dx + dz * dz);
        double speed = Math.sqrt(hs * hs + dy * dy);
        flightSpeed += ((float) speed - flightSpeed) * 0.2F;
        boolean airborne = !onGround();

        float targetPitch = 0, targetRoll = 0;
        if (airborne && speed > 0.04) {
            targetPitch = (float) Math.toDegrees(-Math.atan2(dy, Math.max(hs, 0.08)));
            targetPitch = Mth.clamp(targetPitch, -45.0F, 60.0F);
            float yawRate = Mth.wrapDegrees(getYRot() - yRotO);
            float yaw = getYRot() * Mth.DEG_TO_RAD;
            double lateral = dx * -Mth.cos(yaw) + dz * -Mth.sin(yaw);    // + = derrapando a la derecha
            targetRoll = Mth.clamp(yawRate * 5.0F + (float) lateral * 25.0F, -55.0F, 55.0F);
        }
        flightPitch += (targetPitch - flightPitch) * 0.14F;
        flightRoll += (targetRoll - flightRoll) * 0.12F;
        diveAmount += (Mth.clamp((flightPitch - 18.0F) / 25.0F, 0.0F, 1.0F) - diveAmount) * 0.15F;

        float rate, strength;
        if (!airborne) { rate = 0.0F; strength = 0.0F; }
        else if (diveAmount > 0.5F) { rate = 0.05F; strength = 0.15F; }                 // picada: alas recogidas
        else if (dy > 0.08) { rate = 0.42F; strength = 1.0F; }                           // ascenso: aletazos fuertes
        else if (hs > 0.45) { rate = 0.2F; strength = 0.55F; }                           // crucero: planeo y aleteo lento
        else { rate = 0.32F; strength = 0.85F; }                                         // suspendida en el aire
        flapStrength += (strength - flapStrength) * 0.1F;
        flapPhase += rate;
        // sonido de aletazo cuando el ala baja
        if (airborne && rate > 0.15F && Mth.sin(prevFlapPhase) > 0 && Mth.sin(flapPhase) <= 0) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL,
                    0.5F + flapStrength * 0.6F, 1.1F + getRandom().nextFloat() * 0.15F, false);
        }
    }

    private void serverTick() {
        ServerPlayer owner = getOwnerPlayer();
        boolean flying = isDragon() && isVehicle() && !isWeakened();
        if (isNoGravity() != flying) setNoGravity(flying);
        if (flying || isDragon()) fallDistance = 0;

        if (isWeakened()) {
            setTarget(null);
            getNavigation().stop();
            if (tickCount % 10 == 0) {
                heal(6.0F);                                  // ~12 de vida por segundo
                ((ServerLevel) level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1, getZ(), 2, 0.4, 0.4, 0.4, 0);
            }
            if (getY() < level().getMinBuildHeight() + 1 || isInLava()) teleportToSafety();
            if (getHealth() >= getMaxHealth()) recover(owner);
        } else if (tickCount % 20 == 0 && getHealth() < getMaxHealth()) {
            heal(getTarget() == null ? 4.0F : 1.0F);
        }

        if (getY() < level().getMinBuildHeight() - 8) teleportToSafety();

        // Efectos de las armas en la mano
        if (tickCount % 20 == 0 && !isWeakened()) {
            ItemStack main = getMainHandItem();
            if (main.getItem() instanceof DragonScytheItem) DragonScytheItem.bless(this);
            if (main.getItem() instanceof ForceDragonSwordItem) ForceDragonSwordItem.empower(this);
        }

        // Ataques a distancia
        if (bowCooldown > 0) bowCooldown--;
        if (breathCooldown > 0) breathCooldown--;
        LivingEntity target = getTarget();
        if (!isWeakened()) {
            if (isDragon()) {
                if (target == null && isVehicle()) target = findHostileNear(16.0D);
                if (target != null && target.isAlive() && breathCooldown == 0 && distanceTo(target) < 20.0F && hasLineOfSight(target)) {
                    fireBreath(target);
                }
            } else if (target != null && target.isAlive() && bowCooldown == 0 && !tools.getItem(SLOT_BOW).isEmpty()
                    && distanceTo(target) > 6.0F && distanceTo(target) < 24.0F && hasLineOfSight(target)) {
                shootArrow(target);
            }
        }

        if (owner != null && tickCount % 100 == 0) {
            UUID stored = LauryLookup.getStoredId(owner);
            if (stored != null && !stored.equals(getUUID())) { discard(); return; }
            if (stored == null) LauryLookup.setStoredId(owner, getUUID());
            LauryLookup.storeLastPos(owner, this);
        }

        // conversación
        if (chatWindow > 0) chatWindow--;
        if (expectTicks > 0 && --expectTicks == 0) expecting = null;
        if (worryCooldown > 0) worryCooldown--;
        int spoken = 0;
        while (!pending.isEmpty() && pending.peekFirst().at() <= tickCount && spoken++ < 2) {
            LaurySpeech.say(this, owner, pending.pollFirst().text());
        }
        if (owner != null && !isWeakened() && owner.level() == level() && distanceToSqr(owner) < 24 * 24) {
            if (worryCooldown == 0 && tickCount % 20 == 0 && owner.getHealth() <= owner.getMaxHealth() * 0.3F) {
                worryCooldown = 1200;
                LauryChat.worry(this, owner);
            } else if (getTarget() == null && chatWindow == 0 && !isVehicle() && getRandom().nextInt(4000) == 0) {
                queueSay(LauryChat.ambientLine(this, owner), 0);
            }
        }
    }

    @Nullable
    private LivingEntity findHostileNear(double r) {
        ServerPlayer owner = getOwnerPlayer();
        List<Mob> list = level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(r), m -> isValidDefendTarget(m, owner));
        return list.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    /** Aliento de fuego del dragón: daña en línea recta sin quemar bloques. */
    private void fireBreath(LivingEntity target) {
        if (!(level() instanceof ServerLevel sl)) return;
        breathCooldown = 40;
        entityData.set(DATA_BREATH, (int) level().getGameTime());
        Vec3 from = getEyePosition().add(getLookAngle().scale(2.2));
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        Vec3 step = dir.normalize();
        for (double d = 0; d < len; d += 0.5) {
            Vec3 p = from.add(step.scale(d));
            sl.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 3, 0.15 + d * 0.03, 0.15 + d * 0.03, 0.15 + d * 0.03, 0.01);
            if ((int) (d * 2) % 3 == 0) sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.01);
        }
        sl.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        ServerPlayer owner = getOwnerPlayer();
        AABB area = new AABB(from, to).inflate(1.5D);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != this && !isOwner(e)
                && !(e instanceof Player) && (e == target || e instanceof Enemy))) {
            e.hurt(damageSources().mobAttack(this), 14.0F);
            e.setSecondsOnFire(5);
        }
    }

    private void shootArrow(LivingEntity target) {
        bowCooldown = 30;
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), 1.0F);
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333D) - arrow.getY();
        double dz = target.getZ() - getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + d * 0.2D, dz, 1.8F, 3.0F);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setBaseDamage(5.0D);
        level().addFreshEntity(arrow);
        playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
        swing(InteractionHand.MAIN_HAND);
    }

    // ================================================================ combate y vida

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            ItemStack main = getMainHandItem();
            if (isDragon()) {
                playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.0F, 0.8F);
            } else if (main.getItem() instanceof FireDragonSwordItem) {
                FireDragonSwordItem.ignite(this, living);
            } else if (main.getItem() instanceof ForceDragonSwordItem) {
                ForceDragonSwordItem.heavyBlow(this, living);
            } else if (main.getItem() instanceof DragonMaceItem) {
                DragonMaceItem.smash(this, living);
            }
        }
        return hit;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float distance, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return false;
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) { teleportToSafety(); return false; }
        if (isWeakened()) return false;
        Entity attacker = source.getEntity();
        if (attacker != null && (attacker == this || isOwner(attacker) || (attacker instanceof Player p && p.getVehicle() == this))) return false;
        if (isInvulnerableTo(source)) return false;
        ItemStack shield = tools.getItem(SLOT_SHIELD);
        if (!isDragon() && !shield.isEmpty() && attacker != null && !source.is(DamageTypeTags.BYPASSES_SHIELD) && getRandom().nextFloat() < 0.4F) {
            playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.9F + getRandom().nextFloat() * 0.2F);
            if (attacker instanceof LivingEntity l) l.knockback(0.5D, getX() - l.getX(), getZ() - l.getZ());
            return false;
        }
        if (amount >= getHealth()) {
            becomeWeakened();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && !isRemoved()) {
            dead = false;
            becomeWeakened();
            return;
        }
        super.die(source);
    }

    public void becomeWeakened() {
        setHealth(1.0F);
        entityData.set(DATA_WEAKENED, true);
        setTarget(null);
        setAggressive(false);
        getNavigation().stop();
        clearFire();
        for (Entity p : getPassengers()) {
            if (p instanceof Player pl) pl.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        }
        ejectPassengers();
        setNoGravity(false);
        if (isGathering()) finishGather("stopped");
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 20, 0.5, 0.5, 0.5, 0.02);
        LaurySpeech.say(this, getOwnerPlayer(), LaurySpeech.WEAK);
    }

    public void recover(@Nullable ServerPlayer owner) {
        entityData.set(DATA_WEAKENED, false);
        setHealth(getMaxHealth());
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.HEART, getX(), getY() + getBbHeight(), getZ(), 6, 0.4, 0.3, 0.4, 0.02);
        LaurySpeech.say(this, owner, LaurySpeech.RECOVER);
    }

    private void teleportToSafety() {
        clearFire();
        setDeltaMovement(Vec3.ZERO);
        fallDistance = 0;
        ServerPlayer owner = getOwnerPlayer();
        if (owner != null && owner.level() == level()) {
            moveTo(owner.getX(), owner.getY(), owner.getZ(), getYRot(), getXRot());
        } else if (level() instanceof ServerLevel sl) {
            BlockPos s = sl.getSharedSpawnPos();
            moveTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5, getYRot(), getXRot());
        }
        getNavigation().stop();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target == this || isOwner(target) || target instanceof LauryEntity) return false;
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && (isWeakened() || !canAttack(target))) return;
        LivingEntity prev = getTarget();
        super.setTarget(target);
        if (target != null && prev == null && !level().isClientSide) {
            long now = level().getGameTime();
            if (now - lastCombatSpeech > 400) {
                lastCombatSpeech = now;
                LaurySpeech.say(this, getOwnerPlayer(), LaurySpeech.COMBAT);
            }
        }
    }

    public boolean isValidDefendTarget(Mob mob, @Nullable Player owner) {
        if (!mob.isAlive() || mob == this || !(mob instanceof Enemy)) return false;
        if (mob instanceof NeutralMob n && !n.isAngry() && (owner == null || mob.getTarget() != owner) && mob.getTarget() != this) return false;
        return canAttack(mob);
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean requiresCustomPersistence() { return true; }

    // ================================================================ montar y volar

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (isDragon() && !isWeakened() && getFirstPassenger() instanceof Player p) return p;
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return isDragon() && getPassengers().isEmpty();
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2.2D;
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        float yaw = yBodyRot * Mth.DEG_TO_RAD;
        double back = -0.2D;
        double x = getX() - Mth.sin(yaw) * back;
        double z = getZ() + Mth.cos(yaw) * back;
        move.accept(passenger, x, getY() + getPassengersRidingOffset() + passenger.getMyRidingOffset(), z);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof Player p && !p.onGround()) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        }
    }

    /**
     * Vuelo controlado por el jinete: el dragón gira con inercia hacia donde miras (y se inclina en la curva),
     * acelera en picada, sube más lento, rebota con cada aletazo y da un impulso con la barra espaciadora.
     */
    @Override
    public void travel(Vec3 input) {
        if (isDragon() && !isWeakened() && getControllingPassenger() instanceof Player rider) {
            float diff = Mth.wrapDegrees(rider.getYRot() - getYRot());
            float turn = Mth.clamp(diff, -MAX_TURN, MAX_TURN);
            yRotO = getYRot();
            setYRot(getYRot() + turn);
            setXRot(rider.getXRot() * 0.5F);
            yBodyRot = getYRot();
            yHeadRot = getYRot();

            float forward = rider.zza;
            float strafe = rider.xxa;
            float yawRad = getYRot() * Mth.DEG_TO_RAD;
            float pitchRad = rider.getXRot() * Mth.DEG_TO_RAD;
            // dirección: rumbo del dragón (con inercia) + inclinación de la mirada del jinete
            Vec3 dir = new Vec3(-Mth.sin(yawRad) * Mth.cos(pitchRad), -Mth.sin(pitchRad), Mth.cos(yawRad) * Mth.cos(pitchRad));
            float speed = FLY_SPEED;
            if (dir.y < 0) speed *= 1.0F + (float) -dir.y * 0.95F;      // picada: gana velocidad
            else speed *= 1.0F - (float) dir.y * 0.35F;                 // ascenso: cuesta más
            Vec3 wanted = Vec3.ZERO;
            if (forward > 0) wanted = dir.scale(speed);
            else if (forward < 0) wanted = dir.scale(-FLY_SPEED * 0.3F);
            if (strafe != 0) {
                wanted = wanted.add(Mth.cos(yawRad) * strafe * FLY_SPEED * 0.55F, 0, Mth.sin(yawRad) * strafe * FLY_SPEED * 0.55F);
            }
            Vec3 motion = getDeltaMovement().scale(0.9D).add(wanted.scale(0.1D));
            if (!onGround()) {
                if (forward == 0 && strafe == 0) {
                    motion = motion.add(0, Mth.sin(tickCount * 0.32F) * 0.012D, 0);         // aleteo en el sitio
                } else if (dir.y > -0.3) {
                    motion = motion.add(0, Mth.sin(tickCount * 0.3F) * 0.02D, 0);          // rebote de cada aletazo
                }
            }
            if (pendingBoost > 0) {
                Vec3 fwd = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
                motion = motion.add(fwd.scale(0.5D * pendingBoost)).add(0, 0.85D * pendingBoost, 0);
                level().playLocalSound(getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL, 1.4F, 0.8F, false);
                pendingBoost = 0;
            }
            setDeltaMovement(motion);
            move(MoverType.SELF, motion);
            calculateEntityAnimation(false);
            return;
        }
        super.travel(input);
    }

    // ---- impulso con la barra espaciadora (barra de salto como la de los caballos)

    @Override
    public void onPlayerJump(int power) {
        if (power <= 0 || !isDragon() || isWeakened()) return;
        pendingBoost = Math.min(power, 100) / 100.0F * (power >= 90 ? 1.25F : 1.0F);
    }

    @Override
    public boolean canJump() {
        return isDragon() && !isWeakened() && isVehicle();
    }

    @Override
    public void handleStartJump(int power) {
        if (!level().isClientSide && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.5, getZ(), 12, 1.5, 0.3, 1.5, 0.05);
        }
    }

    @Override
    public void handleStopJump() {
    }

    // ================================================================ interacción

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        ServerPlayer sp = (ServerPlayer) player;
        if (!isOwner(player)) {
            LaurySpeech.say(this, sp, "¡Ey! No te conozco. Solo sigo a mi compañero, jaja.");
            return InteractionResult.CONSUME;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEdible() && !player.isShiftKeyDown()) {
            feed(sp, stack);
            return InteractionResult.CONSUME;
        }
        if (isDragon()) {
            if (player.isShiftKeyDown()) {
                openInventory(sp);
            } else if (!isWeakened()) {
                if (player.startRiding(this)) LaurySpeech.say(this, sp, LaurySpeech.RIDE);
            }
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            if (getMode() == MODE_FOLLOW) commandStay(sp); else commandFollow(sp);
            return InteractionResult.CONSUME;
        }
        openInventory(sp);
        return InteractionResult.CONSUME;
    }

    private void feed(ServerPlayer player, ItemStack stack) {
        FoodProperties food = stack.getItem().getFoodProperties(stack, this);
        int n = food != null ? food.getNutrition() : 4;
        if (isWeakened()) {
            heal(n * 15.0F);
            if (getHealth() >= getMaxHealth()) recover(player);
            else LaurySpeech.say(this, player, "Gracias, {p}... eso me ayuda mucho.");
        } else if (getHealth() >= getMaxHealth()) {
            LaurySpeech.say(this, player, "Jaja, ya estoy llena, {p}. ¡Guárdalo para ti!");
            return;
        } else {
            heal(n * 10.0F);
            LaurySpeech.say(this, player, LaurySpeech.FED);
        }
        playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.0F);
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    public void openInventory(ServerPlayer player) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inv, p) -> new LauryMenu(id, inv, inventory, tools, armor, this),
                Component.literal("Laury")), buf -> buf.writeVarInt(getId()));
    }

    /** Espada (o hacha) en la mano, escudo en la otra, armadura puesta. */
    public void updateEquipment() {
        if (level().isClientSide) return;
        if (!isGathering()) {
            ItemStack weapon = tools.getItem(SLOT_SWORD);
            if (weapon.isEmpty()) weapon = tools.getItem(SLOT_AXE);
            setItemSlot(EquipmentSlot.MAINHAND, weapon);
        }
        setItemSlot(EquipmentSlot.OFFHAND, tools.getItem(SLOT_SHIELD));
        for (int i = 0; i < 4; i++) setItemSlot(ARMOR_ORDER[i], armor.getItem(i));
    }

    // ================================================================ órdenes

    public void commandFollow(ServerPlayer p) {
        setMode(MODE_FOLLOW);
        LaurySpeech.say(this, p, LaurySpeech.FOLLOW);
    }

    public void commandStay(ServerPlayer p) {
        anchorPos = blockPosition();
        setMode(MODE_STAY);
        LaurySpeech.say(this, p, LaurySpeech.STAY);
    }

    public void commandTp(ServerPlayer p) {
        if (getMode() != MODE_FOLLOW) setMode(MODE_FOLLOW);
        teleportToOwner(p);
        LaurySpeech.say(this, p, LaurySpeech.TP);
    }

    public void commandMeat(ServerPlayer p) {
        ItemStack meat = new ItemStack(Items.COOKED_BEEF, 24);
        if (!p.getInventory().add(meat)) p.drop(meat, false);
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.2, getZ(), 15, 0.3, 0.3, 0.3, 0.02);
        LaurySpeech.say(this, p, "¡Carne asada con fuego de dragón! Aquí tienes 24, {p}. ¡Jajaja, término perfecto!");
    }

    public void commandStatus(ServerPlayer p) {
        LaurySpeech.system(p, String.format("Vida: %.0f/%.0f%s | Forma: %s | Modo: %s",
                getHealth(), getMaxHealth(), isWeakened() ? " (debilitada)" : "", isDragon() ? "dragón" : "humana",
                getMode() == MODE_FOLLOW ? "seguir" : "quedarse"));
        LaurySpeech.system(p, "Posición: " + blockPosition().getX() + " " + blockPosition().getY() + " " + blockPosition().getZ()
                + " (" + level().dimension().location() + ")" + (isGathering() ? " | Recolectando " + gatherResource.display
                + " " + gatherDone + "/" + gatherTarget : ""));
    }

    public void commandDismiss(ServerPlayer p) {
        LaurySpeech.say(this, p, "¿Me despides? ...Está bien. Toma mi cuerno; si me necesitas, sopla y vuelvo. ¡Cuídate, {p}!");
        ejectPassengers();
        for (SimpleContainer c : new SimpleContainer[] {inventory, tools, armor}) {
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack s = c.removeItemNoUpdate(i);
                if (!s.isEmpty() && !p.getInventory().add(s)) p.drop(s, false);
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) setItemSlot(slot, ItemStack.EMPTY);
        ItemStack horn = new ItemStack(ModItems.SUMMON_HORN.get());
        if (!p.getInventory().add(horn)) p.drop(horn, false);
        LauryLookup.setStoredId(p, null);
        if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.DRAGON_BREATH, getX(), getY() + 1, getZ(), 40, 0.6, 0.8, 0.6, 0.03);
        discard();
    }

    public void teleportToOwner(ServerPlayer owner) {
        if (isRemoved()) return;
        ejectPassengers();
        if (owner.level() != level()) {
            teleportTo((ServerLevel) owner.level(), owner.getX(), owner.getY(), owner.getZ(), EnumSet.noneOf(RelativeMovement.class), getYRot(), getXRot());
            return;
        }
        BlockPos base = owner.blockPosition();
        for (int i = 0; i < 16; i++) {
            int r = isDragon() ? 4 : 2;
            BlockPos pos = base.offset(getRandom().nextInt(r * 2 + 1) - r, getRandom().nextInt(3) - 1, getRandom().nextInt(r * 2 + 1) - r);
            if (pos.distSqr(base) < 2) continue;
            AABB box = getDimensions(getPose()).makeBoundingBox(Vec3.atBottomCenterOf(pos));
            if (level().noCollision(this, box) && !level().getBlockState(pos.below()).isAir()) {
                moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, getYRot(), getXRot());
                getNavigation().stop();
                return;
            }
        }
        moveTo(owner.getX(), owner.getY(), owner.getZ(), getYRot(), getXRot());
        getNavigation().stop();
    }

    // ================================================================ recolectar

    public boolean isGathering() { return gatherResource != null; }
    @Nullable public LauryResource getGatherResource() { return gatherResource; }
    public boolean isGatherSkipped(BlockPos p) { return gatherSkip.contains(p); }
    public void skipGatherBlock(BlockPos p) { gatherSkip.add(p.immutable()); }

    public void commandGather(ServerPlayer p, LauryResource res, int amount) {
        if (isWeakened()) { LaurySpeech.say(this, p, "Dame un respiro, {p}. Primero me recupero."); return; }
        if (isDragon()) { LaurySpeech.say(this, p, "¡Con estas garras rompería todo! Pásame a forma humana con /laury humana, jaja."); return; }
        if (res.needsTool && tools.getItem(res.toolSlot).isEmpty()) {
            LaurySpeech.say(this, p, "Para sacar " + res.display + " necesito " + res.toolName() + ". ¡Pónmelo en mi barra de herramientas!");
            return;
        }
        gatherResource = res;
        gatherTarget = Mth.clamp(amount, 1, 64);
        gatherDone = 0;
        gatherSkip.clear();
        getNavigation().stop();
        LaurySpeech.say(this, p, "¡Hecho! Voy por " + gatherTarget + " de " + res.display + ". ¡Mírame trabajar, jajaja!");
    }

    public void onGathered(int count) {
        gatherDone += count;
        if (gatherDone >= gatherTarget) finishGather("done");
        else if (!hasFreeSlot()) finishGather("full");
    }

    private boolean hasFreeSlot() {
        for (int i = 0; i < inventory.getContainerSize(); i++) if (inventory.getItem(i).isEmpty()) return true;
        return false;
    }

    public void finishGather(String reason) {
        LauryResource res = gatherResource;
        int done = Math.min(gatherDone, gatherTarget);
        gatherResource = null;
        gatherSkip.clear();
        updateEquipment();
        if (res == null) return;
        String msg = switch (reason) {
            case "done" -> "¡Listo, {p}! " + done + " de " + res.display + " en mi mochila. ¡Pan comido, jajaja!";
            case "full" -> "Mi mochila está llena, {p}. Junté " + done + " de " + res.display + ".";
            case "no_tool" -> "Necesito " + res.toolName() + " para seguir con " + res.display + ". Llevo " + done + ".";
            case "weak_tool" -> "Mi pico es muy débil para " + res.display + ". Dame uno de piedra o mejor. Llevo " + done + ".";
            case "stopped" -> "Ok, paro. Junté " + done + " de " + res.display + ".";
            default -> done == 0 ? "No encuentro " + res.display + " por aquí, {p}. Analicé todo... vamos a otra zona."
                    : "Solo había " + done + " de " + res.display + " cerca. ¡Ya está en mi mochila!";
        };
        LaurySpeech.say(this, getOwnerPlayer(), msg);
    }

    public void commandStop(ServerPlayer p) {
        if (isGathering()) finishGather("stopped");
        else LaurySpeech.say(this, p, "No estoy haciendo nada, {p}. ¡Dame trabajo, jaja!");
    }

    public void storeOrDrop(ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack rest = inventory.addItem(stack);
        if (!rest.isEmpty()) spawnAtLocation(rest);
    }

    // ================================================================ conversación

    public void queueSay(String text, int delay) {
        int base = pending.isEmpty() ? tickCount : Math.max(tickCount, pending.peekLast().at());
        pending.addLast(new Pending(base + delay, text));
    }
    public boolean hasPendingLines() { return !pending.isEmpty(); }
    public void openChatWindow() { chatWindow = 900; }
    public void closeChatWindow() { chatWindow = 0; expecting = null; }
    public boolean isChatting() { return chatWindow > 0; }
    @Nullable public String getExpecting() { return expecting; }
    public void expect(@Nullable String topic) { expecting = topic; expectTicks = topic == null ? 0 : 700; }
    @Nullable public String getNickname() { return nickname; }
    public void setNickname(@Nullable String n) { nickname = n; }
    public List<String> getLikes() { return likes; }

    public void addLike(String like) {
        likes.remove(like);
        likes.add(like);
        while (likes.size() > 8) likes.remove(0);
    }

    public String pickLine(String key, String[] lines) {
        if (lines.length == 1) return lines[0];
        int last = lastPicks.getOrDefault(key, -1);
        int i = getRandom().nextInt(lines.length);
        if (i == last) i = (i + 1 + getRandom().nextInt(lines.length - 1)) % lines.length;
        lastPicks.put(key, i);
        return lines[i];
    }

    // ================================================================ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putBoolean("Dragon", isDragon());
        tag.putBoolean("Weakened", isWeakened());
        tag.putInt("Mode", getMode());
        if (anchorPos != null) tag.putLong("Anchor", anchorPos.asLong());
        tag.put("Inventory", inventory.createTag());
        tag.put("Tools", saveSlots(tools));
        tag.put("ArmorItems2", saveSlots(armor));
        if (nickname != null) tag.putString("Nickname", nickname);
        ListTag l = new ListTag();
        for (String s : likes) l.add(StringTag.valueOf(s));
        tag.put("Likes", l);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        entityData.set(DATA_DRAGON, tag.getBoolean("Dragon"));
        entityData.set(DATA_WEAKENED, tag.getBoolean("Weakened"));
        entityData.set(DATA_MODE, tag.getInt("Mode"));
        anchorPos = tag.contains("Anchor") ? BlockPos.of(tag.getLong("Anchor")) : null;
        inventory.fromTag(tag.getList("Inventory", Tag.TAG_COMPOUND));
        loadSlots(tools, tag.getList("Tools", Tag.TAG_COMPOUND));
        loadSlots(armor, tag.getList("ArmorItems2", Tag.TAG_COMPOUND));
        nickname = tag.contains("Nickname") ? tag.getString("Nickname") : null;
        likes.clear();
        ListTag l = tag.getList("Likes", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) likes.add(l.getString(i));
        setCustomName(Component.literal("Laury"));
        setCustomNameVisible(true);
        refreshDimensions();
        applyFormModifiers();
        updateEquipment();
    }

    private static ListTag saveSlots(SimpleContainer c) {
        ListTag list = new ListTag();
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack s = c.getItem(i);
            if (s.isEmpty()) continue;
            CompoundTag t = new CompoundTag();
            s.save(t);
            t.putInt("LSlot", i);
            list.add(t);
        }
        return list;
    }

    private static void loadSlots(SimpleContainer c, ListTag list) {
        for (int i = 0; i < c.getContainerSize(); i++) c.setItem(i, ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            int slot = t.getInt("LSlot");
            if (slot >= 0 && slot < c.getContainerSize()) c.setItem(slot, ItemStack.of(t));
        }
    }

    public void initFromSummon(ServerPlayer owner) {
        setOwner(owner);
        anchorPos = blockPosition();
        setMode(MODE_FOLLOW);
    }
}
