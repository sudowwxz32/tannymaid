package com.laury.laurymod.registry;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LauryMod.MODID);

    public static final RegistryObject<EntityType<LauryEntity>> LAURY = ENTITIES.register("laury",
            () -> EntityType.Builder.<LauryEntity>of(LauryEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .clientTrackingRange(12)
                    .build("laury"));

    private ModEntities() {}
}
