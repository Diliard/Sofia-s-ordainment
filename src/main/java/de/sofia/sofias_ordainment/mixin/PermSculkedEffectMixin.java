package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.StateSaverAndLoader;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PermSculkedEffectMixin {
    @Inject(method = "clearStatusEffects", at = @At("TAIL"))
    private void sofias_ordainment$onClearEffects(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            StateSaverAndLoader state = StateSaverAndLoader.getServerState(player.getServer());
            if (state.permSculked.contains(player.getUuid())) {
                player.addStatusEffect(new StatusEffectInstance(RegistryHelper.SCULKIFIED, -1, 2));
                SculkifiedSourceTrackerHelper.refreshTargetStackCount(player);
            }
        }
    }
}
