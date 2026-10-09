package com.panthrixsgalaxy.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.panthrixsgalaxy.client.PGClientVisuals;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Solo Fabric (pantalla): la cámara tiembla con los motores (en Forge: ViewportEvent.ComputeCameraAngles). */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /** Minecraft mueve la cámara aquí cuando te hacen daño: aprovechamos para el temblor. */
    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void panthrixsgalaxy$cameraShake(PoseStack poseStack, float partialTick, CallbackInfo info) {
        float[] shake = PGClientVisuals.cameraShake(partialTick);
        if (shake != null) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(shake[1]));
            poseStack.mulPose(Axis.XP.rotationDegrees(shake[0]));
        }
    }
}
