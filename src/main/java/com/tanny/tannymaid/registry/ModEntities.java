package com.tanny.tannymaid.registry;

import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TannyMaid.MODID);

    public static final RegistryObject<EntityType<TannyEntity>> TANNY = ENTITIES.register("tanny",
            () -> EntityType.Builder.<TannyEntity>of(TannyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.65F)
                    .clientTrackingRange(10)
                    .build("tanny"));

    public static final RegistryObject<EntityType<com.tanny.tannymaid.yunn.YunnEntity>> YUNN = ENTITIES.register("yunn",
            () -> EntityType.Builder.<com.tanny.tannymaid.yunn.YunnEntity>of(com.tanny.tannymaid.yunn.YunnEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.65F)
                    .clientTrackingRange(10)
                    .build("yunn"));

    private ModEntities() {}
}
