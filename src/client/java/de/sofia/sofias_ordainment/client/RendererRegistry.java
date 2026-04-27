package de.sofia.sofias_ordainment.client;

import de.sofia.sofias_ordainment.RegistryHelper;
import de.sofia.sofias_ordainment.client.render.SwarmPngRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.EntityType;

public class RendererRegistry {

    public static void init() {
        EntityRendererRegistry.register(RegistryHelper.CUSTOM_POTION_ENTITY, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(EntityType.BAT, SwarmPngRenderer::new);

    }
}
