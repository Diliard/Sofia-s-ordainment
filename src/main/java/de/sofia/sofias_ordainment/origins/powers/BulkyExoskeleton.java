package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.power.ModifyDamageTakenPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Predicate;

public class BulkyExoskeleton extends ModifyDamageTakenPower {

    public BulkyExoskeleton(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(type, entity, condition, biEntityCondition);
    }

    private static final Modifier HALF_DAMAGE = ModifierUtil.createSimpleModifier(ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE, 0.5);
    private static final Modifier EXTRA_DAMAGE = ModifierUtil.createSimpleModifier(ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE, 1.5);

    @Override
    public List<Modifier> getModifiers() {
        float threshold = entity.getMaxHealth()/2;

        if (entity.getHealth() <= threshold) { return List.of(EXTRA_DAMAGE); }

        return List.of(HALF_DAMAGE);
    }
}