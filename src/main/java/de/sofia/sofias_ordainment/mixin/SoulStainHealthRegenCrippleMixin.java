package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.utility.ModTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class SoulStainHealthRegenCrippleMixin {

    @ModifyVariable(
            method = "heal",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float sofias_ordainment$crippleHealing(float amount) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.hasStatusEffect(RegistryHelper.SCULKIFIED)) {
            int amp = livingEntity.getStatusEffect(RegistryHelper.SCULKIFIED).getAmplifier();
            if (amp == 9) {
                final Box BOX = livingEntity.getBoundingBox().expand(16);
                if (livingEntity.getWorld().getStatesInBox(BOX).noneMatch(state -> state.isIn(ModTags.SOUL_BLOCKS))) {
                    return amount * 0.27f;
                }
            }
        }
        return amount;
    }
}

