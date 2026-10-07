package com.panthrixsgalaxy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.mob.PGAlienCreatureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Criatura alienígena: bestia baja de cuatro patas, con púas en el lomo, cola larga
 * y una mandíbula que se abre al morder. Textura 64x64.
 */
public class PGAlienCreatureModel extends EntityModel<PGAlienCreatureEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "alien_creature"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;

    public PGAlienCreatureModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = head.getChild("jaw");
        this.tail = root.getChild("tail");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.backRightLeg = root.getChild("back_right_leg");
        this.backLeftLeg = root.getChild("back_left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -3.0f, -7.0f, 8, 6, 14)
                        // Púas del lomo
                        .texOffs(52, 20).addBox(-0.5f, -6.0f, -5.0f, 1, 3, 2)
                        .texOffs(52, 20).addBox(-0.5f, -6.0f, -1.0f, 1, 3, 2)
                        .texOffs(52, 20).addBox(-0.5f, -6.0f, 3.0f, 1, 3, 2),
                PartPose.offset(0.0f, 14.0f, 0.0f));
        PartDefinition head = parts.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-3.5f, -3.0f, -7.0f, 7, 5, 7),
                PartPose.offset(0.0f, 13.0f, -7.0f));
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(28, 20).addBox(-3.0f, 0.0f, -6.0f, 6, 2, 6),
                PartPose.offset(0.0f, 2.0f, -0.5f));
        parts.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(0, 33).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 10),
                PartPose.offsetAndRotation(0.0f, 12.0f, 7.0f, 0.35f, 0.0f, 0.0f));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(44, 0).addBox(-1.5f, 0.0f, -1.5f, 3, 7, 3);
        parts.addOrReplaceChild("front_right_leg", leg, PartPose.offset(-2.5f, 17.0f, -5.0f));
        parts.addOrReplaceChild("front_left_leg", leg, PartPose.offset(2.5f, 17.0f, -5.0f));
        parts.addOrReplaceChild("back_right_leg", leg, PartPose.offset(-2.5f, 17.0f, 5.0f));
        parts.addOrReplaceChild("back_left_leg", leg, PartPose.offset(2.5f, 17.0f, 5.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(PGAlienCreatureEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float walk = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount;
        frontRightLeg.xRot = walk;
        backLeftLeg.xRot = walk;
        frontLeftLeg.xRot = -walk;
        backRightLeg.xRot = -walk;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float bite = attackTime > 0.0f ? Mth.sin(attackTime * Mth.PI) : 0.0f;
        jaw.xRot = 0.1f + bite * 0.8f + Mth.sin(ageInTicks * 0.1f) * 0.05f;
        tail.yRot = Mth.sin(ageInTicks * 0.2f) * 0.3f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
