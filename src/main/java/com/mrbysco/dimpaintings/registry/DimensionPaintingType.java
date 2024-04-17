package com.mrbysco.dimpaintings.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record DimensionPaintingType(ResourceLocation dimensionLocation, int width, int height) {

	public static final ResourceKey<Registry<DimensionPaintingType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			new ResourceLocation(DimPaintings.MOD_ID, "dimension_painting"));
	public static final Codec<DimensionPaintingType> DIRECT_CODEC = ExtraCodecs.catchDecoderException(
			RecordCodecBuilder.create(
					apply -> apply.group(
									ResourceLocation.CODEC.fieldOf("dimension").forGetter(DimensionPaintingType::dimensionLocation),
									Codec.INT.fieldOf("width").forGetter(DimensionPaintingType::width),
									Codec.INT.fieldOf("height").forGetter(DimensionPaintingType::height)
							)
							.apply(apply, DimensionPaintingType::new)
			)
	);
}