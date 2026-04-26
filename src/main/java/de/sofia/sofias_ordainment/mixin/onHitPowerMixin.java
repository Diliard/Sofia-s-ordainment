package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.abstractPowers.AbstractOnHitPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class onHitPowerMixin {

    @Inject(
            method = "damage",
            at = @At("HEAD")
    )
    private void sofias_ordainment$afterDamage(
            DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity target = (LivingEntity)(Object)this;

        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
        if (amount <= 0) return;

        PowerHolderComponent.getPowers(attacker, AbstractOnHitPower.class)
                .forEach(power -> power.onHit(target, amount));
    }
}