package com.bdsz.block;

import com.bdsz.registry.ModRegistry;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/** Planta de semillas del ermitaño (8 etapas, como el trigo). */
public class SenzuCropBlock extends CropBlock {
    public SenzuCropBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModRegistry.SENZU_BEAN.get();
    }
}
