package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.origins.utility.SculkedSightSensorPackets;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlockEntity.VibrationCallback.class)
public class SculkSensorVibrationCallbackMixin {
    @Inject(method = "accept", at = @At("TAIL"))
    private void sofias_ordainment$markSculkedSightSensorPing(
            ServerWorld world,
            BlockPos pos,
            GameEvent event,
            Entity sourceEntity,
            Entity ownerEntity,
            float distance,
            CallbackInfo ci
    ) {
        ServerPlayerEntity target = null;

        if (sourceEntity instanceof ServerPlayerEntity player) {
            target = player;
        } else if (ownerEntity instanceof ServerPlayerEntity player) {
            target = player;
        }

        if (target != null) {
            SculkedSightSensorPackets.sendSensorPing(target);
        }
    }
}
