package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGScorpionModel;
import com.panthrixsgalaxy.entity.mob.PGScorpionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Escorpiones: mismo modelo; el marciano es más grande y rojo. */
public class PGScorpionRenderer extends MobRenderer<PGScorpionEntity, PGScorpionModel> {

    private static final ResourceLocation LUNAR =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/lunar_scorpion.png");
    private static final ResourceLocation MARTIAN =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/martian_scorpion.png");

    public PGScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new PGScorpionModel(context.bakeLayer(PGScorpionModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    protected void scale(PGScorpionEntity scorpion, PoseStack poseStack, float partialTick) {
        float size = scorpion.isMartian() ? 1.15f : 1.0f;
        poseStack.scale(size, size, size);
    }

    @Override
    public ResourceLocation getTextureLocation(PGScorpionEntity scorpion) {
        return scorpion.isMartian() ? MARTIAN : LUNAR;
    }
}
