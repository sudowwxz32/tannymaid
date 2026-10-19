package com.laury.laurymod.util;

import com.laury.laurymod.entity.LauryEntity;
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
 * /laury craftear: Laury fabrica herramientas vanilla y del mod con lo que
 * tenga en su mochila, usando las recetas reales del juego.
 * Si le faltan tablones o palos, los fabrica primero a partir de troncos/tablones.
 */
public final class LauryCrafting {

    public static final String NO_MATERIALS = "No tengo los materiales en mi inventario para craftear eso, {p}. ¡Pásame lo que falta y lo hago al instante!";

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
        ITEMS.put("arco", "minecraft:bow");
        ITEMS.put("flechas", "minecraft:arrow");
        ITEMS.put("antorchas", "minecraft:torch");
        ITEMS.put("mesa", "minecraft:crafting_table");
        ITEMS.put("cofre", "minecraft:chest");
        ITEMS.put("horno", "minecraft:furnace");
        ITEMS.put("cubo", "minecraft:bucket");
        ITEMS.put("cana", "minecraft:fishing_rod");
        ITEMS.put("tijeras", "minecraft:shears");
        ITEMS.put("pedernal", "minecraft:flint_and_steel");
        String[][] armor = {{"casco", "helmet"}, {"peto", "chestplate"}, {"pantalones", "leggings"}, {"botas", "boots"}};
        String[][] armorMat = {{"cuero", "leather"}, {"hierro", "iron"}, {"oro", "golden"}, {"diamante", "diamond"}};
        for (String[] a : armor) for (String[] m : armorMat) ITEMS.put(a[0] + "_" + m[0], "minecraft:" + m[1] + "_" + a[1]);
        ITEMS.put("mazo", "laury:dragon_mace");
        ITEMS.put("espada_fuego", "laury:fire_dragon_sword");
        ITEMS.put("espada_fuerza", "laury:force_dragon_sword");
        ITEMS.put("guadana", "laury:dragon_scythe");
    }

    private LauryCrafting() {}

    @Nullable
    public static String resolve(String name) {
        String n = Normalizer.normalize(name.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim();
        n = n.replace(' ', '_').replace("_de_", "_");
        if (ITEMS.containsKey(n)) return ITEMS.get(n);
        if (n.startsWith("palo")) return ITEMS.get("palos");
        if (n.startsWith("tablon")) return ITEMS.get("tablones");
        if (n.startsWith("antorcha")) return ITEMS.get("antorchas");
        if (n.startsWith("flecha")) return ITEMS.get("flechas");
        if (n.startsWith("guadan") || n.startsWith("guadaña")) return ITEMS.get("guadana");
        if (n.startsWith("mazo") || n.startsWith("martillo")) return ITEMS.get("mazo");
        if (n.contains("fuego")) return ITEMS.get("espada_fuego");
        if (n.contains("fuerza") || n.contains("salto")) return ITEMS.get("espada_fuerza");
        if (n.contains(":")) return n;
        if (n.endsWith("s") && ITEMS.containsKey(n.substring(0, n.length() - 1))) return ITEMS.get(n.substring(0, n.length() - 1));
        return null;
    }

    public static void craft(LauryEntity laury, ServerPlayer player, String name, int amount) {
        if (!(laury.level() instanceof ServerLevel level)) return;
        String id = resolve(name);
        if (id == null) {
            LaurySpeech.say(laury, player, "Hmm, no sé qué es \"" + name + "\". Puedo hacer palos, herramientas, armaduras, arcos y las armas de dragón.");
            return;
        }
        Item target = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (target == null || target == Items.AIR) {
            LaurySpeech.say(laury, player, "Ese objeto no existe en este mundo, {p}.");
            return;
        }
        List<CraftingRecipe> recipes = recipesFor(level, target);
        if (recipes.isEmpty()) {
            LaurySpeech.say(laury, player, "No conozco la receta de eso, {p}. ¡Y eso que soy lista, jaja!");
            return;
        }

        amount = Math.max(1, Math.min(64, amount));
        int made = 0;
        List<ItemStack> results = new ArrayList<>();
        for (int guard = 0; made < amount && guard < 128; guard++) {
            ItemStack out = craftOnce(laury, level, recipes);
            if (out.isEmpty()) {
                if (!prepareIntermediates(laury, level, recipes.get(0))) break;
                continue;
            }
            made += out.getCount();
            results.add(out);
        }

        if (made == 0) {
            LaurySpeech.say(laury, player, NO_MATERIALS);
            return;
        }
        for (ItemStack stack : results) {
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
        laury.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, laury.blockPosition(), SoundEvents.VILLAGER_WORK_TOOLSMITH, SoundSource.NEUTRAL, 0.8F, 1.2F);
        String itemName = new ItemStack(target).getHoverName().getString();
        if (made < amount) {
            LaurySpeech.say(laury, player, "Te hice " + made + " de " + itemName + ". Para el resto no me alcanzan los materiales, {p}.");
        } else {
            LaurySpeech.say(laury, player, "¡Listo! Aquí tienes " + made + " de " + itemName + ", {p}. ¡Hechos con garras de dragón, jajaja!");
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
    private static ItemStack craftOnce(LauryEntity laury, ServerLevel level, List<CraftingRecipe> recipes) {
        for (CraftingRecipe r : recipes) {
            int[] use = match(laury.getInventory(), r.getIngredients());
            if (use != null) {
                SimpleContainer inv = laury.getInventory();
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
    private static boolean prepareIntermediates(LauryEntity laury, ServerLevel level, CraftingRecipe recipe) {
        boolean needsSticks = false;
        boolean needsPlanks = false;
        for (Ingredient ing : recipe.getIngredients()) {
            if (ing.isEmpty()) continue;
            if (ing.test(new ItemStack(Items.STICK))) needsSticks = true;
            if (ing.test(new ItemStack(Items.OAK_PLANKS))) needsPlanks = true;
        }
        if (needsSticks && count(laury, Items.STICK) < 2) {
            if (craftInto(laury, level, Items.STICK)) return true;
            if (craftPlanksFromLogs(laury, level) && craftInto(laury, level, Items.STICK)) return true;
        }
        if (needsPlanks) {
            return craftPlanksFromLogs(laury, level);
        }
        return false;
    }

    private static boolean craftInto(LauryEntity laury, ServerLevel level, Item item) {
        ItemStack out = craftOnce(laury, level, recipesFor(level, item));
        if (out.isEmpty()) return false;
        laury.storeOrDrop(out);
        return true;
    }

    private static boolean craftPlanksFromLogs(LauryEntity laury, ServerLevel level) {
        SimpleContainer inv = laury.getInventory();
        for (CraftingRecipe r : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            ItemStack result = r.getResultItem(level.registryAccess());
            if (!result.is(ItemTags.PLANKS)) continue;
            int[] use = match(inv, r.getIngredients());
            if (use == null) continue;
            for (int i = 0; i < use.length; i++) {
                if (use[i] > 0) inv.getItem(i).shrink(use[i]);
            }
            inv.setChanged();
            laury.storeOrDrop(result.copy());
            return true;
        }
        return false;
    }

    private static int count(LauryEntity laury, Item item) {
        int c = 0;
        SimpleContainer inv = laury.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) c += inv.getItem(i).getCount();
        }
        return c;
    }
}
