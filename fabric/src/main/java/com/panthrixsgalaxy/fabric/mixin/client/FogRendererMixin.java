package com.panthrixsgalaxy.fabric.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.panthrixsgalaxy.client.PGClientVisuals;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Solo Fabric (pantalla): las nieblas del mod.
 * En Forge se hace con ViewportEvent.ComputeFogColor y ViewportEvent.RenderFog (client/PGClientEvents).
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    /** Color de la niebla, justo antes de que Minecraft lo use. */
    @Inject(method = "setupColor", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
    private static void panthrixsgalaxy$fogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                                 float bossColorModifier, CallbackInfo info) {
        float[] rgb = {fogRed, fogGreen, fogBlue};
        PGClientVisuals.adjustFogColor(rgb);
        fogRed = rgb[0];
        fogGreen = rgb[1];
        fogBlue = rgb[2];
    }

    /** Distancia de la niebla (tormentas de Marte, Venus). */
    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void panthrixsgalaxy$fogDistance(Camera camera, FogRenderer.FogMode mode, float farPlaneDistance,
                                                    boolean shouldCreateFog, float partialTick, CallbackInfo info) {
        float[] distances = PGClientVisuals.fogDistances(farPlaneDistance);
        if (distances != null) {
            RenderSystem.setShaderFogStart(distances[0]);
            RenderSystem.setShaderFogEnd(distances[1]);
        }
    }
}
