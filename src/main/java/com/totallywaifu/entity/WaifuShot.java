package com.totallywaifu.entity;

import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Bolas de fuego de Flama y Nube, y los explosivos de Dina y Mita: solo dañan a los monstruos, nunca rompen bloques. */
public class WaifuShot extends ThrowableItemProjectile {
    private float power = 6, radius = 2;
    private boolean fire;

    public WaifuShot(EntityType<? extends WaifuShot> t, Level l) {
        super(t, l);
    }

    public static WaifuShot fire(WaifuEntity from, LivingEntity target, Item item, float power, float radius, boolean fire, float speed) {
        WaifuShot s = new WaifuShot(ModRegistry.SHOT.get(), from.level());
        s.setOwner(from);
        s.setPos(from.getX(), from.getEyeY() - 0.2, from.getZ());
        s.setItem(new ItemStack(item));
        s.power = power;
        s.radius = radius;
        s.fire = fire;
        double dx = target.getX() - s.getX(), dy = target.getY(0.5) - s.getY(), dz = target.getZ() - s.getZ();
        if (item == Items.TNT) dy += Math.sqrt(dx * dx + dz * dz) * 0.2;
        s.shoot(dx, dy, dz, speed, 1.0F);
        from.level().addFreshEntity(s);
        return s;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected float getGravity() {
        return getItem().is(Items.TNT) ? 0.04F : 0.0F;
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return !(e instanceof Player) && !(e instanceof WaifuEntity) && super.canHitEntity(e);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(getItem().is(Items.TNT) ? ParticleTypes.SMOKE : ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 0, 0.02, 0);
        } else if (tickCount > 120) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult r) {
        super.onHit(r);
        if (!level().isClientSide) {
            boom();
            discard();
        }
    }

    private void boom() {
        ServerLevel lv = (ServerLevel) level();
        Vec3 c = position();
        lv.sendParticles(radius >= 3 ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        if (fire) lv.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 30, radius * 0.4, 0.4, radius * 0.4, 0.05);
        lv.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, Math.min(2.0F, 0.6F + radius * 0.3F), 1.1F);
        Entity owner = getOwner();
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) {
            if (!hostile(e, owner)) continue;
            e.hurt(damageSources().explosion(this, owner), power);
            if (fire && !e.fireImmune()) e.setSecondsOnFire(5);
            Vec3 d = e.position().subtract(c).normalize().scale(0.8);
            e.setDeltaMovement(e.getDeltaMovement().add(d.x, 0.4, d.z));
            e.hurtMarked = true;
        }
    }

    static boolean hostile(LivingEntity e, Entity owner) {
        if (e instanceof Player || e instanceof WaifuEntity || e == owner || (e instanceof TamableAnimal ta && ta.isTame())) return false;
        return e instanceof Enemy || (e instanceof Mob m && (m.getTarget() instanceof Player || m.getTarget() instanceof WaifuEntity));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putFloat("Power", power);
        t.putFloat("Radius", radius);
        t.putBoolean("Fire", fire);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        power = t.getFloat("Power");
        radius = t.getFloat("Radius");
        fire = t.getBoolean("Fire");
    }
}
