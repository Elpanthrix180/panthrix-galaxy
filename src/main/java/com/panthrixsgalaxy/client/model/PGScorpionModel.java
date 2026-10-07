package com.panthrixsgalaxy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.mob.PGScorpionEntity;
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
 * Escorpión: cuerpo plano, cabeza con dos pinzas, 8 patas y una cola de 3 segmentos
 * curvada hacia arriba con el aguijón. Las patas se mueven al andar, la cola se balancea
 * y da un "picotazo" al atacar. Textura 64x64.
 */
public class PGScorpionModel extends EntityModel<PGScorpionEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "scorpion"), "main");

    private static final float[] LEG_Z = {-4.0f, -1.5f, 1.0f, 3.5f};

    private final ModelPart root;
    private final ModelPart[] rightLegs = new ModelPart[4];
    private final ModelPart[] leftLegs = new ModelPart[4];
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart tail;

    public PGScorpionModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < 4; i++) {
            rightLegs[i] = root.getChild("right_leg_" + i);
            leftLegs[i] = root.getChild("left_leg_" + i);
        }
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0f, -8.0f, -7.0f, 10, 5, 14)
                        .texOffs(0, 19).addBox(-4.0f, -7.0f, -12.0f, 8, 4, 5),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        for (int i = 0; i < 4; i++) {
            parts.addOrReplaceChild("right_leg_" + i, CubeListBuilder.create()
                            .texOffs(48, 0).addBox(0.0f, -0.5f, -0.5f, 7, 1, 1),
                    PartPose.offsetAndRotation(5.0f, 18.5f, LEG_Z[i], 0.0f, 0.0f, 0.55f));
            parts.addOrReplaceChild("left_leg_" + i, CubeListBuilder.create()
                            .texOffs(48, 0).mirror().addBox(-7.0f, -0.5f, -0.5f, 7, 1, 1),
                    PartPose.offsetAndRotation(-5.0f, 18.5f, LEG_Z[i], 0.0f, 0.0f, -0.55f));
        }
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition arm = parts.addOrReplaceChild(side > 0 ? "right_arm" : "left_arm", CubeListBuilder.create()
                            .texOffs(26, 19).addBox(-1.0f, -1.0f, -6.0f, 2, 2, 6),
                    PartPose.offsetAndRotation(3.5f * side, 19.0f, -11.0f, 0.0f, 0.4f * side, 0.0f));
            arm.addOrReplaceChild("claw", CubeListBuilder.create()
                            .texOffs(42, 19).addBox(-2.0f, -1.5f, -4.0f, 4, 3, 4),
                    PartPose.offset(0.0f, 0.0f, -6.0f));
        }
        PartDefinition tail = parts.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-1.5f, -1.5f, 0.0f, 3, 3, 5),
                PartPose.offsetAndRotation(0.0f, 17.5f, 6.0f, 0.9f, 0.0f, 0.0f));
        PartDefinition segment2 = tail.addOrReplaceChild("segment_2", CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-1.5f, -1.5f, 0.0f, 3, 3, 5),
                PartPose.offsetAndRotation(0.0f, 0.0f, 5.0f, 0.8f, 0.0f, 0.0f));
        PartDefinition segment3 = segment2.addOrReplaceChild("segment_3", CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-1.5f, -1.5f, 0.0f, 3, 3, 5),
                PartPose.offsetAndRotation(0.0f, 0.0f, 5.0f, 0.8f, 0.0f, 0.0f));
        segment3.addOrReplaceChild("stinger", CubeListBuilder.create()
                        .texOffs(16, 28).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4),
                PartPose.offsetAndRotation(0.0f, 0.0f, 5.0f, 1.0f, 0.0f, 0.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(PGScorpionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        for (int i = 0; i < 4; i++) {
            float phase = i * 1.6f;
            float swing = Mth.cos(limbSwing * 1.3f + phase) * 0.5f * limbSwingAmount;
            float lift = Math.abs(Mth.sin(limbSwing * 1.3f + phase)) * 0.3f * limbSwingAmount;
            rightLegs[i].yRot = swing;
            rightLegs[i].zRot = 0.55f - lift;
            leftLegs[i].yRot = swing;
            leftLegs[i].zRot = -0.55f + lift;
        }
        float strike = attackTime > 0.0f ? Mth.sin(attackTime * Mth.PI) : 0.0f;
        tail.xRot = 0.9f + Mth.sin(ageInTicks * 0.08f) * 0.06f + strike * 0.7f;
        float pinch = Mth.sin(ageInTicks * 0.15f) * 0.08f + strike * 0.4f;
        rightArm.yRot = 0.4f - pinch;
        leftArm.yRot = -0.4f + pinch;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
