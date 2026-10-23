package com.bdsz.mixin;

import com.bdsz.client.KiSense;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sentir el ki: los enemigos cercanos brillan con un contorno (como con una flecha espectral). */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void bdsz$senseKi(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (KiSense.shouldGlow(entity)) cir.setReturnValue(true);
    }
}
