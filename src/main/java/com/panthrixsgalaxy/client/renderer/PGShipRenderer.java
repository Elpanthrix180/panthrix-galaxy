package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGShipModel;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Dibuja la nave. Cada nivel tiene su textura: textures/entity/ship/<nivel>.png (ship.png, advanced.png).
 *
 * Al volar, la nave baja un poco el morro cuando avanza (queda más bonito).
 * En primera persona, si TÚ la pilotas, no se dibuja: ves el exterior como desde la cabina.
 */
public class PGShipRenderer extends EntityRenderer<PGShipEntity> {

    private final PGShipModel model;

    public PGShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PGShipModel(context.bakeLayer(PGShipModel.LAYER_LOCATION));
        this.shadowRadius = 1.3f;
    }

    @Override
    public void render(PGShipEntity ship, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean insideFirstPerson = minecraft.player != null && ship.hasPassenger(minecraft.player)
                && minecraft.options.getCameraType().isFirstPerson();
        if (!insideFirstPerson) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
            if (ship.areEnginesOn()) {
                // Inclinación según la velocidad (máximo 15 grados)
                float tilt = (float) Mth.clamp(ship.getDeltaMovement().horizontalDistance() * 20.0, 0.0, 15.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(-tilt));
            }
            poseStack.scale(-1.0f, -1.0f, 1.0f);
            poseStack.translate(0.0, -1.501, 0.0);
            VertexConsumer vertices = buffer.getBuffer(model.renderType(getTextureLocation(ship)));
            model.renderToBuffer(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.popPose();
        }
        super.render(ship, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PGShipEntity ship) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID,
                "textures/entity/ship/" + ship.getTier().getSerializedName() + ".png");
    }
}
