package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

public class DisableCritPower extends Power {
    public DisableCritPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}
