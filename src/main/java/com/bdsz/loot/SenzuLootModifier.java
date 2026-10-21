package com.bdsz.loot;

import com.bdsz.registry.ModRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/** Añade semillas del ermitaño a los cofres de las aldeas (45% de probabilidad, 1-3 semillas). */
public class SenzuLootModifier extends LootModifier {
    public static final Codec<SenzuLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SenzuLootModifier::new));

    public SenzuLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        String table = ctx.getQueriedLootTableId().getPath();
        if (table.startsWith("chests/village/") && ctx.getRandom().nextFloat() < 0.45F) {
            loot.add(new ItemStack(ModRegistry.SENZU_BEAN.get(), 1 + ctx.getRandom().nextInt(3)));
        }
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
