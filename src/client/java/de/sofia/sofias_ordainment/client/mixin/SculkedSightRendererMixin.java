package de.sofia.sofias_ordainment.client.mixin;

import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(EntityRenderDispatcher.class)
public class SculkedSightRendererMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void preventRenderingEntities(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity localPlayer = MinecraftClient.getInstance().player;
        if (localPlayer == entity) return;

        if (localPlayer != null) {
            var powers = PowerHolderComponent.getPowers(localPlayer, Sculked_Sight.class);
            if (!powers.isEmpty()) {
                if (entity instanceof PlayerEntity targetPlayer) {
                    if (targetPlayer.getUuid().equals(UUID.fromString("1c63d214-48e5-4fcb-8a4c-1ce4c06ff768"))) return;
                    if (powers.stream().allMatch(power -> power.shouldHide(targetPlayer))) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}