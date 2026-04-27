package de.sofia.sofias_ordainment.origins.powers;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Optional;

public class InfectiousLungePower extends CooldownPower implements Active {
    private static final int LUNGE_TICKS = 12;
    private static final int MISS_COOLDOWN_TICKS = 140;
    private static final float LUNGE_DAMAGE = 8.0F;

    private Key key = new Key();
    private int activeLungeTicks;

    public InfectiousLungePower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
        super(type, entity, cooldownDuration, hudRender);
        this.setTicking();
    }

    @Override
    public void onUse() {
        if (!this.canUse()) return;

        Vec3d look = entity.getRotationVec(1.0F).normalize();
        entity.setVelocity(look.x * 1.9D, Math.max(0.2D, look.y * 0.45D), look.z * 1.9D);
        entity.velocityModified = true;
        activeLungeTicks = LUNGE_TICKS;

        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ENTITY_SILVERFISH_HURT, SoundCategory.PLAYERS, 1.0F, 0.65F);
        this.use();
    }

    @Override
    public void tick() {
        if (activeLungeTicks <= 0) return;

        findLungeTarget().ifPresentOrElse(this::landHit, () -> {
            activeLungeTicks--;
            if (activeLungeTicks <= 0) {
                miss();
            }
        });
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    private Optional<LivingEntity> findLungeTarget() {
        Box hitBox = entity.getBoundingBox().expand(0.85D, 0.35D, 0.85D);
        return entity.getWorld().getEntitiesByClass(LivingEntity.class, hitBox, target ->
                        target != entity && target.isAlive() && !target.isSpectator())
                .stream()
                .min(Comparator.comparingDouble(target -> target.squaredDistanceTo(entity)));
    }

    private void landHit(LivingEntity target) {
        activeLungeTicks = 0;

        DamageSource source = entity instanceof PlayerEntity player
                ? entity.getDamageSources().playerAttack(player)
                : entity.getDamageSources().mobAttack(entity);

        if (target.damage(source, LUNGE_DAMAGE)) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1), entity);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0), entity);

            if (entity.getWorld() instanceof ServerWorld world) {
                world.spawnParticles(ParticleTypes.POOF, target.getX(), target.getBodyY(0.5D), target.getZ(),
                        12, 0.25D, 0.25D, 0.25D, 0.02D);
            }
        }
    }

    private void miss() {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0), entity);
        this.modify(-(this.cooldownDuration - MISS_COOLDOWN_TICKS));
        PowerHolderComponent.syncPower(entity, this.type);
    }
}
