package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGAlienModels;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Aliens con forma humana (explorador, soldado, depredador): modelo humanoide,
 * una textura para cada uno y un tamaño (el depredador es más alto).
 */
public class PGAlienRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidModel<T>> {

    private final ResourceLocation texture;
    private final float size;

    public PGAlienRenderer(EntityRendererProvider.Context context, String textureName, float size) {
        super(context, new HumanoidModel<>(context.bakeLayer(PGAlienModels.ALIEN)), 0.5f * size);
        this.texture = new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/" + textureName + ".png");
        this.size = size;
    }

    @Override
    protected void scale(T alien, PoseStack poseStack, float partialTick) {
        poseStack.scale(size, size, size);
    }

    @Override
    public ResourceLocation getTextureLocation(T alien) {
        return texture;
    }
}
