package com.panthrixsgalaxy.client.model;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Los aliens explorador, soldado y depredador tienen forma humana: usan el modelo
 * "humanoide" de Minecraft (el del zombi o el esqueleto) con su propia textura de 64x32.
 * Así no hace falta un modelo nuevo y llevan cosas en la mano (el soldado, su pistola).
 */
public final class PGAlienModels {

    public static final ModelLayerLocation ALIEN =
            new ModelLayerLocation(new ResourceLocation(PanthrixsGalaxy.MOD_ID, "alien"), "main");

    public static LayerDefinition createAlienLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f), 64, 32);
    }

    private PGAlienModels() {
    }
}
