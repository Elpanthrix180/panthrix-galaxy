package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.panthrixsgalaxy.PanthrixsGalaxy;
import com.panthrixsgalaxy.entity.mob.PGCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;

/** Crawlers: el modelo de la araña con texturas propias. El lunar es un poco más pequeño. */
public class PGCrawlerRenderer extends SpiderRenderer<PGCrawlerEntity> {

    private static final ResourceLocation LUNAR =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/lunar_crawler.png");
    private static final ResourceLocation MARTIAN =
            new ResourceLocation(PanthrixsGalaxy.MOD_ID, "textures/entity/mob/martian_crawler.png");

    public PGCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(PGCrawlerEntity crawler, PoseStack poseStack, float partialTick) {
        float size = crawler.isMartian() ? 1.0f : 0.85f;
        poseStack.scale(size, size, size);
    }

    @Override
    public ResourceLocation getTextureLocation(PGCrawlerEntity crawler) {
        return crawler.isMartian() ? MARTIAN : LUNAR;
    }
}
