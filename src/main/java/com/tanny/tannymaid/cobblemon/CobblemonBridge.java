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


    // ================================================================== Yunn: curar, potenciar, Pokédex y apariciones

    private static Object partyOf(ServerPlayer player) throws Exception {
        Class<?> cobblemon = Class.forName("com.cobblemon.mod.common.Cobblemon");
        Object inst = cobblemon.getField("INSTANCE").get(null);
        Object storage = cobblemon.getMethod("getStorage").invoke(inst);
        for (Method m : storage.getClass().getMethods()) {
            if (m.getName().equals("getParty") && m.getParameterCount() == 1) {
                Class<?> t = m.getParameterTypes()[0];
                if (t.isInstance(player)) return m.invoke(storage, player);
                if (t == UUID.class) return m.invoke(storage, player.getUUID());
            }
        }
        throw new NoSuchMethodException("getParty");
    }

    /** Pokémon del equipo del jugador (puede estar vacío). */
    public static List<Object> partyPokemon(ServerPlayer player) {
        List<Object> out = new ArrayList<>();
        if (!isLoaded()) return out;
        try {
            Object party = partyOf(player);
            if (party instanceof Iterable<?> it) {
                for (Object p : it) if (p != null) out.add(p);
            }
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] No se pudo leer el equipo Pokémon: {}", t.toString());
        }
        return out;
    }

    /** Cura a todo el equipo (vida, estados y PP). Devuelve cuántos Pokémon curó, o -1 si falló. */
    public static int healParty(ServerPlayer player) {
        if (!isLoaded()) return -1;
        try {
            Object party = partyOf(player);
            Method heal = findMethod(party.getClass(), "heal", 0);
            if (heal != null) {
                heal.invoke(party);
                return partyPokemon(player).size();
            }
            int n = 0;
            for (Object p : partyPokemon(player)) {
                Method h = findMethod(p.getClass(), "heal", 0);
                if (h != null) { h.invoke(p); n++; }
            }
            return n;
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] No se pudo curar al equipo: {}", t.toString());
            return -1;
        }
    }

    public static String pokemonName(Object pokemon) {
        try {
            Object c = pokemon.getClass().getMethod("getDisplayName").invoke(pokemon);
            if (c instanceof Component comp) return comp.getString();
        } catch (Throwable ignored) {}
        try {
            Object sp = pokemon.getClass().getMethod("getSpecies").invoke(pokemon);
            return String.valueOf(sp.getClass().getMethod("getName").invoke(sp));
        } catch (Throwable t) {
            return "Pokémon";
        }
    }

    /**
     * Potencia al equipo: más amistad, +EVs en todas las estadísticas y lleva al máximo (31) un IV al azar.
     * Devuelve un resumen legible por Pokémon.
     */
    public static List<String> boostParty(ServerPlayer player, net.minecraft.util.RandomSource rnd) {
        List<String> out = new ArrayList<>();
        if (!isLoaded()) return out;
        Object[] stats = null;
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<Enum> statsCls = (Class<Enum>) Class.forName("com.cobblemon.mod.common.api.pokemon.stats.Stats");
            String[] names = {"HP", "ATTACK", "DEFENCE", "SPECIAL_ATTACK", "SPECIAL_DEFENCE", "SPEED"};
            stats = new Object[names.length];
            for (int i = 0; i < names.length; i++) stats[i] = Enum.valueOf(statsCls, names[i]);
        } catch (Throwable t) {
            TannyMaid.LOGGER.debug("[TannyMaid] Sin Stats: {}", t.toString());
        }
        String[] statEs = {"PS", "Ataque", "Defensa", "At. Esp.", "Def. Esp.", "Velocidad"};
        for (Object p : partyPokemon(player)) {
            StringBuilder sb = new StringBuilder(pokemonName(p)).append(": ");
            try {
                int f = (Integer) p.getClass().getMethod("getFriendship").invoke(p);
                Method set = null;
                for (Method m : p.getClass().getMethods()) {
                    if (m.getName().equals("setFriendship") && m.getParameterCount() >= 1 && m.getParameterTypes()[0] == int.class) { set = m; break; }
                }
                if (set != null) {
                    int nf = Math.min(255, f + 40);
                    if (set.getParameterCount() == 1) set.invoke(p, nf); else set.invoke(p, nf, true);
                    sb.append("amistad ").append(nf).append("/255");
                }
            } catch (Throwable t) {
                TannyMaid.LOGGER.debug("[TannyMaid] amistad: {}", t.toString());
            }
            if (stats != null) {
                try {
                    Object evs = p.getClass().getMethod("getEvs").invoke(p);
                    Method add = null;
                    for (Method m : evs.getClass().getMethods()) if (m.getName().equals("add") && m.getParameterCount() == 2) { add = m; break; }
                    if (add != null) {
                        for (Object st : stats) add.invoke(evs, st, 8);
                        sb.append(", +8 EVs en todo");
                    }
                } catch (Throwable t) {
                    TannyMaid.LOGGER.debug("[TannyMaid] EVs: {}", t.toString());
                }
                try {
                    Object ivs = p.getClass().getMethod("getIvs").invoke(p);
                    Method set = null;
                    for (Method m : ivs.getClass().getMethods()) if (m.getName().equals("set") && m.getParameterCount() == 2) { set = m; break; }
                    if (set != null) {
                        int k = rnd.nextInt(stats.length);
                        set.invoke(ivs, stats[k], 31);
                        sb.append(", IV de ").append(statEs[k]).append(" al máximo (31)");
                    }
                } catch (Throwable t) {
                    TannyMaid.LOGGER.debug("[TannyMaid] IVs: {}", t.toString());
                }
            }
            out.add(sb.toString());
        }
        return out;
    }

    @Nullable
    private static Object species(String name) {
        try {
            Class<?> reg = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
            Object inst = reg.getField("INSTANCE").get(null);
            return reg.getMethod("getByName", String.class).invoke(inst, name.toLowerCase(Locale.ROOT));
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable
    private static Object call(Object o, String name) {
        try {
            Method m = findMethod(o.getClass(), name, 0);
            return m == null ? null : m.invoke(o);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String typeName(Object type) {
        if (type == null) return null;
        Object n = call(type, "getName");
        String s = n != null ? String.valueOf(n) : String.valueOf(type);
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "normal" -> "Normal"; case "fire" -> "Fuego"; case "water" -> "Agua"; case "grass" -> "Planta";
            case "electric" -> "Eléctrico"; case "ice" -> "Hielo"; case "fighting" -> "Lucha"; case "poison" -> "Veneno";
            case "ground" -> "Tierra"; case "flying" -> "Volador"; case "psychic" -> "Psíquico"; case "bug" -> "Bicho";
            case "rock" -> "Roca"; case "ghost" -> "Fantasma"; case "dragon" -> "Dragón"; case "dark" -> "Siniestro";
            case "steel" -> "Acero"; case "fairy" -> "Hada";
            default -> s;
        };
    }

    /** Ficha de Pokédex de una especie, o null si no existe. */
    @Nullable
    public static List<String> pokedex(String name) {
        Object sp = species(name);
        if (sp == null) return null;
        List<String> out = new ArrayList<>();
        Object dex = call(sp, "getNationalPokedexNumber");
        Object nm = call(sp, "getName");
        out.add("§d#" + (dex != null ? dex : "?") + " §f§l" + nm);
        String t1 = typeName(call(sp, "getPrimaryType")), t2 = typeName(call(sp, "getSecondaryType"));
        out.add("§7Tipo: §f" + t1 + (t2 != null ? " / " + t2 : ""));
        Object h = call(sp, "getHeight"), w = call(sp, "getWeight");
        if (h instanceof Number hn && w instanceof Number wn) {
            out.add(String.format(Locale.ROOT, "§7Altura: §f%.1f m  §7Peso: §f%.1f kg", hn.floatValue() / 10F, wn.floatValue() / 10F));
        }
        Object stats = call(sp, "getBaseStats");
        if (stats instanceof java.util.Map<?, ?> map && !map.isEmpty()) {
            StringBuilder sb = new StringBuilder("§7Estadísticas base: §f");
            int total = 0;
            for (java.util.Map.Entry<?, ?> e : map.entrySet()) {
                String k = String.valueOf(call(e.getKey(), "getShowdownId") != null ? call(e.getKey(), "getShowdownId") : e.getKey());
                sb.append(k.toUpperCase(Locale.ROOT)).append(" ").append(e.getValue()).append("  ");
                if (e.getValue() instanceof Number n) total += n.intValue();
            }
            out.add(sb.append("§7(total ").append(total).append(")").toString());
        }
        Object cr = call(sp, "getCatchRate");
        if (cr != null) out.add("§7Ratio de captura: §f" + cr + "/255");
        Object evos = call(sp, "getEvolutions");
        if (evos instanceof Iterable<?> it) {
            List<String> names = new ArrayList<>();
            for (Object ev : it) {
                Object res = call(ev, "getResult");
                Object spn = res != null ? call(res, "getSpecies") : null;
                if (spn != null) names.add(String.valueOf(spn));
            }
            if (!names.isEmpty()) out.add("§7Evoluciona a: §f" + String.join(", ", names));
        }
        return out;
    }

    private static List<?> spawnDetails() throws Exception {
        Class<?> pools = Class.forName("com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools");
        Object inst = pools.getField("INSTANCE").get(null);
        Object pool;
        try {
            pool = pools.getMethod("getWORLD_SPAWN_POOL").invoke(inst);
        } catch (NoSuchMethodException e) {
            java.lang.reflect.Field f = pools.getDeclaredField("WORLD_SPAWN_POOL");
            f.setAccessible(true);
            pool = f.get(inst);
        }
        Object details = pool.getClass().getMethod("getDetails").invoke(pool);
        return details instanceof List<?> l ? l : new ArrayList<>();
    }

    @Nullable
    private static String detailSpecies(Object detail) {
        Object props = call(detail, "getPokemon");
        if (props == null) return null;
        Object sp = call(props, "getSpecies");
        if (sp == null) return null;
        String s = String.valueOf(sp).toLowerCase(Locale.ROOT);
        int c = s.indexOf(':');
        return c >= 0 ? s.substring(c + 1) : s;
    }

    private static String friendlyBiome(Object cond) {
        Object id = call(cond, "getIdentifier");
        Object tag = call(cond, "getTag");
        String raw;
        if (id != null) raw = String.valueOf(id);
        else if (tag instanceof net.minecraft.tags.TagKey<?> tk) raw = tk.location().toString();
        else raw = String.valueOf(cond);
        String p = raw.contains(":") ? raw.substring(raw.indexOf(':') + 1) : raw;
        p = p.replace("is_", "").replace("has_", "").replace("biome/", "");
        String[][] es = {{"overworld", "cualquier lugar del mundo"}, {"forest", "bosque"}, {"plains", "llanura"}, {"desert", "desierto"},
                {"ocean", "océano"}, {"deep", "profundo"}, {"mountain", "montaña"}, {"peak", "cumbre"}, {"jungle", "jungla"},
                {"taiga", "taiga"}, {"swamp", "pantano"}, {"savanna", "sabana"}, {"badlands", "páramo"}, {"beach", "playa"},
                {"river", "río"}, {"snowy", "nevado"}, {"freezing", "helado"}, {"cold", "frío"}, {"nether", "Nether"},
                {"end", "End"}, {"cave", "cueva"}, {"lush", "frondoso"}, {"mushroom", "champiñón"}, {"spooky", "tenebroso"},
                {"tropical", "tropical"}, {"temperate", "templado"}, {"arid", "árido"}, {"hills", "colinas"}, {"tundra", "tundra"},
                {"grassland", "pradera"}, {"floral", "florido"}, {"magical", "mágico"}, {"volcanic", "volcánico"}, {"coast", "costa"},
                {"sky", "cielo"}, {"island", "isla"}, {"cherry_blossom", "cerezos"}, {"dripstone", "espeleotemas"}, {"glacial", "glaciar"}};
        for (String[] e : es) p = p.replace(e[0], e[1]);
        return p.replace('_', ' ');
    }

    /** Dónde aparece una especie: biomas, rareza y niveles. null si no hay datos. */
    @Nullable
    public static List<String> whereSpawns(String name) {
        if (!isLoaded()) return null;
        String want = name.toLowerCase(Locale.ROOT).replace(" ", "").replace("-", "");
        List<String> out = new ArrayList<>();
        try {
            java.util.Set<String> seen = new java.util.LinkedHashSet<>();
            for (Object d : spawnDetails()) {
                String sp = detailSpecies(d);
                if (sp == null || !sp.replace("-", "").equals(want)) continue;
                Object bucket = call(d, "getBucket");
                Object bname = bucket != null ? call(bucket, "getName") : null;
                Object lv = call(d, "getLevelRange");
                java.util.List<String> biomes = new ArrayList<>();
                Object conds = call(d, "getConditions");
                String extra = "";
                if (conds instanceof Iterable<?> it) {
                    for (Object c : it) {
                        Object bs = call(c, "getBiomes");
                        if (bs instanceof Iterable<?> bi) for (Object b : bi) biomes.add(friendlyBiome(b));
                        Object tr = call(c, "getTimeRange");
                        if (tr != null) extra += " §7(" + String.valueOf(tr).replace("day", "de día").replace("night", "de noche") + ")";
                    }
                }
                String rar = bname == null ? "" : switch (String.valueOf(bname)) {
                    case "common" -> "común"; case "uncommon" -> "poco común"; case "rare" -> "rara"; case "ultra-rare" -> "ultra rara";
                    default -> String.valueOf(bname);
                };
                String line = "§f" + (biomes.isEmpty() ? "varios lugares" : String.join(", ", biomes)) + " §7· " + rar
                        + (lv != null ? " · niveles " + String.valueOf(lv).replace("..", "–") : "") + extra;
                if (seen.add(line)) out.add(line);
            }
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] No se pudieron leer las apariciones: {}", t.toString());
            return null;
        }
        return out;
    }

    /** Especies que pueden aparecer en el bioma donde está el jugador. */
    @Nullable
    public static List<String> spawnsHere(ServerPlayer player) {
        if (!isLoaded()) return null;
        try {
            net.minecraft.world.level.biome.Biome biome = player.level().getBiome(player.blockPosition()).value();
            net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> reg =
                    player.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
            java.util.TreeSet<String> out = new java.util.TreeSet<>();
            for (Object d : spawnDetails()) {
                String sp = detailSpecies(d);
                if (sp == null) continue;
                Object conds = call(d, "getConditions");
                if (!(conds instanceof Iterable<?> it)) continue;
                boolean any = false;
                for (Object c : it) {
                    Object bs = call(c, "getBiomes");
                    if (!(bs instanceof Iterable<?> bi)) continue;
                    for (Object b : bi) {
                        for (Method m : b.getClass().getMethods()) {
                            if (m.getName().equals("fits") && m.getParameterCount() == 2) {
                                try {
                                    if (Boolean.TRUE.equals(m.invoke(b, biome, reg))) any = true;
                                } catch (Throwable ignored) {}
                            }
                        }
                        if (any) break;
                    }
                    if (any) break;
                }
                if (any) out.add(sp);
            }
            return new ArrayList<>(out);
        } catch (Throwable t) {
            TannyMaid.LOGGER.warn("[TannyMaid] No se pudo leer el bioma: {}", t.toString());
            return null;
        }
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
