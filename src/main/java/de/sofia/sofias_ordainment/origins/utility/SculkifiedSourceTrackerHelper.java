package de.sofia.sofias_ordainment.origins.utility;

import de.sofia.sofias_ordainment.origins.powers.Soul_Stain;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class SculkifiedSourceTrackerHelper {

    @Nullable
    public static Map<LivingEntity, Integer> getSources(LivingEntity target) {
        World world = target.getWorld();
        Map<LivingEntity, Integer> returnVal = new WeakHashMap<>();

        world.getPlayers().forEach(playerEntity -> {
            PowerHolderComponent.getPowers(playerEntity, Soul_Stain.class).forEach(power -> {
                if (power.afflictedPlayer(target)) {
                    returnVal.put(playerEntity, power.getAppliedLevelOfEntityOrDefault(target));
                }
            });
        });

        return returnVal;
    }

    public static void removeTarget(LivingEntity target) {
        getSources(target).forEach((entity, integer) -> {
            PowerHolderComponent.getPowers(entity, Soul_Stain.class).forEach(power -> {
                power.effectRemoved(target);
            });
        });
        refreshSelfAfflictedSize(target);
    }

    public static void decrementIfInflicted(LivingEntity target) {
        refreshTargetStackCount(target);
    }

    public static void refreshTargetStackCount(LivingEntity target) {
        setTargetStackCount(target, Soul_Stain.getSculkifiedStackCount(target));
    }

    public static void setTargetStackCount(LivingEntity target, int stacks) {
        getSources(target).forEach((entity, integer) -> {
            PowerHolderComponent.getPowers(entity, Soul_Stain.class).forEach(power -> {
                if (power.afflictedPlayer(target)) {
                    power.setSourceStackCount(target, stacks);
                }
            });
        });
        refreshSelfAfflictedSize(target);
    }

    private static void refreshSelfAfflictedSize(LivingEntity target) {
        PowerHolderComponent.getPowers(target, Soul_Stain.class)
                .forEach(Soul_Stain::refreshSize);
    }
}
