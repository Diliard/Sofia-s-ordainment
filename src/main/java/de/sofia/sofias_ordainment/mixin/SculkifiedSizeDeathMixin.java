package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SculkifiedSizeDeathMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void triggerEffectOnDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasStatusEffect(RegistryHelper.SCULKIFIED)) {
            SculkifiedSourceTrackerHelper.removeTarget((LivingEntity)(Object)this);
        }
    }
}