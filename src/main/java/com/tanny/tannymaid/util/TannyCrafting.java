package com.tanny.tannymaid.util;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /tanny craftear: Tanny fabrica palos, herramientas vanilla y Poké Balls con lo que
 * tenga en su mochila, usando las recetas reales del juego (incluidas las de Cobblemon).
 * Si le faltan tablones o palos, los fabrica primero a partir de troncos/tablones.
 */
public final class TannyCrafting {

    public static final String NO_MATERIALS = "Amo, requiero de más materiales para lo que me pidió.";

    /** nombre en español → id del objeto */
    public static final Map<String, String> ITEMS = new LinkedHashMap<>();

    static {
        ITEMS.put("palos", "minecraft:stick");
        ITEMS.put("tablones", "minecraft:oak_planks");
        String[][] materials = {{"madera", "wooden"}, {"piedra", "stone"}, {"hierro", "iron"}, {"oro", "golden"}, {"diamante", "diamond"}};
        String[][] tools = {{"pico", "pickaxe"}, {"hacha", "axe"}, {"pala", "shovel"}, {"espada", "sword"}, {"azada", "hoe"}};
        for (String[] t : tools) {
            for (String[] m : materials) {
                ITEMS.put(t[0] + "_" + m[0], "minecraft:" + m[1] + "_" + t[1]);
            }
        }
        ITEMS.put("escudo", "minecraft:shield");
        ITEMS.put("pokebola", "cobblemon:poke_ball");
        ITEMS.put("superball", "cobblemon:great_ball");
        ITEMS.put("ultraball", "cobblemon:ultra_ball");
    }

    private TannyCrafting() {}

    @Nullable
    public static String resolve(String name) {
        String n = Normalizer.normalize(name.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim();
        n = n.replace(' ', '_').replace("_de_", "_");
        if (ITEMS.containsKey(n)) return ITEMS.get(n);
        if (n.startsWith("palo")) return ITEMS.get("palos");
        if (n.startsWith("tablon")) return ITEMS.get("tablones");
        if (n.startsWith("pokebol") || n.startsWith("pokeball") || n.equals("poke_ball")) return ITEMS.get("pokebola");
        if (n.startsWith("super") || n.startsWith("great")) return ITEMS.get("superball");
        if (n.startsWith("ultra")) return ITEMS.get("ultraball");
        if (n.endsWith("s") && ITEMS.containsKey(n.substring(0, n.length() - 1))) return ITEMS.get(n.substring(0, n.length() - 1));
        return null;
    }

    public static void craft(TannyEntity tanny, ServerPlayer player, String name, int amount) {
        if (!(tanny.level() instanceof ServerLevel level)) return;
        String id = resolve(name);
        if (id == null) {
            TannySpeech.say(tanny, player, "Mmm... no sé hacer \"" + name + "\", Amo. Puedo hacer palos, herramientas y Poké Balls ♡");
            return;
        }
        Item target = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (target == null || target == Items.AIR) {
            TannySpeech.say(tanny, player, "Ese objeto no existe en este mundo, Amo. ¿Está instalado Cobblemon?");
            return;
        }
        List<CraftingRecipe> recipes = recipesFor(level, target);
        if (recipes.isEmpty()) {
            TannySpeech.say(tanny, player, "No conozco la receta de eso, Amo... perdón.");
            return;
        }

        amount = Math.max(1, Math.min(64, amount));
        int made = 0;
        List<ItemStack> results = new ArrayList<>();
        for (int guard = 0; made < amount && guard < 128; guard++) {
            ItemStack out = craftOnce(tanny, level, recipes);
            if (out.isEmpty()) {
                if (!prepareIntermediates(tanny, level, recipes.get(0))) break;
                continue;
            }
            made += out.getCount();
            results.add(out);
        }

        if (made == 0) {
            TannySpeech.say(tanny, player, NO_MATERIALS);
            return;
        }
        for (ItemStack stack : results) {
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
        tanny.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, tanny.blockPosition(), SoundEvents.VILLAGER_WORK_TOOLSMITH, SoundSource.NEUTRAL, 0.8F, 1.2F);
        String itemName = new ItemStack(target).getHoverName().getString();
        if (made < amount) {
            TannySpeech.say(tanny, player, "Te hice " + made + " de " + itemName + ", Amo. Para el resto requiero de más materiales.");
        } else {
            TannySpeech.say(tanny, player, "¡Listo, Amo! Aquí tienes " + made + " de " + itemName + ". ¡Hechos con cariño! ♡");
        }
    }

    private static List<CraftingRecipe> recipesFor(ServerLevel level, Item target) {
        List<CraftingRecipe> list = new ArrayList<>();
        for (CraftingRecipe r : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            ItemStack result = r.getResultItem(level.registryAccess());
            if (result.is(target) && hasIngredients(r)) list.add(r);
        }
        return list;
    }

    private static boolean hasIngredients(CraftingRecipe r) {
        for (Ingredient ing : r.getIngredients()) {
            if (!ing.isEmpty()) return true;
        }
        return false;
    }

    /** Intenta cada receta una vez; consume ingredientes y devuelve el resultado, o vacío. */
    private static ItemStack craftOnce(TannyEntity tanny, ServerLevel level, List<CraftingRecipe> recipes) {
        for (CraftingRecipe r : recipes) {
            int[] use = match(tanny.getInventory(), r.getIngredients());
            if (use != null) {
                SimpleContainer inv = tanny.getInventory();
                for (int i = 0; i < use.length; i++) {
                    if (use[i] > 0) inv.getItem(i).shrink(use[i]);
                }
                inv.setChanged();
                return r.getResultItem(level.registryAccess()).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /** Devuelve cuántos objetos usar de cada ranura, o null si faltan ingredientes. */
    @Nullable
    private static int[] match(SimpleContainer inv, NonNullList<Ingredient> ingredients) {
        int size = inv.getContainerSize();
        int[] left = new int[size];
        int[] use = new int[size];
        for (int i = 0; i < size; i++) left[i] = inv.getItem(i).getCount();
        for (Ingredient ing : ingredients) {
            if (ing.isEmpty()) continue;
            boolean found = false;
            for (int i = 0; i < size; i++) {
                if (left[i] > 0 && ing.test(inv.getItem(i))) {
                    left[i]--;
                    use[i]++;
                    found = true;
                    break;
                }
            }
            if (!found) return null;
        }
        return use;
    }

    /** Fabrica tablones (desde troncos) o palos (desde tablones) si la receta los necesita. */
    private static boolean prepareIntermediates(TannyEntity tanny, ServerLevel level, CraftingRecipe recipe) {
        boolean needsSticks = false;
        boolean needsPlanks = false;
        for (Ingredient ing : recipe.getIngredients()) {
            if (ing.isEmpty()) continue;
            if (ing.test(new ItemStack(Items.STICK))) needsSticks = true;
            if (ing.test(new ItemStack(Items.OAK_PLANKS))) needsPlanks = true;
        }
        if (needsSticks && count(tanny, Items.STICK) < 2) {
            if (craftInto(tanny, level, Items.STICK)) return true;
            if (craftPlanksFromLogs(tanny, level) && craftInto(tanny, level, Items.STICK)) return true;
        }
        if (needsPlanks) {
            return craftPlanksFromLogs(tanny, level);
        }
        return false;
    }

    private static boolean craftInto(TannyEntity tanny, ServerLevel level, Item item) {
        ItemStack out = craftOnce(tanny, level, recipesFor(level, item));
        if (out.isEmpty()) return false;
        tanny.storeOrDrop(out);
        return true;
    }

    private static boolean craftPlanksFromLogs(TannyEntity tanny, ServerLevel level) {
        SimpleContainer inv = tanny.getInventory();
        for (CraftingRecipe r : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            ItemStack result = r.getResultItem(level.registryAccess());
            if (!result.is(ItemTags.PLANKS)) continue;
            int[] use = match(inv, r.getIngredients());
            if (use == null) continue;
            for (int i = 0; i < use.length; i++) {
                if (use[i] > 0) inv.getItem(i).shrink(use[i]);
            }
            inv.setChanged();
            tanny.storeOrDrop(result.copy());
            return true;
        }
        return false;
    }

    private static int count(TannyEntity tanny, Item item) {
        int c = 0;
        SimpleContainer inv = tanny.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) c += inv.getItem(i).getCount();
        }
        return c;
    }
}
