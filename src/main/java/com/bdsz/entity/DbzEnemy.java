package com.bdsz.entity;

import com.bdsz.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Enemigo de combate: pelea cuerpo a cuerpo, lanza ráfagas de ki, se teletransporta, pisa el suelo levantando
 * escombros, vuela (jefes), se regenera, invoca refuerzos y entra en furia. Los jefes muestran una barra de vida.
 */
public class DbzEnemy extends Monster {
    private static final EntityDataAccessor<Integer> CAST = SynchedEntityData.defineId(DbzEnemy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(DbzEnemy.class, EntityDataSerializers.BOOLEAN);
    private static final UUID RAGE_SPEED = UUID.fromString("c7a1d2e3-4b5c-4d6e-8f70-112233445566");
    private static final UUID RAGE_DAMAGE = UUID.fromString("c7a1d2e3-4b5c-4d6e-8f70-112233445567");

    private EnemyKind kind;
    private ServerBossEvent bar;
    private int blastCd = 40, teleCd = 60, stompCd = 80, bigCd = 160, meleeCd, castType, phaseTimer;
    private boolean summoned1, summoned2;

    public DbzEnemy(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = kind().xp;
        if (kind().boss) {
            bar = new ServerBossEvent(Component.translatable("entity.blockdragonsuperz." + kind().id), kind().barColor, BossEvent.BossBarOverlay.NOTCHED_10);
            bar.setDarkenScreen(true);
        }
    }

    public EnemyKind kind() {
        if (kind == null) kind = EnemyKind.of(getType());
        return kind;
    }

    public static AttributeSupplier.Builder attributes(EnemyKind k) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, k.attributeHealth())
                .add(Attributes.ATTACK_DAMAGE, k.attack)
                .add(Attributes.MOVEMENT_SPEED, k.speed)
                .add(Attributes.ARMOR, k.armor)
                .add(Attributes.ARMOR_TOUGHNESS, k.armor / 2.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, k.boss ? 0.9 : 0.6)
                .add(Attributes.ATTACK_KNOCKBACK, k.boss ? 1.5 : 0.8);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CAST, 0);
        entityData.define(ENRAGED, false);
    }

    /** Ticks que quedan de la pose de lanzar ki (para el modelo). */
    public int getCast() { return entityData.get(CAST); }
    public boolean isEnraged() { return entityData.get(ENRAGED); }
    public boolean isBigCast() { return castType == 2 || (getCast() > 0 && kind().boss && getCast() > 12); }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        if (!kind().flies) goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    // ------------------------------------------------------------ comportamiento (servidor)

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        EnemyKind k = kind();
        if (blastCd > 0) blastCd--;
        if (teleCd > 0) teleCd--;
        if (stompCd > 0) stompCd--;
        if (bigCd > 0) bigCd--;
        if (meleeCd > 0) meleeCd--;
        phaseTimer++;
        if (k.regen > 0 && tickCount % 20 == 0 && getHealth() < getMaxHealth() && getHealth() > 0) {
            heal(k.regen / k.hpScale());
            if (level() instanceof ServerLevel sl && getHealth() < getMaxHealth()) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + getBbHeight() * 0.6, getZ(), 3, 0.4, 0.6, 0.4, 0);
            }
        }
        if (bar != null) bar.setProgress(getHealth() / getMaxHealth());
        checkPhases();

        LivingEntity tgt = getTarget();
        if (tgt == null || !tgt.isAlive()) {
            if (k.flies) setNoGravity(false);
            if (getCast() > 0) entityData.set(CAST, 0);
            return;
        }
        double d = distanceTo(tgt);
        if (k.flies) fly(tgt, d);

        // lanzando un ataque de ki
        int cast = getCast();
        if (cast > 0) {
            getLookControl().setLookAt(tgt, 30, 30);
            if (castType == 2 && level() instanceof ServerLevel sl) {
                Vec3 h = handPos();
                sl.sendParticles(new DustParticleOptions(color(k.kiColor), 2.5F), h.x, h.y, h.z, 6, 0.3, 0.3, 0.3, 0);
                if (cast % 5 == 0) playSound(SoundEvents.BEACON_AMBIENT, 1.5F, 1.4F + (30 - cast) * 0.02F);
            }
            entityData.set(CAST, cast - 1);
            if (cast - 1 == 0) fire(tgt);
            return;
        }
        if (k.teleport && teleCd == 0 && d > 7 && random.nextInt(3) == 0) {
            teleportNear(tgt);
            teleCd = k.boss ? 70 : 120;
        }
        if (k.stomp && stompCd == 0 && d < 4.5 && onGround()) {
            stomp();
            stompCd = isEnraged() ? 60 : 100;
            return;
        }
        boolean sees = hasLineOfSight(tgt);
        if (k.boss && bigCd == 0 && sees && d < 30) {
            startCast(2, 30);
            bigCd = isEnraged() ? 140 : 220;
            playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 1.0F);
            return;
        }
        if (blastCd == 0 && sees && d > 4 && d < 28) {
            startCast(1, 10);
            blastCd = (int) (k.blastCooldown * (isEnraged() ? 0.6 : 1.0)) + random.nextInt(20);
        }
    }

    private void startCast(int type, int ticks) {
        castType = type;
        entityData.set(CAST, ticks);
        getNavigation().stop();
    }

    private Vec3 handPos() {
        Vec3 look = getLookAngle();
        return position().add(look.x * 0.9, getBbHeight() * 0.7, look.z * 0.9);
    }

    private static Vector3f color(int c) {
        return new Vector3f(((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F);
    }

    /** Dispara una ráfaga (o la gran esfera de los jefes) hacia el objetivo. */
    private void fire(LivingEntity tgt) {
        EnemyKind k = kind();
        Vec3 from = handPos();
        Vec3 dir = tgt.getEyePosition().subtract(from).normalize();
        int shots = castType == 1 && k == EnemyKind.EMPERADOR ? 3 : 1;
        for (int i = 0; i < shots; i++) {
            KiBlastEntity b = new KiBlastEntity(ModRegistry.KI_BLAST.get(), level());
            b.setOwner(this);
            b.setPos(from.x, from.y, from.z);
            float dmg = k.blastDamage * (isEnraged() ? 1.4F : 1.0F);
            if (castType == 2) {
                b.setup(k.kiColor, 2.6F, dmg * 2.2F);
                b.setBigBang(4.0F);
                b.shoot(dir.x, dir.y, dir.z, 0.9F, 0.0F);
            } else if (k == EnemyKind.EMPERADOR) {
                b.setup(k.kiColor, 0.35F, dmg);          // rayos finos y rápidos
                b.shoot(dir.x, dir.y, dir.z, 2.6F, 2.5F * i);
            } else {
                b.setup(k.kiColor, k == EnemyKind.ROBOT ? 0.9F : 0.7F, dmg);
                b.shoot(dir.x, dir.y, dir.z, 1.5F, 1.0F);
            }
            level().addFreshEntity(b);
        }
        swing(InteractionHand.MAIN_HAND);
        playSound(castType == 2 ? SoundEvents.WITHER_SHOOT : k == EnemyKind.ROBOT ? SoundEvents.FIRECHARGE_USE : SoundEvents.BLAZE_SHOOT,
                castType == 2 ? 2.0F : 1.0F, castType == 2 ? 0.6F : 1.3F);
        castType = 0;
    }

    /** Aparece de golpe detrás o al lado del objetivo. */
    private void teleportNear(LivingEntity tgt) {
        if (!(level() instanceof ServerLevel sl)) return;
        Vec3 look = tgt.getLookAngle();
        Vec3 behind = tgt.position().subtract(look.x * 2.5, 0, look.z * 2.5);
        double ox = getX(), oy = getY(), oz = getZ();
        for (int i = 0; i < 6; i++) {
            double x = behind.x + (random.nextDouble() - 0.5) * 3, z = behind.z + (random.nextDouble() - 0.5) * 3;
            if (randomTeleport(x, tgt.getY() + (kind().flies ? 1.5 : 0), z, false)) {
                sl.sendParticles(new DustParticleOptions(color(kind().kiColor), 2.0F), ox, oy + 1, oz, 30, 0.4, 0.8, 0.4, 0);
                sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1, getZ(), 1, 0, 0, 0, 0);
                sl.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.6F);
                sl.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 0.6F);
                meleeCd = 0;
                return;
            }
        }
    }

    /** Pisotón: onda de choque, escombros y daño alrededor. */
    private void stomp() {
        if (!(level() instanceof ServerLevel sl)) return;
        swing(InteractionHand.MAIN_HAND);
        net.minecraft.world.level.block.state.BlockState g = DebrisEntity.groundAt(sl, blockPosition());
        for (int i = 0; i < 14; i++) {
            double a = random.nextDouble() * Math.PI * 2, r = 1 + random.nextDouble() * 2;
            DebrisEntity.launch(sl, position().add(Math.cos(a) * r, 0.2, Math.sin(a) * r), g, Math.cos(a) * 0.2, 0.5 + random.nextDouble() * 0.4, Math.sin(a) * 0.2, 0.25F + random.nextFloat() * 0.3F);
        }
        for (int i = 0; i < 40; i++) {
            double a = i * Math.PI * 2 / 40;
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX() + Math.cos(a) * 1.5, getY() + 0.2, getZ() + Math.sin(a) * 1.5, 0, Math.cos(a), 0.02, Math.sin(a), 0.3);
        }
        sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 4, 1.5, 0.2, 1.5, 0);
        sl.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5F, 0.6F);
        sl.playSound(null, blockPosition(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 2.0F, 0.6F);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5.0), e -> !(e instanceof DbzEnemy) && e.isAlive())) {
            e.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F);
            Vec3 p = e.position().subtract(position()).normalize();
            e.push(p.x * 1.4, 0.7, p.z * 1.4);
            e.hurtMarked = true;
        }
    }

    /** Vuelo de los jefes: flota sobre el objetivo y lanza ki; cada cierto tiempo baja en picada para golpear. */
    private void fly(LivingEntity tgt, double d) {
        setNoGravity(true);
        getNavigation().stop();
        boolean dive = (phaseTimer / 80) % 2 == 1;
        Vec3 want = dive ? tgt.position().add(0, 0.5, 0) : tgt.position().add(0, 3.0, 0);
        Vec3 to = want.subtract(position());
        double dist = to.length();
        double sp = getAttributeValue(Attributes.MOVEMENT_SPEED) * (dive ? 1.4 : 0.9);
        double keep = dive ? 1.2 : 5.0;
        Vec3 v = getDeltaMovement().scale(0.82);
        if (dist > keep) v = v.add(to.normalize().scale(sp * 0.35));
        else v = v.add(0, Mth.sin(tickCount * 0.1F) * 0.01, 0);
        setDeltaMovement(v);
        getLookControl().setLookAt(tgt, 30, 30);
        setYRot(getYHeadRot());
        yBodyRot = getYRot();
        if (dive && d < 2.8 && meleeCd == 0) {
            doHurtTarget(tgt);
            meleeCd = 20;
        }
    }

    /** Invocaciones y furia según la vida que le queda. */
    private void checkPhases() {
        EnemyKind k = kind();
        float hp = getHealth() / getMaxHealth();
        if (k.summon != null && level() instanceof ServerLevel sl) {
            if (!summoned1 && hp < 0.66F) { summoned1 = true; summon(sl, 2); }
            if (!summoned2 && hp < 0.33F) { summoned2 = true; summon(sl, 3); }
        }
        if (k.boss && !isEnraged() && hp < 0.4F) {
            entityData.set(ENRAGED, true);
            var spd = getAttribute(Attributes.MOVEMENT_SPEED);
            var dmg = getAttribute(Attributes.ATTACK_DAMAGE);
            if (spd != null && spd.getModifier(RAGE_SPEED) == null) spd.addTransientModifier(new AttributeModifier(RAGE_SPEED, "furia", 0.4, AttributeModifier.Operation.MULTIPLY_BASE));
            if (dmg != null && dmg.getModifier(RAGE_DAMAGE) == null) dmg.addTransientModifier(new AttributeModifier(RAGE_DAMAGE, "furia", 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
            playSound(SoundEvents.ENDER_DRAGON_GROWL, 3.0F, 0.7F);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 1, 0, 0, 0, 0);
                for (ServerPlayer p : sl.players()) {
                    if (p.distanceTo(this) < 48) p.sendSystemMessage(Component.literal("§c§l¡").append(getDisplayName()).append(Component.literal(" §c§lentra en furia!")));
                }
            }
        }
    }

    private void summon(ServerLevel sl, int count) {
        EnemyKind sk = EnemyKind.byId(kind().summon);
        if (sk == null) return;
        playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.8F);
        for (int i = 0; i < count; i++) {
            DbzEnemy e = ModRegistry.ENEMY.get(sk).get().create(sl);
            if (e == null) continue;
            double a = i * Math.PI * 2 / count;
            e.moveTo(getX() + Math.cos(a) * 3, getY(), getZ() + Math.sin(a) * 3, getYRot(), 0);
            e.setTarget(getTarget());
            sl.addFreshEntity(e);
            sl.sendParticles(ParticleTypes.FLASH, e.getX(), e.getY() + 1, e.getZ(), 1, 0, 0, 0, 0);
            sl.sendParticles(new DustParticleOptions(color(sk.kiColor), 2.0F), e.getX(), e.getY() + 1, e.getZ(), 40, 0.4, 0.8, 0.4, 0);
        }
    }

    // ------------------------------------------------------------ aparición

    /** Color del contorno al sentir el ki (como con una flecha espectral). */
    @Override
    public int getTeamColor() {
        return getTeam() != null ? super.getTeamColor() : (isEnraged() ? 0xFF2020 : kind().kiColor);
    }

    /** Jefes: solo de noche, al aire libre y nunca si ya hay otro jefe cerca. */
    public static boolean checkSpawn(EntityType<DbzEnemy> type, net.minecraft.world.level.ServerLevelAccessor lv,
                                     net.minecraft.world.entity.MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource rnd) {
        if (!Monster.checkMonsterSpawnRules(type, lv, reason, pos, rnd)) return false;
        EnemyKind k = EnemyKind.of(type);
        if (!k.boss || reason == net.minecraft.world.entity.MobSpawnType.SPAWN_EGG || reason == net.minecraft.world.entity.MobSpawnType.COMMAND) return true;
        ServerLevel sl = lv.getLevel();
        if (!sl.isNight() || !sl.canSeeSky(pos)) return false;
        if (rnd.nextInt(4) != 0) return false;                                   // muy poco frecuentes
        return sl.getEntitiesOfClass(DbzEnemy.class, new net.minecraft.world.phys.AABB(pos).inflate(160), e -> e.kind().boss).isEmpty();
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor lv, net.minecraft.world.DifficultyInstance diff,
                                                                    net.minecraft.world.entity.MobSpawnType reason,
                                                                    @javax.annotation.Nullable net.minecraft.world.entity.SpawnGroupData data,
                                                                    @javax.annotation.Nullable CompoundTag tag) {
        var res = super.finalizeSpawn(lv, diff, reason, data, tag);
        if (kind().boss && reason == net.minecraft.world.entity.MobSpawnType.NATURAL) {
            ServerLevel sl = lv.getLevel();
            for (ServerPlayer p : sl.players()) {
                if (p.distanceToSqr(this) < 200 * 200) {
                    p.sendSystemMessage(Component.literal("§4§l⚠ §cSientes un ki enorme acercándose... §7(").append(getDisplayName()).append(Component.literal("§7)")));
                    p.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.6F, 0.8F);
                }
            }
        }
        return res;
    }

    // ------------------------------------------------------------ daño

    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (src.getEntity() instanceof DbzEnemy) return false;                 // no se hacen daño entre ellos
        EnemyKind k = kind();
        if (k.dodge > 0 && src.getEntity() != null && !level().isClientSide && random.nextFloat() < k.dodge && !src.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            // esquiva con un salto rápido hacia un lado
            Vec3 side = getLookAngle().cross(new Vec3(0, 1, 0)).normalize().scale(random.nextBoolean() ? 0.9 : -0.9);
            setDeltaMovement(getDeltaMovement().add(side.x, 0.25, side.z));
            hurtMarked = true;
            if (level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 8, 0.3, 0.5, 0.3, 0.05);
            playSound(SoundEvents.PLAYER_ATTACK_NODAMAGE, 1.0F, 1.5F);
            return false;
        }
        return super.hurt(src, amount / k.hpScale());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && kind() == EnemyKind.BIO_TITAN) heal((float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3F / kind().hpScale());   // absorbe energía
        if (hit && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.5), target.getZ(), 1, 0, 0, 0, 0);
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource src, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(src, looting, recentlyHit);
        EnemyKind k = kind();
        if (k.boss) {
            spawnAtLocation(new ItemStack(ModRegistry.SENZU_BEAN.get(), 2 + random.nextInt(2)));
            spawnAtLocation(new ItemStack(ModRegistry.BLACK_KI_INGOT.get(), 3 + random.nextInt(3)));
            spawnAtLocation(new ItemStack(Items.DIAMOND, 4 + random.nextInt(4)));
            spawnAtLocation(new ItemStack(Items.NETHERITE_SCRAP, 1 + random.nextInt(2)));
        } else {
            if (random.nextFloat() < 0.35F + looting * 0.1F) spawnAtLocation(new ItemStack(ModRegistry.BLACK_KI_INGOT.get()));
            if (random.nextFloat() < 0.1F) spawnAtLocation(new ItemStack(ModRegistry.SENZU_BEAN.get()));
            if (k == EnemyKind.ROBOT || k == EnemyKind.ANDROIDE || k == EnemyKind.DRON) spawnAtLocation(new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(3)));
            if (k == EnemyKind.GOLEM) spawnAtLocation(new ItemStack(Items.AMETHYST_SHARD, 2 + random.nextInt(4)));
            if (k == EnemyKind.MERCENARIO) spawnAtLocation(new ItemStack(Items.EMERALD, 1 + random.nextInt(3)));
        }
    }

    @Override
    public void die(DamageSource src) {
        super.die(src);
        if (level() instanceof ServerLevel sl && kind().boss) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 3, 1, 1, 1, 0);
            sl.sendParticles(new DustParticleOptions(color(kind().kiColor), 3.0F), getX(), getY() + 1, getZ(), 200, 1.5, 2, 1.5, 0);
            sl.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3.0F, 0.5F);
        }
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !kind().boss && super.removeWhenFarAway(dist);
    }

    // ------------------------------------------------------------ barra de jefe

    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        if (bar != null) bar.addPlayer(p);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        if (bar != null) bar.removePlayer(p);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        if (bar != null) bar.setName(getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summoned1 = tag.getBoolean("Summoned1");
        summoned2 = tag.getBoolean("Summoned2");
        if (bar != null && hasCustomName()) bar.setName(getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Summoned1", summoned1);
        tag.putBoolean("Summoned2", summoned2);
    }

    // ------------------------------------------------------------ efectos del cliente y sonidos

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            EnemyKind k = kind();
            if (k.boss || isEnraged() || getCast() > 0) {
                int c = isEnraged() ? 0xFF2020 : k.kiColor;
                DustParticleOptions dust = new DustParticleOptions(color(c), 1.2F);
                for (int i = 0; i < (k.boss ? 2 : 1); i++) {
                    double a = random.nextDouble() * Math.PI * 2, r = getBbWidth() * 0.8;
                    level().addParticle(dust, getX() + Math.cos(a) * r, getY() + random.nextDouble() * getBbHeight(), getZ() + Math.sin(a) * r, 0, 0.12, 0);
                }
            }
            if (isNoGravity() && random.nextInt(2) == 0) {
                level().addParticle(new DustParticleOptions(color(k.kiColor), 1.0F), getX(), getY() - 0.1, getZ(), 0, -0.1, 0);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (kind()) {
            case ROBOT, ANDROIDE, DRON -> SoundEvents.BEACON_AMBIENT;
            case GOLEM -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case SOMBRA -> SoundEvents.SOUL_ESCAPE;
            case ENGENDRO -> SoundEvents.SILVERFISH_AMBIENT;
            case BIO_TITAN -> SoundEvents.RAVAGER_AMBIENT;
            case DEMONIO -> SoundEvents.EVOKER_AMBIENT;
            default -> SoundEvents.PILLAGER_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return switch (kind()) {
            case ROBOT, ANDROIDE, DRON -> SoundEvents.IRON_GOLEM_HURT;
            case GOLEM -> SoundEvents.AMETHYST_BLOCK_BREAK;
            case SOMBRA -> SoundEvents.PHANTOM_HURT;
            case ENGENDRO -> SoundEvents.SPIDER_HURT;
            case BIO_TITAN -> SoundEvents.RAVAGER_HURT;
            case DEMONIO -> SoundEvents.EVOKER_HURT;
            default -> SoundEvents.PILLAGER_HURT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (kind()) {
            case ROBOT, ANDROIDE, DRON -> SoundEvents.IRON_GOLEM_DEATH;
            case GOLEM -> SoundEvents.AMETHYST_CLUSTER_BREAK;
            case SOMBRA -> SoundEvents.PHANTOM_DEATH;
            case ENGENDRO -> SoundEvents.SPIDER_DEATH;
            case BIO_TITAN -> SoundEvents.RAVAGER_DEATH;
            case DEMONIO -> SoundEvents.EVOKER_DEATH;
            case EMPERADOR -> SoundEvents.WITHER_DEATH;
            default -> SoundEvents.PILLAGER_DEATH;
        };
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (kind() == EnemyKind.ROBOT) playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.7F);
        else super.playStepSound(pos, state);
    }
}
