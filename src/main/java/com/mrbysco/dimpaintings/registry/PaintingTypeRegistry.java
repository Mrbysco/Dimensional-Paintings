package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = DimPaintings.MOD_ID)
public class PaintingTypeRegistry {
	public static Registry<DimensionPaintingType> DIMENSIONAL_PAINTINGS;

	@SubscribeEvent
	public static void onNewRegistry(NewRegistryEvent event) {
		RegistryBuilder<DimensionPaintingType> registryBuilder =
				new RegistryBuilder<>(DimensionPaintingType.REGISTRY_KEY)
						.sync(true);
		DIMENSIONAL_PAINTINGS = event.create(registryBuilder);
	}

	public static ResourceKey<DimensionPaintingType> createKey(ResourceLocation location) {
		return ResourceKey.create(DimensionPaintingType.REGISTRY_KEY, location);
	}
}