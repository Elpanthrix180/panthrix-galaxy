package com.panthrixsgalaxy.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;

/**
 * Un planeta que dibuja su PROPIO cielo (estrellas, planetas, Sol...) y sin nubes.
 *
 * Forge llama a estos métodos directamente (son los mismos que añade a DimensionSpecialEffects).
 * Fabric los llama desde su registro de cielos (fabric/client/PGFabricClient).
 */
public interface PGSkyEffects {

    /** Dibuja el cielo. Devuelve true = Minecraft no dibuja el suyo. */
    boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                      Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog);

    /** Devuelve true = sin nubes. */
    boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                         double camX, double camY, double camZ, Matrix4f projectionMatrix);
}
