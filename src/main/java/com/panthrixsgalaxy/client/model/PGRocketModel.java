package com.panthrixsgalaxy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
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
 * Modelo 3D del cohete, hecho con "cajas" como los mobs de Minecraft (unidades = píxeles).
 * Mide 46 píxeles de alto (casi 3 bloques):
 *
 *        ▲  punta
 *       ███ cono (2 piezas)
 *      █████
 *      █ ○ █  cuerpo con ventanilla
 *      █████
 *    ◢ █████ ◣ aletas
 *       ▀▀▀ motor
 *
 * Si quieres cambiar la forma, se puede abrir y editar con Blockbench (formato "Modded Entity").
 */
public class PGRocketModel extends EntityModel<PGRocketEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "rocket"), "main");

    private final ModelPart rocket;

    public PGRocketModel(ModelPart root) {
        this.rocket = root.getChild("rocket");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("rocket", CubeListBuilder.create()
                        // Motor (abajo)
                        .texOffs(0, 48).addBox(-4.0f, -4.0f, -4.0f, 8, 4, 8)
                        // Cuerpo
                        .texOffs(0, 0).addBox(-6.0f, -32.0f, -6.0f, 12, 28, 12)
                        // Cono de proa (3 piezas cada vez más pequeñas)
                        .texOffs(48, 0).addBox(-5.0f, -38.0f, -5.0f, 10, 6, 10)
                        .texOffs(48, 16).addBox(-3.0f, -43.0f, -3.0f, 6, 5, 6)
                        .texOffs(48, 28).addBox(-1.0f, -46.0f, -1.0f, 2, 3, 2)
                        // Aletas (4)
                        .texOffs(0, 64).addBox(6.0f, -14.0f, -1.0f, 4, 14, 2)
                        .texOffs(0, 64).addBox(-10.0f, -14.0f, -1.0f, 4, 14, 2)
                        .texOffs(16, 64).addBox(-1.0f, -14.0f, 6.0f, 2, 14, 4)
                        .texOffs(16, 64).addBox(-1.0f, -14.0f, -10.0f, 2, 14, 4),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(PGRocketEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // El cohete no se anima (todavía).
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        rocket.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
