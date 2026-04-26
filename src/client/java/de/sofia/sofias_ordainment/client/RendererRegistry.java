package de.sofia.sofias_ordainment.client;

import de.sofia.sofias_ordainment.RegistryHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class RendererRegistry {

    public static void init() {
        EntityRendererRegistry.register(
                RegistryHelper.CUSTOM_POTION_ENTITY,
                FlyingItemEntityRenderer::new
        );
    }
}
