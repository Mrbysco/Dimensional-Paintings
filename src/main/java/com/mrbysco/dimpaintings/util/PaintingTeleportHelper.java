package com.mrbysco.dimpaintings.util;

import com.mrbysco.dimpaintings.config.DimensionalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaintingTeleportHelper {

	@Nullable
	public static DimensionTransition getPaintingTeleportData(ServerLevel destination, Entity entity, BlockPos pos, boolean isPlayer) {
		int i = 200;
		BlockPos blockpos = pos;
		boolean isToOverworld = destination.dimension() == Level.OVERWORLD;
		boolean isFromEnd = entity.level().dimension() == Level.END && isToOverworld;
		boolean isToEnd = destination.dimension() == Level.END;

		if (isFromEnd || (isToOverworld && DimensionalConfig.COMMON.overworldToBed.get())) {
			blockpos = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, destination.getSharedSpawnPos());
			float angle = entity.getXRot();

			if (isPlayer && entity instanceof ServerPlayer serverPlayer) {
				BlockPos respawnPos = serverPlayer.getRespawnPosition();
				float respawnAngle = serverPlayer.getRespawnAngle();
				DimensionTransition optional;
				if (serverPlayer != null && respawnPos != null) {
					optional = serverPlayer.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING);
				} else {
					optional = null;
				}

				boolean flag2 = false;
				if (optional != null) {
					BlockState blockstate = destination.getBlockState(respawnPos);
					boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
					Vec3 vector3d = optional.pos();
					float f1;
					if (!blockstate.is(BlockTags.BEDS) && !flag1) {
						f1 = respawnAngle;
					} else {
						Vec3 vector3d1 = Vec3.atBottomCenterOf(respawnPos).subtract(vector3d).normalize();
						f1 = (float) Mth.wrapDegrees(Mth.atan2(vector3d1.z, vector3d1.x) * (double) (180F / (float) Math.PI) - 90.0D);
					}
					angle = f1;
					blockpos = BlockPos.containing(vector3d.x, vector3d.y, vector3d.z);

					flag2 = flag1;
				} else if (blockpos != null) {
					serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
				}

				if (flag2) {
					serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) respawnPos.getX(), (double) respawnPos.getY(), (double) respawnPos.getZ(), 1.0F, 1.0F, destination.getSeed()));
				}
			}
			return new DimensionTransition(
					destination,
					new Vec3((double) blockpos.getX() + 0.5D, (double) blockpos.getY(), (double) blockpos.getZ() + 0.5D),
					entity.getDeltaMovement(),
					angle,
					entity.getXRot(),
					DimensionTransition.DO_NOTHING);
		} else if (isToEnd) {
			blockpos = ServerLevel.END_SPAWN_POINT;

			return new DimensionTransition(
					destination,
					new Vec3((double) blockpos.getX() + 0.5D,
							(double) blockpos.getY(),
							(double) blockpos.getZ() + 0.5D),
					entity.getDeltaMovement(),
					entity.getYRot(),
					entity.getXRot(),
					DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
		} else {
			PaintingWorldData worldData = PaintingWorldData.get(destination);
			List<PaintingLocation> paintingList = worldData.getDimensionPositions(destination.dimension().location());
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
					blockpos = closestList.getFirst().pos();
					return moveToSafeCoords(destination, entity, blockpos, false);
				}
			}
		}

		if (blockpos.equals(BlockPos.ZERO)) {
			return null;
		} else {
			return moveToSafeCoords(destination, entity, blockpos, true);
		}
	}

	private static double distanceTo(BlockPos origin, BlockPos paintingPos) {
		float f = (float) (origin.getX() - paintingPos.getX());
		float f1 = (float) (origin.getZ() - paintingPos.getZ());
		return Mth.sqrt(f * f + f1 * f1);
	}

	//Safety stuff
	private static DimensionTransition moveToSafeCoords(ServerLevel destination, Entity entity, BlockPos pos, boolean withGlass) {
		if (destination.isEmptyBlock(pos.below())) {
			int distance;
			for (distance = 1; distance < 32; ++distance) {
				BlockPos checkPos = pos.below(distance);
				BlockState belowState = destination.getBlockState(checkPos);
				if (belowState.entityCanStandOn(destination, checkPos, entity)) {
					break;
				}
			}

			if (distance > 4) {
				makePlatform(destination, pos, withGlass);
			}
		} else {
			BlockPos abovePos = pos.above(1);
			BlockState aboveState = destination.getBlockState(pos.above());
			BlockState aboveState2 = destination.getBlockState(abovePos);
			if (aboveState.getBlock().isPossibleToRespawnInThis(aboveState) &&
					aboveState2.getBlock().isPossibleToRespawnInThis(aboveState2)) {
				return makePortalInfo(destination, entity, abovePos.getX() + 0.5D, abovePos.getY(), abovePos.getZ() + 0.5D);
			}
			if (!destination.isEmptyBlock(pos.below()) || !destination.isEmptyBlock(pos)) {
				makePlatform(destination, abovePos, withGlass);
				return makePortalInfo(destination, entity, abovePos.getX(), abovePos.getY(), abovePos.getZ());
			}
		}

		return makePortalInfo(destination, entity, pos.getX(), pos.getY(), pos.getZ());
	}

	private static void makePlatform(ServerLevel serverLevel, BlockPos pos, boolean withGlass) {
		int i = pos.getX();
		int j = pos.getY() - 2;
		int k = pos.getZ();
		if (withGlass) {
			BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 4, k + 2).forEach((blockPos) -> {
				if (!serverLevel.getFluidState(blockPos).isEmpty() || serverLevel.getBlockState(blockPos).getDestroySpeed(serverLevel, blockPos) >= 0) {
					serverLevel.setBlockAndUpdate(blockPos, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
				}
			});
			BlockPos.betweenClosed(i - 1, j + 1, k - 1, i + 1, j + 3, k + 1).forEach((blockPos) -> {
				if (serverLevel.getBlockState(blockPos).getDestroySpeed(serverLevel, blockPos) >= 0) {
					serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
				}
			});
		}
		BlockPos.betweenClosed(i - 1, j, k - 1, i + 1, j, k + 1).forEach((blockPos) -> {
			if (serverLevel.getBlockState(blockPos).getDestroySpeed(serverLevel, blockPos) >= 0) {
				serverLevel.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
			}
		});
	}

	private BlockPos dimensionPosition(Entity entity, Level destLevel) {
		boolean flag2 = destLevel.dimension() == Level.NETHER;
		if (entity.level().dimension() != Level.NETHER && !flag2) {
			return entity.blockPosition();
		} else {
			WorldBorder worldborder = destLevel.getWorldBorder();
			double d0 = Math.max(-2.9999872E7D, worldborder.getMinX() + 16.0D);
			double d1 = Math.max(-2.9999872E7D, worldborder.getMinZ() + 16.0D);
			double d2 = Math.min(2.9999872E7D, worldborder.getMaxX() - 16.0D);
			double d3 = Math.min(2.9999872E7D, worldborder.getMaxZ() - 16.0D);
			double d4 = DimensionType.getTeleportationScale(entity.level().dimensionType(), destLevel.dimensionType());
			int maxY = DimensionalConfig.COMMON.netherMaxY.get();
			return BlockPos.containing(Mth.clamp(entity.getX() * d4, d0, d2), Mth.clamp(entity.getY(), 2, maxY), Mth.clamp(entity.getZ() * d4, d1, d3));
		}
	}

	private static DimensionTransition makePortalInfo(ServerLevel destination, Entity entity, double x, double y, double z) {
		return makePortalInfo(destination, entity, new Vec3(x, y, z));
	}

	private static DimensionTransition makePortalInfo(ServerLevel destination, Entity entity, Vec3 pos) {
		return new DimensionTransition(destination, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING);
	}

	record ClosestPosition(int distance, BlockPos pos) implements Comparable<ClosestPosition> {
		@Override
		public int compareTo(ClosestPosition anotherPosition) {
			return compare(this.distance, anotherPosition.distance());
		}

		public static int compare(int x, int y) {
			return Integer.compare(x, y);
		}
	}
}