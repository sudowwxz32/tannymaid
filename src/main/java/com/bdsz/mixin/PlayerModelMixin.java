package com.bdsz.mixin;

import com.bdsz.client.SaiyanPoses;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Después de animar al jugador, aplica las poses saiyajin (recarga, transformación y vuelo). */
@Mixin(PlayerModel.class)
public class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void bdsz$saiyanPose(LivingEntity entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch, CallbackInfo ci) {
        SaiyanPoses.apply((PlayerModel<?>) (Object) this, entity, limbSwing, limbAmount, age);
    }
}
