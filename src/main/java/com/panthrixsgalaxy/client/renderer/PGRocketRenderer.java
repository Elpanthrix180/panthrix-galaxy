package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGRocketModel;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Dibuja el cohete. Cada nivel de cohete tiene su propia textura:
 *   textures/entity/rocket/<nivel>.png   (basic.png, advanced.png...)
 *
 * Si TÚ vas dentro y miras en primera persona, el cohete no se dibuja: así ves el exterior
 * como desde la cabina (si no, verías las paredes por dentro).
 */
public class PGRocketRenderer extends EntityRenderer<PGRocketEntity> {

    private final PGRocketModel model;

    public PGRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PGRocketModel(context.bakeLayer(PGRocketModel.LAYER_LOCATION));
        this.shadowRadius = 0.7f;
    }

    @Override
    public void render(PGRocketEntity rocket, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean insideFirstPerson = minecraft.player != null && rocket.hasPassenger(minecraft.player)
                && minecraft.options.getCameraType().isFirstPerson();
        if (!insideFirstPerson) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
            // Los modelos de Minecraft están "boca abajo": se dan la vuelta y se bajan 1,5 bloques
            poseStack.scale(-1.0f, -1.0f, 1.0f);
            poseStack.translate(0.0, -1.501, 0.0);
            VertexConsumer vertices = buffer.getBuffer(model.renderType(getTextureLocation(rocket)));
            model.renderToBuffer(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.popPose();
        }
        super.render(rocket, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PGRocketEntity rocket) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID,
                "textures/entity/rocket/" + rocket.getTier().getSerializedName() + ".png");
    }
}
