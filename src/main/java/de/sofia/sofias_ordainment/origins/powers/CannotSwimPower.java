package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class CannotSwimPower extends Power {
    public CannotSwimPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking();
    }

    @Override
    public void tick() {
        if (!entity.isTouchingWater()) return;

        entity.setSwimming(false);
        Vec3d velocity = entity.getVelocity();
        double sinkVelocity = Math.min(velocity.y, -0.08D);
        entity.setVelocity(velocity.x, sinkVelocity, velocity.z);
        entity.velocityModified = true;
    }
}
