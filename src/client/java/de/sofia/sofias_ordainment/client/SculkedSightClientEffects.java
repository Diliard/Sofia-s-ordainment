package de.sofia.sofias_ordainment.client;

import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import de.sofia.sofias_ordainment.origins.utility.SculkedSightSensorPackets;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SculkedSightClientEffects {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
                SculkedSightSensorPackets.SENSOR_PING,
                (client, handler, buf, responseSender) -> {
                    var targetId = buf.readUuid();
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();

                    client.execute(() -> {
                        ClientPlayerEntity localPlayer = client.player;
                        if (localPlayer == null) return;

                        PowerHolderComponent.getPowers(localPlayer, Sculked_Sight.class)
                                .forEach(power -> power.markSensorPing(targetId, x, y, z));
                    });
                }
        );

        HudRenderCallback.EVENT.register(SculkedSightClientEffects::renderSensorCues);
    }

    private static void renderSensorCues(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity localPlayer = client.player;
        if (localPlayer == null || client.world == null) return;

        Camera camera = client.gameRenderer.getCamera();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        for (Sculked_Sight power : PowerHolderComponent.getPowers(localPlayer, Sculked_Sight.class)) {
            for (Sculked_Sight.SensorPing ping : power.getActiveSensorCues(client.world.getTime())) {
                Vec3d pos = ping.pos();
                PlayerEntity target = client.world.getPlayerByUuid(ping.targetId());
                if (target != null) {
                    pos = target.getPos().add(0.0D, target.getHeight() * 0.5D, 0.0D);
                }

                if (isInView(camera, pos)) {
                    power.dismissSensorCue(ping.targetId());
                    continue;
                }

                drawCue(context, camera, pos, width, height, ping);
            }
        }
    }

    private static boolean isInView(Camera camera, Vec3d pos) {
        double yawDiff = getYawDiff(camera, pos);
        double pitchDiff = getPitchDiff(camera, pos);

        return Math.abs(yawDiff) < 50.0D && Math.abs(pitchDiff) < 34.0D;
    }

    private static void drawCue(DrawContext context, Camera camera, Vec3d pos, int width, int height, Sculked_Sight.SensorPing ping) {
        double yawDiff = getYawDiff(camera, pos);
        double pitchDiff = getPitchDiff(camera, pos);
        int margin = 18;
        int centerX = width / 2;
        int centerY = height / 2;

        int x = centerX + (int) (MathHelper.clamp(yawDiff / 95.0D, -1.0D, 1.0D) * (centerX - margin));
        int y = centerY + (int) (MathHelper.clamp(pitchDiff / 65.0D, -1.0D, 1.0D) * (centerY - margin));
        float fade = MathHelper.clamp(1.0F - (ping.age() / (float) ping.maxAge()), 0.0F, 1.0F);
        int alpha = (int) (fade * 150.0F);
        int color = (alpha << 24) | 0x04F5D8;
        int coreColor = (Math.min(220, alpha + 50) << 24) | 0xB6FFF2;

        context.fill(x - 1, y - 6, x + 1, y + 6, color);
        context.fill(x - 6, y - 1, x + 6, y + 1, color);
        context.fill(x - 2, y - 2, x + 2, y + 2, coreColor);
    }

    private static double getYawDiff(Camera camera, Vec3d pos) {
        Vec3d toTarget = pos.subtract(camera.getPos());
        double targetYaw = Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90.0D;

        return MathHelper.wrapDegrees(targetYaw - camera.getYaw());
    }

    private static double getPitchDiff(Camera camera, Vec3d pos) {
        Vec3d toTarget = pos.subtract(camera.getPos());
        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        double targetPitch = -Math.toDegrees(Math.atan2(toTarget.y, horizontalDistance));

        return targetPitch - camera.getPitch();
    }
}
