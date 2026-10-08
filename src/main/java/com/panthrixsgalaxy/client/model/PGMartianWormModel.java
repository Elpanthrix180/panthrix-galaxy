package com.panthrixsgalaxy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.mob.PGMartianWormEntity;
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
 * Gusano marciano: 3 anillos cada vez más finos y una cabeza con una corona de dientes
 * arriba (la boca). Se balancea siempre y se lanza hacia delante al morder. Textura 128x64.
 */
public class PGMartianWormModel extends EntityModel<PGMartianWormEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "martian_worm"), "main");

    private final ModelPart base;
    private final ModelPart middle;
    private final ModelPart upper;
    private final ModelPart head;

    public PGMartianWormModel(ModelPart root) {
        this.base = root.getChild("base");
        this.middle = base.getChild("middle");
        this.upper = middle.getChild("upper");
        this.head = upper.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        PartDefinition base = parts.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0f, -10.0f, -6.0f, 12, 10, 12),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition middle = base.addOrReplaceChild("middle", CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-5.0f, -10.0f, -5.0f, 10, 10, 10),
                PartPose.offset(0.0f, -10.0f, 0.0f));
        PartDefinition upper = middle.addOrReplaceChild("upper", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-4.0f, -9.0f, -4.0f, 8, 9, 8),
                PartPose.offset(0.0f, -10.0f, 0.0f));
        upper.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-4.5f, -6.0f, -4.5f, 9, 6, 9)
                        // Corona de dientes alrededor de la boca
                        .texOffs(48, 15).addBox(-4.0f, -8.0f, -4.0f, 1, 2, 1)
                        .texOffs(48, 15).addBox(3.0f, -8.0f, -4.0f, 1, 2, 1)
                        .texOffs(48, 15).addBox(-4.0f, -8.0f, 3.0f, 1, 2, 1)
                        .texOffs(48, 15).addBox(3.0f, -8.0f, 3.0f, 1, 2, 1)
                        .texOffs(48, 15).addBox(-0.5f, -8.0f, -4.0f, 1, 2, 1)
                        .texOffs(48, 15).addBox(-0.5f, -8.0f, 3.0f, 1, 2, 1)
                        .texOffs(48, 15).addBox(-4.0f, -8.0f, -0.5f, 1, 2, 1)
                        .texOffs(48, 15).addBox(3.0f, -8.0f, -0.5f, 1, 2, 1),
                PartPose.offset(0.0f, -9.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(PGMartianWormEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float bite = attackTime > 0.0f ? Mth.sin(attackTime * Mth.PI) : 0.0f;
        middle.zRot = Mth.sin(ageInTicks * 0.08f) * 0.1f;
        upper.zRot = Mth.sin(ageInTicks * 0.08f + 1.0f) * 0.14f;
        middle.xRot = 0.15f + bite * 0.35f;
        upper.xRot = 0.2f + bite * 0.45f;
        head.xRot = 0.25f + bite * 0.5f;
        base.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.5f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        base.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
