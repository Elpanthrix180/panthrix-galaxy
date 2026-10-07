package com.panthrixsgalaxy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.boss.PGAlienQueenEntity;
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
 * Reina alienígena: 3 bloques de alto. Patas largas, torso inclinado hacia delante,
 * cabeza con una gran CRESTA hacia atrás, brazos con garras, tubos en la espalda y
 * una cola larga de 2 segmentos. Levanta las garras al atacar. Textura 128x128.
 */
public class PGAlienQueenModel extends EntityModel<PGAlienQueenEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "alien_queen"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart tail;
    private final ModelPart tailEnd;

    public PGAlienQueenModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.tail = root.getChild("tail");
        this.tailEnd = tail.getChild("tail_end");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, 0.0f, -2.5f, 5, 18, 5);
        parts.addOrReplaceChild("right_leg", leg, PartPose.offset(-4.5f, 6.0f, 0.0f));
        parts.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-2.5f, 0.0f, -2.5f, 5, 18, 5), PartPose.offset(4.5f, 6.0f, 0.0f));
        parts.addOrReplaceChild("pelvis", CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-6.0f, -4.0f, -4.0f, 12, 6, 8),
                PartPose.offset(0.0f, 6.0f, 0.0f));
        parts.addOrReplaceChild("torso", CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-7.0f, -14.0f, -5.0f, 14, 14, 10)
                        // Tubos de la espalda
                        .texOffs(96, 47).addBox(-5.0f, -18.0f, 3.0f, 2, 8, 2)
                        .texOffs(96, 47).addBox(3.0f, -18.0f, 3.0f, 2, 8, 2),
                PartPose.offsetAndRotation(0.0f, 2.0f, 0.0f, 0.2f, 0.0f, 0.0f));
        PartDefinition head = parts.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-4.0f, -8.0f, -7.0f, 8, 8, 8),
                PartPose.offset(0.0f, -11.0f, -3.0f));
        head.addOrReplaceChild("crest", CubeListBuilder.create()
                        .texOffs(60, 16).addBox(-3.5f, -5.0f, 0.0f, 7, 5, 14),
                PartPose.offsetAndRotation(0.0f, -5.0f, -2.0f, -0.45f, 0.0f, 0.0f));
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition arm = parts.addOrReplaceChild(side < 0 ? "right_arm" : "left_arm", CubeListBuilder.create()
                            .texOffs(102, 0).addBox(-2.0f, -2.0f, -2.0f, 4, 16, 4),
                    PartPose.offset(9.0f * side, -9.0f, -2.0f));
            arm.addOrReplaceChild("claw", CubeListBuilder.create()
                            .texOffs(0, 47).addBox(-2.5f, 0.0f, -4.0f, 5, 4, 6),
                    PartPose.offset(0.0f, 14.0f, 0.0f));
        }
        PartDefinition tail = parts.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(22, 47).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 16),
                PartPose.offsetAndRotation(0.0f, 6.0f, 3.0f, -0.35f, 0.0f, 0.0f));
        tail.addOrReplaceChild("tail_end", CubeListBuilder.create()
                        .texOffs(62, 47).addBox(-1.5f, -1.5f, 0.0f, 3, 3, 14),
                PartPose.offsetAndRotation(0.0f, 0.0f, 16.0f, -0.25f, 0.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(PGAlienQueenEntity queen, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float walk = Mth.cos(limbSwing * 0.5f) * 0.9f * limbSwingAmount;
        rightLeg.xRot = walk;
        leftLeg.xRot = -walk;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float attack = attackTime > 0.0f ? Mth.sin(attackTime * Mth.PI) : 0.0f;
        float idle = Mth.sin(ageInTicks * 0.06f) * 0.08f;
        rightArm.xRot = -0.3f - walk * 0.5f - attack * 1.8f + idle;
        leftArm.xRot = -0.3f + walk * 0.5f - attack * 1.8f - idle;
        rightArm.zRot = 0.15f;
        leftArm.zRot = -0.15f;
        tail.yRot = Mth.sin(ageInTicks * 0.08f) * 0.35f;
        tailEnd.yRot = Mth.sin(ageInTicks * 0.08f - 0.8f) * 0.4f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
