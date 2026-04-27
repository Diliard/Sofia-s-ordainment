package de.sofia.sofias_ordainment.effects;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import de.sofia.sofias_ordainment.origins.powers.Soul_Stain;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;

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
        }

        playLowHealthHeartbeat(entity, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (!entity.getWorld().isClient && !SculkifiedSourceTrackerHelper.isRemovalCleanupSuppressed(entity)) {
            SculkifiedSourceTrackerHelper.removeTarget(entity);
        }
    }

    public static void init() {}

    private void playLowHealthHeartbeat(LivingEntity entity, int amplifier) {
        float maxHealth = entity.getMaxHealth();
        if (maxHealth <= 0.0F) return;

        float healthRatio = entity.getHealth() / maxHealth;
        if (healthRatio > 0.35F) return;

        int interval = healthRatio <= 0.2F ? 20 : 30;
        long time = entity.getWorld().getTime();
        if (time % interval != Math.floorMod(entity.getId(), interval)) return;

        List<PlayerEntity> players = entity.getWorld().getOtherEntities(entity, entity.getBoundingBox().expand(16.0D)).stream()
                .filter(target -> target instanceof PlayerEntity)
                .map(target -> (PlayerEntity) target)
                .filter(player -> player != entity)
                .filter(player -> PowerHolderComponent.hasPower(player, Soul_Stain.class)
                        || PowerHolderComponent.hasPower(player, Sculked_Sight.class))
                .toList();

        float pitch = 0.8F + ((amplifier + 1) * 0.035F) + ((1.0F - healthRatio) * 0.25F);
        for (PlayerEntity player : players) {
            entity.getWorld().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                    SoundCategory.PLAYERS,
                    1.8F,
                    pitch
            );
        }
    }
}
