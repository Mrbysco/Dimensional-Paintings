package com.mrbysco.dimpaintings.client;

import com.mrbysco.dimpaintings.client.renderer.DimensionalPaintingRenderer;
import com.mrbysco.dimpaintings.client.renderer.DimensionalPaintingTextureManager;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public class ClientHandler {
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PaintingRegistry.DIMENSIONAL_PAINTING.get(), DimensionalPaintingRenderer::new);
	}

	public static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event) {
		DimensionalPaintingTextureManager.initialize(event);
	}
}