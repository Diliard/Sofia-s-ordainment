package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Screech extends CooldownPower implements Active {
    private Key key = new Key();

    public Screech(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
        super(type, entity, cooldownDuration, hudRender);
    }


    @Override
    public void onUse() {
        if (this.canUse()) {
            if (entity instanceof PlayerEntity player) {
                if (player.experienceLevel >= 5) {
                    entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0F, 1.0F);

                    if (entity.getWorld() instanceof ServerWorld world) {
                        Vec3d look = entity.getRotationVec(1.0F);
                        Vec3d start = entity.getEyePos();

                        for (int i = 1; i <= 15; i++) {
                            Vec3d particlePos = start.add(look.multiply(i));

                            world.spawnParticles(ParticleTypes.SONIC_BOOM,
                                    particlePos.x, particlePos.y, particlePos.z,
                                    1, 0, 0, 0, 0);
                        }
                    }
                    double range = 15.0;
                    Vec3d look = entity.getRotationVec(1.0F);
                    Vec3d start = entity.getEyePos();
                    Vec3d end = start.add(look.multiply(range));

                    Box searchBox = new Box(start, end).expand(0.5);
                    Vec3d entityPos = entity.getPos();

                    List<Entity> targets = entity.getWorld().getOtherEntities(entity, searchBox);

                    for (Entity target : targets) {

                        Vec3d targetPos = target.getPos();

                        if (isPointNearLine(start, end, targetPos, 2.0)) {
                            Vec3d diff = targetPos.subtract(start).normalize();
                            target.addVelocity(diff.x * 2.0, 0.5, diff.z * 2.0);
                            target.velocityModified = true;
                        }
                    }

                    if (targets.isEmpty()) {
                        Vec3d backward = look.multiply(-1.5);
                        entity.addVelocity(backward.x * 2.0, backward.y, backward.z * 2.0);
                        entity.velocityModified = true;
                    }

                    player.experienceLevel = player.experienceLevel - 5;
                    this.use();
                }
            }
        }
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    private boolean isPointNearLine(Vec3d lineStart, Vec3d lineEnd, Vec3d point, double radius) {
        Vec3d lineVec = lineEnd.subtract(lineStart);
        Vec3d pointVec = point.subtract(lineStart);

        double projection = pointVec.dotProduct(lineVec) / lineVec.lengthSquared();

        if (projection < 0.0 || projection > 1.0) return false;

        Vec3d closestPoint = lineStart.add(lineVec.multiply(projection));
        return point.distanceTo(closestPoint) <= radius;
    }
}
