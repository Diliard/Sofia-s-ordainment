package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

public class FireSensitivityPower extends Power {
    private int fireDamageTimer;

    public FireSensitivityPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking();
    }

    @Override
    public void tick() {
        if (entity.getWorld().isClient) return;

        if (!entity.isOnFire()) {
            fireDamageTimer = 0;
            return;
        }

        fireDamageTimer++;
        if (fireDamageTimer >= 20) {
            fireDamageTimer = 0;
            entity.damage(entity.getDamageSources().onFire(), 1.0F);
        }
    }
}
