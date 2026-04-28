package de.sofia.sofias_ordainment.voice;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.sofia.sofias_ordainment.Sofias_ordainment;
import de.sofia.sofias_ordainment.StateSaverAndLoader;
import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import de.sofia.sofias_ordainment.origins.utility.SculkedSightSensorPackets;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SculkedSightVoicechatPlugin implements VoicechatPlugin {
    private static final int SHRIEKER_RADIUS_BLOCKS = 8;
    private static final int SHRIEKER_CHECK_INTERVAL_TICKS = 10;
    private static final long DIRECT_VISIBILITY_INTERVAL_MS = 400L;
    private static final long SHRIEKER_VISIBILITY_INTERVAL_MS = 600L;
    private static final long THROTTLE_ENTRY_TTL_MS = 10_000L;
    private static final int EVENT_PRIORITY = -1_000;

    private final Map<VisibilityKey, Long> directVisibilityPings = new ConcurrentHashMap<>();
    private final Map<VisibilityKey, Long> shriekerVisibilityPings = new ConcurrentHashMap<>();
    private final Map<UUID, ShriekerCheck> shriekerChecks = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> relayChannels = new ConcurrentHashMap<>();
    private VoicechatServerApi voicechatApi;

    @Override
    public String getPluginId() {
        return Sofias_ordainment.MOD_ID + "_sculked_sight";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi serverApi) {
            voicechatApi = serverApi;
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(EntitySoundPacketEvent.class, this::onEntitySoundPacket, EVENT_PRIORITY);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket, EVENT_PRIORITY);
    }

    private void onEntitySoundPacket(EntitySoundPacketEvent event) {
        if (event.isCancelled()) return;
        if (SoundPacketEvent.SOURCE_PLUGIN.equals(event.getSource())) return;

        VoicechatConnection receiverConnection = event.getReceiverConnection();
        ServerPlayerEntity viewer = getServerPlayer(receiverConnection);
        if (viewer == null || !PowerHolderComponent.hasPower(viewer, Sculked_Sight.class)) return;

        UUID targetId = event.getPacket().getEntityUuid();
        if (!shouldSend(directVisibilityPings, new VisibilityKey(viewer.getUuid(), targetId), DIRECT_VISIBILITY_INTERVAL_MS)) {
            return;
        }

        MinecraftServer server = viewer.getServer();
        if (server == null) return;

        server.execute(() -> SculkedSightSensorPackets.sendVoiceNoise(viewer, targetId));
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (event.isCancelled()) return;

        VoicechatServerApi serverApi = voicechatApi;
        VoicechatConnection senderConnection = event.getSenderConnection();
        ServerPlayerEntity speaker = getServerPlayer(senderConnection);
        if (serverApi == null || senderConnection == null || speaker == null) return;

        MinecraftServer server = speaker.getServer();
        if (server == null) return;

        MicrophonePacket packet = event.getPacket();
        server.execute(() -> handleShriekerVoice(serverApi, senderConnection, speaker, packet));
    }

    private void handleShriekerVoice(
            VoicechatServerApi serverApi,
            VoicechatConnection senderConnection,
            ServerPlayerEntity speaker,
            MicrophonePacket packet
    ) {
        BlockPos shriekerPos = getNearbyShrieker(speaker);
        if (shriekerPos == null) return;

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(speaker.getServer());
        for (ServerPlayerEntity viewer : speaker.getServer().getPlayerManager().getPlayerList()) {
            if (viewer == speaker) continue;
            if (viewer.getWorld() != speaker.getWorld()) continue;
            if (!PowerHolderComponent.hasPower(viewer, Sculked_Sight.class)) continue;

            if (shouldSend(shriekerVisibilityPings, new VisibilityKey(viewer.getUuid(), speaker.getUuid()), SHRIEKER_VISIBILITY_INTERVAL_MS)) {
                SculkedSightSensorPackets.sendSensorPing(viewer, speaker);
            }

            VoicechatConnection viewerConnection = serverApi.getConnectionOf(viewer.getUuid());
            if (state.isSculkedSightShriekerRelayDisabled(viewer.getUuid())) continue;
            if (!shouldRelayShriekerAudio(serverApi, senderConnection, viewerConnection, speaker, viewer, packet)) {
                continue;
            }

            StaticSoundPacket relayPacket = packet.staticSoundPacketBuilder()
                    .channelId(getRelayChannelId(speaker.getUuid()))
                    .build();
            serverApi.sendStaticSoundPacketTo(viewerConnection, relayPacket);
        }
    }

    private boolean shouldRelayShriekerAudio(
            VoicechatServerApi serverApi,
            VoicechatConnection senderConnection,
            VoicechatConnection viewerConnection,
            ServerPlayerEntity speaker,
            ServerPlayerEntity viewer,
            MicrophonePacket packet
    ) {
        if (viewerConnection == null || !viewerConnection.isConnected() || viewerConnection.isDisabled()) return false;
        if (isSameGroup(senderConnection, viewerConnection)) return false;

        double directDistance = Math.max(1.0D, serverApi.getVoiceChatDistance());
        if (packet.isWhispering()) {
            directDistance *= 0.25D;
        }

        return viewer.squaredDistanceTo(speaker) > directDistance * directDistance;
    }

    private BlockPos getNearbyShrieker(ServerPlayerEntity speaker) {
        ServerWorld world = speaker.getServerWorld();
        long now = world.getTime();
        ShriekerCheck cached = shriekerChecks.get(speaker.getUuid());
        if (cached != null && now - cached.checkedAtTick() <= SHRIEKER_CHECK_INTERVAL_TICKS) {
            return cached.pos();
        }

        BlockPos pos = findNearestShrieker(speaker, world);
        shriekerChecks.put(speaker.getUuid(), new ShriekerCheck(now, pos));
        return pos;
    }

    private BlockPos findNearestShrieker(ServerPlayerEntity speaker, ServerWorld world) {
        BlockPos center = speaker.getBlockPos();
        int radius = SHRIEKER_RADIUS_BLOCKS;
        double maxDistanceSquared = radius * radius;
        double nearestDistanceSquared = maxDistanceSquared + 1.0D;
        BlockPos nearest = null;

        for (BlockPos pos : BlockPos.iterate(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))) {
            if (!world.getBlockState(pos).isOf(Blocks.SCULK_SHRIEKER)) continue;

            double distanceSquared = squaredDistanceToCenter(pos, speaker);
            if (distanceSquared > maxDistanceSquared || distanceSquared >= nearestDistanceSquared) continue;

            nearestDistanceSquared = distanceSquared;
            nearest = pos.toImmutable();
        }

        return nearest;
    }

    private double squaredDistanceToCenter(BlockPos pos, ServerPlayerEntity player) {
        double dx = pos.getX() + 0.5D - player.getX();
        double dy = pos.getY() + 0.5D - player.getY();
        double dz = pos.getZ() + 0.5D - player.getZ();

        return dx * dx + dy * dy + dz * dz;
    }

    private boolean shouldSend(Map<VisibilityKey, Long> pings, VisibilityKey key, long intervalMs) {
        long now = System.currentTimeMillis();
        Long lastPing = pings.get(key);
        if (lastPing != null && now - lastPing < intervalMs) return false;

        pings.put(key, now);
        cleanupOldPings(pings, now);
        return true;
    }

    private void cleanupOldPings(Map<VisibilityKey, Long> pings, long now) {
        if (pings.size() < 256) return;

        pings.entrySet().removeIf(entry -> now - entry.getValue() > THROTTLE_ENTRY_TTL_MS);
    }

    private UUID getRelayChannelId(UUID speakerId) {
        return relayChannels.computeIfAbsent(speakerId, id -> UUID.nameUUIDFromBytes(
                (Sofias_ordainment.MOD_ID + ":shrieker_voice_relay:" + id).getBytes(StandardCharsets.UTF_8)
        ));
    }

    private ServerPlayerEntity getServerPlayer(VoicechatConnection connection) {
        if (connection == null) return null;

        Object player = connection.getPlayer().getPlayer();
        if (player instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer;
        }
        return null;
    }

    private boolean isSameGroup(VoicechatConnection senderConnection, VoicechatConnection viewerConnection) {
        Group senderGroup = senderConnection.getGroup();
        Group viewerGroup = viewerConnection.getGroup();

        return senderGroup != null && viewerGroup != null && senderGroup.getId().equals(viewerGroup.getId());
    }

    private record VisibilityKey(UUID viewerId, UUID targetId) {
    }

    private record ShriekerCheck(long checkedAtTick, BlockPos pos) {
    }
}
