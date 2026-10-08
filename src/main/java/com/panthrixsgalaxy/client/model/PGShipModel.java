package com.panthrixsgalaxy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.ship.PGShipEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Modelo 3D de la nave, visto desde arriba (la proa apunta hacia delante):
 *
 *              ▄▄   proa
 *            ▐████▌ cabina de cristal
 *     ◢██████████████████◣  alas
 *            ▐████▌
 *            ▐█▌▐█▌ motores (+ aleta de cola encima)
 *
 * Mide unos 2,75 bloques de ancho y 1 de alto. Va sobre dos patines de aterrizaje.
 * Se puede editar con Blockbench (formato "Modded Entity", textura de 128x128).
 */
public class PGShipModel extends EntityModel<PGShipEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "ship"), "main");

    private final ModelPart ship;

    public PGShipModel(ModelPart root) {
        this.ship = root.getChild("ship");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("ship", CubeListBuilder.create()
                        // Fuselaje
                        .texOffs(0, 0).addBox(-8.0f, -12.0f, -18.0f, 16, 8, 36)
                        // Proa
                        .texOffs(0, 92).addBox(-5.0f, -11.0f, -24.0f, 10, 6, 6)
                        // Cabina de cristal
                        .texOffs(0, 44).addBox(-5.0f, -17.0f, -14.0f, 10, 5, 12)
                        // Alas
                        .texOffs(44, 44).addBox(-22.0f, -8.0f, -2.0f, 14, 2, 16)
                        .texOffs(44, 44).addBox(8.0f, -8.0f, -2.0f, 14, 2, 16, true)
                        // Motores
                        .texOffs(0, 62).addBox(-7.0f, -11.0f, 16.0f, 6, 6, 6)
                        .texOffs(0, 62).addBox(1.0f, -11.0f, 16.0f, 6, 6, 6)
                        // Aleta de cola
                        .texOffs(24, 62).addBox(-1.0f, -18.0f, 8.0f, 2, 6, 10)
                        // Patines de aterrizaje y sus patas
                        .texOffs(48, 62).addBox(-8.0f, -1.0f, -14.0f, 2, 1, 28)
                        .texOffs(48, 62).addBox(6.0f, -1.0f, -14.0f, 2, 1, 28)
                        .texOffs(0, 78).addBox(-8.0f, -4.0f, -10.0f, 2, 3, 2)
                        .texOffs(0, 78).addBox(6.0f, -4.0f, -10.0f, 2, 3, 2)
                        .texOffs(0, 78).addBox(-8.0f, -4.0f, 8.0f, 2, 3, 2)
                        .texOffs(0, 78).addBox(6.0f, -4.0f, 8.0f, 2, 3, 2),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(PGShipEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // La nave no se anima: el renderizador la inclina al volar.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        ship.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
