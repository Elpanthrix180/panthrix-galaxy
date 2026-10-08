package com.panthrixsgalaxy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.panthrixsgalaxy.entity.laser.PGLaserBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Dibuja el rayo láser como una barra de luz: un núcleo blanco y fino dentro de un
 * "halo" del color del arma. Usa el mismo tipo de dibujo que los rayos de las tormentas
 * (brilla en la oscuridad y no necesita textura).
 */
public class PGLaserBoltRenderer extends EntityRenderer<PGLaserBoltEntity> {

    /** Largo del rayo dibujado (bloques). */
    private static final float LENGTH = 1.6f;

    public PGLaserBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PGLaserBoltEntity bolt, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        Vec3 motion = bolt.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 direction = motion.normalize();
        // Los primeros ticks el rayo es más corto (para que no salga por detrás del arma)
        float length = Math.min(LENGTH, (bolt.tickCount + partialTick) * (float) motion.length());

        poseStack.pushPose();
        // Girar para que el eje Z apunte en la dirección del rayo
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(direction.x, direction.z)));
        poseStack.mulPose(Axis.XP.rotation((float) -Math.asin(direction.y)));

        VertexConsumer vertices = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        int color = bolt.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        beam(vertices, matrix, 0.09f, length, red, green, blue, 0.55f);   // halo de color
        beam(vertices, matrix, 0.035f, length, 1.0f, 1.0f, 1.0f, 0.9f);  // núcleo blanco
        poseStack.popPose();
        super.render(bolt, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /** Una caja alargada desde z = -length (cola) hasta z = 0 (punta). Cada cara por las dos caras. */
    private static void beam(VertexConsumer v, Matrix4f m, float half, float length, float r, float g, float b, float a) {
        float z0 = -length;
        float z1 = 0.0f;
        // arriba, abajo, izquierda, derecha
        quad(v, m, -half, half, z0, half, half, z0, half, half, z1, -half, half, z1, r, g, b, a);
        quad(v, m, -half, -half, z0, -half, -half, z1, half, -half, z1, half, -half, z0, r, g, b, a);
        quad(v, m, -half, -half, z0, -half, half, z0, -half, half, z1, -half, -half, z1, r, g, b, a);
        quad(v, m, half, -half, z0, half, -half, z1, half, half, z1, half, half, z0, r, g, b, a);
    }

    private static void quad(VertexConsumer v, Matrix4f m,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        // Por delante...
        v.vertex(m, x1, y1, z1).color(r, g, b, a).endVertex();
        v.vertex(m, x2, y2, z2).color(r, g, b, a).endVertex();
        v.vertex(m, x3, y3, z3).color(r, g, b, a).endVertex();
        v.vertex(m, x4, y4, z4).color(r, g, b, a).endVertex();
        // ...y por detrás (así se ve desde cualquier lado)
        v.vertex(m, x4, y4, z4).color(r, g, b, a).endVertex();
        v.vertex(m, x3, y3, z3).color(r, g, b, a).endVertex();
        v.vertex(m, x2, y2, z2).color(r, g, b, a).endVertex();
        v.vertex(m, x1, y1, z1).color(r, g, b, a).endVertex();
    }

    /** No usa textura (el dibujo es solo color). */
    @Override
    public ResourceLocation getTextureLocation(PGLaserBoltEntity bolt) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
