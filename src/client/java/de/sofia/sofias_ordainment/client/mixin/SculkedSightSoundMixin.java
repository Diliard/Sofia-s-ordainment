package de.sofia.sofias_ordainment.client.mixin;

import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class SculkedSightSoundMixin {
    @Inject(
            method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V",
            at = @At("TAIL")
    )
    private void sofias_ordainment$markPlayerSound(
            double x,
            double y,
            double z,
            SoundEvent sound,
            SoundCategory category,
            float volume,
            float pitch,
            boolean useDistance,
            long seed,
            CallbackInfo ci
    ) {
        if (category != SoundCategory.PLAYERS && category != SoundCategory.BLOCKS) return;

        ClientPlayerEntity localPlayer = MinecraftClient.getInstance().player;
        if (localPlayer == null) return;

        double radius = Math.max(1.5D, volume * 4.0D);
        PowerHolderComponent.getPowers(localPlayer, Sculked_Sight.class)
                .forEach(power -> power.markNoiseNear(x, y, z, radius));
    }
}
