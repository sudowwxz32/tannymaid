package com.tanny.tannymaid.cobblemon;

import com.tanny.tannymaid.TannyMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Puente con Cobblemon por reflexión. Así el mod compila y funciona sin Cobblemon,
 * y cuando Cobblemon (Forge 1.5.x) está instalado crea un Lucario real de Cobblemon.
 */
public final class CobblemonBridge {
    private static boolean failed = false;

    private CobblemonBridge() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("cobblemon");
    }

    public static boolean isAvailable() {
        return !failed && isLoaded();
    }

    @Nullable
    public static Mob createLucario(ServerLevel level, int pokemonLevel) {
        if (!isAvailable()) return null;
        try {
            Class<?> speciesRegistry = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
            Object registry = speciesRegistry.getField("INSTANCE").get(null);
            Object species = speciesRegistry.getMethod("getByName", String.class).invoke(registry, "lucario");
            if (species == null) {
                TannyMaid.LOGGER.warn("[TannyMaid] Cobblemon no tiene la especie 'lucario'.");
                return null;
            }

            Class<?> pokemonClass = Class.forName("com.cobblemon.mod.common.pokemon.Pokemon");
            Object pokemon = pokemonClass.getConstructor().newInstance();
            invoke(pokemon, "setSpecies", species);
            invoke(pokemon, "setLevel", pokemonLevel);
            tryInvoke(pokemon, "initialize");
            tryInvoke(pokemon, "setNickname", Component.literal("Lucario de Tanny"));

            Class<?> entityClass = Class.forName("com.cobblemon.mod.common.entity.pokemon.PokemonEntity");
            Object entity = null;
            for (Constructor<?> c : entityClass.getConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 2 && p[0].isInstance(level) && p[1] == pokemonClass) {
                    entity = c.newInstance(level, pokemon);
                    break;
                }
            }
            if (entity == null) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cobblemon", "pokemon"));
                for (Constructor<?> c : entityClass.getConstructors()) {
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
            TannyMaid.LOGGER.error("[TannyMaid] No se pudo crear el Lucario de Cobblemon. Tanny peleará sola.", t);
            failed = true;
            return null;
        }
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
