package com.cz.mixin;

import com.cz.client.ZorroPoses;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Después de animar al jugador aplica las poses del zorro (transformación, cuatro patas, carga de chakra). */
@Mixin(PlayerModel.class)
public class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void cz$zorroPose(LivingEntity entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch, CallbackInfo ci) {
        ZorroPoses.apply((PlayerModel<?>) (Object) this, entity, limbSwing, limbAmount, age);
    }
}
