package com.panthrixsgalaxy.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Aspecto de la dimensión Espacio: sin nubes, sin niebla, fondo negro y un cielo propio
 * dibujado por SpaceSkyRenderer (estrellas, Sol, Tierra, Luna y Marte).
 * Se une a la dimensión por el nombre "panthrixsgalaxy:space" (campo "effects" del JSON).
 */
public class PGSpaceEffects extends DimensionSpecialEffects implements PGSkyEffects {

    public PGSpaceEffects() {
        // altura de nubes (ninguna), ¿tiene suelo?, tipo de cielo (lo dibujamos nosotros), luz forzada, luz constante
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    /** Color de la niebla/fondo: negro. */
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        SpaceSkyRenderer.render(poseStack, camera);
        return true; // true = no dibujar el cielo normal de Minecraft
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true; // sin nubes
    }
}
