package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.IForgeRegistry;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DimPaintings.MOD_ID)
public class PaintingTypeRegistry {
	public static final ResourceLocation registryLocation = new ResourceLocation(DimPaintings.MOD_ID, "dimension_painting");
	public static Supplier<IForgeRegistry<DimensionPaintingType>> DIMENSIONAL_PAINTINGS;

	@SubscribeEvent
	public static void onNewRegistry(NewRegistryEvent event) {
		RegistryBuilder<DimensionPaintingType> registryBuilder = new RegistryBuilder<>();
		registryBuilder.setName(registryLocation);
		DIMENSIONAL_PAINTINGS = event.create(registryBuilder);
	}
}