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
    private static final EntityDataAccessor<Integer> DATA_FORM = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLY = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FORM_AT = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.INT);
    private static final UUID FORM_DMG = UUID.fromString("6b1c9a52-1f0e-4d5b-9c51-3a8e0f6c2a11");
    private static final UUID FORM_SPEED = UUID.fromString("6b1c9a52-1f0e-4d5b-9c51-3a8e0f6c2a12");
    private static final UUID FORM_ARMOR = UUID.fromString("6b1c9a52-1f0e-4d5b-9c51-3a8e0f6c2a13");
    private static final UUID FORM_HP = UUID.fromString("6b1c9a52-1f0e-4d5b-9c51-3a8e0f6c2a14");

    public static final double BASE_HEALTH = 30.0D;

    private final SimpleContainer bag = new SimpleContainer(27);
    /** 0 casco, 1 pechera, 2 grebas, 3 botas, 4 mano principal, 5 mano secundaria */
    private final SimpleContainer equip = new SimpleContainer(EQUIP_SIZE);
    /** 6 pico, 7 hacha, 8 pala, 9 azada, 10 arco, 11 otra herramienta, 12 antorcha del cinturón */
    public static final int EQUIP_SIZE = 13, PICK = 6, AXE = 7, SHOVEL = 8, HOE = 9, BOW = 10, EXTRA = 11, TORCH = 12;
    private static final EntityDataAccessor<ItemStack> DATA_TORCH = SynchedEntityData.defineId(KaoriEntity.class, EntityDataSerializers.ITEM_STACK);
    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos anchor;
    @Nullable private KaoriResource gather;
    private int gatherLeft, gatherDone;
    private final Set<BlockPos> gatherSkip = new HashSet<>();
    private long lastCombatSpeech, lastHurtSpeech;
    // choza de la cápsula
    @Nullable BlockPos hutPos;
    int hutDir, hutType;
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
        entityData.define(DATA_TALK, -100000);
        entityData.define(DATA_COLLECT, true);
        entityData.define(DATA_FORM, 0);
        entityData.define(DATA_FORM_AT, -100000);
        entityData.define(DATA_FLY, false);
        entityData.define(DATA_TORCH, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new KaoriGoals.Rest(this));
        goalSelector.addGoal(2, new KaoriGoals.Combat(this));
        goalSelector.addGoal(3, new KaoriGoals.Gather(this));
        goalSelector.addGoal(3, new KaoriGoals.Follow(this, 1.3D, 3.6F, 2.0F));
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

    /** Cuando tú vuelas, ella saca una pequeña nave de una cápsula y te acompaña sentada en ella (ella no vuela). */
    public boolean isFlying() { return entityData.get(DATA_FLY); }

    private int landedTicks;

    private void flightTick() {
        ServerPlayer o = getOwnerPlayer();
        boolean fly = o != null && o.level() == level() && o.getAbilities().flying && getMode().follows() && !isWeakened()
                && !isGathering() && distanceToSqr(o) < 64 * 64;
        if (fly != isFlying()) {
            entityData.set(DATA_FLY, fly);
            setNoGravity(fly);
            if (fly) {
                setDeltaMovement(getDeltaMovement().add(0, 0.5, 0));
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 40, 0.8, 0.4, 0.8, 0.05);
                    sl.playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.0F, 1.5F);
                }
                if (getRandom().nextInt(2) == 0) say(null, pick("¡Cápsula, va! *POP* Mi nave personal. Diseño propio, obviamente.",
                        "¿Volar? Yo no necesito volar: tengo una nave. Mucho más elegante.", "Súbete al ritmo, yo te sigo en mi nave."));
            } else {
                landedTicks = 60;
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 30, 0.8, 0.4, 0.8, 0.05);
                    sl.playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 0.8F, 1.8F);
                }
            }
        }
        if (landedTicks > 0) { landedTicks--; fallDistance = 0; }
        if (!fly) return;
        fallDistance = 0;
        getNavigation().stop();
        net.minecraft.world.phys.Vec3 goal;
        LivingEntity t = getTarget();
        if (t != null && t.isAlive() && t.distanceToSqr(o) < 20 * 20) {
            goal = t.position().add(0, 0.3, 0);
        } else {
            // a tu lado, un poco atrás y a la derecha
            net.minecraft.world.phys.Vec3 look = o.getLookAngle().multiply(1, 0, 1).normalize();
            net.minecraft.world.phys.Vec3 right = new net.minecraft.world.phys.Vec3(-look.z, 0, look.x);
            goal = o.position().add(look.scale(-2.0)).add(right.scale(1.6)).add(0, 0.4, 0);
        }
        net.minecraft.world.phys.Vec3 d = goal.subtract(position());
        double dist = d.length();
        if (dist > 40) { teleportTo(goal.x, goal.y, goal.z); return; }
        double speed = Math.min(2.2, 0.35 + o.getDeltaMovement().length() * 1.3 + dist * 0.12);
        net.minecraft.world.phys.Vec3 v = dist > 0.6 ? d.normalize().scale(Math.min(speed, dist)) : getDeltaMovement().scale(0.5);
        setDeltaMovement(getDeltaMovement().scale(0.4).add(v.scale(0.6)));
        hasImpulse = true;
        net.minecraft.world.phys.Vec3 face = dist > 1.5 ? d : o.getLookAngle();
        float yaw = (float) (Math.atan2(-face.x, face.z) * (180 / Math.PI));
        setYRot(yaw);
        yBodyRot = yaw;
        yHeadRot = yaw;
        // los motores de la nave
        if (tickCount % 2 == 0 && level() instanceof ServerLevel sl) {
            double yr = Math.toRadians(getYRot());
            double bx = getX() + Math.sin(yr) * 0.9, bz = getZ() - Math.cos(yr) * 0.9;
            sl.sendParticles(ParticleTypes.SMALL_FLAME, bx, getY() + 0.25, bz, 2, 0.2, 0.05, 0.2, 0.01);
            if (getDeltaMovement().lengthSqr() > 0.1) sl.sendParticles(ParticleTypes.CLOUD, bx, getY() + 0.25, bz, 1, 0.1, 0.05, 0.1, 0.0);
        }
    }

    // ------------------------------------------------------------------ más intuitiva

    private long lastWarn, lastNight;

    /** Pequeñas decisiones por su cuenta: equiparse mejor, comer, cuidarte y no quedarse en otra dimensión. */
    private void smartTick() {
        ServerPlayer o = getOwnerPlayer();
        if (o == null || isWeakened()) return;
        long now = level().getGameTime();
        // te sigue a otra dimensión (Nether, End) si está en modo seguir
        if (getMode().follows() && o.level() != level() && !o.isSpectator()) {
            teleportToOwner(o);
            return;
        }
        // se equipa sola lo mejor que haya en su mochila (arma y armadura)
        autoEquip();
        // pone antorchas de su cinturón donde está oscuro (para que no aparezcan monstruos)
        ItemStack torch = equip.getItem(TORCH);
        if (!torch.isEmpty() && torch.getItem() instanceof net.minecraft.world.item.BlockItem bi && onGround()) {
            BlockPos at = blockPosition();
            if (level().getMaxLocalRawBrightness(at) < 6 && level().getBlockState(at).canBeReplaced()
                    && level().getFluidState(at).isEmpty()
                    && level().getBlockState(at.below()).isFaceSturdy(level(), at.below(), net.minecraft.core.Direction.UP)) {
                net.minecraft.world.level.block.state.BlockState ts = bi.getBlock().defaultBlockState();
                if (ts.canSurvive(level(), at)) {
                    level().setBlock(at, ts, 3);
                    level().playSound(null, at, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.2F);
                    swing(InteractionHand.OFF_HAND);
                    torch.shrink(1);
                    equip.setChanged();
                }
            }
        }
        // come de su mochila si está herida
        if (getHealth() < getMaxHealth() * 0.6F) {
            for (int i = 0; i < bag.getContainerSize(); i++) {
                ItemStack s = bag.getItem(i);
                if (!s.isEdible() || KaoriActions.isGift(s) || s.is(ModRegistry.SENZU_BEAN.get())) continue;
                net.minecraft.world.food.FoodProperties fp = s.getFoodProperties(this);
                heal(fp == null ? 4 : fp.getNutrition() * 1.5F);
                s.shrink(1);
                bag.setChanged();
                level().playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8F, 1.2F);
                break;
            }
        }
        // te cuida: si te queda poca vida, te avisa y te da un tazón de arroz de su mochila
        if (o.level() == level() && o.getHealth() <= 6.0F && now - lastWarn > 600 && distanceToSqr(o) < 24 * 24) {
            lastWarn = now;
            if (consumeItem(ModRegistry.RICE_BOWL.get())) {
                ItemStack rice = new ItemStack(ModRegistry.RICE_BOWL.get());
                if (!o.getInventory().add(rice)) o.drop(rice, false);
                say(o, "¡Te queda poca vida! Toma un tazón de arroz y cómetelo YA. No me hagas preocuparme.");
            } else {
                say(o, "¡Cuidado, te queda poca vida! Aléjate y come algo... no quiero cargarte de vuelta.");
            }
        }
        // al anochecer te ofrece sacar una casa-cápsula
        long day = level().getDayTime() % 24000;
        if (day > 12800 && day < 13200 && now - lastNight > 6000 && hutPos == null && o.level() == level() && distanceToSqr(o) < 24 * 24) {
            lastNight = now;
            say(o, "Ya está anocheciendo. Si quieres, saco una casa-cápsula para dormir (menú → Casas).");
        }
    }

    private static double damageOf(ItemStack s) {
        double d = 0;
        for (net.minecraft.world.entity.ai.attributes.AttributeModifier m
                : s.getAttributeModifiers(EquipmentSlot.MAINHAND).get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
            d += m.getAmount();
        }
        return d;
    }

    private static int defenseOf(ItemStack s) {
        return s.getItem() instanceof ArmorItem a ? a.getDefense() : 0;
    }

    private void autoEquip() {
        boolean changed = false;
        // arma
        int best = -1;
        double bestDmg = equip.getItem(4).isEmpty() ? 0 : damageOf(equip.getItem(4));
        for (int i = 0; i < bag.getContainerSize(); i++) {
            ItemStack s = bag.getItem(i);
            if (!(s.getItem() instanceof SwordItem || s.getItem() instanceof AxeItem)) continue;
            double dmg = damageOf(s);
            if (dmg > bestDmg + 0.5) { bestDmg = dmg; best = i; }
        }
        if (best >= 0) {
            ItemStack old = equip.getItem(4);
            equip.setItem(4, bag.getItem(best));
            bag.setItem(best, old);
            changed = true;
        }
        // armadura
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int k = 0; k < 4; k++) {
            int bi = -1, bd = defenseOf(equip.getItem(k));
            for (int i = 0; i < bag.getContainerSize(); i++) {
                ItemStack s = bag.getItem(i);
                if (s.getItem() instanceof ArmorItem a && a.getEquipmentSlot() == slots[k] && a.getDefense() > bd) { bd = a.getDefense(); bi = i; }
            }
            if (bi >= 0) {
                ItemStack old = equip.getItem(k);
                equip.setItem(k, bag.getItem(bi));
                bag.setItem(bi, old);
                changed = true;
            }
        }
        if (changed) {
            level().playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_IRON, SoundSource.NEUTRAL, 0.8F, 1.1F);
            if (getRandom().nextInt(2) == 0) say(null, "Me puse lo mejor de mi mochila. Ahora sí estoy presentable.");
        }
    }

    // ------------------------------------------------------------------ tiempo juntos (desbloquea casas)

    public static final long[] HOUSE_BOND = {0L, 24000L, 72000L, 144000L};

    public static long bond(Player p) {
        return KaoriActions.data(p).getLong("KaoriBond");
    }

    private void bondTick() {
        ServerPlayer o = getOwnerPlayer();
        if (o == null || o.level() != level() || distanceToSqr(o) > 32 * 32) return;
        long before = bond(o), now = before + 20;
        KaoriActions.data(o).putLong("KaoriBond", now);
        for (int i = 1; i < HOUSE_BOND.length; i++) {
            if (before < HOUSE_BOND[i] && now >= HOUSE_BOND[i]) {
                say(o, "Oye... ya pasamos bastante tiempo juntos. Te diseñé una casa nueva: §e" + HutBuilder.NAMES[i]
                        + "§f. Está en mi menú, en Casas. No te emociones.");
                level().playSound(null, blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.2F);
            }
        }
    }

    // ------------------------------------------------------------------ generador protector

    /** El generador protector más cercano (dentro de r bloques). */
    @Nullable
    public com.bdsz.block.ShieldGeneratorBlockEntity nearestGenerator(int r) {
        com.bdsz.block.ShieldGeneratorBlockEntity best = null;
        double bd = Double.MAX_VALUE;
        int cx = blockPosition().getX() >> 4, cz = blockPosition().getZ() >> 4, cr = (r >> 4) + 1;
        for (int x = cx - cr; x <= cx + cr; x++) {
            for (int z = cz - cr; z <= cz + cr; z++) {
                if (!level().hasChunk(x, z)) continue;
                for (net.minecraft.world.level.block.entity.BlockEntity be : level().getChunk(x, z).getBlockEntities().values()) {
                    if (!(be instanceof com.bdsz.block.ShieldGeneratorBlockEntity g)) continue;
                    double d = be.getBlockPos().distSqr(blockPosition());
                    if (d < r * r && d < bd) { bd = d; best = g; }
                }
            }
        }
        return best;
    }

    /** Pone lingotes de cobre de su mochila en el generador. Devuelve cuántos usó. */
    public int feedGenerator(com.bdsz.block.ShieldGeneratorBlockEntity g, int max) {
        int have = 0;
        for (int i = 0; i < bag.getContainerSize(); i++) if (bag.getItem(i).is(Items.COPPER_INGOT)) have += bag.getItem(i).getCount();
        int used = g.addFuel(Math.min(have, max));
        int left = used;
        for (int i = 0; i < bag.getContainerSize() && left > 0; i++) {
            ItemStack s = bag.getItem(i);
            if (!s.is(Items.COPPER_INGOT)) continue;
            int take = Math.min(left, s.getCount());
            s.shrink(take);
            left -= take;
        }
        if (used > 0) {
            bag.setChanged();
            swing(InteractionHand.MAIN_HAND);
            getLookControl().setLookAt(g.getBlockPos().getX() + 0.5, g.getBlockPos().getY() + 0.5, g.getBlockPos().getZ() + 0.5);
            if (level() instanceof ServerLevel sl) {
                net.minecraft.world.phys.Vec3 c = g.getBlockPos().getCenter();
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 0.6, c.z, 20, 0.3, 0.3, 0.3, 0.2);
            }
            level().playSound(null, g.getBlockPos(), SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
        return used;
    }

    private void generatorTick() {
        ServerPlayer o = getOwnerPlayer();
        if (o == null || isWeakened()) return;
        com.bdsz.block.ShieldGeneratorBlockEntity g = nearestGenerator(20);
        if (g == null || !g.isOwner(o) || !g.on()) return;
        // si queda poca energía, ella misma lo alimenta con su cobre
        if (g.fuel() < 20 * 60 * 15) {
            int n = feedGenerator(g, 4);
            if (n > 0) say(o, "El generador se estaba quedando sin energía. Le puse " + n + " de cobre. Ya me lo agradecerás.");
        }
    }

    /** 0 base, 1 Super Saiyajin, 2 Super Saiyajin 2. */
    public int getForm() { return entityData.get(DATA_FORM); }

    /** Ticks desde la última transformación (para el destello y el grito). */
    public int sinceTransform() { return (int) (level().getGameTime() & 0x3FFFFFFF) - entityData.get(DATA_FORM_AT); }

    public void setForm(int form, boolean effects) {
        form = Math.max(0, Math.min(4, form));
        if (form == 3) form = 2;                       // ya no tiene la Fase 3
        int old = getForm();
        entityData.set(DATA_FORM, form);
        entityData.set(DATA_FORM_AT, effects ? (int) (level().getGameTime() & 0x3FFFFFFF) : -100000);
        mod(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, FORM_DMG, new double[]{0, 7, 14, 24, 36}[form]);
        mod(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, FORM_SPEED, new double[]{0, 0.06, 0.1, 0.12, 0.14}[form]);
        mod(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, FORM_ARMOR, new double[]{0, 6, 10, 14, 18}[form]);
        mod(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, FORM_HP, new double[]{0, 10, 20, 30, 40}[form]);
        if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
        if (!effects || !(level() instanceof ServerLevel sl)) return;
        if (form > old) {
            heal(getMaxHealth());
            sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1, getZ(), 2, 0, 0, 0, 0);
            sl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0F, 0.85F, 0.2F), 2.0F),
                    getX(), getY() + 1, getZ(), 120, 1.2, 1.2, 1.2, 0.1);
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.2, getZ(), 6, 1.5, 0.1, 1.5, 0);
            if (form == 2 || form == 3) sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1, getZ(), 80 + form * 30, 0.8, 1.2, 0.8, 0.4);
            if (form == 3) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.2, getZ(), 1, 0, 0, 0, 0);
                net.minecraft.world.entity.LightningBolt b = EntityType.LIGHTNING_BOLT.create(sl);
                if (b != null) { b.moveTo(getX() + 3, getY(), getZ()); b.setVisualOnly(true); sl.addFreshEntity(b); }
            }
            if (form == 4) {
                sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1, getZ(), 60, 0.6, 1.0, 0.6, 0.05);
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1, getZ(), 40, 0.5, 1.0, 0.5, 0.05);
            }
            sl.playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 0.6F, 1.6F);
            sl.playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 1.2F, 0.7F);
            // la onda de choque empuja a los monstruos cercanos
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4), le -> le instanceof Enemy)) {
                le.knockback(1.2, getX() - le.getX(), getZ() - le.getZ());
            }
        } else if (form < old) {
            sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 30, 0.5, 0.8, 0.5, 0.03);
            sl.playSound(null, blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.2F);
        }
    }

    private void mod(net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id, double v) {
        net.minecraft.world.entity.ai.attributes.AttributeInstance in = getAttribute(attr);
        if (in == null) return;
        in.removeModifier(id);
        if (v != 0) in.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, "kaori_form", v,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
    }

    public boolean isCollecting() { return entityData.get(DATA_COLLECT); }

    public boolean isTalking() {
        int now = (int) (level().getGameTime() & 0x3FFFFFFF), at = entityData.get(DATA_TALK);
        return now >= at && now - at < 50;
    }

    @Nullable public BlockPos getAnchor() { return anchor; }

    public SimpleContainer getBag() { return bag; }

    public SimpleContainer getEquip() { return equip; }

    public boolean isGathering() { return gather != null; }

    @Nullable public KaoriResource getGather() { return gather; }

    public boolean skipped(BlockPos p) { return gatherSkip.contains(p); }

    public void skip(BlockPos p) { gatherSkip.add(p.immutable()); }

    /** Habla (al chat del dueño) y pone cara contenta unos segundos. */
    public void say(@Nullable Player to, String text) {
        entityData.set(DATA_TALK, (int) (level().getGameTime() & 0x3FFFFFFF));
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
        // aura de Super Saiyajin
        if (getForm() > 0 && tickCount % 2 == 0 && level() instanceof ServerLevel asl) {
            asl.sendParticles(new net.minecraft.core.particles.DustParticleOptions(getForm() == 4 ? new org.joml.Vector3f(1.0F, 0.3F, 0.25F)
                            : new org.joml.Vector3f(1.0F, 0.86F, 0.25F), 1.3F + (getForm() == 3 ? 0.4F : 0F)),
                    getX(), getY() + 0.2 + getRandom().nextDouble() * 1.8, getZ(), 3, 0.35, 0.1, 0.35, 0.02);
            if ((getForm() == 2 || getForm() == 3) && getRandom().nextInt(getForm() == 3 ? 2 : 3) == 0)
                asl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1, getZ(), 2, 0.4, 0.8, 0.4, 0.2);
        }
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
        flightTick();
        if (tickCount % 20 == 0) bondTick();
        if (tickCount % 200 == 0) generatorTick();
        if (tickCount % 40 == 0) smartTick();
        if (tickCount % 20 == 0 && KaoriActions.isStale(this)) {
            // solo puede haber una Kaori: la copia vieja desaparece (sus cosas ya viajaron con la nueva)
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 30, 0.5, 0.8, 0.5, 0.05);
            discard();
            return;
        }
        if (tickCount % 100 == 0) backup();
        // si quedó atrapada dentro de un bloque, sale a tu lado
        if (isInWall() && tickCount % 10 == 0) {
            ServerPlayer o = getOwnerPlayer();
            if (o != null && o.level() == level()) teleportNear(o); else teleportToSafety();
        }
    }

    // ------------------------------------------------------------------ vida: nunca muere

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return false;
        // nunca se asfixia dentro de bloques ni se golpea volando
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.CRAMMING)) return false;
        if (isFlying() && source.is(DamageTypes.FALL)) return false;
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
        setForm(0, false);
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
        blast.setup(getForm() > 0 ? 0xFFE066 : 0xFF9A3C, 0.8F + getForm() * 0.3F, 9.0F * (1 + getForm()));
        blast.shoot(dir.x, dir.y, dir.z, 1.6F, 0.0F);
        level().addFreshEntity(blast);
        swing(InteractionHand.MAIN_HAND, true);
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.7F, 1.6F);
    }

    /** Onda de ki cargada: una esfera grande que explota (sin romper bloques) contra enemigos fuertes. */
    public void kiWave(LivingEntity target) {
        com.bdsz.entity.KiBlastEntity blast = new com.bdsz.entity.KiBlastEntity(ModRegistry.KI_BLAST.get(), level());
        blast.setOwner(this);
        net.minecraft.world.phys.Vec3 dir = target.getEyePosition().subtract(getEyePosition()).normalize();
        blast.setPos(getX() + dir.x * 1.2, getEyeY() - 0.2, getZ() + dir.z * 1.2);
        blast.setup(getForm() > 0 ? 0xFFF09A : 0xFF7A2C, 2.0F + getForm() * 0.4F, 18.0F * (1 + getForm()));
        blast.setBigBang(3.0F + getForm());
        blast.shoot(dir.x, dir.y, dir.z, 1.1F, 0.0F);
        level().addFreshEntity(blast);
        swing(InteractionHand.MAIN_HAND, true);
        level().playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 1.0F, 0.6F);
        say(null, pick("¡Toma esto! ¡HAAA!", "¡Onda de ki!", "¡Desaparece!"));
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return !isFlying() && landedTicks <= 0 && super.causeFallDamage(dist, mult, src);
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
            // solo puedes tener una Kaori
            if (KaoriActions.findKaori(sp) != null) {
                sp.sendSystemMessage(Component.literal("§6[Kaori] §fYa tienes una Kaori. Solo hay una como yo, ¿sabes?"));
                if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 30, 0.5, 0.8, 0.5, 0.05);
                discard();
                return InteractionResult.CONSUME;
            }
            setOwner(player);
            KaoriActions.data(sp).putUUID("KaoriUUID", getUUID());
        }
        if (!isOwner(player)) {
            say(sp, "Disculpa, pero ya tengo compañero. Búscate tu propia mecánica genio.");
            return InteractionResult.CONSUME;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEdible() && getHealth() < getMaxHealth()) {
            net.minecraft.world.food.FoodProperties food = stack.getFoodProperties(this);
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

    public ItemStack getBeltTorch() { return entityData.get(DATA_TORCH); }

    public void applyEquipment() {
        if (level().isClientSide) return;
        ItemStack t = equip.getItem(TORCH);
        if (!ItemStack.matches(t, entityData.get(DATA_TORCH))) entityData.set(DATA_TORCH, t.copy());
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
        int[] belt = {4, PICK, AXE, SHOVEL, HOE, EXTRA};
        for (int i = 0; i < bag.getContainerSize() + belt.length; i++) {
            ItemStack s = i < bag.getContainerSize() ? bag.getItem(i) : equip.getItem(belt[i - bag.getContainerSize()]);
            if (s.isEmpty() || !(s.getItem() instanceof DiggerItem)) continue;
            float sp = s.getDestroySpeed(state);
            if (sp > speed) { speed = sp; best = s; }
        }
        return best;
    }

    /** La azada: primero la de su cinturón de herramientas, si no, una de la mochila. */
    public ItemStack hoe() {
        if (equip.getItem(HOE).getItem() instanceof HoeItem) return equip.getItem(HOE);
        for (int i = 0; i < bag.getContainerSize(); i++) if (bag.getItem(i).getItem() instanceof HoeItem) return bag.getItem(i);
        return ItemStack.EMPTY;
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

    /** Copia de seguridad en el jugador (mochila, equipo, casa, forma) para que la única Kaori nunca pierda sus cosas. */
    public void backup() {
        ServerPlayer o = getOwnerPlayer();
        if (o == null || KaoriActions.isStale(this)) return;
        CompoundTag t = new CompoundTag();
        addAdditionalSaveData(t);
        t.putString("Dim", level().dimension().location().toString());
        t.putLong("Pos", blockPosition().asLong());
        KaoriActions.data(o).put("KaoriBackup", t);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && reason == RemovalReason.UNLOADED_TO_CHUNK) backup();
        super.remove(reason);
    }

    // ------------------------------------------------------------------ guardado

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putInt("Mode", getMode().ordinal());
        tag.putBoolean("Weak", isWeakened());
        tag.putInt("Form", getForm());
        tag.putBoolean("Collect", isCollecting());
        if (anchor != null) tag.putLong("Anchor", anchor.asLong());
        tag.put("Bag", save(bag));
        tag.put("Equip", save(equip));
        if (hutPos != null) {
            tag.putLong("Hut", hutPos.asLong());
            tag.putInt("HutDir", hutDir);
            tag.putInt("HutType", hutType);
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
        setForm(tag.getInt("Form"), false);
        anchor = tag.contains("Anchor") ? BlockPos.of(tag.getLong("Anchor")) : null;
        load(bag, tag.getList("Bag", 10));
        load(equip, tag.getList("Equip", 10));
        hutPos = tag.contains("Hut") ? BlockPos.of(tag.getLong("Hut")) : null;
        hutDir = tag.getInt("HutDir");
        hutType = tag.getInt("HutType");
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
