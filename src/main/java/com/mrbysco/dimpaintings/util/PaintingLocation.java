package com.mrbysco.dimpaintings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record PaintingLocation(BlockPos pos, Direction direction) {
	public static final Codec<PaintingLocation> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							BlockPos.CODEC.fieldOf("pos").forGetter(PaintingLocation::pos),
							Direction.LEGACY_ID_CODEC_2D.fieldOf("direction").forGetter(PaintingLocation::direction)
					)
					.apply(instance, PaintingLocation::new)
	);
	public static final StreamCodec<FriendlyByteBuf, PaintingLocation> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC,
			location -> location.pos,
			Direction.STREAM_CODEC,
			location -> location.direction,
			PaintingLocation::new
	);

	public PaintingLocation(BlockPos pos, Direction direction) {
		this.pos = pos;
		this.direction = direction;
	}

	public double distanceTo(BlockPos newPos) {
		float f = (float) (pos.getX() - newPos.getX());
		float f1 = (float) (pos.getY() - newPos.getY());
		float f2 = (float) (pos.getZ() - newPos.getZ());
		return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
	}
}