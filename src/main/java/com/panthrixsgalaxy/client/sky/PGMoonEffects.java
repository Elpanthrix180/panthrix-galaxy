package com.panthrixsgalaxy.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Aspecto de la Luna: cielo negro con estrellas aunque sea de día (no hay atmósfera que
 * disperse la luz), el Sol y la TIERRA grande en el cielo.
 * Se une a la dimensión por "effects": "panthrixsgalaxy:moon".
 */
public class PGMoonEffects extends DimensionSpecialEffects {

    /** Dirección fija de la Tierra en el cielo lunar (alta, hacia el sur). */
    private static final Vector3f EARTH_DIRECTION = new Vector3f(-0.25f, 0.75f, 0.6f);
    private static final float EARTH_SIZE = 16.0f;

    public PGMoonEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    // Forge lo llama directamente; en Fabric lo llama el registro de cielos (sin @Override)
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        SpaceSkyRenderer.renderFromSurface(poseStack, PGPlanets.EARTH.texture(), EARTH_DIRECTION, EARTH_SIZE);
        return true;
    }

    // Forge lo llama directamente; en Fabric lo llama el registro de cielos (sin @Override)
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
    }
}
