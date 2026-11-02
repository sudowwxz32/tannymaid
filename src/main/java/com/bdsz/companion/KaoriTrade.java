package com.bdsz.companion;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.*;

/**
 * Kaori negocia con los aldeanos: te consigue descuentos, te dice qué ofertas valen la pena y qué piden ellos,
 * vende por ti lo que tenga en su mochila, compra lo mejor con sus esmeraldas y sale a conseguir lo que falta.
 */
public final class KaoriTrade {
    private KaoriTrade() {}

    private static final int RANGE = 32;

    private static List<Villager> villagers(KaoriEntity k) {
        List<Villager> list = k.level().getEntitiesOfClass(Villager.class, k.getBoundingBox().inflate(RANGE),
                v -> v.isAlive() && !v.isBaby() && v.getVillagerData().getProfession() != VillagerProfession.NONE
                        && v.getVillagerData().getProfession() != VillagerProfession.NITWIT);
        list.sort(Comparator.comparingDouble(k::distanceToSqr));
        return list;
    }

    private static String prof(Villager v) {
        String id = v.getVillagerData().getProfession().name();
        return switch (id) {
            case "armorer" -> "Armero";
            case "butcher" -> "Carnicero";
            case "cartographer" -> "Cartógrafo";
            case "cleric" -> "Clérigo";
            case "farmer" -> "Granjero";
            case "fisherman" -> "Pescador";
            case "fletcher" -> "Flechero";
            case "leatherworker" -> "Peletero";
            case "librarian" -> "Bibliotecario";
            case "mason" -> "Albañil";
            case "shepherd" -> "Pastor";
            case "toolsmith" -> "Herrero de herramientas";
            case "weaponsmith" -> "Herrero de armas";
            default -> id;
        };
    }

    private static String name(ItemStack s) {
        return s.getCount() + " " + s.getHoverName().getString();
    }

    /** ¿Es de lo mejor que se puede comprar? (libros encantados, diamante, objetos encantados, etc.) */
    private static int value(ItemStack s) {
        Item i = s.getItem();
        if (i == Items.ENCHANTED_BOOK) return 10;
        if (s.isEnchanted()) return 9;
        String id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(i).getPath();
        if (id.startsWith("diamond_")) return 8;
        if (i == Items.GOLDEN_CARROT || i == Items.EXPERIENCE_BOTTLE || i == Items.ENDER_PEARL || i == Items.NAME_TAG) return 6;
        if (id.startsWith("iron_") || i == Items.BELL || i == Items.SADDLE) return 4;
        return 0;
    }

    private static int count(net.minecraft.world.Container c, Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    private static int countPlayer(Player p, Item item) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) if (s.is(item)) n += s.getCount();
        return n;
    }

    private static boolean take(net.minecraft.world.Container c, Item item, int amount) {
        if (count(c, item) < amount) return false;
        for (int i = 0; i < c.getContainerSize() && amount > 0; i++) {
            ItemStack s = c.getItem(i);
            if (!s.is(item)) continue;
            int t = Math.min(amount, s.getCount());
            s.shrink(t);
            amount -= t;
        }
        c.setChanged();
        return true;
    }

    /** Precio con el descuento de Kaori (30 % menos en esmeraldas). */
    private static int discounted(ItemStack cost) {
        return cost.is(Items.EMERALD) ? Math.max(1, (int) Math.floor(cost.getCount() * 0.7)) : cost.getCount();
    }

    // ------------------------------------------------------------------ informe

    public static void report(ServerPlayer p, KaoriEntity k) {
        List<Villager> vs = villagers(k);
        if (vs.isEmpty()) {
            k.say(p, "No veo aldeanos con oficio cerca. Llévame a una aldea y negocio por ti.");
            return;
        }
        k.say(p, "Revisé a " + vs.size() + " aldeanos. Esto es lo que vale la pena (precios con mi descuento):");
        record Deal(int score, String text) {}
        List<Deal> best = new ArrayList<>();
        Map<Item, Integer> wanted = new LinkedHashMap<>();
        for (Villager v : vs) {
            int m = (int) Math.round(Math.sqrt(v.distanceToSqr(p)));
            for (MerchantOffer o : v.getOffers()) {
                if (o.isOutOfStock()) continue;
                ItemStack res = o.getResult(), a = o.getBaseCostA(), b = o.getCostB();
                int sc = value(res);
                if (sc > 0) {
                    String cost = discounted(a) + " " + a.getHoverName().getString() + (b.isEmpty() ? "" : " + " + name(b));
                    StringBuilder en = new StringBuilder();
                    net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(res).forEach((ench, lvl) ->
                            en.append(en.length() == 0 ? " (" : ", ").append(ench.getFullname(lvl).getString()));
                    String ench = en.length() == 0 ? "" : en + ")";
                    best.add(new Deal(sc * 100 - discounted(a), "§e" + prof(v) + " §7(" + m + " m)§f: " + name(res) + ench + " §7por§f " + cost));
                }
                if (res.is(Items.EMERALD)) wanted.merge(a.getItem(), a.getCount(), Integer::sum);
            }
        }
        best.sort(Comparator.comparingInt(Deal::score).reversed());
        for (int i = 0; i < Math.min(6, best.size()); i++) p.sendSystemMessage(Component.literal("  §6★ " + best.get(i).text()));
        if (best.isEmpty()) p.sendSystemMessage(Component.literal("  §7Nada especial por ahora. Sube de nivel a los aldeanos comerciando."));
        if (!wanted.isEmpty()) {
            StringBuilder sb = new StringBuilder("§aLo que ellos piden (y te pagan en esmeraldas):");
            int n = 0;
            for (Map.Entry<Item, Integer> e : wanted.entrySet()) {
                if (n++ >= 6) break;
                ItemStack s = new ItemStack(e.getKey());
                int have = countPlayer(p, e.getKey()) + count(k.getBag(), e.getKey());
                sb.append("\n  §f• ").append(e.getValue()).append(" ").append(s.getHoverName().getString())
                        .append(have >= e.getValue() ? " §a(tienen ✔ " + have + ")" : " §c(faltan " + (e.getValue() - have) + ")");
            }
            p.sendSystemMessage(Component.literal(sb.toString()));
            p.sendSystemMessage(Component.literal("§7Ponlo en mi mochila y uso «Comerciar por ti», o pulsa «Conseguir lo que falta»."));
        }
        k.swing(InteractionHand.MAIN_HAND);
    }

    // ------------------------------------------------------------------ comerciar por ti

    public static void tradeForYou(ServerPlayer p, KaoriEntity k) {
        List<Villager> vs = villagers(k);
        if (vs.isEmpty()) {
            k.say(p, "No hay aldeanos con oficio cerca.");
            return;
        }
        int sold = 0, emeralds = 0, bought = 0;
        List<String> got = new ArrayList<>();
        // 1) vende lo que le piden (a cambio de esmeraldas)
        for (Villager v : vs) {
            for (MerchantOffer o : v.getOffers()) {
                if (!o.getResult().is(Items.EMERALD)) continue;
                ItemStack a = o.getBaseCostA(), b = o.getCostB();
                for (int g = 0; g < 12 && !o.isOutOfStock(); g++) {
                    if (count(k.getBag(), a.getItem()) < a.getCount() || (!b.isEmpty() && count(k.getBag(), b.getItem()) < b.getCount())) break;
                    take(k.getBag(), a.getItem(), a.getCount());
                    if (!b.isEmpty()) take(k.getBag(), b.getItem(), b.getCount());
                    k.storeOrDrop(o.getResult().copy());
                    v.notifyTrade(o);
                    sold++;
                    emeralds += o.getResult().getCount();
                }
            }
        }
        // 2) con sus esmeraldas compra lo mejor (libros encantados, diamante...) con su descuento
        for (Villager v : vs) {
            for (MerchantOffer o : v.getOffers()) {
                if (o.isOutOfStock() || value(o.getResult()) < 6) continue;
                ItemStack a = o.getBaseCostA(), b = o.getCostB();
                if (!a.is(Items.EMERALD)) continue;
                int price = discounted(a);
                if (count(k.getBag(), Items.EMERALD) < price || (!b.isEmpty() && count(k.getBag(), b.getItem()) < b.getCount())) continue;
                take(k.getBag(), Items.EMERALD, price);
                if (!b.isEmpty()) take(k.getBag(), b.getItem(), b.getCount());
                ItemStack res = o.getResult().copy();
                got.add(res.getHoverName().getString());
                k.storeOrDrop(res);
                v.notifyTrade(o);
                bought++;
            }
        }
        k.swing(InteractionHand.MAIN_HAND);
        if (sold == 0 && bought == 0) {
            k.say(p, "No pude cerrar ningún trato: me faltan las cosas que piden o esmeraldas. Revisa «Ofertas».");
            return;
        }
        k.level().playSound(null, k.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
        k.say(p, "Negocios cerrados: " + sold + " ventas (" + emeralds + " esmeraldas)" + (bought > 0 ? " y compré " + String.join(", ", got) : "")
                + ". Todo está en mi mochila. Soy buena negociando, lo sé.");
    }

    // ------------------------------------------------------------------ conseguir lo que falta

    public static void fetchMissing(ServerPlayer p, KaoriEntity k) {
        Map<Item, Integer> wanted = new LinkedHashMap<>();
        for (Villager v : villagers(k)) {
            for (MerchantOffer o : v.getOffers()) {
                if (!o.isOutOfStock() && o.getResult().is(Items.EMERALD)) wanted.merge(o.getBaseCostA().getItem(), o.getBaseCostA().getCount(), Integer::sum);
            }
        }
        for (Map.Entry<Item, Integer> e : wanted.entrySet()) {
            Item it = e.getKey();
            int missing = e.getValue() - countPlayer(p, it) - count(k.getBag(), it);
            if (missing <= 0) continue;
            KaoriResource r = null;
            if (it == Items.COAL) r = KaoriResource.CARBON;
            else if (it == Items.STICK || new ItemStack(it).is(net.minecraft.tags.ItemTags.LOGS)) r = KaoriResource.MADERA;
            else if (it == Items.STONE || it == Items.COBBLESTONE || it == Items.ANDESITE || it == Items.DIORITE || it == Items.GRANITE) r = KaoriResource.PIEDRA;
            else if (it == Items.IRON_INGOT) r = KaoriResource.HIERRO;
            else if (it == Items.CLAY_BALL || it == Items.SAND) r = KaoriResource.ARENA;
            if (r != null) {
                k.say(p, "Les falta " + new ItemStack(it).getHoverName().getString() + ". Voy a conseguirlo.");
                k.startGather(p, r, Math.min(64, Math.max(8, missing)));
                return;
            }
            if (it == Items.WHEAT || it == Items.CARROT || it == Items.POTATO || it == Items.BEETROOT || it == Items.PUMPKIN || it == Items.MELON) {
                k.setMode(KaoriMode.FARM);
                k.say(p, "Piden " + new ItemStack(it).getHoverName().getString() + ". Me pongo con la agricultura: dame semillas y una azada.");
                return;
            }
            if (it == Items.STRING || it == Items.ROTTEN_FLESH || it == Items.FEATHER || it == Items.LEATHER || it == Items.CHICKEN
                    || it == Items.BEEF || it == Items.PORKCHOP || it == Items.MUTTON || it == Items.RABBIT) {
                k.setMode(KaoriMode.DEFEND);
                k.setCollecting(true);
                k.say(p, "Piden " + new ItemStack(it).getHoverName().getString() + ". Saldré de caza contigo y recojo lo que caiga.");
                return;
            }
        }
        k.say(p, "Lo que piden no lo puedo conseguir yo sola (o ya lo tienes todo). Revisa «Ofertas».");
    }

    // ------------------------------------------------------------------ descuentos al comerciar tú

    /** Cuando hablas con un aldeano y Kaori está cerca, ella negocia: precios rebajados y mejor reputación. */
    public static void onInteract(ServerPlayer p, Entity target) {
        if (!(target instanceof Villager v) || v.isBaby()) return;
        net.minecraft.nbt.CompoundTag d = KaoriActions.data(p);
        if (!d.hasUUID("KaoriUUID") || !(p.level() instanceof ServerLevel sl)) return;
        Entity e = sl.getEntity(d.getUUID("KaoriUUID"));
        if (!(e instanceof KaoriEntity k) || k.isWeakened() || k.distanceToSqr(v) > 16 * 16) return;
        p.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 100, 2, true, false));
        long day = sl.getDayTime() / 24000;
        String key = "KaoriGossip" + v.getUUID();
        if (d.getLong(key) != day + 1) {
            d.putLong(key, day + 1);
            v.getGossips().add(p.getUUID(), GossipType.MINOR_POSITIVE, 25);
            if (k.getRandom().nextInt(3) == 0) k.say(p, k.pick("Déjame a mí: este aldeano te dará buen precio.", "Sonríe y no digas nada. Yo negocio.",
                    "Con mi encanto, todo te sale más barato."));
        }
    }
}
