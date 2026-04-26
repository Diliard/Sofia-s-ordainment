package de.sofia.sofias_ordainment.origins.utility;

import de.sofia.sofias_ordainment.Sofias_ordainment;
import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SculkedSightSensorPackets {
    public static final Identifier SENSOR_PING = new Identifier(Sofias_ordainment.MOD_ID, "sculk_sensor_ping");

    public static void sendSensorPing(ServerPlayerEntity target) {
        if (target.getServer() == null) return;

        for (ServerPlayerEntity viewer : target.getServer().getPlayerManager().getPlayerList()) {
            if (viewer == target) continue;
            if (viewer.getWorld() != target.getWorld()) continue;
            if (!PowerHolderComponent.hasPower(viewer, Sculked_Sight.class)) continue;
            if (!ServerPlayNetworking.canSend(viewer, SENSOR_PING)) continue;

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeUuid(target.getUuid());
            buf.writeDouble(target.getX());
            buf.writeDouble(target.getBodyY(0.5D));
            buf.writeDouble(target.getZ());

            ServerPlayNetworking.send(viewer, SENSOR_PING, buf);
        }
    }
}
