package com.linterna.client;

import com.linterna.Linterna;
import com.linterna.data.LanternState;
import com.linterna.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/** Rayo verde que sale de la mano del portador hasta lo que sostiene con el anillo. */
@Mod.EventBusSubscriber(modid = Linterna.MODID, value = Dist.CLIENT)
public final class BeamRenderer {
    private BeamRenderer() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float pt = e.getPartialTick();
        Vec3 cam = e.getCamera().getPosition();
        PoseStack pose = e.getPoseStack();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        boolean any = false;
        long now = mc.level.getGameTime();
        for (Player p : mc.level.players()) {
            LanternState s = ClientStates.get(p.getId());
            // ---- la abducción: rayo de luz desde el cielo, ciclón verde y estela de cometa
            if (s.abduct == 0) { START.remove(p.getId()); }
            else {
                long st = START.computeIfAbsent(p.getId(), k -> now);
                float age = now - st + pt;
                Vec3 feet = p.getPosition(pt);
                pose.pushPose();
                pose.translate(-cam.x, -cam.y, -cam.z);
                Matrix4f m = pose.last().pose();
                VertexConsumer vc = buf.getBuffer(RenderType.lightning());
                if (s.abduct <= com.linterna.data.Abduction.ASK) {
                    double sky = Math.max(feet.y + 120, mc.level.getMaxBuildHeight() + 60);
                    float grow = s.abduct == com.linterna.data.Abduction.LIGHT ? Mth.clamp(age / 20F, 0, 1) : 1;
                    Vec3 top = new Vec3(feet.x, sky, feet.z), bottom = new Vec3(feet.x, sky - (sky - feet.y + 0.5) * grow, feet.z);
                    float flick = 0.85F + 0.15F * Mth.sin(age * 1.7F);
                    column(m, vc, top, bottom, cam, 0.35F, 1.0F * flick);
                    column(m, vc, top, bottom, cam, 0.9F, 0.55F * flick);
                    column(m, vc, top, bottom, cam, 1.8F, 0.25F * flick);
                    if (grow >= 1) cyclone(m, vc, feet, cam, age, s.abduct == com.linterna.data.Abduction.LIGHT ? Mth.clamp((age - 20) / 30F, 0.2F, 1) : 1.6F);
                } else if (s.abduct == com.linterna.data.Abduction.FALL) {
                    Vec3 a = feet.add(0, 1, 0);
                    column(m, vc, a.add(0, 16, 0), a, cam, 0.5F, 0.8F);
                    column(m, vc, a.add(0, 10, 0), a, cam, 1.2F, 0.35F);
                }
                pose.popPose();
                any = true;
            }
            if (!s.bearer() || s.held < 0) continue;
            Entity t = mc.level.getEntity(s.held);
            if (t == null) continue;
            float yaw = Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot) * Mth.DEG_TO_RAD;
            Vec3 fwd = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)), right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
            Vec3 base = p.getPosition(pt);
            boolean firstPerson = p == mc.player && mc.options.getCameraType().isFirstPerson();
            Vec3 from = firstPerson ? cam.add(Vec3.directionFromRotation(p.getViewXRot(pt), p.getViewYRot(pt)).scale(0.6))
                    .add(right.scale(0.25)).subtract(0, 0.25, 0)
                    : base.add(0, 1.25, 0).add(right.scale(0.42)).add(fwd.scale(0.3));
            Vec3 to = t.getPosition(pt).add(0, t.getBbHeight() / 2, 0);
            pose.pushPose();
            pose.translate(-cam.x, -cam.y, -cam.z);
            beam(pose.last().pose(), buf.getBuffer(RenderType.lightning()), from, to, cam, p.tickCount + pt);
            pose.popPose();
            any = true;
        }
        if (any) buf.endBatch(RenderType.lightning());
    }

    private static final java.util.Map<Integer, Long> START = new java.util.HashMap<>();

    /** Columna de luz (cinta ancha que mira a la cámara). */
    private static void column(Matrix4f m, VertexConsumer vc, Vec3 a, Vec3 b, Vec3 cam, float w, float br) {
        int seg = 12;
        Vec3 d = b.subtract(a);
        for (int i = 0; i < seg; i++) quad(m, vc, a.add(d.scale(i / (float) seg)), a.add(d.scale((i + 1) / (float) seg)), cam, w, br);
    }

    /** Ciclón: anillos de luz en espiral, cada vez más anchos hacia arriba, girando. */
    private static void cyclone(Matrix4f m, VertexConsumer vc, Vec3 feet, Vec3 cam, float t, float size) {
        for (int k = 0; k < 16; k++) {
            double h = -0.6 + k * 0.32 * size;
            double r = (0.55 + k * 0.14 + 0.08 * Mth.sin(t * 0.3F + k)) * size;
            float spin = t * (0.45F - k * 0.012F) + k * 0.7F;
            int segs = 18;
            Vec3 prev = null;
            for (int j = 0; j <= segs; j++) {
                double a = spin + j * (Math.PI * 1.5 / segs);              // arcos de 3/4 de vuelta (parece un remolino)
                double y = h + j * 0.02 * size;
                Vec3 q = feet.add(Math.cos(a) * r, y, Math.sin(a) * r);
                if (prev != null) quad(m, vc, prev, q, cam, 0.05F + k * 0.004F, 0.35F + 0.45F * (j / (float) segs));
                prev = q;
            }
        }
    }

    private static void beam(Matrix4f m, VertexConsumer vc, Vec3 a, Vec3 b, Vec3 cam, float t) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 0.1) return;
        int seg = Math.max(6, (int) (len * 3));
        for (int strand = 0; strand < 3; strand++) {
            float w = strand == 0 ? 0.07F : 0.035F;
            float br = strand == 0 ? 0.9F : 0.55F;
            Vec3 prev = a;
            for (int i = 1; i <= seg; i++) {
                float k = i / (float) seg;
                Vec3 q = a.add(d.scale(k));
                if (strand > 0 && i < seg) {
                    float wob = Mth.sin(t * 0.6F + i * 1.3F + strand * 2.1F) * 0.12F * Mth.sin(k * Mth.PI);
                    q = q.add(wob, Mth.cos(t * 0.5F + i + strand) * 0.1F * Mth.sin(k * Mth.PI), -wob);
                }
                quad(m, vc, prev, q, cam, w, br);
                prev = q;
            }
        }
    }

    /** Cinta que mira a la cámara entre dos puntos. */
    private static void quad(Matrix4f m, VertexConsumer vc, Vec3 p0, Vec3 p1, Vec3 cam, float w, float br) {
        Vec3 dir = p1.subtract(p0);
        Vec3 view = p0.subtract(cam);
        Vec3 c = dir.cross(view);
        if (c.lengthSqr() < 1.0E-8) return;
        Vec3 side = c.normalize().scale(w);
        float r = 0.3F * br, g = 1.0F * br, bl = 0.4F * br;
        vc.vertex(m, (float) (p0.x - side.x), (float) (p0.y - side.y), (float) (p0.z - side.z)).color(r, g, bl, 1).endVertex();
        vc.vertex(m, (float) (p0.x + side.x), (float) (p0.y + side.y), (float) (p0.z + side.z)).color(r, g, bl, 1).endVertex();
        vc.vertex(m, (float) (p1.x + side.x), (float) (p1.y + side.y), (float) (p1.z + side.z)).color(r, g, bl, 1).endVertex();
        vc.vertex(m, (float) (p1.x - side.x), (float) (p1.y - side.y), (float) (p1.z - side.z)).color(r, g, bl, 1).endVertex();
        // la otra cara (para que se vea desde cualquier lado)
        vc.vertex(m, (float) (p1.x - side.x), (float) (p1.y - side.y), (float) (p1.z - side.z)).color(r, g, bl, 1).endVertex();
        vc.vertex(m, (float) (p1.x + side.x), (float) (p1.y + side.y), (float) (p1.z + side.z)).color(r, g, bl, 1).endVertex();
        vc.vertex(m, (float) (p0.x + side.x), (float) (p0.y + side.y), (float) (p0.z + side.z)).color(r, g, bl, 1).endVertex();
        vc.vertex(m, (float) (p0.x - side.x), (float) (p0.y - side.y), (float) (p0.z - side.z)).color(r, g, bl, 1).endVertex();
    }
}
