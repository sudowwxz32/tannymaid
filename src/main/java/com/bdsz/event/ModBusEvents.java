package com.bdsz.event;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.entity.DbzEnemy;
import com.bdsz.entity.EnemyKind;
import com.bdsz.registry.ModRegistry;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Atributos de los enemigos y dónde pueden aparecer. */
@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    private ModBusEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        for (EnemyKind k : EnemyKind.values()) e.put(ModRegistry.ENEMY.get(k).get(), DbzEnemy.attributes(k).build());
    }

    @SubscribeEvent
    public static void spawns(SpawnPlacementRegisterEvent e) {
        for (EnemyKind k : EnemyKind.values()) {
            e.register(ModRegistry.ENEMY.get(k).get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }
}
