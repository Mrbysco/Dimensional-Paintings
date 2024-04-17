package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DimPaintings.MOD_ID)
public class PaintingTypeRegistry {
	@SubscribeEvent
	public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(DimensionPaintingType.REGISTRY_KEY,
				DimensionPaintingType.DIRECT_CODEC, DimensionPaintingType.DIRECT_CODEC);
		DimPaintings.LOGGER.info("Registered dimensional painting registry");
	}

	public static ResourceLocation getKey(Level level, DimensionPaintingType paintingType) {
		return level.registryAccess().registryOrThrow(DimensionPaintingType.REGISTRY_KEY).getKey(paintingType);
	}

	public static DimensionPaintingType getValue(Level level, ResourceLocation location) {
		return level.registryAccess().registryOrThrow(DimensionPaintingType.REGISTRY_KEY).get(location);
	}
}