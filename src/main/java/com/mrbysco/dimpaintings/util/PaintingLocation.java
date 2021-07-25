package com.mrbysco.dimpaintings.util;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class PaintingLocation {
	public final BlockPos pos;
	public final int direction2D;

	PaintingLocation(BlockPos pos, int direction2D) {
		this.pos = pos;
		this.direction2D = direction2D;
	}

	PaintingLocation(BlockPos pos, Direction direction) {
		this.pos = pos;
		this.direction2D = direction.get2DDataValue();
	}

	public double distanceTo(BlockPos newPos) {
		float f = (float)(pos.getX() - newPos.getX());
		float f1 = (float)(pos.getY() - newPos.getY());
		float f2 = (float)(pos.getZ() - newPos.getZ());
		return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
	}

	public Direction getDirection() {
		return Direction.from2DDataValue(direction2D);
	}
}