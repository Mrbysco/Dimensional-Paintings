package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DimensionPaintingType {
	public static final ResourceKey<Registry<DimensionPaintingType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(DimPaintings.MOD_ID, "dimension_painting"));

	private final ResourceLocation dimensionLocation;
	private final int width;
	private final int height;

	DimensionPaintingType(ResourceLocation dimensionLocation, int width, int height) {
		this.dimensionLocation = dimensionLocation;
		this.width = width;
		this.height = height;
	}

	public ResourceLocation getDimensionLocation() {
		return dimensionLocation;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}
}