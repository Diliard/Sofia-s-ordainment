package de.sofia.sofias_ordainment.origins.powers;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.effects.Sculkified;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Grounding extends CooldownPower implements Active {
    private Key key = new Key();

    public Grounding(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
        super(type, entity, cooldownDuration, hudRender);
    }

    @Override
    public void onUse() {
        if (this.canUse()) {
            if (entity instanceof PlayerEntity playerEntity) {
                if (playerEntity.experienceLevel >= 10) {
                    Box area = entity.getBoundingBox().expand(12.5);
                    List<PlayerEntity> sculkifiedPlayers = entity.getWorld().getOtherEntities(entity, area).stream()
                            .filter(target -> target instanceof PlayerEntity)
                            .map(target -> (PlayerEntity) target)
                            .filter(player -> player.hasStatusEffect(RegistryHelper.SCULKIFIED))
                            .toList();

                    if (sculkifiedPlayers.isEmpty()) return;

                    for (PlayerEntity player : sculkifiedPlayers) {
                        Vec3d playerVel = player.getVelocity();
                        playerVel = new Vec3d(playerVel.x, -5, playerVel.z);
                        player.setVelocity(playerVel);
                        player.velocityModified = true;
                    }

                    playerEntity.playSound(SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.PLAYERS, 100f, 1f);
                    playerEntity.experienceLevel = playerEntity.experienceLevel - 10;
                    this.use();
                }
            }
        }
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }
}