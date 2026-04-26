package de.sofia.sofias_ordainment.origins.abstractPowers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public abstract class AbstractOnHitPower extends Power {

    public AbstractOnHitPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public abstract void onHit(Entity target, float damage);
}

