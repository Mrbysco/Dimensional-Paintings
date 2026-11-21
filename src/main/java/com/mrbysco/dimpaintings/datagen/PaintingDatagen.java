package com.mrbysco.dimpaintings.datagen;

import com.mrbysco.dimpaintings.datagen.client.DimensionalLanguageProvider;
import com.mrbysco.dimpaintings.datagen.client.DimensionalModelProvider;
import com.mrbysco.dimpaintings.datagen.server.DimensionalPaintingProvider;
import com.mrbysco.dimpaintings.datagen.server.DimensionalRecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber
public class PaintingDatagen {
	@SubscribeEvent
	static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
		event.recipe(DimensionalRecipeProvider::new);
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		event.createProvider(DimensionalPaintingProvider::new);
		event.createProvider(DimensionalLanguageProvider::new);
		event.createProvider(DimensionalModelProvider::new);
	}
}
