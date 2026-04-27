package de.sofia.sofias_ordainment.origins.powers;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.mixin.EntityGetMoveEffectInvoker;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Sculked_Sight extends Power {
    private static final int NOISE_GRACE_TICKS = 30;
    private static final int NOISE_HISTORY_TICKS = 200;
    private static final int DISAPPEAR_CUE_TICKS = 24;
    private static final int SENSOR_GLOW_TICKS = 120;
    private static final int SENSOR_CUE_TICKS = 90;
    private static final int SENSOR_HISTORY_TICKS = 180;
    private static final int[] SENSOR_GLOW_COLORS = {
            0x04F5D8,
            0x0AA6A7,
            0x063F70,
            0x1E5EA8,
            0x04F5D8
    };

    private final Object2LongMap<UUID> lastNoiseTicks = new Object2LongOpenHashMap<>();
    private final Object2LongMap<UUID> lastSensorTicks = new Object2LongOpenHashMap<>();
    private final Object2LongMap<UUID> lastSensorCueTicks = new Object2LongOpenHashMap<>();
    private final Object2LongMap<UUID> lastDisappearCueTicks = new Object2LongOpenHashMap<>();
    private final Map<UUID, Vec3d> sensorPingPositions = new HashMap<>();
    private final Map<UUID, Vec3d> disappearCuePositions = new HashMap<>();
    private final Map<UUID, Boolean> hiddenStates = new HashMap<>();

    public Sculked_Sight(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public boolean shouldHide(PlayerEntity target) {
        if (target == entity) return false;
        if (target.hasStatusEffect(RegistryHelper.SCULKIFIED)) return false;

        long now = target.getWorld().getTime();
        UUID targetId = target.getUuid();

        if (isNoisyMovement(target)) {
            lastNoiseTicks.put(targetId, now);
        }

        if (now % 100 == 0) {
            cleanup(now);
        }

        if (isSensorHighlighted(target, now)) return false;

        boolean shouldHide = !lastNoiseTicks.containsKey(targetId)
                || now - lastNoiseTicks.getLong(targetId) > NOISE_GRACE_TICKS;
        boolean wasHidden = hiddenStates.getOrDefault(targetId, true);

        if (shouldHide && !wasHidden) {
            lastDisappearCueTicks.put(targetId, now);
            disappearCuePositions.put(targetId, target.getPos().add(0.0D, target.getHeight() * 0.5D, 0.0D));
        } else if (!shouldHide) {
            lastDisappearCueTicks.removeLong(targetId);
            disappearCuePositions.remove(targetId);
        }

        hiddenStates.put(targetId, shouldHide);
        return shouldHide;
    }

    public void markNoiseNear(double x, double y, double z, double radius) {
        long now = entity.getWorld().getTime();
        Box box = new Box(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);

        entity.getWorld().getEntitiesByClass(PlayerEntity.class, box, target -> target != entity)
                .forEach(target -> lastNoiseTicks.put(target.getUuid(), now));
    }

    public void markSensorPing(UUID targetId, double x, double y, double z) {
        long now = entity.getWorld().getTime();

        lastSensorTicks.put(targetId, now);
        lastSensorCueTicks.put(targetId, now);
        sensorPingPositions.put(targetId, new Vec3d(x, y, z));
        cleanup(now);
    }

    public boolean isSensorHighlighted(Entity target) {
        return isSensorHighlighted(target, target.getWorld().getTime());
    }

    public int getSensorGlowColor(Entity target) {
        long now = target.getWorld().getTime();
        long lastPing = lastSensorTicks.getLong(target.getUuid());
        long age = now - lastPing;
        int speed = Math.max(4, 12 - (int) Math.min(age / 12, 6));
        int index = (int) (((now / speed) + Math.floorMod(target.getId(), SENSOR_GLOW_COLORS.length)) % SENSOR_GLOW_COLORS.length);

        return snapColor(SENSOR_GLOW_COLORS[index]);
    }

    public List<SensorPing> getActiveSensorCues(long now) {
        cleanup(now);

        List<SensorPing> pings = new ArrayList<>();
        lastSensorCueTicks.object2LongEntrySet().forEach(entry -> {
            Vec3d pos = sensorPingPositions.get(entry.getKey());
            if (pos != null) {
                pings.add(new SensorPing(entry.getKey(), pos, now - entry.getLongValue(), SENSOR_CUE_TICKS));
            }
        });

        return pings;
    }

    public List<SensorPing> getActiveDisappearCues(long now) {
        cleanup(now);

        List<SensorPing> pings = new ArrayList<>();
        lastDisappearCueTicks.object2LongEntrySet().forEach(entry -> {
            Vec3d pos = disappearCuePositions.get(entry.getKey());
            if (pos != null) {
                pings.add(new SensorPing(entry.getKey(), pos, now - entry.getLongValue(), DISAPPEAR_CUE_TICKS));
            }
        });

        return pings;
    }

    public void dismissSensorCue(UUID targetId) {
        lastSensorCueTicks.removeLong(targetId);
    }

    private boolean isNoisyMovement(PlayerEntity target) {
        if (target.isSneaking()) return false;

        double dx = target.getX() - target.prevX;
        double dy = target.getY() - target.prevY;
        double dz = target.getZ() - target.prevZ;
        boolean moved = (dx * dx + dy * dy + dz * dz) > 0.0004D;
        if (!moved) return false;

        Entity.MoveEffect effect = ((EntityGetMoveEffectInvoker) target).invokeGetMoveEffect();
        return effect.hasAny();
    }

    private boolean isSensorHighlighted(Entity target, long now) {
        UUID targetId = target.getUuid();
        return lastSensorTicks.containsKey(targetId)
                && now - lastSensorTicks.getLong(targetId) <= SENSOR_GLOW_TICKS;
    }

    private void cleanup(long now) {
        lastNoiseTicks.object2LongEntrySet()
                .removeIf(entry -> now - entry.getLongValue() > NOISE_HISTORY_TICKS);
        lastSensorTicks.object2LongEntrySet()
                .removeIf(entry -> now - entry.getLongValue() > SENSOR_HISTORY_TICKS);
        lastSensorCueTicks.object2LongEntrySet()
                .removeIf(entry -> now - entry.getLongValue() > SENSOR_CUE_TICKS);
        lastDisappearCueTicks.object2LongEntrySet()
                .removeIf(entry -> now - entry.getLongValue() > DISAPPEAR_CUE_TICKS);
        sensorPingPositions.keySet().removeIf(targetId ->
                !lastSensorTicks.containsKey(targetId) && !lastSensorCueTicks.containsKey(targetId));
        disappearCuePositions.keySet().removeIf(targetId -> !lastDisappearCueTicks.containsKey(targetId));
        hiddenStates.keySet().removeIf(targetId ->
                !lastNoiseTicks.containsKey(targetId)
                        && !lastSensorTicks.containsKey(targetId)
                        && !lastDisappearCueTicks.containsKey(targetId));
    }

    private int snapColor(int color) {
        int r = ((color >> 16) & 0xFF) & 0xF0;
        int g = ((color >> 8) & 0xFF) & 0xF0;
        int b = (color & 0xFF) & 0xF0;

        return (r << 16) | (g << 8) | b;
    }

    public record SensorPing(UUID targetId, Vec3d pos, long age, int maxAge) {
    }
}
