package com.mrbysco.dimpaintings.client;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.client.renderer.DimensionalPaintingRenderer;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientHandler {
	public static final Identifier DIMENSIONAL_PAINTINGS_MAP_NAME = DimPaintings.modLoc("textures/atlas/dimensional_paintings.png");
	public static final Identifier DIMENSIONAL_PAINTINGS = DimPaintings.modLoc("dimensional_paintings");

	@SubscribeEvent
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PaintingRegistry.DIMENSIONAL_PAINTING.get(), DimensionalPaintingRenderer::new);
	}

	private static AtlasManager.AtlasConfig dimensionalSpriteUploader = null;

	@SubscribeEvent
	public static void onAtlasRegister(final RegisterTextureAtlasesEvent event) {
		dimensionalSpriteUploader = new AtlasManager.AtlasConfig(DIMENSIONAL_PAINTINGS_MAP_NAME, DIMENSIONAL_PAINTINGS, false);
		event.register(dimensionalSpriteUploader);
	}
}