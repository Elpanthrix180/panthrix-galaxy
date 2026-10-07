package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGMartianWormModel;
import com.panthrixsgalaxy.entity.mob.PGMartianWormEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Gusano marciano. Mientras sale del suelo se dibuja más abajo (oculto por los bloques);
 * enterrado del todo no se dibuja.
 */
public class PGMartianWormRenderer extends MobRenderer<PGMartianWormEntity, PGMartianWormModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/martian_worm.png");

    public PGMartianWormRenderer(EntityRendererProvider.Context context) {
        super(context, new PGMartianWormModel(context.bakeLayer(PGMartianWormModel.LAYER_LOCATION)), 0.8f);
    }

    @Override
    public void render(PGMartianWormEntity worm, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float progress = worm.getEmergeProgress(partialTick);
        shadowRadius = 0.8f * progress; // enterrado tampoco tiene sombra (¡no se le delata!)
        if (progress <= 0.01f) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, -(1.0f - progress) * worm.getBbHeight(), 0.0);
        super.render(worm, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PGMartianWormEntity worm) {
        return TEXTURE;
    }
}
