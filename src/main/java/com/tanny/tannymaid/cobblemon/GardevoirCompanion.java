package com.tanny.tannymaid.cobblemon;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannyLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * La Gardevoir de Tanny: siempre al lado del Master. Es más protectora que Lucario
 * (vigila alrededor del Master, le reduce el daño y lo cura con Deseo), más cariñosa y
 * un poco más fuerte (Psíquico y Fuerza Lunar).
 * Se gestiona por jugador, así acompaña al Master aunque Tanny se quede en casa.
 */
public final class GardevoirCompanion {
    private static final Map<UUID, GardevoirCompanion> BY_PLAYER = new ConcurrentHashMap<>();
    private static final String KEY_OFF = "TannyGardevoirOff";
    private static final int LEVEL = 55;
    private static final float PSYCHIC_DAMAGE = 10.0F;   // Lucario: 8 cuerpo a cuerpo
    private static final float MOONBLAST_DAMAGE = 14.0F; // Lucario: 7 Esfera Aural
    private static final DustParticleOptions PINK = new DustParticleOptions(new Vector3f(1.0F, 0.55F, 0.8F), 1.2F);
    private static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.45F, 0.95F, 0.6F), 1.0F);

    private static final String[] LOVE = {
            "Gardevoir se acerca y apoya su cabeza en tu hombro ♡",
            "Gardevoir te toma la mano con dulzura... «Gar~» ♡",
            "Gardevoir sonríe y deja un brillo rosado a tu alrededor ♡",
            "Gardevoir te mira con cariño. Siente que estás feliz, y eso la hace feliz ♡",
            "Gardevoir acomoda tu ropa con cuidado. «Voir... ♡»",
            "Gardevoir tararea una melodía suave solo para ti ♡"
    };
    private static final String[] PROTECT = {
            "Gardevoir se pone delante de ti. ¡Nadie te tocará!",
            "«¡Gar!» Gardevoir levanta un escudo psíquico para protegerte.",
            "Gardevoir frunce el ceño: ¡alguien quiere hacerte daño!"
    };
    private static final String[] WISH = {
            "Gardevoir usa Deseo: una luz cálida cura tus heridas ♡",
            "Gardevoir te abraza y su energía te cura ♡"
    };

    private final UUID playerId;
    @Nullable private Mob mob;
    @Nullable private LivingEntity target;
    private int cooldown, psychicCd, moonCd, wishCd, protectMsgCd, repath, scan;
    private int loveCd = 1200;

    private GardevoirCompanion(UUID playerId) {
        this.playerId = playerId;
    }

    // ------------------------------------------------------------------ API

    public static void tickPlayer(ServerPlayer player) {
        GardevoirCompanion c = BY_PLAYER.get(player.getUUID());
        if (c == null) {
            if (!wanted(player)) return;          // no crea nada si no hace falta (optimización)
            c = new GardevoirCompanion(player.getUUID());
            BY_PLAYER.put(player.getUUID(), c);
        }
        c.tick(player);
    }

    public static void onLogout(ServerPlayer player) {
        GardevoirCompanion c = BY_PLAYER.remove(player.getUUID());
        if (c != null) c.recall();
    }

    public static void recallAll() {
        for (GardevoirCompanion c : BY_PLAYER.values()) c.recall();
        BY_PLAYER.clear();
    }

    @Nullable
    public static GardevoirCompanion of(Player player) {
        return BY_PLAYER.get(player.getUUID());
    }

    /** El Master recibió daño: Gardevoir lo protege (menos daño) y va por el atacante. */
    public static float onOwnerHurt(ServerPlayer player, @Nullable LivingEntity attacker, float amount) {
        GardevoirCompanion c = BY_PLAYER.get(player.getUUID());
        if (c == null || !c.isOut() || c.mob.level() != player.level() || c.mob.distanceToSqr(player) > 12 * 12) return amount;
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(PINK, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.5, 0.6, 0.5, 0.0);
        if (attacker != null && c.canTarget(player, attacker)) c.target = attacker;
        if (c.protectMsgCd == 0) {
            c.protectMsgCd = 600;
            say(player, pick(player.getRandom(), PROTECT));
        }
        return amount * 0.7F;   // Protección: 30 % menos de daño
    }

    /** El Master atacó a algo: Gardevoir lo ayuda. */
    public static void onOwnerAttack(ServerPlayer player, LivingEntity victim) {
        GardevoirCompanion c = BY_PLAYER.get(player.getUUID());
        if (c != null && c.isOut() && c.canTarget(player, victim)) c.target = victim;
    }

    public static void toggle(ServerPlayer player) {
        CompoundTag data = data(player);
        boolean off = !data.getBoolean(KEY_OFF);
        data.putBoolean(KEY_OFF, off);
        if (off) {
            GardevoirCompanion c = BY_PLAYER.remove(player.getUUID());
            if (c != null) c.recall();
            say(player, "Gardevoir vuelve a su Poké Ball con Tanny... pero te mira hasta el último segundo ♡");
        } else if (!CobblemonBridge.isAvailable()) {
            player.sendSystemMessage(Component.literal("§d[Tanny]§f Gardevoir no puede salir... parece que Cobblemon no está instalado, Amo."));
        } else {
            say(player, "¡Gardevoir sale de su Poké Ball y corre a tu lado! «Gar~♡»");
        }
    }

    public static String status(ServerPlayer player) {
        if (!CobblemonBridge.isLoaded()) return "no disponible (Cobblemon no instalado)";
        if (data(player).getBoolean(KEY_OFF)) return "en su Poké Ball (/tanny gardevoir para sacarla)";
        GardevoirCompanion c = BY_PLAYER.get(player.getUUID());
        if (c != null && c.isOut()) return "contigo ♡";
        if (c != null && c.cooldown > 0) return "recuperándose (" + c.cooldown / 20 + " s)";
        return "saliendo...";
    }

    public boolean isOut() {
        return mob != null && mob.isAlive() && !mob.isRemoved();
    }

    // ------------------------------------------------------------------ lógica

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static boolean wanted(ServerPlayer p) {
        return CobblemonBridge.isAvailable() && p.isAlive() && !p.isSpectator()
                && TannyLookup.getStoredId(p) != null && !data(p).getBoolean(KEY_OFF);
    }

    private void tick(ServerPlayer player) {
        if (cooldown > 0) cooldown--;
        if (psychicCd > 0) psychicCd--;
        if (moonCd > 0) moonCd--;
        if (wishCd > 0) wishCd--;
        if (protectMsgCd > 0) protectMsgCd--;
        if (loveCd > 0) loveCd--;

        if (mob != null && (!mob.isAlive() || mob.isRemoved())) {
            boolean fainted = !mob.isAlive();
            LucarioCompanion.untrack(mob.getUUID());
            mob = null;
            target = null;
            if (fainted) {
                cooldown = 1200;
                say(player, "Gardevoir se debilitó protegiéndote... Tanny la está curando (60 s). ¡Volverá pronto!");
            }
        }
        if (!wanted(player)) {
            recall();
            if (!player.isAlive() || player.isSpectator()) return;
            BY_PLAYER.remove(playerId);
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        if (mob == null) {
            if (cooldown == 0 && player.tickCount % 20 == 0) summon(level, player);
            return;
        }
        if (mob.level() != level) {           // el Master cambió de dimensión: ella lo sigue
            recall();
            return;
        }

        if (--scan <= 0) {
            scan = 10;
            pickTarget(player);
        }
        if (target != null && (!target.isAlive() || target.level() != level || target.distanceToSqr(player) > 24 * 24)) {
            target = null;
        }

        if (target != null) {
            fight(level, player, target);
        } else {
            follow(player);
            care(level, player);
        }
    }

    private void summon(ServerLevel level, ServerPlayer player) {
        Mob m = CobblemonBridge.createPokemon(level, "gardevoir", LEVEL, "Gardevoir de Tanny", true);
        if (m == null) {
            cooldown = 1200;
            return;
        }
        Vec3 side = sidePos(player);
        m.moveTo(side.x, player.getY(), side.z, player.getYRot(), 0.0F);
        m.addTag(LucarioCompanion.TAG);
        m.setPersistenceRequired();
        LucarioCompanion.track(m.getUUID());
        if (!level.addFreshEntity(m)) {
            LucarioCompanion.untrack(m.getUUID());
            cooldown = 200;
            return;
        }
        mob = m;
        level.sendParticles(PINK, side.x, player.getY() + 1.0, side.z, 30, 0.4, 0.7, 0.4, 0.0);
        level.sendParticles(ParticleTypes.HEART, side.x, player.getY() + 1.8, side.z, 3, 0.3, 0.2, 0.3, 0.0);
        level.playSound(null, m.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.3F);
    }

    /** Un lugar a la derecha y un poco detrás del Master. */
    private static Vec3 sidePos(Player p) {
        float yaw = p.getYRot() * ((float) Math.PI / 180F);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -fz, rz = fx;
        return new Vec3(p.getX() + rx * 1.4 - fx * 0.6, p.getY(), p.getZ() + rz * 1.4 - fz * 0.6);
    }

    private void follow(ServerPlayer player) {
        double d = mob.distanceToSqr(player);
        if (d > 18 * 18) {
            Vec3 side = sidePos(player);
            mob.moveTo(side.x, player.getY(), side.z, mob.getYRot(), mob.getXRot());
            mob.getNavigation().stop();
            mob.level().playSound(null, mob.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.NEUTRAL, 0.5F, 1.6F);
        } else if (d > 2.4 * 2.4) {
            if (--repath <= 0) {
                repath = 8;
                Vec3 side = sidePos(player);
                mob.getNavigation().moveTo(side.x, player.getY(), side.z, d > 8 * 8 ? 1.6D : 1.25D);
            }
        } else {
            mob.getNavigation().stop();
            mob.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }

    private void care(ServerLevel level, ServerPlayer player) {
        // Deseo: cura al Master cuando está herido
        if (wishCd == 0 && player.getHealth() < player.getMaxHealth() * 0.5F && mob.distanceToSqr(player) < 8 * 8) {
            wishCd = 400;
            player.heal(6.0F);
            level.sendParticles(GREEN, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.7, 0.4, 0.0);
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 2.0, player.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
            say(player, pick(player.getRandom(), WISH));
        }
        // Cariño: gestos dulces de vez en cuando
        if (loveCd == 0 && mob.distanceToSqr(player) < 4 * 4) {
            loveCd = 1800 + player.getRandom().nextInt(1800);
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
            mob.getLookControl().setLookAt(player, 30.0F, 30.0F);
            say(player, pick(player.getRandom(), LOVE));
        } else if (player.tickCount % 100 == 0 && mob.distanceToSqr(player) < 3 * 3) {
            level.sendParticles(PINK, mob.getX(), mob.getY() + 1.2, mob.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
        }
    }

    private void pickTarget(ServerPlayer player) {
        if (target != null && target.isAlive()) return;
        target = null;
        LivingEntity hurtBy = player.getLastHurtByMob();
        if (hurtBy != null && player.tickCount - player.getLastHurtByMobTimestamp() < 100 && canTarget(player, hurtBy)) {
            target = hurtBy;
            return;
        }
        // Más protectora: vigila a todo lo hostil que se acerque al Master
        List<Mob> near = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(12.0, 5.0, 12.0),
                m -> canTarget(player, m) && (m.getTarget() == player || (m instanceof Enemy && m.distanceToSqr(player) < 9 * 9)));
        double best = Double.MAX_VALUE;
        for (Mob m : near) {
            double s = m.distanceToSqr(player) - (m.getTarget() == player ? 1000 : 0);
            if (s < best) {
                best = s;
                target = m;
            }
        }
    }

    private boolean canTarget(ServerPlayer player, LivingEntity e) {
        if (e == null || !e.isAlive() || e == player || e == mob) return false;
        if (e instanceof Player || e instanceof TannyEntity || LucarioCompanion.isTannyPokemon(e)) return false;
        if (e instanceof OwnableEntity own && player.getUUID().equals(own.getOwnerUUID())) return false;
        if (CobblemonBridge.isPokemonEntity(e) && !(e instanceof Mob m && m.getTarget() == player)) return false;
        return true;
    }

    private void fight(ServerLevel level, ServerPlayer player, LivingEntity t) {
        mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
        // Se queda entre el Master y el enemigo (protectora)
        if (--repath <= 0) {
            repath = 6;
            Vec3 dir = t.position().subtract(player.position());
            Vec3 guard = dir.lengthSqr() > 0.01 ? player.position().add(dir.normalize().scale(Math.min(2.5, dir.length() * 0.5)))
                    : player.position();
            mob.getNavigation().moveTo(guard.x, guard.y, guard.z, 1.5D);
        }
        double d = mob.distanceToSqr(t);
        if (d > 18 * 18 || !mob.hasLineOfSight(t)) return;
        if (moonCd == 0) {
            moonblast(level, t);
        } else if (psychicCd == 0) {
            psychic(level, t);
        }
    }

    private void psychic(ServerLevel level, LivingEntity t) {
        psychicCd = 20;
        Vec3 from = mob.position().add(0, mob.getBbHeight() * 0.7, 0);
        Vec3 to = t.position().add(0, t.getBbHeight() * 0.5, 0);
        beam(level, from, to, ParticleTypes.WITCH);
        level.sendParticles(ParticleTypes.ENCHANT, to.x, to.y, to.z, 25, 0.4, 0.5, 0.4, 0.6);
        level.playSound(null, mob.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 0.8F, 1.5F);
        t.hurt(level.damageSources().mobAttack(mob), PSYCHIC_DAMAGE);
        t.setDeltaMovement(t.getDeltaMovement().add(0, 0.25, 0));   // lo levanta con telequinesis
        t.hurtMarked = true;
        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private void moonblast(ServerLevel level, LivingEntity t) {
        moonCd = 70;
        Vec3 from = mob.position().add(0, mob.getBbHeight() * 0.8, 0);
        Vec3 to = t.position().add(0, t.getBbHeight() * 0.5, 0);
        beam(level, from, to, PINK);
        level.sendParticles(ParticleTypes.END_ROD, to.x, to.y, to.z, 30, 0.3, 0.3, 0.3, 0.15);
        level.sendParticles(PINK, to.x, to.y, to.z, 30, 0.6, 0.6, 0.6, 0.0);
        level.playSound(null, t.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 1.2F, 0.8F);
        level.playSound(null, mob.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 0.6F, 1.6F);
        t.hurt(level.damageSources().mobAttack(mob), MOONBLAST_DAMAGE);
        t.knockback(0.8D, mob.getX() - t.getX(), mob.getZ() - t.getZ());
    }

    private static void beam(ServerLevel level, Vec3 from, Vec3 to, net.minecraft.core.particles.ParticleOptions p) {
        Vec3 delta = to.subtract(from);
        int steps = Math.max(4, (int) (delta.length() * 3));
        for (int i = 0; i <= steps; i++) {
            Vec3 q = from.add(delta.scale(i / (double) steps));
            level.sendParticles(p, q.x, q.y, q.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    private void recall() {
        if (mob == null) return;
        Mob m = mob;
        mob = null;
        target = null;
        LucarioCompanion.untrack(m.getUUID());
        if (m.level() instanceof ServerLevel level && m.isAlive()) {
            level.sendParticles(PINK, m.getX(), m.getY() + 0.8, m.getZ(), 20, 0.3, 0.5, 0.3, 0.0);
        }
        m.discard();
    }

    private static String pick(RandomSource r, String[] lines) {
        return lines[r.nextInt(lines.length)];
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§d✿ §f" + text));
    }
}
