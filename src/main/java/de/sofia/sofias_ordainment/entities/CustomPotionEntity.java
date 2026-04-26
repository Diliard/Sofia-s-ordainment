package de.sofia.sofias_ordainment.entities;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.StateSaverAndLoader;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class CustomPotionEntity extends ThrownItemEntity {

    public CustomPotionEntity(EntityType<? extends ThrownItemEntity> type, World world) {
        super(type, world);
    }

    public CustomPotionEntity(World world, LivingEntity owner) {
        super(RegistryHelper.CUSTOM_POTION_ENTITY, owner, world);
    }

    public CustomPotionEntity(World world, double x, double y, double z) {
        super(RegistryHelper.CUSTOM_POTION_ENTITY, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return RegistryHelper.CUSTOM_POTION;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient) {
            explodeEffect();
            this.discard();
        }
    }

    private void explodeEffect() {
        Box box = this.getBoundingBox().expand(4.0);

        for (LivingEntity entity : getWorld().getEntitiesByClass(
                LivingEntity.class, box, e -> true)) {

            double dist = this.squaredDistanceTo(entity);
            if (dist < 16) {
                if (entity instanceof PlayerEntity playerEntity) {
                    StateSaverAndLoader state = StateSaverAndLoader.getServerState(getWorld().getServer());

                    playerEntity.addStatusEffect(new StatusEffectInstance(RegistryHelper.SCULKIFIED, -1, 2));
                    SculkifiedSourceTrackerHelper.refreshTargetStackCount(playerEntity);
                    if (!state.permSculked.contains(playerEntity.getUuid())) {
                        state.permSculked.add(playerEntity.getUuid());
                        state.markDirty();
                    }
                }
            }
        }
        this.getWorld().syncWorldEvent(WorldEvents.SPLASH_POTION_SPLASHED, this.getBlockPos(), 0x012a39);
    }
}
