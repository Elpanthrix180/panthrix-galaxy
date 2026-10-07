package com.panthrixsgalaxy.client.renderer;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.client.model.PGAlienCreatureModel;
import com.panthrixsgalaxy.entity.mob.PGAlienCreatureEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PGAlienCreatureRenderer extends MobRenderer<PGAlienCreatureEntity, PGAlienCreatureModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/alien_creature.png");

    public PGAlienCreatureRenderer(EntityRendererProvider.Context context) {
        super(context, new PGAlienCreatureModel(context.bakeLayer(PGAlienCreatureModel.LAYER_LOCATION)), 0.6f);
    }

    @Override
    public ResourceLocation getTextureLocation(PGAlienCreatureEntity creature) {
        return TEXTURE;
    }
}
