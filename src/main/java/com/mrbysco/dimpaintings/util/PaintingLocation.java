package com.mrbysco.dimpaintings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record PaintingLocation(BlockPos pos, int direction2D) {
	public static final Codec<PaintingLocation> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							BlockPos.CODEC.fieldOf("pos").forGetter(PaintingLocation::pos),
							Codec.INT.fieldOf("direction").forGetter(PaintingLocation::direction2D)
					)
					.apply(instance, PaintingLocation::new)
	);
	public static final StreamCodec<FriendlyByteBuf, PaintingLocation> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC,
			location -> location.pos,
			ByteBufCodecs.INT,
			location -> location.direction2D,
			PaintingLocation::new
	);

	public PaintingLocation(BlockPos pos, int direction2D) {
		this.pos = pos;
		this.direction2D = direction2D;
	}

	public PaintingLocation(BlockPos pos, Direction direction) {
		this(pos, direction.get2DDataValue());
	}

	public double distanceTo(BlockPos newPos) {
		float f = (float) (pos.getX() - newPos.getX());
		float f1 = (float) (pos.getY() - newPos.getY());
		float f2 = (float) (pos.getZ() - newPos.getZ());
		return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
	}

	public Direction direction() {
		return Direction.from2DDataValue(direction2D);
	}
}