package de.sofia.sofias_ordainment.mixin;

import de.sofia.sofias_ordainment.entity.CockroachSwarmMarked;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.BatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatEntity.class)
public abstract class SwarmBatEntityMixin implements CockroachSwarmMarked {

    @Unique
    private static final String COCKROACH_TAG = "sofias_ordainment_cockroach_swarm";

    @Unique
    private static final TrackedData<Boolean> SOFIAS_COCKROACH_SWARM =
            DataTracker.registerData(BatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void sofias_ordainment$initDataTracker(CallbackInfo ci) {
        BatEntity self = (BatEntity) (Object) this;

        self.getDataTracker().startTracking(SOFIAS_COCKROACH_SWARM, false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sofias_ordainment$syncCockroachSwarmTag(CallbackInfo ci) {
        BatEntity self = (BatEntity) (Object) this;

        if (!self.getWorld().isClient) {
            boolean hasTag = self.getCommandTags().contains(COCKROACH_TAG);

            if (self.getDataTracker().get(SOFIAS_COCKROACH_SWARM) != hasTag) {
                self.getDataTracker().set(SOFIAS_COCKROACH_SWARM, hasTag);
            }
        }
    }

    @Override
    public boolean sofias_ordainment$isCockroachSwarm() {
        BatEntity self = (BatEntity) (Object) this;
        return self.getDataTracker().get(SOFIAS_COCKROACH_SWARM);
    }

    @Override
    public void sofias_ordainment$setCockroachSwarm(boolean value) {
        BatEntity self = (BatEntity) (Object) this;
        self.getDataTracker().set(SOFIAS_COCKROACH_SWARM, value);
    }
}