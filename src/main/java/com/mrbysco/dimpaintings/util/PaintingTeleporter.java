package com.mrbysco.dimpaintings.util;

import net.minecraft.block.Blocks;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class PaintingTeleporter implements ITeleporter {
	@Nullable
	@Override
	public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
		PortalInfo pos;

		if ((pos = placeInExistingPortal(destWorld, entity, dimensionPosition(entity, destWorld), entity instanceof PlayerEntity)) == null) {
			pos = moveToSafeCoords(destWorld, entity, dimensionPosition(entity, destWorld));
			pos = placeInExistingPortal(destWorld, entity, new BlockPos(pos.pos), entity instanceof PlayerEntity);
		} else {
			pos = moveToSafeCoords(destWorld, entity, dimensionPosition(entity, destWorld));
		}
		return pos;
	}

	@Nullable
	private static PortalInfo placeInExistingPortal(ServerWorld world, Entity entity, BlockPos pos, boolean isPlayer) {
		int i = 200;
		BlockPos blockpos = pos;
		boolean isFromEnd = entity.level.dimension() == World.END && world.dimension() == World.OVERWORLD;
		boolean isToEnd = world.dimension() == World.END;

		if(isFromEnd) {
			blockpos = world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, world.getSharedSpawnPos());
			return new PortalInfo(new Vector3d((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), entity.getDeltaMovement(), entity.yRot, entity.xRot);
		} else if(isToEnd) {
			world.makeObsidianPlatform(world);
			blockpos = ServerWorld.END_SPAWN_POINT;

			return new PortalInfo(new Vector3d((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), entity.getDeltaMovement(), entity.yRot, entity.xRot);
		} else {
			PaintingWorldData worldData = PaintingWorldData.get(world);
			List<PaintingLocation> paintingList = worldData.getDimensionPositions(world.dimension().location());
			if(!paintingList.isEmpty()) {
				for(PaintingLocation paintingPos : paintingList) {
					if(distanceTo(pos, paintingPos.pos) < i) {
						blockpos = paintingPos.pos.relative(paintingPos.getDirection());
						break;
					}
				}
			}
		}

		if (blockpos.equals(BlockPos.ZERO)) {
			return null;
		} else {
			return makePortalInfo(entity, blockpos.getX(), blockpos.getY(), blockpos.getZ());
		}
	}

	private static double distanceTo(BlockPos origin, BlockPos paintingPos) {
		float f = (float)(origin.getX() - paintingPos.getX());
		float f1 = (float)(origin.getY() - paintingPos.getY());
		float f2 = (float)(origin.getZ() - paintingPos.getZ());
		return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
	}

	//Safety stuff
	private static PortalInfo moveToSafeCoords(ServerWorld world, Entity entity, BlockPos pos) {
		if (world.isEmptyBlock(pos.below())) {
			int distance;
			for(distance = 1; world.isEmptyBlock(pos.below(distance)); ++distance) {
			}

			if (distance > 3) {
				makePlatform(world, pos);
			}
		} else {
			if(!world.isEmptyBlock(pos) && world.isEmptyBlock(pos.above()) && world.isEmptyBlock(pos.above(2))) {
				BlockPos abovePos = pos.above(2);
				return makePortalInfo(entity, abovePos.getX(), abovePos.getY(), abovePos.getZ());
			}
			if(!world.isEmptyBlock(pos.below()) || !world.isEmptyBlock(pos)) {
				makePlatform(world, pos);
			}
		}

		return makePortalInfo(entity, pos.getX(), pos.getY(), pos.getZ());
	}

	private static void makePlatform(ServerWorld world, BlockPos pos) {
		int i = pos.getX();
		int j = pos.getY() - 2;
		int k = pos.getZ();
		BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 4, k + 2).forEach((blockPos) -> {
			if(!world.getFluidState(blockPos).isEmpty() || world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
				world.setBlockAndUpdate(blockPos, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
			}
		});
		BlockPos.betweenClosed(i - 1, j + 1, k - 1, i + 1, j + 3, k + 1).forEach((blockPos) -> {
			if(world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
				world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
			}
		});
		BlockPos.betweenClosed(i - 1, j, k - 1, i + 1, j, k + 1).forEach((blockPos) -> {
			if(world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
				world.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
			}
		});
	}

	private BlockPos dimensionPosition(Entity entity, World destWorld) {
		boolean flag2 = destWorld.dimension() == World.NETHER;
		if (entity.level.dimension() != World.NETHER && !flag2) {
			return entity.blockPosition();
		} else {
			WorldBorder worldborder = destWorld.getWorldBorder();
			double d0 = Math.max(-2.9999872E7D, worldborder.getMinX() + 16.0D);
			double d1 = Math.max(-2.9999872E7D, worldborder.getMinZ() + 16.0D);
			double d2 = Math.min(2.9999872E7D, worldborder.getMaxX() - 16.0D);
			double d3 = Math.min(2.9999872E7D, worldborder.getMaxZ() - 16.0D);
			double d4 = DimensionType.getTeleportationScale(entity.level.dimensionType(), destWorld.dimensionType());
			BlockPos blockpos1 = new BlockPos(MathHelper.clamp(entity.getX() * d4, d0, d2), entity.getY(), MathHelper.clamp(entity.getZ() * d4, d1, d3));

			return blockpos1;
		}
	}

	private static PortalInfo makePortalInfo(Entity entity, double x, double y, double z) {
		return makePortalInfo(entity, new Vector3d(x, y, z));
	}

	private static PortalInfo makePortalInfo(Entity entity, Vector3d pos) {
		return new PortalInfo(pos, Vector3d.ZERO, entity.yRot, entity.xRot);
	}

	public PaintingTeleporter(ServerWorld worldIn) {
	}

	@Override
	public Entity placeEntity(Entity newEntity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
		newEntity.fallDistance = 0;
		return repositionEntity.apply(false); //Must be false or we fall on vanilla
	}
}