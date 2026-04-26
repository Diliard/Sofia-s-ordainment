package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.powers.DisableCritPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class DisableCritMixin {
    @ModifyVariable(
            method = "attack",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private boolean sofias_ordainment$disableCrit(boolean original) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (PowerHolderComponent.hasPower(player, DisableCritPower.class)) {
            return false;
        }

        return original;
    }
}
