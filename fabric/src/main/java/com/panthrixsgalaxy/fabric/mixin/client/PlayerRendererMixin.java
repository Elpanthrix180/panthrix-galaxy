package com.panthrixsgalaxy.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.client.PGClientVisuals;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Solo Fabric (pantalla): el astronauta dentro del cohete o la nave no se dibuja (en Forge: RenderPlayerEvent). */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void panthrixsgalaxy$hideRider(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poseStack,
                                           MultiBufferSource buffers, int light, CallbackInfo info) {
        if (PGClientVisuals.hideRider(player)) {
            info.cancel();
        }
    }
}
