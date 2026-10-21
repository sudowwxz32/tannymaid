package com.bdsz.item;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.registry.ModRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/** Materiales de armadura con la misma resistencia que la Netherite. */
public enum ModArmorMaterials implements ArmorMaterial {
    WARRIOR("warrior", () -> Ingredient.of(ModRegistry.ORANGE_KI_INGOT.get())),
    BATTLE("battle", () -> Ingredient.of(ModRegistry.BLUE_KI_INGOT.get()));

    private static final int[] DURABILITY = {13, 15, 16, 11};       // botas, pantalones, pecho, casco (×37 como Netherite)
    private static final int[] DEFENSE = {3, 6, 8, 3};

    private final String name;
    private final Supplier<Ingredient> repair;

    ModArmorMaterials(String name, Supplier<Ingredient> repair) {
        this.name = name;
        this.repair = repair;
    }

    private static int index(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 0;
            case LEGGINGS -> 1;
            case CHESTPLATE -> 2;
            case HELMET -> 3;
        };
    }

    @Override public int getDurabilityForType(ArmorItem.Type type) { return DURABILITY[index(type)] * 37; }
    @Override public int getDefenseForType(ArmorItem.Type type) { return DEFENSE[index(type)]; }
    @Override public int getEnchantmentValue() { return 15; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
    @Override public String getName() { return BlockDragonSuperZ.MODID + ":" + name; }
    @Override public float getToughness() { return 3.0F; }
    @Override public float getKnockbackResistance() { return 0.1F; }
}
