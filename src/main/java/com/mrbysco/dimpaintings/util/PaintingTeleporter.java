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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PaintingTeleporter implements ITeleporter {
	@Nullable
	@Override
	public PortalInfo getPortalInfo(Entity entity, ServerLevel destLevel, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
		PortalInfo pos;

		pos = placeInExistingPortal(destLevel, entity, dimensionPosition(entity, destLevel), entity instanceof Player);

		return pos;
	}

	@Nullable
	private static PortalInfo placeInExistingPortal(ServerLevel destLevel, Entity entity, BlockPos pos, boolean isPlayer) {
		int i = 200;
		BlockPos blockpos = pos;
		boolean isToOverworld = destLevel.dimension() == Level.OVERWORLD;
		boolean isFromEnd = entity.level().dimension() == Level.END && isToOverworld;
		boolean isToEnd = destLevel.dimension() == Level.END;

		if (isFromEnd || (isToOverworld && DimensionalConfig.COMMON.overworldToBed.get())) {
			blockpos = destLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, destLevel.getSharedSpawnPos());
			float angle = entity.getXRot();

			if (isPlayer && entity instanceof ServerPlayer serverPlayer) {
				BlockPos respawnPos = serverPlayer.getRespawnPosition();
				float respawnAngle = serverPlayer.getRespawnAngle();
				Optional<Vec3> optional;
				if (serverPlayer != null && respawnPos != null) {
					optional = Player.findRespawnPositionAndUseSpawnBlock(destLevel, respawnPos, respawnAngle, false, false);
				} else {
					optional = Optional.empty();
				}

				boolean flag2 = false;
				if (optional.isPresent()) {
					BlockState blockstate = destLevel.getBlockState(respawnPos);
					boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
					Vec3 vector3d = optional.get();
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
					serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double) respawnPos.getX(), (double) respawnPos.getY(), (double) respawnPos.getZ(), 1.0F, 1.0F, destLevel.getSeed()));
				}
			}
			return new PortalInfo(new Vec3((double) blockpos.getX() + 0.5D, (double) blockpos.getY(), (double) blockpos.getZ() + 0.5D), entity.getDeltaMovement(), angle, entity.getXRot());
		} else if (isToEnd) {
			ServerLevel.makeObsidianPlatform(destLevel);
			blockpos = ServerLevel.END_SPAWN_POINT;

			return new PortalInfo(new Vec3((double) blockpos.getX() + 0.5D, (double) blockpos.getY(), (double) blockpos.getZ() + 0.5D), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
		} else {
			PaintingWorldData worldData = PaintingWorldData.get(destLevel);
			List<PaintingLocation> paintingList = worldData.getDimensionPositions(destLevel.dimension().location());
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
					blockpos = closestList.get(0).pos();
					return moveToSafeCoords(destLevel, entity, blockpos, false);
				}
			}
		}

		if (blockpos.equals(BlockPos.ZERO)) {
			return null;
		} else {
			return moveToSafeCoords(destLevel, entity, blockpos, true);
		}
	}

	private static double distanceTo(BlockPos origin, BlockPos paintingPos) {
		float f = (float) (origin.getX() - paintingPos.getX());
		float f1 = (float) (origin.getZ() - paintingPos.getZ());
		return Mth.sqrt(f * f + f1 * f1);
	}

	//Safety stuff
	private static PortalInfo moveToSafeCoords(ServerLevel serverLevel, Entity entity, BlockPos pos, boolean withGlass) {
		if (serverLevel.isEmptyBlock(pos.below())) {
			int distance;
			for (distance = 1; distance < 32; ++distance) {
				BlockPos checkPos = pos.below(distance);
				BlockState belowState = serverLevel.getBlockState(checkPos);
				if (belowState.entityCanStandOn(serverLevel, checkPos, entity)) {
					break;
				}
			}

			if (distance > 4) {
				makePlatform(serverLevel, pos, withGlass);
			}
		} else {
			BlockPos abovePos = pos.above(1);
			BlockState aboveState = serverLevel.getBlockState(pos.above());
			BlockState aboveState2 = serverLevel.getBlockState(abovePos);
			if (aboveState.getBlock().isPossibleToRespawnInThis(aboveState) &&
					aboveState2.getBlock().isPossibleToRespawnInThis(aboveState2)) {
				return makePortalInfo(entity, abovePos.getX() + 0.5D, abovePos.getY(), abovePos.getZ() + 0.5D);
			}
			if (!serverLevel.isEmptyBlock(pos.below()) || !serverLevel.isEmptyBlock(pos)) {
				makePlatform(serverLevel, abovePos, withGlass);
				return makePortalInfo(entity, abovePos.getX(), abovePos.getY(), abovePos.getZ());
			}
		}

		return makePortalInfo(entity, pos.getX(), pos.getY(), pos.getZ());
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

	private static PortalInfo makePortalInfo(Entity entity, double x, double y, double z) {
		return makePortalInfo(entity, new Vec3(x, y, z));
	}

	private static PortalInfo makePortalInfo(Entity entity, Vec3 pos) {
		return new PortalInfo(pos, Vec3.ZERO, entity.getYRot(), entity.getXRot());
	}

	public PaintingTeleporter(ServerLevel serverLevel) {
	}

	@Override
	public Entity placeEntity(Entity newEntity, ServerLevel currentLevel, ServerLevel destLevel, float yaw, Function<Boolean, Entity> repositionEntity) {
		newEntity.fallDistance = 0;
		return repositionEntity.apply(false); //Must be false or we fall on vanilla
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