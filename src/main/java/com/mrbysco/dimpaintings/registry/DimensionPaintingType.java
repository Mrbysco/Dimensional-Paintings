package com.mrbysco.dimpaintings.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrbysco.dimpaintings.DimPaintings;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Optional;

public record DimensionPaintingType(Identifier dimensionId, int width, int height, Identifier assetId) {
	public static final ResourceKey<Registry<DimensionPaintingType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			DimPaintings.modLoc("dimension_painting"));
	public static final Codec<DimensionPaintingType> DIRECT_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Identifier.CODEC.fieldOf("dimension_id").forGetter(DimensionPaintingType::dimensionId),
							ExtraCodecs.intRange(1, 16).fieldOf("width").forGetter(DimensionPaintingType::width),
							ExtraCodecs.intRange(1, 16).fieldOf("height").forGetter(DimensionPaintingType::height),
							Identifier.CODEC.fieldOf("asset_id").forGetter(DimensionPaintingType::assetId)
					)
					.apply(instance, DimensionPaintingType::new)
	);
	public static final Codec<Optional<WithConditions<DimensionPaintingType>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(DIRECT_CODEC);
	public static final StreamCodec<ByteBuf, DimensionPaintingType> DIRECT_STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC,
			DimensionPaintingType::dimensionId,
			ByteBufCodecs.VAR_INT,
			DimensionPaintingType::width,
			ByteBufCodecs.VAR_INT,
			DimensionPaintingType::height,
			Identifier.STREAM_CODEC,
			DimensionPaintingType::assetId,
			DimensionPaintingType::new
	);
	public static final Codec<Holder<DimensionPaintingType>> CODEC = RegistryFileCodec.create(DimensionPaintingType.REGISTRY_KEY, DIRECT_CODEC);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DimensionPaintingType>> STREAM_CODEC = ByteBufCodecs.holder(
			DimensionPaintingType.REGISTRY_KEY, DIRECT_STREAM_CODEC
	);

	public int area() {
		return this.width() * this.height();
	}
}