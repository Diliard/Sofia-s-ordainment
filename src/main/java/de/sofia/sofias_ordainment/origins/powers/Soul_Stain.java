package de.sofia.sofias_ordainment.origins.powers;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.abstractPowers.AbstractOnHitPower;
import de.sofia.sofias_ordainment.origins.utility.ModTags;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Map;
import java.util.WeakHashMap;

public class Soul_Stain extends AbstractOnHitPower {
    private final Random RDM = Random.create();
    private final Map<LivingEntity, Integer> SOURCES = new WeakHashMap<>();
    public Soul_Stain(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    @Override
    public void onHit(Entity target, float damage) {
        float chance = isinSculk() ? 50 : (10+damage/2);

        if (getParticipatingStackCount() >= 5) chance = chance*2;

        if (target instanceof LivingEntity livingEntity) {
            if (RDM.nextBetween(0, 100) <= chance) {
                int amplifier = livingEntity.getStatusEffect(RegistryHelper.SCULKIFIED) != null
                        ? livingEntity.getStatusEffect(RegistryHelper.SCULKIFIED).getAmplifier() : -1;

                if (amplifier >= 9) return;

                int newAmplifier = amplifier + 1;
                SOURCES.put(livingEntity, newAmplifier + 1);
                livingEntity.addStatusEffect(new StatusEffectInstance(RegistryHelper.SCULKIFIED, 600, newAmplifier));
                SculkifiedSourceTrackerHelper.setTargetStackCount(livingEntity, newAmplifier + 1);
            }
        }
    }

    private boolean isinSculk() {
        final Box BOX = this.entity.getBoundingBox().expand(5);
        return this.entity.getWorld().getStatesInBox(BOX).anyMatch(state -> state.isIn(ModTags.SCULK_BLOCKS));
    }

    public static boolean canSeeGlow(ServerPlayerEntity viewer) {
        return PowerHolderComponent.hasPower(viewer, Soul_Stain.class);
    }

    public static boolean shouldGlow(Entity target) {
        return target instanceof LivingEntity living &&
                living.hasStatusEffect(RegistryHelper.SCULKIFIED);
    }

    public void refreshSize() {
        ScaleData data = ScaleTypes.BASE.getScaleData(this.entity);
        data.markForSync(true);

        int activeStacks = getParticipatingStackCount() + getSelfAfflictedStackCount();
        float baseHeight = Math.max(0.1F, this.entity.getType().getHeight());
        float targetScale = 1.0F + ((0.025F * activeStacks) / baseHeight);

        data.setTargetScale(targetScale);
        data.setScaleTickDelay(0);
        data.setScaleTickDelay(1);
    }

    public Integer getAppliedLevelOfEntityOrDefault(LivingEntity target) {
        return SOURCES.getOrDefault(target, 0);
    }

    public boolean afflictedPlayer(LivingEntity player) {
        return SOURCES.containsKey(player);
    }

    public void effectRemoved(LivingEntity player) {
        clear(player);
    }

    public boolean decrementSource(LivingEntity target) {
        Integer stacks = SOURCES.get(target);
        if (stacks == null) return false;

        if (stacks <= 1) {
            clear(target);
            return true;
        }

        SOURCES.put(target, stacks - 1);
        refreshSize();
        return true;
    }

    public void setSourceStackCount(LivingEntity target, int stacks) {
        if (!SOURCES.containsKey(target)) return;

        if (stacks <= 0) {
            clear(target);
            return;
        }

        SOURCES.put(target, stacks);
        refreshSize();
    }

    private void clear(LivingEntity target) {
        SOURCES.remove(target);
        refreshSize();
    }

    private int getParticipatingStackCount() {
        return SOURCES.entrySet().stream()
                .filter(entry -> entry.getKey() != this.entity)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private int getSelfAfflictedStackCount() {
        return getSculkifiedStackCount(this.entity);
    }

    public static int getSculkifiedStackCount(LivingEntity target) {
        var effect = target.getStatusEffect(RegistryHelper.SCULKIFIED);
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }
}
