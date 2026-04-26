package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.powers.Soul_Stain;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SculkifiedDamageMixin {
    @Inject(
            method = "modifyAppliedDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sofias_ordainment$modifyPvPDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (!(source.getAttacker() instanceof PlayerEntity attacker)) return;

        float newAmount = amount;

        if ((Object) this instanceof PlayerEntity target) {
            if (target.hasStatusEffect(RegistryHelper.SCULKIFIED)) {
                var effect = attacker.getStatusEffect(RegistryHelper.SCULKIFIED);
                if (effect != null && PowerHolderComponent.hasPower(target, Soul_Stain.class)) {
                    int amplifier = effect.getAmplifier() + 1;
                    newAmount *= (1.0f - (0.07f * amplifier));
                }
            }

            if (PowerHolderComponent.hasPower(attacker, Soul_Stain.class)) {
                var targetEffect = target.getStatusEffect(RegistryHelper.SCULKIFIED);
                if (targetEffect != null && targetEffect.getAmplifier() == 9) {
                    newAmount *= 1.5f;
                }
            }
        }

        if (newAmount != amount) {
            cir.setReturnValue(newAmount);
        }
    }
}
