package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class LowHealthSwiftnessPower extends Power {
    public LowHealthSwiftnessPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking();
    }

    @Override
    public void tick() {
        if (entity.getHealth() <= entity.getMaxHealth() / 2.0F) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, true, false, true));
        }
    }
}
