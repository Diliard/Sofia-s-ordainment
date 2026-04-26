package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.powers.Soul_Conversion;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerInventory.class)
public class SoulConversionInventoryMixin {

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(method = "setStack", at = @At("HEAD"))
    public void sofias_ordainment$onInventoryUpdate(int slot, ItemStack stack, CallbackInfo ci) {
        List<Soul_Conversion> powers = PowerHolderComponent.getPowers(player, Soul_Conversion.class);

        for (Soul_Conversion power : powers) {
            power.removeEnchant();
            break;
        }
    }
}
