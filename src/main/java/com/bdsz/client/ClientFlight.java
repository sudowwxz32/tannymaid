package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Vuelo dinámico: el cuerpo se inclina hacia adelante según la velocidad (pose Superman),
 * estela de energía, anillos de velocidad, sonido de viento y cámara/campo de visión.
 * También sacude la cámara cuando alguien cerca se transforma o recarga ki.
 */
@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientFlight {
    private static final Map<Integer, float[]> TILT = new HashMap<>();   // {actual, anterior}
    private static float fovBoost;
    private static WindSound wind;

    private ClientFlight() {}

    /** Inclinación del cuerpo (0 = de pie, 1 = horizontal) interpolada. */
    public static float tilt(Player p, float partial) {
        float[] t = TILT.get(p.getId());
        return t == null ? 0 : Mth.lerp(partial, t[1], t[0]);
    }

    private static boolean flying(Player p) {
        SaiyanState s = ClientStates.get(p.getId());
        return s.saiyan && s.flight && p.getAbilities().flying;
    }

    /** Se llama cada tick del cliente. */
    public static void tick(Minecraft mc) {
        RandomSource rnd = mc.level.random;
        for (Player p : mc.level.players()) {
            float[] t = TILT.computeIfAbsent(p.getId(), k -> new float[2]);
            t[1] = t[0];
            double dx = p.getX() - p.xo, dy = p.getY() - p.yo, dz = p.getZ() - p.zo;
            double hs = Math.sqrt(dx * dx + dz * dz);
            float target = 0;
            if (flying(p) && hs > 0.08) {
                target = (float) Mth.clamp((hs - 0.08) / 0.45, 0, 1) * 0.9F;
                target += (float) Mth.clamp(-dy * 1.2, -0.25, 0.3);     // en picada se inclina más
                target = Mth.clamp(target, 0, 1.1F);
            }
            t[0] += (target - t[0]) * 0.18F;

            // estela de vuelo
            if (flying(p) && p.distanceToSqr(mc.player) < 64 * 64) {
                SaiyanState s = ClientStates.get(p.getId());
                Form f = s.activeForm();
                Vector3f col = f == Form.BASE ? (s.activeKaioken() > 0 ? new Vector3f(1, 0.2F, 0.1F) : new Vector3f(0.8F, 0.92F, 1))
                        : new Vector3f(f.ar(), f.ag(), f.ab());
                double speed = Math.sqrt(hs * hs + dy * dy);
                int n = speed > 0.3 ? 3 : 1;
                for (int i = 0; i < n; i++) {
                    level(mc).addParticle(new DustParticleOptions(col, 1.0F + (float) speed),
                            p.getX() - dx * rnd.nextDouble() * 2, p.getY() + 0.9 - dy * rnd.nextDouble(), p.getZ() - dz * rnd.nextDouble() * 2,
                            0, 0, 0);
                }
                if (speed > 0.5 && rnd.nextInt(3) == 0) {
                    level(mc).addParticle(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.9, p.getZ(), -dx * 0.2, -dy * 0.2, -dz * 0.2);
                }
                if (speed > 0.9 && p.tickCount % 12 == 0) {
                    // anillo de velocidad
                    Vec3 dir = new Vec3(dx, dy, dz).normalize();
                    Vec3 a = dir.cross(new Vec3(0, 1, 0));
                    if (a.lengthSqr() < 1e-4) a = new Vec3(1, 0, 0);
                    a = a.normalize();
                    Vec3 b = dir.cross(a).normalize();
                    for (int i = 0; i < 20; i++) {
                        double ang = i * Math.PI * 2 / 20;
                        Vec3 off = a.scale(Math.cos(ang) * 1.3).add(b.scale(Math.sin(ang) * 1.3));
                        level(mc).addParticle(ParticleTypes.END_ROD, p.getX() + off.x, p.getY() + 0.9 + off.y, p.getZ() + off.z, 0, 0, 0);
                    }
                }
            }
        }
        // sonido de viento para el propio jugador
        if (flying(mc.player) && (wind == null || wind.isStopped())) {
            wind = new WindSound(mc.player);
            mc.getSoundManager().play(wind);
        }
    }

    private static net.minecraft.client.multiplayer.ClientLevel level(Minecraft mc) {
        return mc.level;
    }

    @SubscribeEvent
    public static void preRender(RenderPlayerEvent.Pre e) {
        Player p = e.getEntity();
        float tilt = tilt(p, e.getPartialTick());
        PoseStack pose = e.getPoseStack();
        pose.pushPose();
        if (tilt > 0.01F && flying(p)) {
            float yaw = Mth.rotLerp(e.getPartialTick(), p.yBodyRotO, p.yBodyRot);
            pose.translate(0, 0.9, 0);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            pose.mulPose(Axis.XP.rotationDegrees(-tilt * 80.0F));
            pose.mulPose(Axis.YP.rotationDegrees(-(180.0F - yaw)));
            pose.translate(0, -0.9, 0);
        }
        // Fase 4 / Ultra Ego: más alto y robusto
        SaiyanState s = ClientStates.get(p.getId());
        if (s.saiyan && (s.transformed || s.powering > 0)) {
            Form f = s.visibleForm();
            float h = f.height();
            if (h != 1.0F) {
                float grow = s.powering > 0 ? 1.0F - (float) s.powering / Math.max(1, s.poweringTotal) : 1.0F;
                float k = 1.0F + (h - 1.0F) * grow;
                pose.scale(k, k, k);
            }
        }
    }

    @SubscribeEvent
    public static void postRender(RenderPlayerEvent.Post e) {
        e.getPoseStack().popPose();
    }

    /** Temblor de cámara al transformarse/recargar (tú o alguien cerca). */
    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        float amount = 0;
        for (Player p : mc.level.players()) {
            double d = p.distanceTo(mc.player);
            if (d > 32) continue;
            SaiyanState s = ClientStates.get(p.getId());
            if (!s.saiyan) continue;
            if (com.bdsz.data.Form.byId(s.selected).calm() && s.kaioken == 0 && !s.ozaru) continue;   // Ultra Instinto: calma total
            float near = (float) (1.0 - d / 32.0);
            long oz = ClientStates.ticksSinceOzaru(p.getId());
            if (oz < 60) amount = Math.max(amount, near * (60 - oz) / 60.0F * 3.0F);
            if (s.ozaru && p.onGround() && p.getDeltaMovement().horizontalDistanceSqr() > 0.004) amount = Math.max(amount, near * 0.5F);
            if (s.powering > 0) {
                float progress = 1.0F - (float) s.powering / Math.max(1, s.poweringTotal);
                amount = Math.max(amount, near * (0.4F + progress * 1.6F) * (0.6F + com.bdsz.data.Form.byId(s.selected).drama() * 1.8F));
            }
            long since = ClientStates.ticksSinceTransform(p.getId());
            if (since < 20) amount = Math.max(amount, near * (20 - since) / 20.0F * (2.5F + s.selected * 0.3F));
            if (s.charging && p == mc.player) amount = Math.max(amount, 0.35F);
        }
        if (amount > 0.01F) {
            double t = mc.level.getGameTime() + e.getPartialTick();
            e.setYaw(e.getYaw() + (float) Math.sin(t * 2.3) * amount * 0.6F);
            e.setPitch(e.getPitch() + (float) Math.cos(t * 2.9) * amount * 0.6F);
            e.setRoll(e.getRoll() + (float) Math.sin(t * 3.7) * amount * 0.8F);
        }
        // ligera inclinación al girar volando rápido
        if (flying(mc.player)) {
            float tl = tilt(mc.player, (float) e.getPartialTick());
            float yawDelta = Mth.wrapDegrees(mc.player.getYRot() - mc.player.yRotO);
            e.setRoll(e.getRoll() + Mth.clamp(yawDelta * 0.6F, -12, 12) * tl);
        }
    }

    @SubscribeEvent
    public static void fov(ViewportEvent.ComputeFov e) {
        Minecraft mc = Minecraft.getInstance();
        float target = 0;
        if (mc.player != null && flying(mc.player)) {
            Vec3 v = mc.player.getDeltaMovement();
            target = (float) Mth.clamp((v.length() - 0.15) * 30.0, 0, 22);
        }
        fovBoost += (target - fovBoost) * 0.1F;
        if (fovBoost > 0.05F) e.setFOV(e.getFOV() + fovBoost);
    }

    /** Viento que sube de volumen con la velocidad. */
    static class WindSound extends AbstractTickableSoundInstance {
        private final Player player;

        WindSound(Player player) {
            super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, net.minecraft.client.resources.sounds.SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.0F;
        }

        @Override
        public void tick() {
            if (player.isRemoved() || !flying(player)) {
                stop();
                return;
            }
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            double v = player.getDeltaMovement().length();
            volume = (float) Mth.clamp((v - 0.1) * 1.4, 0, 1.0);
            pitch = 0.8F + (float) Mth.clamp(v * 0.5, 0, 0.6);
        }
    }
}
