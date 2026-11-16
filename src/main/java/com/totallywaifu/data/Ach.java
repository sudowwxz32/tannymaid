package com.totallywaifu.data;

import com.totallywaifu.TotallyWaifu;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** Logros del mod (advancements de verdad, con su aviso) y fuegos artificiales de celebración. */
public final class Ach {
    private Ach() {}

    public static void grant(ServerPlayer p, String id) {
        Advancement a = p.server.getAdvancements().getAdvancement(new ResourceLocation(TotallyWaifu.MODID, id));
        if (a == null) return;
        AdvancementProgress pr = p.getAdvancements().getOrStartProgress(a);
        if (pr.isDone()) return;
        for (String c : pr.getRemainingCriteria()) p.getAdvancements().award(a, c);
    }

    public static void firework(ServerLevel lv, Vec3 at, int i) {
        ItemStack st = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag fw = st.getOrCreateTagElement("Fireworks");
        fw.putByte("Flight", (byte) 1);
        ListTag ex = new ListTag();
        CompoundTag e = new CompoundTag();
        e.putByte("Type", (byte) (i % 2 == 0 ? 1 : 4));
        e.putIntArray("Colors", new int[]{0xFF7AC8, 0xFFD24A, 0xB57AFF, 0x6AE6E0}[i % 4] == 0 ? new int[]{0xFF7AC8} : new int[]{new int[]{0xFF7AC8, 0xFFD24A, 0xB57AFF, 0x6AE6E0}[i % 4], 0xFFFFFF});
        e.putIntArray("FadeColors", new int[]{0xFFC0E0});
        e.putBoolean("Flicker", true);
        e.putBoolean("Trail", true);
        ex.add(e);
        fw.put("Explosions", ex);
        lv.addFreshEntity(new FireworkRocketEntity(lv, at.x, at.y, at.z, st));
    }
}
