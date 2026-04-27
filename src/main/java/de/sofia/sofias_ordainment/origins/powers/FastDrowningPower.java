package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

public class FastDrowningPower extends Power {
    public FastDrowningPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking();
    }

    @Override
    public void tick() {
        if (entity.isSubmergedInWater() && !entity.canBreatheInWater() && entity.getAir() > -20) {
            entity.setAir(entity.getAir() - 1);
        }
    }
}
