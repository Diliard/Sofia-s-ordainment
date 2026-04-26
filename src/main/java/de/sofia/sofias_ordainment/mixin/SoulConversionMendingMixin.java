package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.powers.Soul_Conversion;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(PlayerEntity.class)
public class SoulConversionMendingMixin {

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int sofias_ordainment$onXpGain(int value) {
        PlayerEntity player = ((PlayerEntity)(Object)this);
        int totalDamage = 0;

        List<Soul_Conversion> powers = PowerHolderComponent.getPowers(player, Soul_Conversion.class);

        for (Soul_Conversion power : powers) {
            totalDamage += power.getTotalItemDamage();
            power.mendItems(value);
            if (totalDamage > value) value = 0;
            else {
                value -= totalDamage;
            }
            break;
        }
        return value;
    }
}