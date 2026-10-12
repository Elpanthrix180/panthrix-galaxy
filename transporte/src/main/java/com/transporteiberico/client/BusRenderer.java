package com.transporteiberico.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.transporteiberico.TransporteIberico;
import com.transporteiberico.entity.BusEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Dibuja el autobús: lo gira hacia donde mira y lo hace temblar un poco al golpearlo. */
public class BusRenderer extends EntityRenderer<BusEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TransporteIberico.MODID, "textures/entity/autobus_urbano_azul.png");

    private final BusModel model;

    public BusRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new BusModel(ctx.bakeLayer(TIClient.BUS_LAYER));
        this.shadowRadius = 2.0F;
    }

    @Override
    public void render(BusEntity bus, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        float hurt = bus.getHurtTime() - partialTicks;
        if (hurt > 0.0F) {
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(hurt) * hurt * 0.3F));
        }
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);
        this.model.setupAnim(bus, 0.0F, 0.0F, bus.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer vc = buffers.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(bus, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(BusEntity bus) {
        return TEXTURE;
    }
}
