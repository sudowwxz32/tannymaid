package com.bdsz.registry;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.block.SenzuCropBlock;
import com.bdsz.entity.KiBlastEntity;
import com.bdsz.entity.KameBeamEntity;
import com.bdsz.item.SenzuBeanItem;
import com.bdsz.item.TrainingListItem;
import com.bdsz.item.ModArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.util.valueproviders.UniformInt;
import com.bdsz.loot.SenzuLootModifier;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BlockDragonSuperZ.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BlockDragonSuperZ.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BlockDragonSuperZ.MODID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BlockDragonSuperZ.MODID);

    public static final RegistryObject<EntityType<KiBlastEntity>> KI_BLAST = ENTITIES.register("ki_blast",
            () -> EntityType.Builder.<KiBlastEntity>of(KiBlastEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F).clientTrackingRange(8).updateInterval(1).fireImmune().build("ki_blast"));

    // ---- enemigos y sus huevos de aparición
    public static final java.util.Map<com.bdsz.entity.EnemyKind, RegistryObject<EntityType<com.bdsz.entity.DbzEnemy>>> ENEMY =
            new java.util.EnumMap<>(com.bdsz.entity.EnemyKind.class);
    public static final java.util.Map<com.bdsz.entity.EnemyKind, RegistryObject<Item>> ENEMY_EGG =
            new java.util.EnumMap<>(com.bdsz.entity.EnemyKind.class);
    private static final int[][] EGG_COLORS = {
            {0x22305A, 0x6FE8FF}, {0x686F52, 0xFF3A30}, {0x1C1C22, 0x28A0A0}, {0x2A3E80, 0xFF8A2A},
            {0x201E26, 0xFF7A1A}, {0x6E3C8C, 0xD6AA3C}, {0x18161E, 0xE2B63E}};
    static {
        for (com.bdsz.entity.EnemyKind k : com.bdsz.entity.EnemyKind.values()) {
            var type = ENTITIES.register(k.id, () -> {
                var b = EntityType.Builder.<com.bdsz.entity.DbzEnemy>of(com.bdsz.entity.DbzEnemy::new, MobCategory.MONSTER)
                        .sized(k.width, k.height).clientTrackingRange(10);
                if (k.boss || k == com.bdsz.entity.EnemyKind.ROBOT) b = b.fireImmune();
                return b.build(k.id);
            });
            ENEMY.put(k, type);
            int[] c = EGG_COLORS[k.ordinal()];
            ENEMY_EGG.put(k, ITEMS.register(k.id + "_spawn_egg",
                    () -> new net.minecraftforge.common.ForgeSpawnEggItem(type, c[0], c[1], new Item.Properties())));
        }
    }

    public static final RegistryObject<EntityType<com.bdsz.entity.DebrisEntity>> DEBRIS = ENTITIES.register("debris",
            () -> EntityType.Builder.<com.bdsz.entity.DebrisEntity>of(com.bdsz.entity.DebrisEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(6).updateInterval(4).fireImmune().noSave().build("debris"));

    public static final RegistryObject<EntityType<KameBeamEntity>> KAME_BEAM = ENTITIES.register("kamehameha",
            () -> EntityType.Builder.<KameBeamEntity>of(KameBeamEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(1).fireImmune().noSave().build("kamehameha"));

    public static final RegistryObject<Block> SENZU_CROP = BLOCKS.register("senzu_crop",
            () -> new SenzuCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));

    public static final RegistryObject<Item> SENZU_BEAN = ITEMS.register("senzu_bean",
            () -> new SenzuBeanItem(SENZU_CROP.get(), new Item.Properties().rarity(Rarity.RARE)
                    .food(new FoodProperties.Builder().nutrition(20).saturationMod(1.0F).alwaysEat().fast().build())));

    public static final RegistryObject<Item> TRAINING_LIST_1 = ITEMS.register("training_list_1",
            () -> new TrainingListItem(2, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> TRAINING_LIST_2 = ITEMS.register("training_list_2",
            () -> new TrainingListItem(3, new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> TRAINING_LIST_3 = ITEMS.register("training_list_3",
            () -> new TrainingListItem(4, new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> TRAINING_LIST_4 = ITEMS.register("training_list_4",
            () -> new TrainingListItem(5, new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ULTIMATE_TRAINING = ITEMS.register("ultimate_training",
            () -> new com.bdsz.item.UltimateTrainingItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> TOTAL_MASTERY = ITEMS.register("training_list_5",
            () -> new TrainingListItem(6, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // ---- mineral de Ki (a la altura del diamante) y sus lingotes
    public static final RegistryObject<Block> KI_ORE = BLOCKS.register("ki_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE).lightLevel(s -> 3), UniformInt.of(2, 5)));
    public static final RegistryObject<Block> DEEPSLATE_KI_ORE = BLOCKS.register("deepslate_ki_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_DIAMOND_ORE).lightLevel(s -> 3), UniformInt.of(2, 5)));
    public static final RegistryObject<Item> KI_ORE_ITEM = ITEMS.register("ki_ore", () -> new BlockItem(KI_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_KI_ORE_ITEM = ITEMS.register("deepslate_ki_ore", () -> new BlockItem(DEEPSLATE_KI_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> BLACK_KI_INGOT = ITEMS.register("black_ki_ingot", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ORANGE_KI_INGOT = ITEMS.register("orange_ki_ingot", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> BLUE_KI_INGOT = ITEMS.register("blue_ki_ingot", () -> new Item(new Item.Properties().fireResistant()));

    // ---- armaduras (resistencia de Netherite)
    public static final RegistryObject<Item> WARRIOR_HELMET = armor("warrior_helmet", ModArmorMaterials.WARRIOR, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> WARRIOR_CHESTPLATE = armor("warrior_chestplate", ModArmorMaterials.WARRIOR, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WARRIOR_LEGGINGS = armor("warrior_leggings", ModArmorMaterials.WARRIOR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> WARRIOR_BOOTS = armor("warrior_boots", ModArmorMaterials.WARRIOR, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BATTLE_HELMET = armor("battle_helmet", ModArmorMaterials.BATTLE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BATTLE_CHESTPLATE = armor("battle_chestplate", ModArmorMaterials.BATTLE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BATTLE_LEGGINGS = armor("battle_leggings", ModArmorMaterials.BATTLE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BATTLE_BOOTS = armor("battle_boots", ModArmorMaterials.BATTLE, ArmorItem.Type.BOOTS);

    private static RegistryObject<Item> armor(String name, ModArmorMaterials mat, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(mat, type, new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    }

    public static final RegistryObject<Codec<SenzuLootModifier>> SENZU_LOOT = LOOT.register("senzu_in_villages", () -> SenzuLootModifier.CODEC);

    private ModRegistry() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        LOOT.register(bus);
    }
}
