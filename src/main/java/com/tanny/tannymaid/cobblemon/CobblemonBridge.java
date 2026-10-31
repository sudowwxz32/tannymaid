package com.tanny.tannymaid.cobblemon;

import com.tanny.tannymaid.TannyMaid;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Puente con Cobblemon por reflexión. Así el mod compila y funciona sin Cobblemon,
 * y cuando Cobblemon (Forge 1.5.x) está instalado crea Pokémon reales, abre el PC
 * y reconoce las especies de los Pokémon salvajes. Los métodos se buscan una sola vez.
 */
public final class CobblemonBridge {
    private static boolean failed = false;
    private static Boolean loaded;

    // caché de reflexión para leer especies (se usa a menudo en los avisos)
    private static Class<?> entityClass;
    private static Method getPokemon, getSpecies, getResId, isWild;
    private static boolean speciesBroken;
    private static List<String> speciesNames;

    private CobblemonBridge() {}

    public static boolean isLoaded() {
        if (loaded == null) loaded = ModList.get().isLoaded("cobblemon");
        return loaded;
    }

    public static boolean isAvailable() {
        return !failed && isLoaded();
    }

    // ------------------------------------------------------------------ crear Pokémon

    @Nullable
    public static Mob createLucario(ServerLevel level, int pokemonLevel) {
        return createPokemon(level, "lucario", pokemonLevel, "Lucario de Tanny", false);
    }

    @Nullable
    public static Mob createPokemon(ServerLevel level, String speciesName, int pokemonLevel, String nickname, boolean female) {
        if (!isAvailable()) return null;
        try {
            Class<?> speciesRegistry = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
            Object registry = speciesRegistry.getField("INSTANCE").get(null);
            Object species = speciesRegistry.getMethod("getByName", String.class).invoke(registry, speciesName);
            if (species == null) {
                TannyMaid.LOGGER.warn("[TannyMaid] Cobblemon no tiene la especie '{}'.", speciesName);
                return null;
            }

            Class<?> pokemonClass = Class.forName("com.cobblemon.mod.common.pokemon.Pokemon");
            Object pokemon = pokemonClass.getConstructor().newInstance();
            invoke(pokemon, "setSpecies", species);
            invoke(pokemon, "setLevel", pokemonLevel);
            tryInvoke(pokemon, "initialize");
            if (female) {
                try {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Object g = Enum.valueOf((Class) Class.forName("com.cobblemon.mod.common.pokemon.Gender"), "FEMALE");
                    tryInvoke(pokemon, "setGender", g);
                } catch (Throwable t) {
                    TannyMaid.LOGGER.debug("[TannyMaid] No se pudo fijar el género: {}", t.toString());
                }
            }
            tryInvoke(pokemon, "setNickname", Component.literal(nickname));

            Class<?> entityCls = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
            Object entity = null;
            for (Constructor<?> c : entityCls.getConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 2 && p[0].isInstance(level) && p[1] == pokemonClass) {
                    entity = c.newInstance(level, pokemon);
                    break;
                }
            }
            if (entity == null) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cobblemon", "pokemon"));
                for (Constructor<?> c : entityCls.getConstructors()) {
                    Class<?>[] p = c.getParameterTypes();
                    if (p.length == 3 && p[0].isInstance(level) && p[1] == pokemonClass && p[2].isInstance(type)) {
                        entity = c.newInstance(level, pokemon, type);
                        break;
                    }
                }
            }
            if (entity instanceof Mob mob) {
                return mob;
            }
            TannyMaid.LOGGER.warn("[TannyMaid] No se encontró un constructor compatible de PokemonEntity.");
            failed = true;
            return null;
        } catch (Throwable t) {
            TannyMaid.LOGGER.error("[TannyMaid] No se pudo crear el Pokémon '" + speciesName + "' de Cobblemon.", t);
            failed = true;
            return null;
        }
    }

    // ------------------------------------------------------------------ especies

    public static boolean isPokemonEntity(Entity e) {
        if (!isLoaded() || e == null) return false;
        if (entityClass == null) {
            try {
                entityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
            } catch (Throwable t) {
                loaded = false;
                return false;
            }
        }
        return entityClass.isInstance(e);
    }

    /** Id de la especie ("pikachu", "gardevoir"...) o null. */
    @Nullable
    public static String speciesId(Entity e) {
        if (speciesBroken || !isPokemonEntity(e)) return null;
        try {
            if (getPokemon == null) getPokemon = entityClass.getMethod("getPokemon");
            Object pokemon = getPokemon.invoke(e);
            if (pokemon == null) return null;
            if (getSpecies == null) getSpecies = pokemon.getClass().getMethod("getSpecies");
            Object species = getSpecies.invoke(pokemon);
            if (species == null) return null;
            if (getResId == null) getResId = species.getClass().getMethod("getResourceIdentifier");
            Object id = getResId.invoke(species);
            return id instanceof ResourceLocation rl ? rl.getPath() : String.valueOf(id).toLowerCase(Locale.ROOT);
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] No se pudo leer la especie de un Pokémon: {}", t.toString());
            speciesBroken = true;
            return null;
        }
    }

    /** ¿Es salvaje (no pertenece a ningún entrenador)? */
    public static boolean isWild(Entity e) {
        try {
            if (getPokemon == null) getPokemon = entityClass.getMethod("getPokemon");
            Object pokemon = getPokemon.invoke(e);
            if (isWild == null) isWild = pokemon.getClass().getMethod("isWild");
            return Boolean.TRUE.equals(isWild.invoke(pokemon));
        } catch (Throwable t) {
            return true;
        }
    }

    /** Nombres de todas las especies (para autocompletar el comando de avisos). */
    public static Collection<String> allSpecies() {
        if (speciesNames != null) return speciesNames;
        if (!isLoaded()) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        try {
            Class<?> speciesRegistry = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
            Object registry = speciesRegistry.getField("INSTANCE").get(null);
            Object all;
            try {
                all = speciesRegistry.getMethod("getImplemented").invoke(registry);
            } catch (NoSuchMethodException ex) {
                all = speciesRegistry.getMethod("getSpecies").invoke(registry);
            }
            if (all instanceof Collection<?> c) {
                for (Object s : c) {
                    Object id = s.getClass().getMethod("getResourceIdentifier").invoke(s);
                    if (id instanceof ResourceLocation rl) out.add(rl.getPath());
                }
            }
        } catch (Throwable t) {
            TannyMaid.LOGGER.debug("[TannyMaid] No se pudo listar especies: {}", t.toString());
        }
        Collections.sort(out);
        if (!out.isEmpty()) speciesNames = out;   // si aún no cargaron, se reintenta luego
        return out;
    }

    // ------------------------------------------------------------------ PC

    /** Abre el PC de Cobblemon (las cajas) del jugador, sin necesitar el bloque del PC. */
    public static boolean openPC(ServerPlayer player) {
        if (!isLoaded()) return false;
        try {
            Class<?> cobblemon = Class.forName("com.cobblemon.mod.common.Cobblemon");
            Object inst = cobblemon.getField("INSTANCE").get(null);
            Object storage = cobblemon.getMethod("getStorage").invoke(inst);
            Object pc = findMethod(storage.getClass(), "getPC", 1, UUID.class).invoke(storage, player.getUUID());

            Class<?> linkMgr = Class.forName("com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager");
            Object mgr = linkMgr.getField("INSTANCE").get(null);
            Class<?> linkCls = Class.forName("com.cobblemon.mod.common.api.storage.pc.link.PCLink");
            Object link = null;
            for (Constructor<?> c : linkCls.getConstructors()) {
                if (c.getParameterCount() == 2 && c.getParameterTypes()[1] == UUID.class) {
                    link = c.newInstance(pc, player.getUUID());
                    break;
                }
            }
            if (link == null) throw new NoSuchMethodException("PCLink(pc, uuid)");
            findMethod(linkMgr, "addLink", 1, null).invoke(mgr, link);

            UUID pcId = (UUID) pc.getClass().getMethod("getUuid").invoke(pc);
            Class<?> packetCls = Class.forName("com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket");
            Object packet = packetCls.getConstructor(UUID.class).newInstance(pcId);
            if (!sendPacket(player, packet)) throw new NoSuchMethodException("sendToPlayer");
            return true;
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] No se pudo abrir el PC directamente ({}); probando con /pc.", t.toString());
        }
        try {
            CommandSourceStack src = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            return player.getServer().getCommands().performPrefixedCommand(src, "pc") > 0;
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] Tampoco funcionó /pc: {}", t.toString());
            return false;
        }
    }

    private static boolean sendPacket(ServerPlayer player, Object packet) {
        for (Method m : packet.getClass().getMethods()) {
            if (m.getName().equals("sendToPlayer") && m.getParameterCount() == 1 && m.getParameterTypes()[0].isInstance(player)) {
                try { m.invoke(packet, player); return true; } catch (Throwable ignored) {}
            }
        }
        try {
            Class<?> net = Class.forName("com.cobblemon.mod.common.CobblemonNetwork");
            Object inst = net.getField("INSTANCE").get(null);
            for (Method m : net.getMethods()) {
                if (m.getName().equals("sendPacketToPlayer") && m.getParameterCount() == 2) {
                    m.invoke(inst, player, packet);
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    // ------------------------------------------------------------------ objetos

    @Nullable
    public static ItemStack item(String path, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("cobblemon", path));
        if (item == null || item == net.minecraft.world.item.Items.AIR) return null;
        return new ItemStack(item, count);
    }

    /** ¿Es una Poké Ball (de Cobblemon o de otro mod)? */
    public static boolean isPokeBall(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return false;
        return !id.getNamespace().equals("minecraft") && id.getPath().endsWith("ball");
    }

    // ------------------------------------------------------------------ util

    private static Method findMethod(Class<?> cls, String name, int params, @Nullable Class<?> firstParam) throws NoSuchMethodException {
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params
                    && (firstParam == null || m.getParameterTypes()[0] == firstParam)) return m;
        }
        throw new NoSuchMethodException(cls.getName() + "#" + name);
    }

    private static Method findMethod(Class<?> cls, String name, int params) {
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params) return m;
        }
        return null;
    }

    private static void invoke(Object target, String name, Object... args) throws Exception {
        Method m = findMethod(target.getClass(), name, args.length);
        if (m == null) throw new NoSuchMethodException(target.getClass().getName() + "#" + name);
        m.invoke(target, args);
    }

    private static void tryInvoke(Object target, String name, Object... args) {
        try {
            Method m = findMethod(target.getClass(), name, args.length);
            if (m != null) m.invoke(target, args);
        } catch (Throwable t) {
            TannyMaid.LOGGER.debug("[TannyMaid] Llamada opcional {} falló: {}", name, t.toString());
        }
    }
}
