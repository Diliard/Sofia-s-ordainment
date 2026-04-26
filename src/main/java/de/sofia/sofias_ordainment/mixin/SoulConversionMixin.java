package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.powers.Soul_Conversion;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class SoulConversionMixin {

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(method = "markDirty", at = @At("TAIL"))
    private void sofias_ordainment$onInventoryUpdate(CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        PowerHolderComponent.getPowers(serverPlayer, Soul_Conversion.class)
                .forEach(Soul_Conversion::removeEnchant);
    }
}

