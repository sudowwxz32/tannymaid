package com.totallywaifu.entity;

import com.totallywaifu.data.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Inari, la Diosa Zorro Blanca: educada, respetuosa, tranquila y juguetona.
 * Como chica lanza fuego de zorro y se teletransporta; como zorro dios gigante vuela y corre por los cielos,
 * embiste atravesando lo que se ponga en medio y puedes montarla. Nunca deja que nadie te lastime.
 */
public final class Inari {
    private Inari() {}

    static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.82F, 0.3F), 1.6F);
    static final DustParticleOptions WHITE = new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.3F);

    static final String[] TALK = {
            "¿Se encuentra bien? Si necesita algo, solo tiene que pedírmelo.",
            "Qué tarde tan serena... me gusta caminar a su lado.",
            "Mientras yo esté aquí, nada ni nadie le hará daño. Se lo prometo.",
            "¿Sabía que los zorros sonreímos con la cola? Mire... *mueve la cola*",
            "Fufu~ ¿me estaba mirando? Está bien, puede seguir haciéndolo.",
            "Le agradezco que me haya traído a su mundo. Es muy bonito.",
            "Si se cansa, puedo llevarle volando. Los cielos son mi camino.",
            "¿Jugamos a algo? Le advierto que soy muy rápida~",
            "Respire hondo. No hay prisa. Yo vigilo.",
            "Me alegra mucho verle sonreír. De verdad.",
            "Las nubes de hoy parecen colas de zorro, ¿no le parece?",
            "Tome su tiempo. Estaré aquí, como siempre."};

    private static int timer(WaifuEntity w, String k) {
        return w.getPersistentData().getInt("TW_I" + k);
    }

    private static void set(WaifuEntity w, String k, int v) {
        w.getPersistentData().putInt("TW_I" + k, v);
    }

    // ================================================================== cada tick
    static void tick(WaifuEntity w) {
        if (w.level().isClientSide) {
            clientFx(w);
            return;
        }
        ServerLevel lv = (ServerLevel) w.level();
        if (w.transform() > 0) w.setTransformTicks(w.transform() - 1);
        for (String k : new String[]{"cd", "say", "talk"}) if (timer(w, k) > 0) set(w, k, timer(w, k) - 1);
        Player o = w.ownerPlayer();
        if (w.isFox()) {
            w.setNoGravity(true);
            w.fallDistance = 0;
            if (!w.isVehicle()) foxBrain(w, lv, o);
        } else {
            if (w.isNoGravity()) w.setNoGravity(false);
            girlBrain(w, lv, o);
        }
        if (o != null && o.isAlive() && o.level() == lv && o.distanceToSqr(w) < 96 * 96) {
            o.fallDistance = 0;                                     // si caes, ella te atrapa
            guard(w, lv, o);
            if (timer(w, "talk") == 0) {
                set(w, "talk", 4800 + w.getRandom().nextInt(4800));
                if (o.distanceToSqr(w) < 20 * 20 && w.tickCount > 200) w.say(o, TALK[w.getRandom().nextInt(TALK.length)]);
            }
        }
        if (w.tickCount % 40 == 0 && w.getHealth() < w.getMaxHealth()) w.heal(4.0F);
    }

    // ================================================================== la guardiana: nada te toca
    private static void guard(WaifuEntity w, ServerLevel lv, Player o) {
        // proyectiles que van hacia ti: se deshacen en luz
        if (w.tickCount % 2 == 0) {
            for (Projectile pr : lv.getEntitiesOfClass(Projectile.class, o.getBoundingBox().inflate(7), pr -> pr.isAlive() && pr.getOwner() != o
                    && !(pr.getOwner() instanceof WaifuEntity) && !(pr instanceof WaifuShot))) {
                Vec3 v = pr.getDeltaMovement();
                Vec3 to = o.position().add(0, 1, 0).subtract(pr.position());
                if (v.lengthSqr() < 0.01 || v.normalize().dot(to.normalize()) < 0.3) continue;
                lv.sendParticles(GOLD, pr.getX(), pr.getY(), pr.getZ(), 12, 0.2, 0.2, 0.2, 0.02);
                lv.sendParticles(ParticleTypes.END_ROD, pr.getX(), pr.getY(), pr.getZ(), 6, 0.1, 0.1, 0.1, 0.05);
                lv.playSound(null, pr.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.8F, 1.6F);
                pr.discard();
            }
        }
        if (w.tickCount % 5 != 0) return;
        // monstruos que te buscan (o un creeper a punto de explotar cerca)
        Mob threat = lv.getEntitiesOfClass(Mob.class, o.getBoundingBox().inflate(32, 16, 32), m -> m.isAlive() && danger(m, o))
                .stream().min(Comparator.comparingDouble(m -> m.distanceToSqr(o) - (m instanceof Creeper c && c.getSwellDir() > 0 ? 1000 : 0))).orElse(null);
        if (threat == null) return;
        if (w.getMode() == WaifuEntity.STAY && w.distanceToSqr(threat) > 24 * 24 && !(threat instanceof Creeper)) return;
        w.setTarget(threat);
        if (!w.isFox() && timer(w, "cd") == 0) {                 // fuego de zorro desde donde esté
            if (w.distanceToSqr(threat) > 20 * 20 || !w.hasLineOfSight(threat)) blink(w, lv, threat.position().add(threat.getLookAngle().scale(-2.5)));
            smite(w, lv, threat, 40.0F);
            set(w, "cd", 14);
        } else if (w.isFox() && threat instanceof Creeper c && c.getSwellDir() > 0 && timer(w, "cd") == 0) {
            smite(w, lv, threat, 60.0F);
            set(w, "cd", 8);
        }
    }

    static boolean danger(Mob m, Player o) {
        if (m instanceof WaifuEntity || (m instanceof TamableAnimal t && t.isTame())) return false;
        if (m.getTarget() == o) return true;
        if (m instanceof Creeper c && c.getSwellDir() > 0 && c.distanceToSqr(o) < 8 * 8) return true;
        return m instanceof Enemy && m.distanceToSqr(o) < 7 * 7;
    }

    /** Golpe divino: un rayo de fuego de zorro dorado. */
    static void smite(WaifuEntity w, ServerLevel lv, LivingEntity t, float dmg) {
        Vec3 a = w.position().add(0, w.getBbHeight() * 0.7, 0), b = t.position().add(0, t.getBbHeight() * 0.5, 0);
        int n = (int) Math.min(40, a.distanceTo(b) * 2);
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.lerp(b, i / (double) Math.max(1, n));
            lv.sendParticles(GOLD, q.x, q.y, q.z, 1, 0.05, 0.05, 0.05, 0);
        }
        lv.sendParticles(ParticleTypes.FLASH, b.x, b.y, b.z, 1, 0, 0, 0, 0);
        lv.sendParticles(ParticleTypes.END_ROD, b.x, b.y, b.z, 24, 0.4, 0.5, 0.4, 0.12);
        lv.sendParticles(WHITE, b.x, b.y, b.z, 16, 0.5, 0.5, 0.5, 0.02);
        lv.playSound(null, t.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.4F, 0.7F);
        lv.playSound(null, t.blockPosition(), SoundEvents.FOX_SCREECH, SoundSource.NEUTRAL, 0.7F, 0.6F);
        t.hurt(w.damageSources().mobAttack(w), dmg);
        Vec3 d = t.position().subtract(w.position()).normalize().scale(1.6);
        t.setDeltaMovement(d.x, 0.5, d.z);
        t.hurtMarked = true;
        w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        w.cheer(20);
    }

    /** Se teletransporta en un destello. */
    static void blink(WaifuEntity w, ServerLevel lv, Vec3 to) {
        BlockPos bp = BlockPos.containing(to);
        for (int dy = 0; dy < 4; dy++) {
            BlockPos q = bp.above(dy);
            if (lv.noCollision(w, w.getBoundingBox().move(Vec3.atBottomCenterOf(q).subtract(w.position())))) {
                lv.sendParticles(WHITE, w.getX(), w.getY() + 1, w.getZ(), 20, 0.3, 0.8, 0.3, 0.02);
                w.teleportTo(q.getX() + 0.5, q.getY(), q.getZ() + 0.5);
                lv.sendParticles(GOLD, w.getX(), w.getY() + 1, w.getZ(), 20, 0.3, 0.8, 0.3, 0.02);
                lv.playSound(null, w.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.6F, 1.6F);
                return;
            }
        }
    }

    // ================================================================== como chica
    private static void girlBrain(WaifuEntity w, ServerLevel lv, @Nullable Player o) {
        if (o == null || w.getMode() == WaifuEntity.STAY) return;
        if (o.level() == lv && w.distanceToSqr(o) > 40 * 40) blink(w, lv, o.position().add(o.getLookAngle().scale(-2)));
        LivingEntity tg = w.getTarget();
        if (tg != null && tg.isAlive() && timer(w, "cd") == 0 && w.distanceToSqr(tg) < 24 * 24 && w.hasLineOfSight(tg)) {
            smite(w, lv, tg, 30.0F);
            set(w, "cd", 20);
        }
    }

    // ================================================================== como zorro dios gigante (vuela y corre por los cielos)
    private static void foxBrain(WaifuEntity w, ServerLevel lv, @Nullable Player o) {
        LivingEntity tg = w.getTarget();
        if (tg != null && (!tg.isAlive() || tg.level() != lv || w.distanceToSqr(tg) > 64 * 64)) {
            w.setTarget(null);
            tg = null;
        }
        Vec3 goal;
        double speed;
        boolean charging = false;
        if (tg != null) {
            goal = tg.position().add(0, Math.max(0, tg.getBbHeight() * 0.5 - 1.2), 0);
            speed = 1.35;
            charging = true;
            double reach = w.getBbWidth() * 0.5 + tg.getBbWidth() * 0.5 + 1.6;
            if (w.distanceToSqr(tg) < reach * reach && timer(w, "cd") == 0) {
                w.doHurtTarget(tg);
                smite(w, lv, tg, 30.0F);
                lv.sendParticles(ParticleTypes.SWEEP_ATTACK, tg.getX(), tg.getY() + 1, tg.getZ(), 3, 0.6, 0.3, 0.6, 0);
                // onda expansiva: todos los monstruos cerca salen volando
                for (Mob m : lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(5), m -> m != w && m instanceof Enemy && m.isAlive())) {
                    Vec3 d = m.position().subtract(w.position()).normalize().scale(1.8);
                    m.setDeltaMovement(d.x, 0.8, d.z);
                    m.hurtMarked = true;
                    m.hurt(w.damageSources().mobAttack(w), 12.0F);
                }
                set(w, "cd", 12);
            }
        } else if (o != null && o.isAlive() && w.getMode() != WaifuEntity.STAY) {
            if (o.level() != lv || w.distanceToSqr(o) > 72 * 72) {
                if (o.level() == lv) w.teleportTo(o.getX(), o.getY() + 4, o.getZ());
                return;
            }
            Vec3 look = Vec3.directionFromRotation(0, o.getYRot());
            Vec3 right = new Vec3(-look.z, 0, look.x);
            goal = o.position().add(right.scale(3.8)).add(look.scale(-1.5));
            boolean air = !o.onGround() && !o.isInWater() && (o.isFallFlying() || o.getAbilities().flying || o.fallDistance > 1 || o.getDeltaMovement().y > 0.1);
            if (air) goal = goal.add(0, 0.5, 0);
            else {
                int gy = lv.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(goal.x), Mth.floor(goal.z));
                goal = new Vec3(goal.x, Math.max(gy, o.getY() - 1), goal.z);
            }
            double dist = goal.distanceTo(w.position());
            speed = dist > 16 ? 1.5 : dist > 5 ? 0.7 : 0.25;
            if (o.isFallFlying()) speed = Math.max(speed, o.getDeltaMovement().length() + 0.4);
        } else {
            goal = w.position();
            speed = 0.1;
        }
        Vec3 to = goal.subtract(w.position());
        double d = to.length();
        Vec3 want = d < 0.35 ? Vec3.ZERO : to.normalize().scale(Math.min(speed, d * 0.3 + 0.04));
        Vec3 v = w.getDeltaMovement().lerp(want, charging ? 0.3 : 0.18);
        w.setDeltaMovement(v);
        // orientación: hacia donde corre; quieta, te mira a ti
        float yaw;
        if (v.horizontalDistanceSqr() > 0.004) yaw = (float) (Mth.atan2(v.z, v.x) * Mth.RAD_TO_DEG) - 90.0F;
        else if (tg != null) yaw = (float) (Mth.atan2(tg.getZ() - w.getZ(), tg.getX() - w.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        else if (o != null) yaw = (float) (Mth.atan2(o.getZ() - w.getZ(), o.getX() - w.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        else yaw = w.getYRot();
        float ny = Mth.approachDegrees(w.getYRot(), yaw, 12.0F);
        w.setYRot(ny);
        w.yBodyRot = ny;
        w.setYHeadRot(ny);
        if (tg != null) w.getLookControl().setLookAt(tg, 30, 30);
        // nada la detiene: atraviesa hojas, plantas y lo que se ponga en su camino al embestir
        if ((w.horizontalCollision || charging) && w.tickCount % 2 == 0 && lv.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) smash(w, lv, charging);
    }

    private static void smash(WaifuEntity w, ServerLevel lv, boolean hard) {
        AABB box = w.getBoundingBox().move(w.getDeltaMovement().scale(2)).inflate(0.2);
        int broken = 0;
        for (BlockPos q : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY + 0.5), Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockState st = lv.getBlockState(q);
            if (st.isAir() || lv.getBlockEntity(q) != null) continue;
            float hard_ = st.getDestroySpeed(lv, q);
            boolean soft = st.is(net.minecraft.tags.BlockTags.LEAVES) || st.canBeReplaced() || st.is(net.minecraft.tags.BlockTags.FLOWERS) || hard_ <= 0.6F;
            if (hard_ < 0 || !(soft || (hard && hard_ <= 2.5F))) continue;
            lv.destroyBlock(q, true, w);
            if (++broken > 12) break;
        }
        if (broken > 0) lv.playSound(null, w.blockPosition(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.5F, 1.3F);
    }

    /** Movimiento del zorro (vuela). Montada: va hacia donde miras, rapidísima. */
    static boolean travel(WaifuEntity w, Vec3 in) {
        if (w.getControllingPassenger() instanceof Player p) {
            if (!w.isControlledByLocalInstance()) {
                w.calculateEntityAnimation(true);
                return true;
            }
            float fwd = p.zza, side = p.xxa;
            Vec3 look = p.getLookAngle();
            Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
            double sp = 1.25;
            Vec3 want = look.scale(fwd * sp).add(right.scale(-side * sp * 0.6));
            if (fwd < 0) want = want.scale(0.4);
            Vec3 v = w.getDeltaMovement().lerp(want, 0.12);
            w.setDeltaMovement(v);
            w.move(MoverType.SELF, v);
            w.calculateEntityAnimation(true);
            return true;
        }
        if (w.level().isClientSide) {
            w.calculateEntityAnimation(true);
            return true;
        }
        w.move(MoverType.SELF, w.getDeltaMovement());
        w.setDeltaMovement(w.getDeltaMovement().scale(0.94));
        w.calculateEntityAnimation(true);
        return true;
    }

    // ================================================================== transformarse
    public static void transform(WaifuEntity w, boolean fox, @Nullable Player who) {
        if (w.isFox() == fox || !(w.level() instanceof ServerLevel lv)) return;
        w.setFox(fox, true);
        w.getNavigation().stop();
        if (fox) w.setDeltaMovement(0, 0.3, 0);
        for (int i = 0; i < 40; i++) {
            double a = i * 0.45, r = 0.6 + i * 0.05;
            lv.sendParticles(i % 2 == 0 ? GOLD : WHITE, w.getX() + Math.cos(a) * r, w.getY() + i * 0.07, w.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
        lv.sendParticles(ParticleTypes.END_ROD, w.getX(), w.getY() + 1.5, w.getZ(), 60, 0.8, 1.2, 0.8, 0.12);
        lv.sendParticles(ParticleTypes.CHERRY_LEAVES, w.getX(), w.getY() + 2, w.getZ(), 30, 1.2, 1.0, 1.2, 0);
        lv.sendParticles(ParticleTypes.FLASH, w.getX(), w.getY() + 1.5, w.getZ(), 1, 0, 0, 0, 0);
        lv.playSound(null, w.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.4F, fox ? 0.7F : 1.4F);
        lv.playSound(null, w.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.4F, 0.6F);
        if (fox) lv.playSound(null, w.blockPosition(), SoundEvents.FOX_SCREECH, SoundSource.NEUTRAL, 1.2F, 0.5F);
        if (who != null) w.say(who, fox ? "Con su permiso... *se envuelve en luz dorada* Súbase cuando guste, le llevaré por los cielos."
                : "*vuelve a su forma* Aquí estoy. ¿Me extrañó, aunque sea un poquito? Fufu~");
        w.cheer(60);
    }

    static InteractionResult interact(WaifuEntity w, ServerPlayer p, ItemStack held) {
        if (w.isFox() && held.isEmpty() && !w.isVehicle() && !p.isPassenger()) {
            p.startRiding(w);
            w.say(p, "Agárrese de mi pelaje. Mire hacia donde quiera ir... y yo correré por el cielo.");
            w.cheer(60);
            return InteractionResult.CONSUME;
        }
        if (held.is(Items.SWEET_BERRIES) || held.is(Items.GLOW_BERRIES) || held.is(Items.GOLDEN_APPLE)) {
            if (!p.getAbilities().instabuild) held.shrink(1);
            w.heal(w.getMaxHealth());
            w.cheer(120);
            w.say(p, held.is(Items.GOLDEN_APPLE) ? "¿Una manzana dorada? Es usted demasiado amable... Gracias ♥" : "¡Bayas! Mis favoritas... *las come con delicadeza* Gracias ♥");
            w.playSound(SoundEvents.FOX_EAT, 1.0F, 1.2F);
            return InteractionResult.CONSUME;
        }
        if (!w.isFox() && held.isEmpty()) {                          // caricia en la cabeza
            w.cheer(80);
            w.playSound(SoundEvents.FOX_AMBIENT, 0.8F, 1.3F);
            String[] l = {"*cierra los ojos* ...Mmm. Me gusta cuando hace eso.", "Fufu~ me hace cosquillas en las orejas.",
                    "*su cola se mueve sola* ...No diga nada, por favor.", "Gracias. Es usted muy gentil conmigo."};
            w.say(p, l[w.getRandom().nextInt(l.length)]);
            if (w.level() instanceof ServerLevel lv) lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 2.2, w.getZ(), 3, 0.3, 0.2, 0.3, 0);
            return InteractionResult.CONSUME;
        }
        return null;
    }

    // ================================================================== órdenes (comando /inari y tecla)
    @Nullable
    public static WaifuEntity find(ServerPlayer p) {
        return Collection.findLoaded(p, WaifuKind.DIOSA);
    }

    public static void order(ServerPlayer p, String what) {
        if (!Collection.has(p, WaifuKind.DIOSA)) {
            p.displayClientMessage(Component.literal("§7Aún no conoces a la Diosa Zorro Blanca... encuentra a todas las leyendas."), true);
            return;
        }
        WaifuEntity w = find(p);
        if (w == null || w.level() != p.level() || w.distanceToSqr(p) > 64 * 64 || what.equals("llamar")) {
            Collection.summon(p, WaifuKind.DIOSA);
            w = find(p);
            if (w == null) return;
            if (what.equals("llamar")) return;
        }
        switch (what) {
            case "zorro" -> transform(w, true, p);
            case "chica" -> transform(w, false, p);
            case "cambiar" -> transform(w, !w.isFox(), p);
            case "montar" -> {
                if (!w.isFox()) transform(w, true, p);
                if (!w.isVehicle()) p.startRiding(w, true);
            }
            default -> { }
        }
    }

    // ================================================================== la defensa absoluta (evento)
    /** ¿Te protege ahora mismo? (cargada, en tu dimensión y a menos de 96 bloques). */
    @Nullable
    public static WaifuEntity guardian(ServerPlayer p) {
        WaifuEntity w = find(p);
        if (w == null || !w.isAlive() || w.level() != p.level() || w.distanceToSqr(p) > 96 * 96) return null;
        return w;
    }

    public static void defended(WaifuEntity w, ServerPlayer p, @Nullable Entity attacker) {
        ServerLevel lv = (ServerLevel) w.level();
        for (int i = 0; i < 16; i++) {                              // escudo dorado alrededor tuyo
            double a = i * Mth.TWO_PI / 16;
            lv.sendParticles(GOLD, p.getX() + Math.cos(a) * 0.9, p.getY() + 1.0, p.getZ() + Math.sin(a) * 0.9, 1, 0, 0.3, 0, 0);
        }
        if (attacker instanceof Mob m && m != w && danger2(m, p) && timer(w, "cd") <= 4) {
            smite(w, lv, m, 45.0F);
            set(w, "cd", 6);
        } else if (attacker instanceof Player other && other != p) {
            Vec3 d = other.position().subtract(p.position()).normalize().scale(1.2);
            other.setDeltaMovement(d.x, 0.4, d.z);
            other.hurtMarked = true;
        }
        if (timer(w, "say") == 0) {
            set(w, "say", 400);
            String[] l = {"Nadie le pondrá una mano encima mientras yo exista.", "Lo siento, pero a esta persona no se le toca.",
                    "Tranquilo, tranquila. Ya me encargué.", "...¿Se atrevió a intentarlo? Qué descortés."};
            w.say(p, l[w.getRandom().nextInt(l.length)]);
        }
    }

    private static boolean danger2(Mob m, Player p) {
        return !(m instanceof WaifuEntity) && !(m instanceof TamableAnimal t && t.isTame() && p.getUUID().equals(t.getOwnerUUID()));
    }

    // ================================================================== efectos en el cliente
    private static void clientFx(WaifuEntity w) {
        var lv = w.level();
        var r = w.getRandom();
        if (w.transform() > 0) {
            float k = w.transform() / 30.0F;
            for (int i = 0; i < 5; i++) {
                double a = (w.tickCount + i * 72) * 0.35, rad = 0.6 + (1 - k) * 1.6;
                lv.addParticle(i % 2 == 0 ? GOLD : ParticleTypes.END_ROD, w.getX() + Math.cos(a) * rad, w.getY() + (1 - k) * 2.6, w.getZ() + Math.sin(a) * rad, 0, 0.02, 0);
            }
        }
        if (w.isFox()) {
            Vec3 v = w.getDeltaMovement();
            boolean moving = new Vec3(w.getX() - w.xo, w.getY() - w.yo, w.getZ() - w.zo).lengthSqr() > 0.01;
            if (!w.onGround() && moving) {                          // huellas de luz: corre sobre el cielo
                for (int i = 0; i < 2; i++)
                    lv.addParticle(GOLD, w.getX() + (r.nextDouble() - 0.5) * 1.6, w.getY() + 0.05, w.getZ() + (r.nextDouble() - 0.5) * 1.6, 0, -0.01, 0);
                if (r.nextInt(3) == 0) lv.addParticle(ParticleTypes.END_ROD, w.getX() - v.x * 3, w.getY() + 1.6, w.getZ() - v.z * 3, 0, 0, 0);
            }
            if (r.nextInt(10) == 0) lv.addParticle(GOLD, w.getRandomX(1.0), w.getY() + 2.6 + r.nextDouble() * 0.6, w.getRandomZ(1.0), 0, 0.02, 0);
        } else if (r.nextInt(14) == 0) {
            lv.addParticle(GOLD, w.getRandomX(0.8), w.getY() + 1.2 + r.nextDouble() * 0.8, w.getRandomZ(0.8), 0, 0.01, 0);
        }
    }
}
