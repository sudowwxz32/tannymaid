package com.laury.laurymod.registry;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LauryMod.MODID);

    public static final RegistryObject<Item> SUMMON_HORN = ITEMS.register("laury_summon_horn",
            () -> new LaurySummonItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> DRAGON_MACE = ITEMS.register("dragon_mace",
            () -> new DragonMaceItem(Tiers.NETHERITE, 9, -3.3F, new Item.Properties().rarity(Rarity.RARE).fireResistant()));

    public static final RegistryObject<Item> FIRE_DRAGON_SWORD = ITEMS.register("fire_dragon_sword",
            () -> new FireDragonSwordItem(Tiers.DIAMOND, 7, -2.4F, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> FORCE_DRAGON_SWORD = ITEMS.register("force_dragon_sword",
            () -> new ForceDragonSwordItem(Tiers.DIAMOND, 8, -2.6F, new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> DRAGON_SCYTHE = ITEMS.register("dragon_scythe",
            () -> new DragonScytheItem(Tiers.DIAMOND, 6, -2.8F, new Item.Properties().rarity(Rarity.EPIC)));

    private ModItems() {}
}
