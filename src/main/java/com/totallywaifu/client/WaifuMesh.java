package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Cuerpo de malla suave: cada parte es un "loft" (anillos redondeados a lo largo de un hueso) con normales suaves,
 * así el cuerpo se ve liso y redondeado, sin cubos. Misma geometría que tools/gen_smooth.py.
 */
public final class WaifuMesh {
    private static final class Seg {
        int bone, sides, rings;
        boolean capTop, capBot, skirt, planar;
        float u0, v0, u1, v1;
        float[][] x, y, z, nx, ny, nz, u, v, front;
        float[] ccx, ccz;
    }

    private final Seg[] segs;

    public WaifuMesh(float[][] table) {
        segs = new Seg[table.length];
        for (int i = 0; i < table.length; i++) segs[i] = build(table[i]);
    }

    private static Seg build(float[] t) {
        Seg s = new Seg();
        s.bone = (int) t[0];
        s.sides = (int) t[1];
        float p = t[2];
        s.planar = t[3] == 1;
        s.u0 = t[4]; s.v0 = t[5]; s.u1 = t[6]; s.v1 = t[7];
        s.capTop = t[8] == 1;
        s.capBot = t[9] == 1;
        boolean mirror = t[10] == 1;
        s.skirt = t[11] == 1;
        s.rings = (int) t[12];
        int n = s.rings, m = s.sides + 1;
        s.x = new float[n][m]; s.y = new float[n][m]; s.z = new float[n][m];
        s.nx = new float[n][m]; s.ny = new float[n][m]; s.nz = new float[n][m];
        s.u = new float[n][m]; s.v = new float[n][m]; s.front = new float[n][m];
        s.ccx = new float[n]; s.ccz = new float[n];
        float e = 2.0F / p;
        for (int i = 0; i < n; i++) {
            int o = 13 + i * 6;
            float ry = t[o], rx = t[o + 1], rz = t[o + 2], cx = t[o + 3], cz = t[o + 4], rv = t[o + 5];
            s.ccx[i] = mirror ? -cx : cx;
            s.ccz[i] = cz;
            for (int k = 0; k < m; k++) {
                double a = 2 * Math.PI * k / s.sides;
                float sa = (float) Math.sin(a), ca = (float) Math.cos(a);
                float x = cx + rx * Math.copySign((float) Math.pow(Math.abs(sa), e), sa);
                float z = cz - rz * Math.copySign((float) Math.pow(Math.abs(ca), e), ca);
                s.x[i][k] = mirror ? -x : x;
                s.y[i][k] = ry;
                s.z[i][k] = z;
                s.u[i][k] = s.u0 + (s.u1 - s.u0) * k / s.sides;
                s.v[i][k] = s.v0 + (s.v1 - s.v0) * rv;
                s.front[i][k] = ca;
            }
        }
        // normales suaves (diferencias centrales), orientadas hacia afuera
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                int kp = (k + 1) % s.sides, km = (k - 1 + s.sides) % s.sides;
                int ip = Math.min(n - 1, i + 1), im = Math.max(0, i - 1);
                float ax = s.x[i][kp] - s.x[i][km], ay = s.y[i][kp] - s.y[i][km], az = s.z[i][kp] - s.z[i][km];
                float lx = s.x[ip][k] - s.x[im][k], ly = s.y[ip][k] - s.y[im][k], lz = s.z[ip][k] - s.z[im][k];
                float cx = ly * az - lz * ay, cy = lz * ax - lx * az, cz = lx * ay - ly * ax;
                float ox = s.x[i][k] - s.ccx[i], oz = s.z[i][k] - s.ccz[i];
                if (cx * ox + cz * oz < 0) { cx = -cx; cy = -cy; cz = -cz; }
                float len = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
                if (len < 1.0E-5F) { cx = 0; cy = i == 0 ? -1 : 1; cz = 0; len = 1; }
                s.nx[i][k] = cx / len; s.ny[i][k] = cy / len; s.nz[i][k] = cz / len;
            }
        }
        return s;
    }

    /**
     * bones: 0 cabeza, 1 cuerpo, 2 brazo der., 3 brazo izq., 4 pierna der., 5 pierna izq.
     * skirtFwd / skirtBack: cuánto se abre la falda por delante / por detrás (al caminar).
     */
    public void render(VertexConsumer vc, PoseStack pose, ModelPart[] bones, int light, int overlay, float skirtFwd, float skirtBack) {
        for (Seg s : segs) {
            pose.pushPose();
            bones[s.bone].translateAndRotate(pose);
            emit(s, vc, pose.last().pose(), pose.last().normal(), light, overlay, skirtFwd, skirtBack);
            pose.popPose();
        }
    }

    private static final float[] PX = new float[4], PY = new float[4], PZ = new float[4], NX = new float[4], NY = new float[4], NZ = new float[4],
            U = new float[4], V = new float[4];

    private static void emit(Seg s, VertexConsumer vc, Matrix4f mat, Matrix3f nm, int light, int overlay, float fwd, float back) {
        int n = s.rings;
        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k < s.sides; k++) {
                load(s, 0, i, k, fwd, back);
                load(s, 1, i + 1, k, fwd, back);
                load(s, 2, i + 1, k + 1, fwd, back);
                load(s, 3, i, k + 1, fwd, back);
                if (s.planar) planarUv();
                quad(vc, mat, nm, light, overlay);
            }
        }
        if (s.capTop) cap(s, 0, -1, vc, mat, nm, light, overlay, fwd, back);
        if (s.capBot) cap(s, n - 1, 1, vc, mat, nm, light, overlay, fwd, back);
    }

    private static void load(Seg s, int j, int i, int k, float fwd, float back) {
        float x = s.x[i][k], y = s.y[i][k], z = s.z[i][k];
        if (s.skirt && i > 0) {
            float t = i / (float) (s.rings - 1), f = s.front[i][k];
            if (f > 0) { z -= fwd * 1.7F * t * f; y -= fwd * 0.7F * t * f; }
            else { z -= back * 1.7F * t * f; y += back * 0.7F * t * f; }
        }
        PX[j] = x; PY[j] = y; PZ[j] = z;
        NX[j] = s.nx[i][k]; NY[j] = s.ny[i][k]; NZ[j] = s.nz[i][k];
        U[j] = s.u[i][k]; V[j] = s.v[i][k];
    }

    /** Cabeza: el frente lleva la cara (proyección plana); el resto, el parche de cabello o de piel. */
    private static void planarUv() {
        float az = 0, ay = 0, ny = 0;
        for (int j = 0; j < 4; j++) { az += PZ[j]; ay += PY[j]; ny += NY[j]; }
        az /= 4; ay /= 4; ny /= 4;
        if (az < -1.6F && ay < -0.6F) {
            for (int j = 0; j < 4; j++) {
                U[j] = (PX[j] + 4.0F) / 8.0F * 0.5F;
                V[j] = (PY[j] + 8.0F) / 8.0F;
            }
        } else {
            float u = ny > 0.5F ? 0.6875F : 0.6875F, v = ny > 0.5F ? 0.625F : 0.25F;
            for (int j = 0; j < 4; j++) { U[j] = u; V[j] = v; }
        }
    }

    private static void cap(Seg s, int i, int dir, VertexConsumer vc, Matrix4f mat, Matrix3f nm, int light, int overlay, float fwd, float back) {
        float cx = 0, cy = 0, cz = 0;
        for (int k = 0; k < s.sides; k++) { cx += s.x[i][k]; cy += s.y[i][k]; cz += s.z[i][k]; }
        cx /= s.sides; cy /= s.sides; cz /= s.sides;
        for (int k = 0; k < s.sides; k++) {
            load(s, 0, i, k, fwd, back);
            load(s, 1, i, k + 1, fwd, back);
            PX[2] = cx; PY[2] = cy; PZ[2] = cz;
            PX[3] = cx; PY[3] = cy; PZ[3] = cz;
            for (int j = 0; j < 4; j++) { NX[j] = 0; NY[j] = dir; NZ[j] = 0; }
            U[2] = U[3] = (U[0] + U[1]) / 2;
            V[2] = V[3] = V[0];
            if (s.planar) planarUv();
            quad(vc, mat, nm, light, overlay);
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f mat, Matrix3f nm, int light, int overlay) {
        for (int j = 0; j < 4; j++) {
            vc.vertex(mat, PX[j] / 16.0F, PY[j] / 16.0F, PZ[j] / 16.0F).color(1.0F, 1.0F, 1.0F, 1.0F).uv(U[j], V[j])
                    .overlayCoords(overlay).uv2(light).normal(nm, NX[j], NY[j], NZ[j]).endVertex();
        }
    }
}
