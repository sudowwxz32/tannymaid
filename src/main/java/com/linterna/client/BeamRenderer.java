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
        for (Player p : mc.level.players()) {
            LanternState s = ClientStates.get(p.getId());
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
        Vec3 side = dir.cross(view).normalize().scale(w);
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
