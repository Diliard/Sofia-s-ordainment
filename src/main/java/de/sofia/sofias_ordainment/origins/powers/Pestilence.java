package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.ActionOnHitPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Pair;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Pestilence extends ActionOnHitPower {
    public Pestilence(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Pair<Entity, Entity>> bientityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity, cooldownDuration, hudRender, damageCondition, bientityAction, bientityCondition);
    }

    @Override
    public void onHit(Entity target, DamageSource damageSource, float damageAmount) {
        Random rdm = new Random();
        int roll = rdm.nextInt( 1,101);
        StatusEffectInstance instance = new StatusEffectInstance(StatusEffects.POISON, 60);

        if (target instanceof LivingEntity livingEntity) {
            if (damageSource.getSource() instanceof ArrowEntity) {
                if (roll <= 40) {
                    livingEntity.addStatusEffect(instance);
                }
            } else if (damageSource.getSource() instanceof PlayerEntity) {
                if (roll <= 20) {
                    livingEntity.addStatusEffect(instance);
                }
            }
        }

        super.onHit(target, damageSource, damageAmount);
    }
}
