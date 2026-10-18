package com.tanny.tannymaid.registry;

import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.item.TannyContractItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TannyMaid.MODID);

    public static final RegistryObject<Item> TANNY_CONTRACT = ITEMS.register("tanny_contract",
            () -> new TannyContractItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    private ModItems() {}
}
