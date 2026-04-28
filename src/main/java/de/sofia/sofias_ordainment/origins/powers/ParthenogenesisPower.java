package de.sofia.sofias_ordainment.origins.powers;

import de.sofia.sofias_ordainment.entity.CockroachSwarmMarked;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ParthenogenesisPower extends CooldownPower implements Active {
    private static final String SWARM_TAG = "sofias_ordainment_cockroach_swarm";
    private static final String OWNER_TAG_PREFIX = "sofias_ordainment_owner_";
    private static final String SLOT_TAG_PREFIX = "sofias_ordainment_swarm_slot_";
    private static final int SWARM_COUNT = 3;
    private static final float SWARM_HEALTH = 15.0F;
    private static final float SWARM_DAMAGE = 2.0F;
    private static final double SWARM_SPEED = 0.45D;
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int FAILED_ATTACK_RETRY_TICKS = 2;
    private static final int ATTACK_STAGGER_TICKS = 6;

    private static final Map<UUID, Integer> attackCooldowns = new HashMap<>();
    private static final Map<UUID, UUID> lastOwnerTargets = new HashMap<>();
    private static boolean registeredEvents;

    private Key key = new Key();

    public ParthenogenesisPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
        super(type, entity, cooldownDuration, hudRender);
    }

    public static void registerEvents() {
        if (registeredEvents) return;
        registeredEvents = true;
        ServerLifecycleEvents.SERVER_STARTED.register(ParthenogenesisPower::cleanupPersistedSwarms);
        ServerTickEvents.END_SERVER_TICK.register(ParthenogenesisPower::tickSwarms);
    }

    @Override
    public void onUse() {
        if (!this.canUse() || !(entity instanceof PlayerEntity player) || !(entity.getWorld() instanceof ServerWorld world)) {
            return;
        }

        clearOwnerTarget(entity.getUuid());
        discardSwarmsOwnedBy(world, entity.getUuid());

        int spawned = 0;
        for (int i = 0; i < SWARM_COUNT; i++) {
            BatEntity swarm = EntityType.BAT.create(world);
            if (swarm == null) continue;

            double angle = Math.toRadians(entity.getYaw()) + i * (Math.PI * 2.0D / SWARM_COUNT);
            double offsetX = Math.cos(angle) * 0.7D;
            double offsetZ = Math.sin(angle) * 0.7D;

            swarm.refreshPositionAndAngles(entity.getX() + offsetX, entity.getY() + 1.0D, entity.getZ() + offsetZ, entity.getYaw(), 0.0F);
            swarm.setRoosting(false);
            swarm.setNoGravity(true);
            swarm.setPersistent();
            //swarm.setInvisible(true);
            swarm.setCustomName(Text.literal("Cockroach Swarm"));
            swarm.addCommandTag(SWARM_TAG);
            swarm.addCommandTag(OWNER_TAG_PREFIX + entity.getUuidAsString());
            swarm.addCommandTag(SLOT_TAG_PREFIX + i);
            setAttribute(swarm, EntityAttributes.GENERIC_MAX_HEALTH, SWARM_HEALTH);
            swarm.setHealth(SWARM_HEALTH);

            world.spawnEntity(swarm);
            attackCooldowns.put(swarm.getUuid(), i * ATTACK_STAGGER_TICKS);
            spawned++;
        }

        if (spawned == 0) return;

        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS, 1.5F, 0.75F);

        this.use();
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public void rememberOwnerHit(LivingEntity target) {
        if (target == entity || isSwarm(target)) return;

        lastOwnerTargets.put(entity.getUuid(), target.getUuid());
    }

    private static void tickSwarms(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof BatEntity swarm) || !isSwarm(swarm)) continue;

                Optional<UUID> ownerUuid = getOwnerUuid(swarm);
                if (ownerUuid.isEmpty()) {
                    swarm.discard();
                    continue;
                }

                ServerPlayerEntity owner = server.getPlayerManager().getPlayer(ownerUuid.get());
                if (owner == null || owner.isRemoved()) {
                    clearOwnerTargetForSwarm(swarm);
                    attackCooldowns.remove(swarm.getUuid());
                    swarm.discard();
                    continue;
                }

                swarm.setRoosting(false);
                swarm.setNoGravity(true);
                Optional<LivingEntity> target = findTarget(world, owner, swarm);
                setSwarmHasTarget(swarm, target.isPresent());
                if (target.isPresent()) {
                    moveToward(swarm, target.get().getEyePos());
                    tryAttack(world, swarm, owner, target.get());
                } else {
                    moveToward(swarm, getOwnerSlotPos(owner, swarm, server.getTicks()));
                }

                if (server.getTicks() % 8 == 0) {
                    world.spawnParticles(ParticleTypes.SMOKE, swarm.getX(), swarm.getBodyY(0.5D), swarm.getZ(),
                            3, 0.25D, 0.2D, 0.25D, 0.01D);
                }
            }
        }
    }

    private static void cleanupPersistedSwarms(MinecraftServer server) {
        attackCooldowns.clear();
        lastOwnerTargets.clear();

        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (isSwarm(entity)) {
                    entity.discard();
                }
            }
        }
    }

    private static void discardSwarmsOwnedBy(ServerWorld world, UUID ownerUuid) {
        for (Entity entity : world.iterateEntities()) {
            if (!isSwarm(entity)) continue;

            Optional<UUID> swarmOwnerUuid = getOwnerUuid(entity);
            if (swarmOwnerUuid.isPresent() && swarmOwnerUuid.get().equals(ownerUuid)) {
                attackCooldowns.remove(entity.getUuid());
                setSwarmHasTarget(entity, false);
                entity.discard();
            }
        }
    }

    public static void clearOwnerTargetForSwarm(Entity swarm) {
        if (!isSwarm(swarm)) return;

        getOwnerUuid(swarm).ifPresent(ParthenogenesisPower::clearOwnerTarget);
        attackCooldowns.remove(swarm.getUuid());
        setSwarmHasTarget(swarm, false);
    }

    public static void clearOwnerTarget(UUID ownerUuid) {
        lastOwnerTargets.remove(ownerUuid);
    }

    public static void clearTargetReferencesTo(UUID targetUuid) {
        lastOwnerTargets.entrySet().removeIf(entry -> entry.getValue().equals(targetUuid));
    }

    private static boolean isSwarm(Entity entity) {
        return entity.getCommandTags().contains(SWARM_TAG);
    }

    private static Optional<UUID> getOwnerUuid(Entity entity) {
        return entity.getCommandTags().stream()
                .filter(tag -> tag.startsWith(OWNER_TAG_PREFIX))
                .map(tag -> tag.substring(OWNER_TAG_PREFIX.length()))
                .findFirst()
                .flatMap(ParthenogenesisPower::parseUuid);
    }

    private static int getSwarmSlot(Entity entity) {
        return entity.getCommandTags().stream()
                .filter(tag -> tag.startsWith(SLOT_TAG_PREFIX))
                .map(tag -> tag.substring(SLOT_TAG_PREFIX.length()))
                .findFirst()
                .flatMap(ParthenogenesisPower::parseInt)
                .map(slot -> Math.floorMod(slot, SWARM_COUNT))
                .orElse(Math.floorMod(entity.getUuid().hashCode(), SWARM_COUNT));
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<LivingEntity> findTarget(ServerWorld world, ServerPlayerEntity owner, BatEntity swarm) {
        return findPriorityTarget(world, owner, swarm);
    }

    private static Optional<LivingEntity> findPriorityTarget(ServerWorld world, ServerPlayerEntity owner, BatEntity swarm) {
        UUID targetUuid = lastOwnerTargets.get(owner.getUuid());
        if (targetUuid == null) return Optional.empty();

        Entity entity = world.getEntity(targetUuid);
        if (!(entity instanceof LivingEntity target)) {
            return Optional.empty();
        }

        if (!target.isAlive()) {
            lastOwnerTargets.remove(owner.getUuid());
            return Optional.empty();
        }

        if (target == owner || target == swarm || target.isSpectator() || isSwarm(target)) {
            return Optional.empty();
        }

        return Optional.of(target);
    }

    private static void setSwarmHasTarget(Entity swarm, boolean hasTarget) {
        if (swarm instanceof CockroachSwarmMarked marker) {
            marker.sofias_ordainment$setCockroachSwarmTarget(hasTarget);
        }
    }

    private static Vec3d getOwnerSlotPos(ServerPlayerEntity owner, BatEntity swarm, int ticks) {
        int slot = getSwarmSlot(swarm);
        double angle = Math.toRadians(owner.getYaw()) + slot * (Math.PI * 2.0D / SWARM_COUNT);
        double hover = Math.sin((ticks + slot * 13) * 0.12D) * 0.12D;

        return new Vec3d(
                owner.getX() + Math.cos(angle) * 1.35D,
                owner.getY() + 1.65D + hover,
                owner.getZ() + Math.sin(angle) * 1.35D
        );
    }

    private static void moveToward(BatEntity swarm, Vec3d targetPos) {
        Vec3d toTarget = targetPos.subtract(swarm.getPos());
        double distance = toTarget.length();
        if (distance < 0.12D) {
            swarm.setVelocity(Vec3d.ZERO);
            swarm.velocityModified = true;
            return;
        }

        Vec3d velocity = toTarget.normalize().multiply(Math.min(SWARM_SPEED, distance * 0.35D));
        swarm.setVelocity(velocity);
        swarm.velocityModified = true;
    }

    private static void tryAttack(ServerWorld world, BatEntity swarm, ServerPlayerEntity owner, LivingEntity target) {
        int cooldown = attackCooldowns.getOrDefault(swarm.getUuid(), 0);
        if (cooldown > 0) {
            attackCooldowns.put(swarm.getUuid(), cooldown - 1);
            return;
        }

        if (target == owner || target == swarm || swarm.distanceTo(target) > 1.35F) return;

        if (target.damage(swarm.getDamageSources().mobAttack(swarm), SWARM_DAMAGE)) {
            owner.heal(SWARM_DAMAGE);
            if (swarm.getRandom().nextFloat() < 0.1F) {
                target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.POISON, 60, 0), swarm);
            }
            world.spawnParticles(ParticleTypes.POOF, target.getX(), target.getBodyY(0.5D), target.getZ(),
                    6, 0.2D, 0.2D, 0.2D, 0.02D);
            attackCooldowns.put(swarm.getUuid(), ATTACK_COOLDOWN_TICKS);
        } else {
            attackCooldowns.put(swarm.getUuid(), FAILED_ATTACK_RETRY_TICKS);
        }
    }

    private static void setAttribute(LivingEntity entity, net.minecraft.entity.attribute.EntityAttribute attribute, double value) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    public static boolean isOwnerDamagingOwnSwarm(Entity target, DamageSource source) {
        if (!isSwarm(target) || source.getAttacker() == null) return false;

        Optional<UUID> ownerUuid = getOwnerUuid(target);
        return ownerUuid.isPresent() && ownerUuid.get().equals(source.getAttacker().getUuid());
    }
}
