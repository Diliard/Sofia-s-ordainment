package de.sofia.sofias_ordainment.origins.powers;

import de.sofia.sofias_ordainment.origins.utility.ModTags;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;

public class Succulence extends Power {
    public Succulence(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    @Override
    public void tick() {
        final Box BOX = entity.getBoundingBox().expand(16);
        Vec3d axoPos;
        try {
            axoPos = entity.getServer().getPlayerManager().getPlayer(UUID.fromString("1c63d214-48e5-4fcb-8a4c-1ce4c06ff768")).getPos();
        } catch (NullPointerException e) {
            axoPos = new Vec3d(0,0,0);
        }

        if (entity.getWorld().getStatesInBox(BOX).anyMatch(state -> state.isIn(ModTags.SOUL_BLOCKS))  || this.entity.getPos().distanceTo(axoPos) <= 50) {
            StatusEffectInstance regeneration = new StatusEffectInstance(
                    StatusEffects.REGENERATION,
                    200,
                    1
            );

            entity.addStatusEffect(regeneration);
        }

        if (entity.getWorld().getStatesInBox(BOX).anyMatch(state -> state.isIn(ModTags.SCULK_BLOCKS))) {
            StatusEffectInstance haste = new StatusEffectInstance(
                    StatusEffects.HASTE,
                    200,
                    3
            );
            StatusEffectInstance speed = new StatusEffectInstance(
                    StatusEffects.SPEED,
                    200,
                    1
            );

            entity.addStatusEffect(haste);
            entity.addStatusEffect(speed);
        }
    }
}