package de.sofia.sofias_ordainment.client.render;

import de.sofia.sofias_ordainment.entity.CockroachSwarmMarked;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SwarmPngRenderer extends BatEntityRenderer {

    private static final String TAG = "sofias_ordainment_cockroach_swarm";

    private static final Identifier TEXTURE = new Identifier(
            "sofias_ordainment",
            "textures/entity/swarm.png"
    );

    public SwarmPngRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            BatEntity bat,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        if (!(bat instanceof CockroachSwarmMarked marker)
                || !marker.sofias_ordainment$isCockroachSwarm()) {
            super.render(bat, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }

        matrices.push();

        // Position around the bat body
        matrices.translate(0.0D, 0.35D, 0.0D);

        // Billboard: make PNG face the camera
        matrices.multiply(MinecraftClient.getInstance()
                .getEntityRenderDispatcher()
                .getRotation());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        // PNG size in world units
        float size = 0.8F;
        matrices.scale(size, size, size);

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();

        VertexConsumer buffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(TEXTURE)
        );

        vertex(buffer, positionMatrix, normalMatrix, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F, light);
        vertex(buffer, positionMatrix, normalMatrix,  0.5F, -0.5F, 0.0F, 1.0F, 1.0F, light);
        vertex(buffer, positionMatrix, normalMatrix,  0.5F,  0.5F, 0.0F, 1.0F, 0.0F, light);
        vertex(buffer, positionMatrix, normalMatrix, -0.5F,  0.5F, 0.0F, 0.0F, 0.0F, light);

        matrices.pop();

        // Do NOT call super here.
        // Calling super would render the normal bat model too.
    }

    private static void vertex(
            VertexConsumer buffer,
            Matrix4f positionMatrix,
            Matrix3f normalMatrix,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light
    ) {
        buffer.vertex(positionMatrix, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                .next();
    }
}