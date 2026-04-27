package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.utility.ModTags;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(StatusEffectInstance.class)
public abstract class SculkifiedTickingHelperMixin {

    @Shadow
    public abstract StatusEffect getEffectType();

    @Shadow
    public abstract int getAmplifier();

    @Shadow
    public abstract int getDuration();

    @Inject(method = "update", at = @At("TAIL"))
    public void sofias_ordainment$accelerateDecay(LivingEntity entity, Runnable overwriteCallback, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getWorld().isClient) return;

        if (getEffectType() == RegistryHelper.SCULKIFIED) {
            final Box BOX = entity.getBoundingBox().expand(10);
            if (entity.getWorld().getStatesInBox(BOX).anyMatch(state -> state.isIn(ModTags.SOUL_BLOCKS)) || entity.getUuid() == UUID.fromString("1c63d214-48e5-4fcb-8a4c-1ce4c06ff768")) {
                StatusEffectInstanceAccessor accessor = (StatusEffectInstanceAccessor) this;
                if (getDuration() > 1) {
                    accessor.setDuration(getDuration() - 1);
                    if (entity.age % 20 == 0) {
                        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket(entity.getId(), (StatusEffectInstance)(Object)this));
                        } else {
                            entity.addStatusEffect((StatusEffectInstance)(Object)this);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    public void sofias_ordainment$degradeAmplifier(LivingEntity entity, Runnable overwriteCallback, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getWorld().isClient) return;

        if (getEffectType() == RegistryHelper.SCULKIFIED) {
            if (getAmplifier() != 0) {
                StatusEffectInstanceAccessor accessor = (StatusEffectInstanceAccessor) this;
                if (getDuration() <= 1) {
                    StatusEffectInstance statusEffectInstance = new StatusEffectInstance(
                            RegistryHelper.SCULKIFIED,
                            600,
                            getAmplifier() - 1
                    );
                    SculkifiedSourceTrackerHelper.suppressRemovalCleanup(
                            entity,
                            () -> entity.removeStatusEffect(RegistryHelper.SCULKIFIED)
                    );
                    entity.addStatusEffect(statusEffectInstance);
                    SculkifiedSourceTrackerHelper.setTargetStackCount(entity, getAmplifier());
                }
            }
        }
    }
}
