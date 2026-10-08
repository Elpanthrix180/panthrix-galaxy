package com.panthrixsgalaxy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.item.PGBackpackItem;
import com.panthrixsgalaxy.system.backpack.PGBackpackSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Dibuja la mochila equipada en la espalda del jugador.
 * Usa un modelo 3D aparte: assets/panthrixsgalaxy/models/item/worn/<mochila>.json
 */
public class BackpackRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public BackpackRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    public static ResourceLocation wornModelLocation(PGBackpackItem backpack) {
        return new ResourceLocation(PanthrixsGalaxy.MOD_ID, "item/worn/" + backpack.getWornModel());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack stack = PGBackpackSlot.getEquipped(player);
        if (!(stack.getItem() instanceof PGBackpackItem backpack) || player.isInvisible()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(wornModelLocation(backpack));

        poseStack.pushPose();
        // Seguir al cuerpo del jugador (se inclina al agacharse)
        getParentModel().body.translateAndRotate(poseStack);
        // Colocarla en la espalda: 6 píxeles hacia abajo y justo detrás de la pechera
        poseStack.translate(0.0, 0.375, 0.19);
        // Los modelos del jugador están "boca abajo": girar 180° para que la mochila quede derecha
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        minecraft.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }
}
