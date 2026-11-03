package com.bdsz.companion;

import com.bdsz.network.Net;
import com.bdsz.registry.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

/** Órdenes del menú de Kaori (las ejecuta el servidor) y los datos de sus regalos guardados en el jugador. */
public final class KaoriActions {
    private KaoriActions() {}

    public static final int SENZU = 0, RICE = 1, HUT = 2, FOLLOW = 3, STAY = 4, TP = 5, DEFEND = 6, FARM = 7, COLLECT = 8,
            SORT = 9, CRAFT = 10, GATHER = 11, STOP = 12, INVENTORY = 13, STATUS = 14, FORM = 15,
            SHIELD_FEED = 16, SHIELD_POWER = 17, TRADE_INFO = 18, TRADE_AUTO = 19, TRADE_GET = 20;

    public static final long SENZU_WAIT = 48000L;   // 2 días de Minecraft
    public static final long RICE_WAIT = 24000L;    // 1 día de Minecraft
    public static final String GIFT_TAG = "KaoriGift";

    // ------------------------------------------------------------------ datos en el jugador

    public static CompoundTag data(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static boolean isGift(ItemStack s) {
        return s.is(ModRegistry.SENZU_BEAN.get()) && s.hasTag() && s.getTag().getBoolean(GIFT_TAG);
    }

    private static int giftsInInventory(Player p) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) if (isGift(s)) n += s.getCount();
        for (ItemStack s : p.getInventory().offhand) if (isGift(s)) n += s.getCount();
        return n;
    }

    /** Te comiste una semilla de Kaori: cuando te terminas las tres empieza la espera de 2 días. */
    public static void onGiftEaten(ServerPlayer p) {
        CompoundTag d = data(p);
        int left = Math.max(0, d.getInt("KaoriSenzuLeft") - 1);
        d.putInt("KaoriSenzuLeft", left);
        if (left == 0) {
            d.putLong("KaoriSenzuAt", p.level().getGameTime() + SENZU_WAIT);
            p.sendSystemMessage(Component.literal("§6[Kaori] §fTe terminaste mis semillas. Tch... en 2 días te consigo más. ¡No las desperdicies!"));
        }
    }

    public static long senzuIn(ServerPlayer p) {
        return Math.max(0, data(p).getLong("KaoriSenzuAt") - p.level().getGameTime());
    }

    public static long riceIn(ServerPlayer p) {
        return Math.max(0, data(p).getLong("KaoriRiceAt") - p.level().getGameTime());
    }

    /** Las semillas que perdiste también cuentan como usadas (para que el temporizador no se quede trabado). */
    private static int syncSeeds(ServerPlayer p) {
        CompoundTag d = data(p);
        int left = d.getInt("KaoriSenzuLeft");
        int have = giftsInInventory(p);
        if (left > have) {
            left = have;
            d.putInt("KaoriSenzuLeft", left);
            if (left == 0 && d.getLong("KaoriSenzuAt") <= p.level().getGameTime()) d.putLong("KaoriSenzuAt", p.level().getGameTime() + SENZU_WAIT);
        }
        return left;
    }

    public static String time(long ticks) {
        long days = ticks / 24000, hours = (ticks % 24000) / 1000, mins = (ticks + 1199) / 1200;
        String mc = days > 0 ? days + (days == 1 ? " día" : " días") + (hours > 0 ? " y " + hours + " h" : "") : Math.max(1, hours) + " h";
        return mc + " de Minecraft (≈ " + mins + " min)";
    }

    // ------------------------------------------------------------------ menú

    public static void openMenu(ServerPlayer p, KaoriEntity k) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new KaoriScreenPacket(k.getId(), k.getMode().ordinal(), k.isCollecting(),
                syncSeeds(p), senzuIn(p), riceIn(p), k.hutPos != null, k.isGathering(), k.getForm(), KaoriEntity.bond(p), k.hutType));
    }

    public static void handle(ServerPlayer p, int entityId, int action, int arg, String text) {
        Entity e = p.level().getEntity(entityId);
        if (!(e instanceof KaoriEntity k) || !k.isOwner(p) || k.distanceToSqr(p) > 64 * 64) return;
        if (k.isWeakened() && action != STATUS && action != INVENTORY && action != TP) {
            k.say(p, "Ahora no... estoy descansando. Dame un momento.");
            return;
        }
        switch (action) {
            case SENZU -> senzu(p, k);
            case RICE -> rice(p, k);
            case HUT -> HutBuilder.toggle(p, k, arg);
            case FOLLOW -> { k.setMode(KaoriMode.FOLLOW); k.say(p, k.pick("Voy contigo. Intenta no perderte, ¿sí?", "Está bien, te sigo. Camina tú delante.")); }
            case STAY -> { k.setMode(KaoriMode.STAY); k.say(p, "Me quedo aquí. No tardes, ¿eh?"); }
            case TP -> { k.teleportToOwner(p); k.say(p, "¿Me llamabas? Aquí estoy."); }
            case DEFEND -> { k.setMode(KaoriMode.DEFEND); k.say(p, "Defensa activa. Que se atrevan a acercarse."); }
            case FARM -> {
                k.setMode(KaoriMode.FARM);
                k.say(p, "Agricultura, ¿eh? Cosecho, siembro y aro la tierra junto al agua. Ponme semillas y una azada en la mochila, por favor.");
            }
            case COLLECT -> {
                k.setCollecting(!k.isCollecting());
                k.say(p, k.isCollecting() ? "Recogeré lo que haya en el suelo." : "Ya no recojo cosas del suelo.");
            }
            case SORT -> {
                int n = ChestOrganizer.organize((ServerLevel) k.level(), k.blockPosition(), 8);
                k.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                k.say(p, n == 0 ? "No veo cofres cerca para ordenar." : "Listo, ordené " + n + (n == 1 ? " cofre" : " cofres") + ". Mira qué limpio quedó todo. De nada.");
            }
            case CRAFT -> KaoriCrafting.craft(k, p, text);
            case GATHER -> {
                int amount = 16;
                try { amount = Integer.parseInt(text.trim()); } catch (NumberFormatException ignored) { }
                k.startGather(p, KaoriResource.byId(arg), Math.max(1, Math.min(64, amount)));
            }
            case STOP -> {
                if (k.isGathering()) k.finishGather("stop");
                else k.say(p, "No estoy haciendo nada... todavía.");
            }
            case INVENTORY -> k.openInventory(p);
            case SHIELD_FEED, SHIELD_POWER -> {
                com.bdsz.block.ShieldGeneratorBlockEntity g = k.nearestGenerator(24);
                if (g == null) { k.say(p, "No veo ningún Generador Protector cerca (24 bloques)."); break; }
                if (!g.isOwner(p)) { k.say(p, "Ese generador no es tuyo. No pienso tocarlo."); break; }
                k.getNavigation().moveTo(g.getBlockPos().getX() + 0.5, g.getBlockPos().getY(), g.getBlockPos().getZ() + 0.5, 1.1);
                if (action == SHIELD_FEED) {
                    int n = k.feedGenerator(g, 64);
                    k.say(p, n > 0 ? "Le puse " + n + " lingotes de cobre al generador. " + g.status()
                            : "No tengo lingotes de cobre en la mochila (o el generador ya está lleno). " + g.status());
                } else {
                    boolean on = arg == 1;
                    if (on && g.fuel() <= 0 && k.feedGenerator(g, 2) == 0) { k.say(p, "No tiene energía y no tengo cobre. Dame lingotes de cobre."); break; }
                    g.power(on);
                    k.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    k.say(p, (on ? "Domo encendido. Aquí no entra nadie. " : "Generador apagado. ") + g.status());
                }
            }
            case TRADE_INFO -> KaoriTrade.report(p, k);
            case TRADE_AUTO -> KaoriTrade.tradeForYou(p, k);
            case TRADE_GET -> KaoriTrade.fetchMissing(p, k);
            case FORM -> {
                int f = Math.max(0, Math.min(4, arg));
                if (f == 3) f = 2;
                if (f == k.getForm()) { k.say(p, f == 0 ? "Ya estoy en mi estado base." : "Ya estoy transformada, ¿no lo ves?"); break; }
                k.setForm(f, true);
                k.say(p, switch (f) {
                    case 1 -> "¡HAAAAA! ...Super Saiyajin. Sí, el dorado me queda perfecto.";
                    case 2 -> "¡¡HAAAAAAAA!! Super Saiyajin 2. Ahora sí, que tiemblen.";
                    case 4 -> "...Super Saiyajin Dios. Siente esta calma. Es poder divino, no lo olvides.";
                    default -> "Vuelvo a mi estado base. Tampoco hay que presumir todo el tiempo.";
                });
            }
            case STATUS -> k.say(p, String.format("Vida %.0f/%.0f · Modo: %s · Recoger objetos: %s%s", k.getHealth(), k.getMaxHealth(),
                    k.getMode().display, k.isCollecting() ? "sí" : "no", k.isWeakened() ? " · descansando" : ""));
            default -> { }
        }
    }

    private static void senzu(ServerPlayer p, KaoriEntity k) {
        int left = syncSeeds(p);
        if (left > 0) {
            k.say(p, "Todavía tienes " + left + (left == 1 ? " semilla" : " semillas") + " mías. Cómetelas primero, no soy una tienda.");
            return;
        }
        long in = senzuIn(p);
        if (in > 0) {
            k.say(p, "Las semillas no crecen en los árboles... bueno, sí, pero tardan. Vuelve en " + time(in) + ".");
            return;
        }
        ItemStack s = new ItemStack(ModRegistry.SENZU_BEAN.get(), 3);
        s.getOrCreateTag().putBoolean(GIFT_TAG, true);
        s.setHoverName(Component.literal("§aSemilla del Ermitaño de Kaori"));
        if (!p.getInventory().add(s)) p.drop(s, false);
        data(p).putInt("KaoriSenzuLeft", 3);
        data(p).putLong("KaoriSenzuAt", 0);
        k.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
        k.say(p, "Toma, tres Semillas del Ermitaño. Te curan todo. Cuando te las termines, tendrás que esperar 2 días para más.");
    }

    private static void rice(ServerPlayer p, KaoriEntity k) {
        long in = riceIn(p);
        if (in > 0) {
            k.say(p, "¿Ya te comiste todo el arroz? ¡Glotón! Cocino otra vez en " + time(in) + ".");
            return;
        }
        ItemStack s = new ItemStack(ModRegistry.RICE_BOWL.get(), 20);
        if (!p.getInventory().add(s)) p.drop(s, false);
        data(p).putLong("KaoriRiceAt", p.level().getGameTime() + RICE_WAIT);
        p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
        k.say(p, k.pick("Aquí tienes 20 tazones de arroz. Los cociné yo, así que más te vale que te gusten.",
                "Veinte tazones de arroz recién hechos. Te recuperan vida, ki y hambre. No hace falta que me lo agradezcas... aunque deberías."));
    }

    // ------------------------------------------------------------------ cápsula para invocarla

    /** Usa la Cápsula de Kaori: la invoca (o la trae si ya la tienes). */
    /** Tu Kaori si está cargada en algún mundo. */
    @javax.annotation.Nullable
    public static KaoriEntity findKaori(ServerPlayer p) {
        CompoundTag d = data(p);
        if (!d.hasUUID("KaoriUUID")) return null;
        UUID id = d.getUUID("KaoriUUID");
        for (ServerLevel l : p.server.getAllLevels()) {
            if (l.getEntity(id) instanceof KaoriEntity k && k.isAlive()) return k;
        }
        return null;
    }

    public static void summon(ServerPlayer p) {
        CompoundTag d = data(p);
        ServerLevel lv = p.serverLevel();
        KaoriEntity old = findKaori(p);
        if (old != null) {
            old.teleportToOwner(p);
            old.say(p, "¿Otra vez me llamas? Está bien, aquí estoy. Y sí, solo hay una Kaori.");
            return;
        }
        KaoriEntity k = ModRegistry.KAORI.get().create(lv);
        if (k == null) return;
        // si la anterior estaba lejos (zona sin cargar) viene la MISMA Kaori: con su mochila, equipo y casa;
        // la copia vieja desaparece sola al cargarse (solo puede haber una)
        boolean back = d.contains("KaoriBackup");
        if (back) k.readAdditionalSaveData(d.getCompound("KaoriBackup"));
        net.minecraft.world.phys.Vec3 look = p.getLookAngle();
        k.moveTo(p.getX() + look.x * 2, p.getY(), p.getZ() + look.z * 2, p.getYRot() + 180, 0);
        k.setOwner(p);
        lv.addFreshEntity(k);
        d.putUUID("KaoriUUID", k.getUUID());
        lv.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, k.getX(), k.getY() + 1, k.getZ(), 40, 0.6, 0.8, 0.6, 0.05);
        lv.playSound(null, k.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.0F, 1.4F);
        if (back) {
            k.say(p, "¡Aquí estoy! Vine volando desde donde me dejaste, con todas mis cosas.");
            return;
        }
        k.say(p, "¡Hola! Soy Kaori, mecánica, inventora y la chica más lista que vas a conocer. Mucho gusto. "
                + "Clic derecho: mi menú · Shift + clic derecho: mi mochila.");
    }

    /** Si el dueño invocó otra Kaori, la vieja se despide (no hay duplicadas). */
    public static boolean isStale(KaoriEntity k) {
        ServerPlayer o = k.getOwnerPlayer();
        if (o == null) return false;
        CompoundTag d = data(o);
        return d.hasUUID("KaoriUUID") && !d.getUUID("KaoriUUID").equals(k.getUUID());
    }
}
