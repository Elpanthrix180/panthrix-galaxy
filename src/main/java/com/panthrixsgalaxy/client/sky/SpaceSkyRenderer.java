package com.panthrixsgalaxy.client.sky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.panthrixsgalaxy.entity.rocket.PGRocketEntity;
import com.panthrixsgalaxy.planet.PGPlanet;
import com.panthrixsgalaxy.planet.PGPlanets;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dibuja el cielo del Espacio:
 *   - 2 000 estrellas fijas de distintos tamaños y brillos.
 *   - El Sol.
 *   - Los planetas (PGPlanets.ALL), cada uno en su dirección real y con un tamaño que
 *     depende de la distancia: al acercarte a la Luna, se hace cada vez más grande.
 *
 * Todo se dibuja en una esfera imaginaria de radio 100 alrededor de la cámara.
 */
public final class SpaceSkyRenderer {

    private static final float SKY_RADIUS = 100.0f;
    private static final int STAR_COUNT = 2000;
    private static final ResourceLocation SUN = new ResourceLocation("textures/environment/sun.png");
    private static final Vector3f SUN_DIRECTION = new Vector3f(0.5f, 0.45f, -0.75f).normalize();

    /** Estrellas: dirección + tamaño + brillo (se calculan una sola vez). */
    private record Star(Vector3f direction, float size, float brightness) {
    }

    private static final List<Star> STARS = createStars();

    private static List<Star> createStars() {
        RandomSource random = RandomSource.create(10842L);
        List<Star> stars = new ArrayList<>();
        while (stars.size() < STAR_COUNT) {
            float x = random.nextFloat() * 2.0f - 1.0f;
            float y = random.nextFloat() * 2.0f - 1.0f;
            float z = random.nextFloat() * 2.0f - 1.0f;
            float lengthSquared = x * x + y * y + z * z;
            if (lengthSquared > 0.01f && lengthSquared < 1.0f) {
                stars.add(new Star(new Vector3f(x, y, z).normalize(),
                        0.12f + random.nextFloat() * 0.22f, 0.5f + random.nextFloat() * 0.5f));
            }
        }
        return stars;
    }

    public static void render(PoseStack poseStack, Camera camera) {
        Matrix4f matrix = poseStack.last().pose();
        FogRenderer.setupNoFog();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        renderStars(matrix);
        drawTexturedQuad(matrix, SUN_DIRECTION, 12.0f, SUN);
        renderPlanets(matrix, camera.getPosition());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private static void renderStars(Matrix4f matrix) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (Star star : STARS) {
            Vector3f[] corners = quadCorners(star.direction(), star.size());
            float b = star.brightness();
            for (Vector3f corner : corners) {
                builder.vertex(matrix, corner.x(), corner.y(), corner.z()).color(b, b, b, 1.0f).endVertex();
            }
        }
        BufferUploader.drawWithShader(builder.end());
    }

    /** Dibuja los planetas del más lejano al más cercano. */
    private static void renderPlanets(Matrix4f matrix, Vec3 cameraPos) {
        BlockPos origin = BlockPos.ZERO;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getVehicle() instanceof PGRocketEntity rocket) {
            origin = rocket.getSpaceOrigin();
        }
        Vec3 originCenter = Vec3.atCenterOf(origin);
        List<PGPlanet> planets = new ArrayList<>(PGPlanets.ALL);
        planets.sort(Comparator.comparingDouble(
                (PGPlanet planet) -> originCenter.add(planet.spaceOffset()).distanceTo(cameraPos)).reversed());

        for (PGPlanet planet : planets) {
            Vec3 toPlanet = originCenter.add(planet.spaceOffset()).subtract(cameraPos);
            double distance = toPlanet.length();
            if (distance < 1.0) {
                continue;
            }
            // Tamaño aparente: cuanto más cerca, más grande (con un máximo para que no tape todo)
            float halfSize = (float) Math.min(SKY_RADIUS * 1.6, SKY_RADIUS * planet.radius() / distance);
            halfSize = Math.max(halfSize, 1.5f);
            Vector3f direction = new Vector3f((float) toPlanet.x, (float) toPlanet.y, (float) toPlanet.z).normalize();
            drawTexturedQuad(matrix, direction, halfSize, planet.texture());
        }
    }

    private static void drawTexturedQuad(Matrix4f matrix, Vector3f direction, float halfSize, ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Vector3f[] corners = quadCorners(direction, halfSize);
        float[][] uvs = {{0, 1}, {1, 1}, {1, 0}, {0, 0}};
        for (int i = 0; i < 4; i++) {
            builder.vertex(matrix, corners[i].x(), corners[i].y(), corners[i].z()).uv(uvs[i][0], uvs[i][1]).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());
    }

    /** Las 4 esquinas de un cuadrado que mira a la cámara, en la esfera del cielo. */
    private static Vector3f[] quadCorners(Vector3f direction, float halfSize) {
        Vector3f center = new Vector3f(direction).mul(SKY_RADIUS);
        Vector3f helper = Math.abs(direction.y()) > 0.99f ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f(direction).cross(helper).normalize().mul(halfSize);
        Vector3f up = new Vector3f(right).cross(direction).normalize().mul(halfSize);
        return new Vector3f[]{
                new Vector3f(center).sub(right).sub(up),
                new Vector3f(center).add(right).sub(up),
                new Vector3f(center).add(right).add(up),
                new Vector3f(center).sub(right).add(up)
        };
    }

    private SpaceSkyRenderer() {
    }
}
