package com.transporteiberico.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.transporteiberico.entity.BusEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Modelo 3D del autobús, hecho con cajas (como los mobs de Minecraft).
 * Medidas en píxeles (16 píxeles = 1 bloque): 6 bloques de largo, 2,75 de ancho y 2,9 de alto.
 * El frente está hacia -Z. El suelo está en y = 24.
 *
 * Cada pieza toma su color de una zona de la textura (512×512):
 *   (0,0) azul · (0,140) blanco · (0,245) cristal · (300,0) puertas · (300,50) ruedas
 *   (300,90) faros · (300,105) pilotos rojos · (300,120) parachoques · (300,135) letrero
 */
public class BusModel extends EntityModel<BusEntity> {

    private final ModelPart root;
    private final ModelPart[] wheels;

    public BusModel(ModelPart root) {
        this.root = root;
        this.wheels = new ModelPart[]{root.getChild("wheel_fl"), root.getChild("wheel_fr"), root.getChild("wheel_rl"), root.getChild("wheel_rr")};
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();

        // Carrocería azul y techo blanco
        p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-22.0F, -21.0F, -48.0F, 44.0F, 39.0F, 96.0F), PartPose.ZERO);
        p.addOrReplaceChild("roof", CubeListBuilder.create().texOffs(0, 140).addBox(-21.0F, -23.0F, -46.0F, 42.0F, 2.0F, 92.0F), PartPose.ZERO);
        // Franja blanca a los lados
        p.addOrReplaceChild("stripe_l", CubeListBuilder.create().texOffs(0, 140).addBox(-22.5F, 3.0F, -48.0F, 1.0F, 3.0F, 96.0F), PartPose.ZERO);
        p.addOrReplaceChild("stripe_r", CubeListBuilder.create().texOffs(0, 140).addBox(21.5F, 3.0F, -48.0F, 1.0F, 3.0F, 96.0F), PartPose.ZERO);

        // Ventanas laterales, parabrisas y luna trasera
        p.addOrReplaceChild("windows_l", CubeListBuilder.create().texOffs(0, 245).addBox(-22.6F, -17.0F, -42.0F, 1.0F, 14.0F, 82.0F), PartPose.ZERO);
        p.addOrReplaceChild("windows_r", CubeListBuilder.create().texOffs(0, 245).addBox(21.6F, -17.0F, -42.0F, 1.0F, 14.0F, 82.0F), PartPose.ZERO);
        p.addOrReplaceChild("windshield", CubeListBuilder.create().texOffs(0, 245).addBox(-20.0F, -18.0F, -48.6F, 40.0F, 22.0F, 1.0F), PartPose.ZERO);
        p.addOrReplaceChild("rear_window", CubeListBuilder.create().texOffs(0, 245).addBox(-18.0F, -17.0F, 47.6F, 36.0F, 12.0F, 1.0F), PartPose.ZERO);

        // Puertas en el lado derecho (delantera y central)
        p.addOrReplaceChild("door_front", CubeListBuilder.create().texOffs(300, 0).addBox(22.0F, -16.0F, -45.0F, 1.0F, 31.0F, 10.0F), PartPose.ZERO);
        p.addOrReplaceChild("door_middle", CubeListBuilder.create().texOffs(300, 0).addBox(22.0F, -16.0F, -5.0F, 1.0F, 31.0F, 10.0F), PartPose.ZERO);

        // Ruedas
        CubeListBuilder wheel = CubeListBuilder.create().texOffs(300, 50).addBox(-2.0F, -6.0F, -6.0F, 4.0F, 12.0F, 12.0F);
        p.addOrReplaceChild("wheel_fl", wheel, PartPose.offset(-21.0F, 18.0F, -32.0F));
        p.addOrReplaceChild("wheel_fr", wheel, PartPose.offset(21.0F, 18.0F, -32.0F));
        p.addOrReplaceChild("wheel_rl", wheel, PartPose.offset(-21.0F, 18.0F, 32.0F));
        p.addOrReplaceChild("wheel_rr", wheel, PartPose.offset(21.0F, 18.0F, 32.0F));

        // Faros, pilotos, parachoques y letrero de línea
        p.addOrReplaceChild("headlights", CubeListBuilder.create().texOffs(300, 90)
                .addBox(-19.0F, 8.0F, -48.8F, 6.0F, 3.0F, 1.0F).addBox(13.0F, 8.0F, -48.8F, 6.0F, 3.0F, 1.0F), PartPose.ZERO);
        p.addOrReplaceChild("taillights", CubeListBuilder.create().texOffs(300, 105)
                .addBox(-19.0F, 8.0F, 47.8F, 5.0F, 4.0F, 1.0F).addBox(14.0F, 8.0F, 47.8F, 5.0F, 4.0F, 1.0F), PartPose.ZERO);
        p.addOrReplaceChild("bumper_front", CubeListBuilder.create().texOffs(300, 120).addBox(-22.0F, 14.0F, -49.5F, 44.0F, 4.0F, 2.0F), PartPose.ZERO);
        p.addOrReplaceChild("bumper_rear", CubeListBuilder.create().texOffs(300, 120).addBox(-22.0F, 14.0F, 47.5F, 44.0F, 4.0F, 2.0F), PartPose.ZERO);
        p.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(300, 135).addBox(-14.0F, -20.5F, -48.9F, 28.0F, 4.0F, 1.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(BusEntity bus, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Las ruedas giran según la distancia recorrida
        float spin = bus.walkDist * 2.0F;
        for (ModelPart w : wheels) {
            w.xRot = bus.getSpeed() >= 0 ? spin : -spin;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
