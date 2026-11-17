package com.totallywaifu.registry;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.item.HeartItem;
import com.totallywaifu.menu.WaifuMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TotallyWaifu.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TotallyWaifu.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TotallyWaifu.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TotallyWaifu.MODID);

    public static final Map<WaifuKind, RegistryObject<EntityType<WaifuEntity>>> TYPES = new EnumMap<>(WaifuKind.class);
    public static final Map<WaifuKind, RegistryObject<Item>> EGGS = new EnumMap<>(WaifuKind.class);

    static {
        for (WaifuKind k : WaifuKind.values()) {
            RegistryObject<EntityType<WaifuEntity>> t = ENTITIES.register(k.id(), () -> {
                EntityType.Builder<WaifuEntity> b = EntityType.Builder.<WaifuEntity>of(WaifuEntity::new, MobCategory.CREATURE)
                        .sized(Math.min(0.9F, 0.6F * k.scale), 1.95F * k.scale).clientTrackingRange(10);
                if (k.fireproof()) b = b.fireImmune();
                return b.build(k.id());
            });
            TYPES.put(k, t);
            EGGS.put(k, ITEMS.register(k.id() + "_spawn_egg", () -> new ForgeSpawnEggItem(t, k.eggA, k.eggB, new Item.Properties())));
        }
    }

    public static final RegistryObject<EntityType<com.totallywaifu.entity.WaifuShot>> SHOT = ENTITIES.register("disparo",
            () -> EntityType.Builder.<com.totallywaifu.entity.WaifuShot>of(com.totallywaifu.entity.WaifuShot::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F).clientTrackingRange(6).updateInterval(5).build("disparo"));

    public static final RegistryObject<Item> HEART = ITEMS.register("corazon_waifu",
            () -> new HeartItem(null, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WAND = ITEMS.register("vara_de_transformacion",
            () -> new com.totallywaifu.item.WandItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<MenuType<WaifuMenu>> PANEL = MENUS.register("panel", () -> IForgeMenuType.create(WaifuMenu::fromNetwork));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.literal("Totally Waifu?"))
            .icon(() -> new ItemStack(WAND.get()))
            .displayItems((p, out) -> {
                out.accept(WAND.get());
                for (WaifuKind k : WaifuKind.DISPLAY) out.accept(EGGS.get(k).get());
            }).build());

    private ModRegistry() {}

    public static WaifuKind kindOf(EntityType<?> type) {
        for (Map.Entry<WaifuKind, RegistryObject<EntityType<WaifuEntity>>> e : TYPES.entrySet()) if (e.getValue().get() == type) return e.getKey();
        return WaifuKind.KOZUE;
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
        MENUS.register(bus);
        TABS.register(bus);
    }
}
