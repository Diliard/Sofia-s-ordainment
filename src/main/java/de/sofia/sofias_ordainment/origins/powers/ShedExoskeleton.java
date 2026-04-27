package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.EffectImmunityPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

public class ShedExoskeleton extends EffectImmunityPower {
    public ShedExoskeleton(PowerType<?> type, LivingEntity entity, boolean inverted) {
        //Inverted is needed as part of the constructor but shall be ignored because ignorance is bliss✨
        super(type, entity, false);

        this.addEffect(StatusEffects.POISON);
        this.addEffect(StatusEffects.HUNGER);
    }

    @Override
    public void onGained() {
        entity.removeStatusEffect(StatusEffects.HUNGER);
        entity.removeStatusEffect(StatusEffects.POISON);
    }
}
