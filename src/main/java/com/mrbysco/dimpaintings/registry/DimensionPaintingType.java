package com.mrbysco.dimpaintings.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class DimensionPaintingType extends ForgeRegistryEntry<DimensionPaintingType> {
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
