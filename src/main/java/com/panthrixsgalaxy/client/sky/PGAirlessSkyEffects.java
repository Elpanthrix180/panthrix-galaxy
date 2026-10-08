package com.panthrixsgalaxy.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Cielo de un planeta SIN atmósfera (Mercurio, Plutón...): negro con estrellas, el Sol y
 * otro planeta en el cielo. Igual que la Luna, pero se elige el planeta, su dirección y tamaño.
 */
public class PGAirlessSkyEffects extends DimensionSpecialEffects {

    private final ResourceLocation skyPlanet;
    private final Vector3f direction;
    private final float size;

    public PGAirlessSkyEffects(ResourceLocation skyPlanet, Vector3f direction, float size) {
        super(Float.NaN, true, SkyType.NONE, false, false);
        this.skyPlanet = skyPlanet;
        this.direction = direction;
        this.size = size;
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
        SpaceSkyRenderer.renderFromSurface(poseStack, skyPlanet, direction, size);
        return true;
    }

    // Forge lo llama directamente; en Fabric lo llama el registro de cielos (sin @Override)
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
    }
}
