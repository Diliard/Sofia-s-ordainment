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
    private static final int[] LOW_STACK_GLOW_COLORS = {
            0x99EEFF,
            0x55DDEE,
            0x11BBDD,
            0x008899
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
            return LOW_STACK_GLOW_COLORS[Math.max(0, level - 1)];
        }

        long time = MinecraftClient.getInstance().world == null ? entity.age : MinecraftClient.getInstance().world.getTime();
        int speed = Math.max(3, 13 - level);
        int entityOffset = Math.floorMod(entity.getId(), SCULK_GLOW_COLORS.length);
        int index = (int) (((time / speed) + entityOffset) % SCULK_GLOW_COLORS.length);

        return SCULK_GLOW_COLORS[index];
    }

    private int sofias_ordainment$applyWeakHealthColor(LivingEntity living, int color) {
        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0.0F) return color;

        float healthRatio = living.getHealth() / maxHealth;
        float weakness = Math.max(0.0F, Math.min(1.0F, (0.45F - healthRatio) / 0.45F));
        if (weakness <= 0.0F) return color;

        int healthStep = Math.min(3, (int) (weakness * 4.0F));
        int weakenedColor = switch (healthStep) {
            case 1 -> sofias_ordainment$mixQuarter(color, 0xCCFFF7, 1);
            case 2 -> sofias_ordainment$mixQuarter(color, 0xDDFFF7, 2);
            case 3 -> sofias_ordainment$mixQuarter(color, 0xEEFFF7, 3);
            default -> color;
        };
        if (healthRatio > 0.2F) return weakenedColor;

        long time = MinecraftClient.getInstance().world == null ? living.age : MinecraftClient.getInstance().world.getTime();
        return ((time / 4) % 2 == 0)
                ? sofias_ordainment$mixQuarter(weakenedColor, 0x002233, 1)
                : weakenedColor;
    }

    private int sofias_ordainment$mixQuarter(int start, int end, int quarters) {
        int inverse = 4 - quarters;
        int r = ((((start >> 16) & 0xFF) * inverse) + (((end >> 16) & 0xFF) * quarters)) / 4;
        int g = ((((start >> 8) & 0xFF) * inverse) + (((end >> 8) & 0xFF) * quarters)) / 4;
        int b = (((start & 0xFF) * inverse) + ((end & 0xFF) * quarters)) / 4;

        return sofias_ordainment$snapColor((r << 16) | (g << 8) | b);
    }

    private int sofias_ordainment$snapColor(int color) {
        int r = ((color >> 16) & 0xFF) & 0xF0;
        int g = ((color >> 8) & 0xFF) & 0xF0;
        int b = (color & 0xFF) & 0xF0;

        return (r << 16) | (g << 8) | b;
    }
}
