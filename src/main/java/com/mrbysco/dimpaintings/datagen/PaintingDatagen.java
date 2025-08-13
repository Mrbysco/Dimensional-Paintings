package com.mrbysco.dimpaintings.datagen;

import com.mrbysco.dimpaintings.datagen.client.DimensionalItemModelProvider;
import com.mrbysco.dimpaintings.datagen.client.DimensionalLanguageProvider;
import com.mrbysco.dimpaintings.datagen.server.DimensionalPaintingProvider;
import com.mrbysco.dimpaintings.datagen.server.DimensionalRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class PaintingDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if (event.includeServer()) {
			generator.addProvider(true, new DimensionalRecipeProvider(packOutput, lookupProvider));
			generator.addProvider(true, new DimensionalPaintingProvider(packOutput, lookupProvider));
		}
		if (event.includeClient()) {
			generator.addProvider(true, new DimensionalLanguageProvider(packOutput));
			generator.addProvider(true, new DimensionalItemModelProvider(packOutput, helper));
		}
	}
}
