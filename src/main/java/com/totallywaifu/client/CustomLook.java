package com.totallywaifu.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.*;

/**
 * Aspecto PERSONALIZADO: carga un modelo de Blockbench (formato Bedrock, .geo.json) desde un paquete de recursos.
 *
 * <pre>
 *   assets/totallywaifu/geo/custom.geo.json                (modelo, UV de caja)
 *   assets/totallywaifu/textures/entity/custom/custom.png  (textura)
 * </pre>
 * Los huesos llamados head, body, rightArm, leftArm, rightLeg y leftLeg se animan (caminar, mirar, brazos);
 * cualquier otro hueso sigue a su padre (o al cuerpo si no tiene padre).
 */
public final class CustomLook implements ResourceManagerReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    public static final ResourceLocation GEO = new ResourceLocation(TotallyWaifu.MODID, "geo/custom.geo.json");
    public static final ResourceLocation TEX = new ResourceLocation(TotallyWaifu.MODID, "textures/entity/custom/custom.png");
    private static final String[][] HUMANOID = {{"head", "head"}, {"hat", "hat"}, {"body", "body"}, {"rightarm", "right_arm"},
            {"leftarm", "left_arm"}, {"rightleg", "right_leg"}, {"leftleg", "left_leg"}};
    private static final float[][] DEFAULT_PIVOT = {{0, 24, 0}, {0, 24, 0}, {0, 24, 0}, {-5, 22, 0}, {5, 22, 0}, {-1.9F, 12, 0}, {1.9F, 12, 0}};

    private static LayerDefinition layer;
    private static int version;

    public static boolean present() {
        return layer != null;
    }

    public static int version() {
        return version;
    }

    @Override
    public void onResourceManagerReload(ResourceManager rm) {
        layer = null;
        version++;
        rm.getResource(GEO).ifPresent(res -> {
            try (Reader r = res.openAsReader()) {
                layer = parse(JsonParser.parseReader(r).getAsJsonObject());
                LOG.info("[Totally Waifu] Modelo personalizado cargado: {}", GEO);
            } catch (Exception ex) {
                LOG.error("[Totally Waifu] No se pudo leer {}: {}", GEO, ex.toString());
            }
        });
    }

    // ------------------------------------------------------------------ lectura del .geo.json
    private record Bone(String name, String parent, float[] pivot, float[] rot, JsonArray cubes) {}

    private static float[] vec(JsonObject o, String k, float[] def) {
        if (!o.has(k)) return def;
        JsonArray a = o.getAsJsonArray(k);
        return new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()};
    }

    private static String humanoid(String name) {
        String n = name.toLowerCase(Locale.ROOT).replace("_", "");
        for (String[] h : HUMANOID) if (h[0].equals(n)) return h[1];
        return null;
    }

    static LayerDefinition parse(JsonObject json) {
        JsonObject geo = json.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject desc = geo.getAsJsonObject("description");
        int tw = desc.has("texture_width") ? desc.get("texture_width").getAsInt() : 64;
        int th = desc.has("texture_height") ? desc.get("texture_height").getAsInt() : 64;
        Map<String, Bone> bones = new LinkedHashMap<>();
        for (JsonElement e : geo.getAsJsonArray("bones")) {
            JsonObject b = e.getAsJsonObject();
            String name = b.get("name").getAsString();
            bones.put(name, new Bone(name, b.has("parent") ? b.get("parent").getAsString() : null, vec(b, "pivot", new float[3]),
                    vec(b, "rotation", new float[3]), b.has("cubes") ? b.getAsJsonArray("cubes") : new JsonArray()));
        }
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        Map<String, PartDefinition> defs = new HashMap<>();
        Map<String, float[]> pivots = new HashMap<>();
        // primero, los huesos humanoides (siempre en la raíz para que HumanoidModel los anime)
        for (int i = 0; i < HUMANOID.length; i++) {
            String java = HUMANOID[i][1];
            Bone b = bones.values().stream().filter(x -> java.equals(humanoid(x.name))).findFirst().orElse(null);
            float[] p = b != null ? b.pivot : DEFAULT_PIVOT[i];
            PartDefinition d = root.addOrReplaceChild(java, b != null ? cubes(b, p) : CubeListBuilder.create(),
                    PartPose.offsetAndRotation(p[0], 24 - p[1], p[2], rad(b, 0, -1), rad(b, 1, -1), rad(b, 2, 1)));
            if (b != null) {
                defs.put(b.name, d);
                pivots.put(b.name, p);
                rotatedCubes(b, d, p);
            }
            defs.putIfAbsent("§" + java, d);
            pivots.putIfAbsent("§" + java, p);
        }
        // después, el resto en orden de jerarquía
        Set<String> done = new HashSet<>(defs.keySet());
        boolean progress = true;
        while (progress) {
            progress = false;
            for (Bone b : bones.values()) {
                if (done.contains(b.name)) continue;
                String parent = b.parent != null && bones.containsKey(b.parent) ? b.parent : "§body";
                if (!defs.containsKey(parent)) continue;
                float[] pp = pivots.get(parent);
                PartDefinition d = defs.get(parent).addOrReplaceChild("b_" + b.name, cubes(b, b.pivot),
                        PartPose.offsetAndRotation(b.pivot[0] - pp[0], -(b.pivot[1] - pp[1]), b.pivot[2] - pp[2],
                                rad(b, 0, -1), rad(b, 1, -1), rad(b, 2, 1)));
                rotatedCubes(b, d, b.pivot);
                defs.put(b.name, d);
                pivots.put(b.name, b.pivot);
                done.add(b.name);
                progress = true;
            }
        }
        return LayerDefinition.create(mesh, tw, th);
    }

    private static float rad(Bone b, int i, int sign) {
        return b == null ? 0 : sign * (float) Math.toRadians(b.rot[i]);
    }

    /** Cubos sin rotación propia, relativos al pivote del hueso. */
    private static CubeListBuilder cubes(Bone b, float[] p) {
        CubeListBuilder c = CubeListBuilder.create();
        for (JsonElement e : b.cubes) {
            JsonObject o = e.getAsJsonObject();
            if (o.has("rotation")) continue;
            addCube(c, o, p);
        }
        return c;
    }

    /** Los cubos con rotación propia van en una sub-pieza con su pivote. */
    private static void rotatedCubes(Bone b, PartDefinition d, float[] p) {
        int i = 0;
        for (JsonElement e : b.cubes) {
            JsonObject o = e.getAsJsonObject();
            if (!o.has("rotation")) continue;
            float[] cp = vec(o, "pivot", p), cr = vec(o, "rotation", new float[3]);
            CubeListBuilder c = CubeListBuilder.create();
            addCube(c, o, cp);
            d.addOrReplaceChild("cube" + (i++), c, PartPose.offsetAndRotation(cp[0] - p[0], -(cp[1] - p[1]), cp[2] - p[2],
                    -(float) Math.toRadians(cr[0]), -(float) Math.toRadians(cr[1]), (float) Math.toRadians(cr[2])));
        }
    }

    private static void addCube(CubeListBuilder c, JsonObject o, float[] p) {
        float[] org = vec(o, "origin", new float[3]), size = vec(o, "size", new float[3]);
        int u = 0, v = 0;
        if (o.has("uv") && o.get("uv").isJsonArray()) {
            JsonArray uv = o.getAsJsonArray("uv");
            u = Math.round(uv.get(0).getAsFloat());
            v = Math.round(uv.get(1).getAsFloat());
        } else if (o.has("uv") && o.get("uv").isJsonObject()) {
            // UV por cara: se aproxima con la cara norte (recomendado: usar "UV de caja" en Blockbench)
            JsonObject n = o.getAsJsonObject("uv").getAsJsonObject("north");
            if (n != null && n.has("uv")) {
                u = Math.round(n.getAsJsonArray("uv").get(0).getAsFloat());
                v = Math.round(n.getAsJsonArray("uv").get(1).getAsFloat());
            }
        }
        float inflate = o.has("inflate") ? o.get("inflate").getAsFloat() : 0;
        boolean mirror = o.has("mirror") && o.get("mirror").getAsBoolean();
        c.texOffs(u, v).mirror(mirror).addBox(org[0] - p[0], -(org[1] + size[1] - p[1]), org[2] - p[2], size[0], size[1], size[2],
                new CubeDeformation(inflate)).mirror(false);
    }

    // ------------------------------------------------------------------ dibujo
    public static class Renderer extends HumanoidMobRenderer<WaifuEntity, Model> {
        public final int builtFor;

        public Renderer(EntityRendererProvider.Context ctx) {
            super(ctx, new Model(layer.bakeRoot()), 0.45F);
            builtFor = version;
        }

        @Override
        public ResourceLocation getTextureLocation(WaifuEntity e) {
            return TEX;
        }
    }

    /** Respeta los pivotes del modelo (HumanoidModel los cambia a los de Steve en cada fotograma). */
    public static class Model extends HumanoidModel<WaifuEntity> {
        private final ModelPart[] parts;
        private final float[][] base;

        public Model(ModelPart root) {
            super(root);
            parts = new ModelPart[]{head, body, rightArm, leftArm, rightLeg, leftLeg};
            base = new float[parts.length][];
            for (int i = 0; i < parts.length; i++) base[i] = new float[]{parts[i].x, parts[i].y, parts[i].z};
        }

        @Override
        public void prepareMobModel(WaifuEntity e, float limbSwing, float amount, float partial) {
            super.prepareMobModel(e, limbSwing, amount, partial);
            rightArmPose = e.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
            leftArmPose = ArmPose.EMPTY;
        }

        @Override
        public void setupAnim(WaifuEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
            super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
            for (int i = 0; i < parts.length; i++) {
                if (i >= 2 && i <= 3 && attackTime > 0) continue;
                parts[i].x = base[i][0];
                parts[i].y = base[i][1] + (crouching && i != 4 && i != 5 ? 3.2F : 0);
                parts[i].z = base[i][2];
            }
            hat.copyFrom(head);
        }
    }
}
