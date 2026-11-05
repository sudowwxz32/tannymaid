package com.linterna.registry;

import com.linterna.Linterna;
import com.linterna.block.CandelabraBlock;
import com.linterna.block.ConstructBlock;
import com.linterna.entity.RayoVerde;
import com.linterna.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Linterna.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Linterna.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Linterna.MODID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Linterna.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Linterna.MODID);

    public static final RegistryObject<Block> CANDELABRA = BLOCKS.register("candelabro_de_poder",
            () -> new CandelabraBlock(BlockBehaviour.Properties.of().mapColor(MapColor.EMERALD).strength(3.0F, 1200.0F)
                    .sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()));
    public static final RegistryObject<Block> CONSTRUCT = BLOCKS.register("constructo",
            () -> new ConstructBlock(BlockBehaviour.Properties.of().mapColor(MapColor.EMERALD).strength(2.0F, 1200.0F)
                    .sound(SoundType.AMETHYST).lightLevel(s -> 9).noOcclusion().noLootTable()
                    .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));

    public static final RegistryObject<Item> RING = ITEMS.register("anillo_verde",
            () -> new RingItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> CANDELABRA_ITEM = ITEMS.register("candelabro_de_poder",
            () -> new CandelabraItem(CANDELABRA.get(), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ENERGY_PICK = ITEMS.register("pico_de_energia",
            () -> new EnergyPickItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_SWORD = ITEMS.register("espada_de_energia",
            () -> new EnergySwordItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_CANNON = ITEMS.register("canon_de_energia",
            () -> new CannonItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_SCYTHE = ITEMS.register("guadana_de_energia",
            () -> new ScytheItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_AXE = ITEMS.register("hacha_de_energia",
            () -> new EnergyAxeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_SHIELD = ITEMS.register("escudo_de_energia",
            () -> new EnergyShieldItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_BOW = ITEMS.register("arco_de_energia",
            () -> new EnergyBowItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_LANCE = ITEMS.register("lanza_de_energia",
            () -> new LanceItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_MACE = ITEMS.register("mazo_de_energia",
            () -> new MaceItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENERGY_BOMB = ITEMS.register("bomba_de_energia",
            () -> new BombItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<EntityType<RayoVerde>> RAYO = ENTITIES.register("rayo_verde",
            () -> EntityType.Builder.<RayoVerde>of(RayoVerde::new, MobCategory.MISC).sized(0.4F, 0.4F)
                    .clientTrackingRange(8).updateInterval(1).fireImmune().build("rayo_verde"));

    public static final RegistryObject<net.minecraft.world.inventory.MenuType<com.linterna.data.RingMenu>> RING_MENU = MENUS.register("anillo",
            () -> new net.minecraft.world.inventory.MenuType<>(com.linterna.data.RingMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.literal("Linterna"))
            .icon(() -> new ItemStack(RING.get()))
            .displayItems((params, out) -> {
                out.accept(RING.get());
                out.accept(CANDELABRA_ITEM.get());
                out.accept(ENERGY_BOMB.get());
            }).build());

    private ModRegistry() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        MENUS.register(bus);
        TABS.register(bus);
    }
}
