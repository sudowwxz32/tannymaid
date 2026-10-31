package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static com.tanny.tannymaid.client.TannyHairLocks.*;

/**
 * Cabello de Tanny hecho de mechones curvos (malla suave), no de cajas.
 * Cada mechón nace en el cuero cabelludo, se pega a la forma de la cabeza y luego cae con vuelo,
 * con la punta curvada hacia adentro como un bob de anime. Debajo hay un casquete para que no
 * se vean huecos. Es la misma geometría que tools/hair_mesh.py (con el que se hace la vista previa).
 */
public final class TannyHairMesh {
    private static final int KIND_BOB = 0, KIND_BANG = 1, KIND_LONG = 2, KIND_AHOGE = 3;
    private static final int MAXP = SCALP_STEPS + HANG_STEPS + 2;
    private static final float HAIR_R = 26 / 255F, HAIR_G = 24 / 255F, HAIR_B = 34 / 255F;
    private static final float SHINE_R = 118 / 255F, SHINE_G = 122 / 255F, SHINE_B = 162 / 255F;
    private static final int CAP_TH = 10, CAP_PHI = 28;

    // buffers reutilizados (sin crear objetos cada fotograma)
    private final float[] px = new float[MAXP], py = new float[MAXP], pz = new float[MAXP];
    private final float[] nx = new float[MAXP], ny = new float[MAXP], nz = new float[MAXP], pt = new float[MAXP];
    private final float[][] rx = new float[MAXP][SIDES], ry = new float[MAXP][SIDES], rz = new float[MAXP][SIDES];
    private final float[][] qx = new float[MAXP][SIDES], qy = new float[MAXP][SIDES], qz = new float[MAXP][SIDES];
    private final float[][] col = new float[MAXP][3];
    private int count;

    /**
     * Dibuja el cabello. swayX: hacia atrás (al caminar), swayZ: de lado, bangSway: flequillo,
     * ahogeBounce: rebote del mechón rebelde. helmet: solo dibuja el pelo que sale bajo el casco.
     */
    public void render(VertexConsumer vc, Matrix4f mat, Matrix3f nm, int light, int overlay,
                       float swayX, float swayZ, float bangSway, float ahogeBounce, boolean helmet) {
        if (!helmet) cap(vc, mat, nm, light, overlay);
        for (float[] lock : L) {
            int kind = (int) lock[10];
            if (helmet && (kind == KIND_BANG || kind == KIND_AHOGE || lock[3] <= 0)) continue;
            float sx = swayX, sz = swayZ;
            if (kind == KIND_BANG) { sx = bangSway; sz = swayZ * 0.3F; }
            else if (kind == KIND_LONG) { sx = swayX * 0.7F; }
            else if (kind == KIND_AHOGE) { sx = ahogeBounce; sz = swayZ; }
            path(lock, sx, sz);
            build(lock);
            emit(vc, mat, nm, light, overlay, helmet ? SCALP_STEPS : 0);
        }
    }

    // ------------------------------------------------------------------ geometría (igual que hair_mesh.py)

    private static float seScale(float dx, float dy, float dz) {
        double s = Math.pow(Math.pow(Math.abs(dx), P) + Math.pow(Math.abs(dy), P) + Math.pow(Math.abs(dz), P), 1.0 / P);
        return (float) (R / Math.max(1e-6, s));
    }

    private void path(float[] lock, float swayX, float swayZ) {
        float phi = lock[0], th0 = lock[1], th1 = lock[2], hang = lock[3], flare = lock[4], curl = lock[5], lift = lock[8];
        int kind = (int) lock[10];
        float sp = Mth.sin(phi), cp = Mth.cos(phi);
        float ox = sp, oz = -cp;
        count = 0;
        if (kind == KIND_AHOGE) {
            float x = sp * 0.6F, y = CY - R - 0.2F, z = -cp * 0.6F - 0.8F;
            for (int k = 0; k <= HANG_STEPS; k++) {
                float t = (float) k / HANG_STEPS;
                float ang = -0.25F + t * 2.4F + swayX * t;
                float dx = Mth.sin(swayZ) * 0.3F, dy = -Mth.cos(ang), dz = -Mth.sin(ang);
                if (k > 0) {
                    x += dx * hang / HANG_STEPS;
                    y += dy * hang / HANG_STEPS;
                    z += dz * hang / HANG_STEPS;
                }
                put(x, y, z, 1, 0, 0, t);
            }
            return;
        }
        int total = SCALP_STEPS + (hang > 0 ? HANG_STEPS : 0);
        for (int k = 0; k <= SCALP_STEPS; k++) {
            float th = th0 + (th1 - th0) * k / SCALP_STEPS;
            float dx = Mth.sin(th) * sp, dy = -Mth.cos(th), dz = -Mth.sin(th) * cp;
            float s = seScale(dx, dy, dz) + lift;
            put(dx * s, CY + dy * s, dz * s, dx, dy, dz, (float) k / total);
        }
        if (hang <= 0) return;
        float tx = Mth.cos(th1) * sp, ty = Mth.sin(th1), tz = -Mth.cos(th1) * cp;
        float tl = Mth.sqrt(tx * tx + ty * ty + tz * tz);
        tx /= tl; ty /= tl; tz /= tl;
        float x = px[count - 1], y = py[count - 1], z = pz[count - 1];
        float step = hang / HANG_STEPS;
        float dnx = ox * flare, dny = 1, dnz = oz * flare;
        float dl = Mth.sqrt(dnx * dnx + dny * dny + dnz * dnz);
        dnx /= dl; dny /= dl; dnz /= dl;
        for (int k = 1; k <= HANG_STEPS; k++) {
            float t = (float) k / HANG_STEPS;
            float blend = Math.min(1.0F, t * 2.2F);
            float dx = tx * (1 - blend) + dnx * blend, dy = ty * (1 - blend) + dny * blend, dz = tz * (1 - blend) + dnz * blend;
            float l = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= l; dy /= l; dz /= l;
            float c = curl * (float) Math.pow(Math.max(0.0F, (t - 0.55F) / 0.45F), 1.5);
            dx -= ox * c; dz -= oz * c;
            l = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= l; dy /= l; dz /= l;
            float tw = (float) Math.pow(t, 1.5);
            dx += swayZ * tw; dz += swayX * tw;
            l = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= l; dy /= l; dz /= l;
            x += dx * step; y += dy * step; z += dz * step;
            float hl = Mth.sqrt(x * x + z * z);
            if (hl > 0.01F) put(x, y, z, x / hl, 0, z / hl, (float) (SCALP_STEPS + k) / total);
            else put(x, y, z, ox, 0, oz, (float) (SCALP_STEPS + k) / total);
        }
    }

    private void put(float x, float y, float z, float a, float b, float c, float t) {
        px[count] = x; py[count] = y; pz[count] = z;
        nx[count] = a; ny[count] = b; nz[count] = c; pt[count] = t;
        count++;
    }

    private static float widthAt(float[] lock, float t) {
        float w = lock[6];
        if ((int) lock[10] == KIND_AHOGE) return w * (1.0F - 0.85F * t);
        float root = 0.55F + 0.45F * Math.min(1.0F, t / 0.25F);
        float tip = 1.0F - 0.92F * (float) Math.pow(Math.max(0.0F, (t - 0.45F) / 0.55F), 1.6);
        return w * root * tip;
    }

    /** Secciones elípticas planas a lo largo del mechón + color por anillo. */
    private void build(float[] lock) {
        for (int i = 0; i < count; i++) {
            int a = Math.max(0, i - 1), b = Math.min(count - 1, i + 1);
            float tx = px[b] - px[a], ty = py[b] - py[a], tz = pz[b] - pz[a];
            float tl = Mth.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1e-5F) tl = 1;
            tx /= tl; ty /= tl; tz /= tl;
            float n0 = nx[i], n1 = ny[i], n2 = nz[i];
            // b = t x n ; nn = b x t
            float bx = ty * n2 - tz * n1, by = tz * n0 - tx * n2, bz = tx * n1 - ty * n0;
            float bl = Mth.sqrt(bx * bx + by * by + bz * bz);
            if (bl < 1e-5F) { bx = 1; by = 0; bz = 0; bl = 1; }
            bx /= bl; by /= bl; bz /= bl;
            float mx = by * tz - bz * ty, my = bz * tx - bx * tz, mz = bx * ty - by * tx;
            float ml = Mth.sqrt(mx * mx + my * my + mz * mz);
            if (ml < 1e-5F) ml = 1;
            mx /= ml; my /= ml; mz /= ml;
            if (mx * n0 + my * n1 + mz * n2 < 0) { mx = -mx; my = -my; mz = -mz; }
            float t = pt[i];
            float w = widthAt(lock, t) * 0.5F;
            float k = lock[7] * (0.5F + 0.5F * (1 - t)) * 0.5F;
            for (int j = 0; j < SIDES; j++) {
                float ang = Mth.TWO_PI * j / SIDES;
                float ca = Mth.cos(ang), sa = Mth.sin(ang);
                rx[i][j] = px[i] + bx * ca * w + mx * sa * k;
                ry[i][j] = py[i] + by * ca * w + my * sa * k;
                rz[i][j] = pz[i] + bz * ca * w + mz * sa * k;
                float qx0 = bx * ca + mx * sa * 1.6F, qy0 = by * ca + my * sa * 1.6F, qz0 = bz * ca + mz * sa * 1.6F;
                float ql = Mth.sqrt(qx0 * qx0 + qy0 * qy0 + qz0 * qz0);
                qx[i][j] = qx0 / ql; qy[i][j] = qy0 / ql; qz[i][j] = qz0 / ql;
            }
            color(lock, py[i], ny[i], t, col[i]);
        }
    }

    private static void color(float[] lock, float y, float ny, float t, float[] out) {
        float shade = lock[9];
        float r = HAIR_R * shade, g = HAIR_G * shade, b = HAIR_B * shade;
        int kind = (int) lock[10];
        if (kind != KIND_AHOGE) {
            float centre = -6.55F + (shade - 1.0F) * 5.0F + Mth.sin(lock[0] * 7.0F) * 0.35F;
            float band = (float) Math.pow(Math.max(0.0F, 1.0F - Math.abs(y - centre) / 0.5F), 0.8);
            float facing = Math.max(0.0F, ny * -0.3F + 0.75F);
            float k = band * facing * (kind == KIND_BANG ? 0.95F : 0.8F);
            r += (SHINE_R - r) * k;
            g += (SHINE_G - g) * k;
            b += (SHINE_B - b) * k;
        }
        if (t > 0.8F) {
            float d = 1.0F - (t - 0.8F) * 1.2F;
            r *= d; g *= d; b *= d;
        }
        out[0] = r; out[1] = g; out[2] = b;
    }

    private void emit(VertexConsumer vc, Matrix4f mat, Matrix3f nm, int light, int overlay, int from) {
        for (int i = Math.max(0, from); i < count - 1; i++) {
            float v0 = pt[i], v1 = pt[i + 1];
            float[] c = col[i], c1 = col[i + 1];
            for (int j = 0; j < SIDES; j++) {
                int jj = (j + 1) % SIDES;
                float u0 = (float) j / SIDES, u1 = (float) (j + 1) / SIDES;
                vert(vc, mat, nm, i, j, u0, v0, c, light, overlay);
                vert(vc, mat, nm, i, jj, u1, v0, c, light, overlay);
                vert(vc, mat, nm, i + 1, jj, u1, v1, c1, light, overlay);
                vert(vc, mat, nm, i + 1, j, u0, v1, c1, light, overlay);
            }
        }
    }

    private void vert(VertexConsumer vc, Matrix4f mat, Matrix3f nm, int i, int j, float u, float v, float[] c,
                      int light, int overlay) {
        vc.vertex(mat, rx[i][j] / 16.0F, ry[i][j] / 16.0F, rz[i][j] / 16.0F).color(c[0], c[1], c[2], 1.0F).uv(u, v)
                .overlayCoords(overlay).uv2(light).normal(nm, qx[i][j], qy[i][j], qz[i][j]).endVertex();
    }

    // ------------------------------------------------------------------ casquete

    private static float capMaxTh(float phi) {
        float back = 0.5F - 0.5F * Mth.cos(phi);
        return 1.0F + 1.05F * back;
    }

    private void cap(VertexConsumer vc, Matrix4f mat, Matrix3f nm, int light, int overlay) {
        float c = 20 / 255F, cb = 26 / 255F;
        for (int i = 0; i < CAP_PHI; i++) {
            float p0 = Mth.TWO_PI * i / CAP_PHI, p1 = Mth.TWO_PI * (i + 1) / CAP_PHI;
            float m0 = capMaxTh(p0), m1 = capMaxTh(p1);
            for (int j = 0; j < CAP_TH; j++) {
                float a0 = (float) j / CAP_TH, a1 = (float) (j + 1) / CAP_TH;
                capVert(vc, mat, nm, m0 * a0, p0, 0, a0, c, cb, light, overlay);
                capVert(vc, mat, nm, m1 * a0, p1, 1, a0, c, cb, light, overlay);
                capVert(vc, mat, nm, m1 * a1, p1, 1, a1, c, cb, light, overlay);
                capVert(vc, mat, nm, m0 * a1, p0, 0, a1, c, cb, light, overlay);
            }
        }
    }

    private static void capVert(VertexConsumer vc, Matrix4f mat, Matrix3f nm, float th, float phi, float u, float v,
                                float c, float cb, int light, int overlay) {
        float dx = Mth.sin(th) * Mth.sin(phi), dy = -Mth.cos(th), dz = -Mth.sin(th) * Mth.cos(phi);
        float s = seScale(dx, dy, dz) + 0.05F;
        vc.vertex(mat, dx * s / 16.0F, (CY + dy * s) / 16.0F, dz * s / 16.0F).color(c, c, cb, 1.0F).uv(u, v)
                .overlayCoords(overlay).uv2(light).normal(nm, dx, dy, dz).endVertex();
    }
}
