package de.sofia.sofias_ordainment.client;

import net.fabricmc.api.ClientModInitializer;

public class Sofias_ordainmentClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RendererRegistry.init();
        SculkedSightClientEffects.init();
    }
}
