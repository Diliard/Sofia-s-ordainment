package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.abstractPowers.AbstractOnHitPower;
import de.sofia.sofias_ordainment.origins.powers.ParthenogenesisPower;
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
            at = @At("HEAD"),
            cancellable = true
    )
    private void sofias_ordainment$afterDamage(
            DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity target = (LivingEntity)(Object)this;

        if (ParthenogenesisPower.isOwnerDamagingOwnSwarm(target, source)) {
            cir.setReturnValue(false);
            return;
        }

        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
        if (amount <= 0) return;

        PowerHolderComponent.getPowers(attacker, AbstractOnHitPower.class)
                .forEach(power -> power.onHit(target, amount));
    }

    @Inject(
            method = "damage",
            at = @At("RETURN")
    )
    private void sofias_ordainment$rememberParthenogenesisTarget(
            DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) return;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
        if (amount <= 0) return;

        LivingEntity target = (LivingEntity)(Object)this;
        PowerHolderComponent.getPowers(attacker, ParthenogenesisPower.class)
                .forEach(power -> power.rememberOwnerHit(target));
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void sofias_ordainment$clearParthenogenesisTargetOnSwarmDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity deadEntity = (LivingEntity) (Object) this;

        ParthenogenesisPower.clearOwnerTargetForSwarm(deadEntity);
        ParthenogenesisPower.clearOwnerTarget(deadEntity.getUuid());
        ParthenogenesisPower.clearTargetReferencesTo(deadEntity.getUuid());
    }
}
