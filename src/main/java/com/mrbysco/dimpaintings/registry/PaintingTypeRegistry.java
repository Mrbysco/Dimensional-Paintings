package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = DimPaintings.MOD_ID)
public class PaintingTypeRegistry {
	@SubscribeEvent
	public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(DimensionPaintingType.REGISTRY_KEY,
				DimensionPaintingType.DIRECT_CODEC, DimensionPaintingType.DIRECT_CODEC);
		DimPaintings.LOGGER.info("Registered dimensional painting registry");
	}

	public static Holder<DimensionPaintingType> getHolder(RegistryAccess registryAccess, ResourceLocation location) {
		return registryAccess.lookupOrThrow(DimensionPaintingType.REGISTRY_KEY).get(location).orElse(null);
	}
}