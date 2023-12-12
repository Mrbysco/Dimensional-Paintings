package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DimPaintings.MOD_ID)
public class PaintingTypeRegistry {
	public static Registry<DimensionPaintingType> DIMENSIONAL_PAINTINGS;

	@SubscribeEvent
	public static void onNewRegistry(NewRegistryEvent event) {
		RegistryBuilder<DimensionPaintingType> registryBuilder =
				new RegistryBuilder<>(DimensionPaintingType.REGISTRY_KEY)
						.sync(true);
		DIMENSIONAL_PAINTINGS = event.create(registryBuilder);
	}
}