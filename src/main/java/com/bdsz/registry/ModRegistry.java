package com.bdsz.registry;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.block.SenzuCropBlock;
import com.bdsz.entity.KiBlastEntity;
import com.bdsz.item.SenzuBeanItem;
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

    public static final RegistryObject<Block> SENZU_CROP = BLOCKS.register("senzu_crop",
            () -> new SenzuCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));

    public static final RegistryObject<Item> SENZU_BEAN = ITEMS.register("senzu_bean",
            () -> new SenzuBeanItem(SENZU_CROP.get(), new Item.Properties().rarity(Rarity.RARE)
                    .food(new FoodProperties.Builder().nutrition(20).saturationMod(1.0F).alwaysEat().fast().build())));

    public static final RegistryObject<Codec<SenzuLootModifier>> SENZU_LOOT = LOOT.register("senzu_in_villages", () -> SenzuLootModifier.CODEC);

    private ModRegistry() {}

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        LOOT.register(bus);
    }
}
