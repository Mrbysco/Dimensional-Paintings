package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DimPaintings.MOD_ID)
public class PaintingTypeRegistry {
	public static IForgeRegistry<DimensionPaintingType> DIMENSIONAL_PAINTINGS;

	@SubscribeEvent
	public static void onNewRegistry(RegistryEvent.NewRegistry event) {
		RegistryBuilder<DimensionPaintingType> registryBuilder = new RegistryBuilder<>();
		registryBuilder.setName(new ResourceLocation(DimPaintings.MOD_ID, "dimension_painting"));
		registryBuilder.setType(DimensionPaintingType.class);
		DIMENSIONAL_PAINTINGS = registryBuilder.create();
	}
}