package de.sofia.sofias_ordainment.origins.utility;

import de.sofia.sofias_ordainment.Sofias_ordainment;
import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class SculkedSightSensorPackets {
    public static final Identifier SENSOR_PING = new Identifier(Sofias_ordainment.MOD_ID, "sculk_sensor_ping");
    public static final Identifier VOICE_NOISE = new Identifier(Sofias_ordainment.MOD_ID, "sculked_sight_voice_noise");

    public static void sendSensorPing(ServerPlayerEntity target) {
        if (target.getServer() == null) return;

        for (ServerPlayerEntity viewer : target.getServer().getPlayerManager().getPlayerList()) {
            sendSensorPing(viewer, target);
        }
    }

    public static void sendSensorPing(ServerPlayerEntity viewer, ServerPlayerEntity target) {
        if (viewer == target) return;
        if (viewer.getWorld() != target.getWorld()) return;
        if (!PowerHolderComponent.hasPower(viewer, Sculked_Sight.class)) return;
        if (!ServerPlayNetworking.canSend(viewer, SENSOR_PING)) return;

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(target.getUuid());
        buf.writeDouble(target.getX());
        buf.writeDouble(target.getBodyY(0.5D));
        buf.writeDouble(target.getZ());

        ServerPlayNetworking.send(viewer, SENSOR_PING, buf);
    }

    public static void sendVoiceNoise(ServerPlayerEntity viewer, UUID targetId) {
        if (viewer.getUuid().equals(targetId)) return;
        if (!PowerHolderComponent.hasPower(viewer, Sculked_Sight.class)) return;
        if (!ServerPlayNetworking.canSend(viewer, VOICE_NOISE)) return;

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(targetId);

        ServerPlayNetworking.send(viewer, VOICE_NOISE, buf);
    }
}
