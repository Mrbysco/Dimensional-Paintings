package com.mrbysco.dimpaintings.util;

import com.mrbysco.dimpaintings.config.DimensionalConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PaintingTeleporter implements ITeleporter {
	@Nullable
	@Override
	public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
		PortalInfo pos;

		pos = placeInExistingPortal(destWorld, entity, dimensionPosition(entity, destWorld), entity instanceof PlayerEntity);

		return pos;
	}

	@Nullable
	private static PortalInfo placeInExistingPortal(ServerWorld destWorld, Entity entity, BlockPos pos, boolean isPlayer) {
		int i = 200;
		BlockPos blockpos = pos;
		boolean isToOverworld = destWorld.dimension() == World.OVERWORLD;
		boolean isFromEnd = entity.level.dimension() == World.END && isToOverworld;
		boolean isToEnd = destWorld.dimension() == World.END;

		if (isFromEnd || (isToOverworld && DimensionalConfig.COMMON.overworldToBed.get())) {
			blockpos = destWorld.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, destWorld.getSharedSpawnPos());
			float angle = entity.xRot;

			if (isPlayer && entity instanceof ServerPlayerEntity) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
				BlockPos respawnPos = serverPlayer.getRespawnPosition();
				float respawnAngle = serverPlayer.getRespawnAngle();
				Optional<Vector3d> optional;
				if (serverPlayer != null && respawnPos != null) {
					optional = PlayerEntity.findRespawnPositionAndUseSpawnBlock(destWorld, respawnPos, respawnAngle, false, false);
				} else {
					optional = Optional.empty();
				}

				boolean flag2 = false;
				if (optional.isPresent()) {
					BlockState blockstate = destWorld.getBlockState(respawnPos);
					boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
					Vector3d vector3d = optional.get();
					float f1;
					if (!blockstate.is(BlockTags.BEDS) && !flag1) {
						f1 = respawnAngle;
					} else {
						Vector3d vector3d1 = Vector3d.atBottomCenterOf(respawnPos).subtract(vector3d).normalize();
						f1 = (float) MathHelper.wrapDegrees(MathHelper.atan2(vector3d1.z, vector3d1.x) * (double) (180F / (float) Math.PI) - 90.0D);
					}
					angle = f1;
					blockpos = new BlockPos(vector3d.x, vector3d.y, vector3d.z);

					flag2 = flag1;
				} else if (blockpos != null) {
					serverPlayer.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
				}

				if (flag2) {
					serverPlayer.connection.send(new SPlaySoundEffectPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double) respawnPos.getX(), (double) respawnPos.getY(), (double) respawnPos.getZ(), 1.0F, 1.0F));
				}
			}
			return new PortalInfo(new Vector3d((double) blockpos.getX() + 0.5D, (double) blockpos.getY(), (double) blockpos.getZ() + 0.5D), entity.getDeltaMovement(), angle, entity.xRot);
		} else if (isToEnd) {
			ServerWorld.makeObsidianPlatform(destWorld);
			blockpos = ServerWorld.END_SPAWN_POINT;

			return new PortalInfo(new Vector3d((double) blockpos.getX() + 0.5D, (double) blockpos.getY(), (double) blockpos.getZ() + 0.5D), entity.getDeltaMovement(), entity.yRot, entity.xRot);
		} else {
			PaintingWorldData worldData = PaintingWorldData.get(destWorld);
			List<PaintingLocation> paintingList = worldData.getDimensionPositions(destWorld.dimension().location());
			if (!paintingList.isEmpty()) {
				List<ClosestPosition> closestList = new ArrayList<>();
				for (PaintingLocation paintingPos : paintingList) {
					int distance = (int) distanceTo(pos, paintingPos.pos);
					if (distance < i) {
						blockpos = paintingPos.pos.relative(paintingPos.getDirection());
						if (!blockpos.equals(BlockPos.ZERO)) {
							closestList.add(new ClosestPosition(distance, blockpos));
						}
					}
				}
				if (!closestList.isEmpty()) {
					Collections.sort(closestList);
					blockpos = closestList.get(0).getPos();
					return moveToSafeCoords(destWorld, entity, blockpos, false);
				}
			}
		}

		if (blockpos.equals(BlockPos.ZERO)) {
			return null;
		} else {
			return moveToSafeCoords(destWorld, entity, blockpos, true);
		}
	}

	private static double distanceTo(BlockPos origin, BlockPos paintingPos) {
		float f = (float) (origin.getX() - paintingPos.getX());
		float f1 = (float) (origin.getZ() - paintingPos.getZ());
		return MathHelper.sqrt(f * f + f1 * f1);
	}

	//Safety stuff
	private static PortalInfo moveToSafeCoords(ServerWorld world, Entity entity, BlockPos pos, boolean withGlass) {
		if (world.isEmptyBlock(pos.below())) {
			int distance;
			for (distance = 1; world.getBlockState(pos.below(distance)).getBlock().isPossibleToRespawnInThis() && distance < 32; ++distance) {
			}

			if (distance > 4) {
				makePlatform(world, pos, withGlass);
			}
		} else {
			BlockPos abovePos = pos.above(1);
			if (world.getBlockState(pos.above()).getBlock().isPossibleToRespawnInThis() &&
					world.getBlockState(pos.above(1)).getBlock().isPossibleToRespawnInThis()) {
				return makePortalInfo(entity, abovePos.getX() + 0.5D, abovePos.getY(), abovePos.getZ() + 0.5D);
			}
			if (!world.isEmptyBlock(pos.below()) || !world.isEmptyBlock(pos)) {
				makePlatform(world, abovePos, withGlass);
				return makePortalInfo(entity, abovePos.getX(), abovePos.getY(), abovePos.getZ());
			}
		}

		return makePortalInfo(entity, pos.getX(), pos.getY(), pos.getZ());
	}

	private static void makePlatform(ServerWorld world, BlockPos pos, boolean withGlass) {
		int i = pos.getX();
		int j = pos.getY() - 2;
		int k = pos.getZ();
		if (withGlass) {
			BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 4, k + 2).forEach((blockPos) -> {
				if (!world.getFluidState(blockPos).isEmpty() || world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
					world.setBlockAndUpdate(blockPos, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
				}
			});
			BlockPos.betweenClosed(i - 1, j + 1, k - 1, i + 1, j + 3, k + 1).forEach((blockPos) -> {
				if (world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
					world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
				}
			});
		}
		BlockPos.betweenClosed(i - 1, j, k - 1, i + 1, j, k + 1).forEach((blockPos) -> {
			if (world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
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
			int maxY = DimensionalConfig.COMMON.netherMaxY.get();
			return new BlockPos(MathHelper.clamp(entity.getX() * d4, d0, d2), MathHelper.clamp(entity.getY(), 2, maxY), MathHelper.clamp(entity.getZ() * d4, d1, d3));
		}
	}

	private static PortalInfo makePortalInfo(Entity entity, double x, double y, double z) {
		return makePortalInfo(entity, new Vector3d(x, y, z));
	}

	private static PortalInfo makePortalInfo(Entity entity, Vector3d pos) {
		return new PortalInfo(pos, Vector3d.ZERO, entity.yRot, entity.xRot);
	}

	public PaintingTeleporter(ServerWorld world) {
	}

	@Override
	public Entity placeEntity(Entity newEntity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
		newEntity.fallDistance = 0;
		return repositionEntity.apply(false); //Must be false or we fall on vanilla
	}

	static class ClosestPosition implements Comparable<ClosestPosition> {
		private final int distance;
		private final BlockPos pos;

		ClosestPosition(int distance, BlockPos pos) {
			this.distance = distance;
			this.pos = pos;
		}

		public int getDistance() {
			return distance;
		}

		public BlockPos getPos() {
			return pos;
		}

		@Override
		public int compareTo(ClosestPosition anotherPosition) {
			return compare(this.distance, anotherPosition.getDistance());
		}

		public static int compare(int x, int y) {
			return Integer.compare(x, y);
		}
	}
}