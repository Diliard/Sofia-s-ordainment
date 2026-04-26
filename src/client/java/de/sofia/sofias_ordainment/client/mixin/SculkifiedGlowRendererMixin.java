package de.sofia.sofias_ordainment.client.mixin;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import de.sofia.sofias_ordainment.origins.powers.Soul_Stain;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class SculkifiedGlowRendererMixin {
    private static final int[] SCULK_GLOW_COLORS = {
            0x8FEBFF,
            0x55DCEB,
            0x16B9C8,
            0x087988,
            0x042F3A,
            0x0B1E4F,
            0x37206C,
            0x6F3FA8
    };

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void sofias_ordainment$renderLocalSculkifiedGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (sofias_ordainment$shouldRenderSculkGlow(entity) || sofias_ordainment$shouldRenderSensorGlow(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void sofias_ordainment$customGlowColor(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity) (Object) this;
        Sculked_Sight sensorPower = sofias_ordainment$getSensorGlowPower(entity);
        if (sensorPower != null && !sofias_ordainment$shouldRenderSculkGlow(entity)) {
            cir.setReturnValue(sensorPower.getSensorGlowColor(entity));
            return;
        }

        if (!sofias_ordainment$shouldRenderSculkGlow(entity)) return;

        LivingEntity living = (LivingEntity) entity;
        int level = Math.min(living.getStatusEffect(RegistryHelper.SCULKIFIED).getAmplifier() + 1, 10);
        int color = sofias_ordainment$getSculkGlowColor(entity, level);
        cir.setReturnValue(sofias_ordainment$applyWeakHealthColor(living, color));
    }

    private boolean sofias_ordainment$shouldRenderSculkGlow(Entity target) {
        if (!(target instanceof LivingEntity living)) return false;

        StatusEffectInstance sculkified = living.getStatusEffect(RegistryHelper.SCULKIFIED);
        if (sculkified == null) return false;

        ClientPlayerEntity viewer = MinecraftClient.getInstance().player;
        if (viewer == null || viewer == target) return false;

        return PowerHolderComponent.hasPower(viewer, Sculked_Sight.class)
                || PowerHolderComponent.hasPower(viewer, Soul_Stain.class);
    }

    private boolean sofias_ordainment$shouldRenderSensorGlow(Entity target) {
        return sofias_ordainment$getSensorGlowPower(target) != null;
    }

    private Sculked_Sight sofias_ordainment$getSensorGlowPower(Entity target) {
        ClientPlayerEntity viewer = MinecraftClient.getInstance().player;
        if (viewer == null || viewer == target) return null;

        return PowerHolderComponent.getPowers(viewer, Sculked_Sight.class).stream()
                .filter(power -> power.isSensorHighlighted(target))
                .findFirst()
                .orElse(null);
    }

    private int sofias_ordainment$getSculkGlowColor(Entity entity, int level) {
        if (level < 5) {
            float progress = (level - 1) / 3.0F;
            return sofias_ordainment$lerpColor(0x9AEFFF, 0x16B9C8, progress);
        }

        long time = MinecraftClient.getInstance().world == null ? entity.age : MinecraftClient.getInstance().world.getTime();
        int speed = Math.max(2, 11 - level);
        int index = (int) ((time / speed) % SCULK_GLOW_COLORS.length);
        int nextIndex = (index + 1) % SCULK_GLOW_COLORS.length;
        float progress = (time % speed) / (float) speed;

        return sofias_ordainment$lerpColor(SCULK_GLOW_COLORS[index], SCULK_GLOW_COLORS[nextIndex], progress);
    }

    private int sofias_ordainment$applyWeakHealthColor(LivingEntity living, int color) {
        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0.0F) return color;

        float healthRatio = living.getHealth() / maxHealth;
        float weakness = Math.max(0.0F, Math.min(1.0F, (0.45F - healthRatio) / 0.45F));
        if (weakness <= 0.0F) return color;

        int weakenedColor = sofias_ordainment$lerpColor(color, 0xD9FFF7, weakness * 0.65F);
        if (healthRatio > 0.2F) return weakenedColor;

        long time = MinecraftClient.getInstance().world == null ? living.age : MinecraftClient.getInstance().world.getTime();
        float flicker = ((time / 3) % 2 == 0) ? 0.35F : 0.0F;
        return sofias_ordainment$lerpColor(weakenedColor, 0x082A33, flicker * weakness);
    }

    private int sofias_ordainment$lerpColor(int start, int end, float progress) {
        int r = (int) (((start >> 16) & 0xFF) + ((((end >> 16) & 0xFF) - ((start >> 16) & 0xFF)) * progress));
        int g = (int) (((start >> 8) & 0xFF) + ((((end >> 8) & 0xFF) - ((start >> 8) & 0xFF)) * progress));
        int b = (int) ((start & 0xFF) + (((end & 0xFF) - (start & 0xFF)) * progress));

        return (r << 16) | (g << 8) | b;
    }
}
