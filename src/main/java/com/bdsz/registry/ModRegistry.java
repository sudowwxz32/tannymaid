package com.bdsz.registry;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.block.SenzuCropBlock;
import com.bdsz.entity.KiBlastEntity;
import com.bdsz.entity.KameBeamEntity;
import com.bdsz.item.SenzuBeanItem;
import com.bdsz.item.TrainingListItem;
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

    public static final RegistryObject<Item> TOTAL_MASTERY = ITEMS.register("training_list_5",
            () -> new TrainingListItem(6, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Codec<SenzuLootModifier>> SENZU_LOOT = LOOT.register("senzu_in_villages", () -> SenzuLootModifier.CODEC);

    private ModRegistry() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        LOOT.register(bus);
    }
}
