package com.mrbysco.dimpaintings.client;

import com.mrbysco.dimpaintings.client.renderer.DimensionalPaintingRenderer;
import com.mrbysco.dimpaintings.client.renderer.DimensionalPaintingSpriteUploader;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ClientHandler {
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PaintingRegistry.DIMENSIONAL_PAINTING.get(), DimensionalPaintingRenderer::new);
	}

	public static void registerItemColors(final ColorHandlerEvent.Item event) {
		DimensionalPaintingSpriteUploader.initialize();
	}
}