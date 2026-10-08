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
 * Aspecto del cinturón de asteroides: el vacío negro con estrellas, el Sol y Marte
 * pequeñito a lo lejos. Se une a la dimensión por "effects": "panthrixsgalaxy:asteroids".
 */
public class PGAsteroidEffects extends DimensionSpecialEffects {

    private static final Vector3f MARS_DIRECTION = new Vector3f(0.8f, 0.25f, 0.5f);
    private static final float MARS_SIZE = 5.0f;

    public PGAsteroidEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

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
        SpaceSkyRenderer.renderFromSurface(poseStack, PGPlanets.MARS.texture(), MARS_DIRECTION, MARS_SIZE);
        return true;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
    }
}
