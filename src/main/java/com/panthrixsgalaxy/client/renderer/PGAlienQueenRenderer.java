package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGAlienQueenModel;
import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Reina alienígena. Con el escudo de cambio de fase "palpita" (se hace un poco más grande y pequeña). */
public class PGAlienQueenRenderer extends MobRenderer<PGAlienQueenEntity, PGAlienQueenModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/boss/alien_queen.png");

    public PGAlienQueenRenderer(EntityRendererProvider.Context context) {
        super(context, new PGAlienQueenModel(context.bakeLayer(PGAlienQueenModel.LAYER_LOCATION)), 1.2f);
    }

    @Override
    protected void scale(PGAlienQueenEntity queen, PoseStack poseStack, float partialTick) {
        float pulse = queen.isShielded() ? 1.0f + Mth.sin((queen.tickCount + partialTick) * 0.8f) * 0.04f : 1.0f;
        poseStack.scale(pulse, pulse, pulse);
    }

    @Override
    public ResourceLocation getTextureLocation(PGAlienQueenEntity queen) {
        return TEXTURE;
    }
}
