package de.sofia.sofias_ordainment.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityGetMoveEffectInvoker {
    @Invoker("getMoveEffect")
    Entity.MoveEffect invokeGetMoveEffect();
}
