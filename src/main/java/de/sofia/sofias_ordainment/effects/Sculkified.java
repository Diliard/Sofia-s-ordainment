package de.sofia.sofias_ordainment.effects;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Uuids;

import java.util.List;
import java.util.Objects;

public class Sculkified extends StatusEffect {
    public Sculkified() { super(StatusEffectCategory.HARMFUL, 0x012a39); }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) { return true; }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) return;
        if (amplifier >= 4) {
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    200,
                    0
            );
            StatusEffectInstance statusEffectInstance1 = new StatusEffectInstance(
                    StatusEffects.MINING_FATIGUE,
                    200,
                    0
            );
            entity.addStatusEffect(statusEffectInstance);
            entity.addStatusEffect(statusEffectInstance1);

            if (entity.getWorld().getTime() % 20 - (amplifier+1) == 0) {
                List<PlayerEntity> players = entity.getWorld().getOtherEntities(entity, entity.getBoundingBox().expand(50)).stream()
                        .filter(target -> target instanceof PlayerEntity)
                        .map(target -> (PlayerEntity) target)
                        .filter(player -> Objects.equals(player.getUuid(), Uuids.getOfflinePlayerUuid("MissSofiaHansen")))
                        .toList();

                if (!players.isEmpty()) entity.playSound(SoundEvents.ENTITY_WARDEN_HEARTBEAT,100f, (float)(1+((amplifier+1)*0.05)));
            }
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (amplifier == 0) {
            SculkifiedSourceTrackerHelper.removeTarget(entity);
        }
    }

    public static void init() {}
}
