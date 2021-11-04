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
	public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
		PortalInfo pos;

		pos = placeInExistingPortal(destWorld, entity, dimensionPosition(entity, destWorld), entity instanceof Player);

		return pos;
	}

	@Nullable
	private static PortalInfo placeInExistingPortal(ServerLevel destWorld, Entity entity, BlockPos pos, boolean isPlayer) {
		int i = 200;
		BlockPos blockpos = pos;
		boolean isToOverworld = destWorld.dimension() == Level.OVERWORLD;
		boolean isFromEnd = entity.level.dimension() == Level.END && isToOverworld;
		boolean isToEnd = destWorld.dimension() == Level.END;

		if(isFromEnd || (isToOverworld && DimensionalConfig.COMMON.overworldToBed.get())) {
			blockpos = destWorld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, destWorld.getSharedSpawnPos());
			float angle = entity.getXRot();

			if(isPlayer && entity instanceof ServerPlayer) {
				ServerPlayer serverPlayer = (ServerPlayer) entity;
				BlockPos respawnPos = serverPlayer.getRespawnPosition();
				float respawnAngle = serverPlayer.getRespawnAngle();
				Optional<Vec3> optional;
				if (serverPlayer != null && respawnPos != null) {
					optional = Player.findRespawnPositionAndUseSpawnBlock(destWorld, respawnPos, respawnAngle, false, false);
				} else {
					optional = Optional.empty();
				}

				boolean flag2 = false;
				if (optional.isPresent()) {
					BlockState blockstate = destWorld.getBlockState(respawnPos);
					boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
					Vec3 vector3d = optional.get();
					float f1;
					if (!blockstate.is(BlockTags.BEDS) && !flag1) {
						f1 = respawnAngle;
					} else {
						Vec3 vector3d1 = Vec3.atBottomCenterOf(respawnPos).subtract(vector3d).normalize();
						f1 = (float)Mth.wrapDegrees(Mth.atan2(vector3d1.z, vector3d1.x) * (double)(180F / (float)Math.PI) - 90.0D);
					}
					angle = f1;
					blockpos = new BlockPos(vector3d.x, vector3d.y, vector3d.z);

					flag2 = flag1;
				} else if (blockpos != null) {
					serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
				}

				if (flag2) {
					serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)respawnPos.getX(), (double)respawnPos.getY(), (double)respawnPos.getZ(), 1.0F, 1.0F));
				}
			}
			return new PortalInfo(new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), entity.getDeltaMovement(), angle, entity.getXRot());
		} else if(isToEnd) {
			ServerLevel.makeObsidianPlatform(destWorld);
			blockpos = ServerLevel.END_SPAWN_POINT;

			return new PortalInfo(new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
		} else {
			PaintingWorldData worldData = PaintingWorldData.get(destWorld);
			List<PaintingLocation> paintingList = worldData.getDimensionPositions(destWorld.dimension().location());
			if(!paintingList.isEmpty()) {
				List<ClosestPosition> closestList = new ArrayList<>();
				for(PaintingLocation paintingPos : paintingList) {
					int distance = (int)distanceTo(pos, paintingPos.pos);
					if(distance < i) {
						blockpos = paintingPos.pos.relative(paintingPos.getDirection());
						if (!blockpos.equals(BlockPos.ZERO)) {
							closestList.add(new ClosestPosition(distance, blockpos));
						}
					}
				}
				if(!closestList.isEmpty()) {
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
		float f = (float)(origin.getX() - paintingPos.getX());
		float f1 = (float)(origin.getZ() - paintingPos.getZ());
		return Mth.sqrt(f * f + f1 * f1);
	}

	//Safety stuff
	private static PortalInfo moveToSafeCoords(ServerLevel world, Entity entity, BlockPos pos, boolean withGlass) {
		if (world.isEmptyBlock(pos.below())) {
			int distance;
			for(distance = 1; world.getBlockState(pos.below(distance)).getBlock().isPossibleToRespawnInThis(); ++distance) {
			}

			if (distance > 4) {
				makePlatform(world, pos, withGlass);
			}
		} else {
			BlockPos abovePos = pos.above(1);
			if(!world.getBlockState(pos.below()).getMaterial().isLiquid() && world.getBlockState(pos.above()).getBlock().isPossibleToRespawnInThis() &&
					world.getBlockState(abovePos).getBlock().isPossibleToRespawnInThis()) {
				return makePortalInfo(entity, abovePos.getX() + 0.5D, abovePos.getY(), abovePos.getZ() + 0.5D);
			}
			if(!world.isEmptyBlock(pos.below()) || !world.isEmptyBlock(pos)) {
				makePlatform(world, abovePos, withGlass);
				return makePortalInfo(entity, abovePos.getX(), abovePos.getY(), abovePos.getZ());
			}
		}

		return makePortalInfo(entity, pos.getX(), pos.getY(), pos.getZ());
	}

	private static void makePlatform(ServerLevel world, BlockPos pos, boolean withGlass) {
		int i = pos.getX();
		int j = pos.getY() - 2;
		int k = pos.getZ();
		if(withGlass) {
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
		}
		BlockPos.betweenClosed(i - 1, j, k - 1, i + 1, j, k + 1).forEach((blockPos) -> {
			if(world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0) {
				world.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
			}
		});
	}

	private BlockPos dimensionPosition(Entity entity, Level destWorld) {
		boolean flag2 = destWorld.dimension() == Level.NETHER;
		if (entity.level.dimension() != Level.NETHER && !flag2) {
			return entity.blockPosition();
		} else {
			WorldBorder worldborder = destWorld.getWorldBorder();
			double d0 = Math.max(-2.9999872E7D, worldborder.getMinX() + 16.0D);
			double d1 = Math.max(-2.9999872E7D, worldborder.getMinZ() + 16.0D);
			double d2 = Math.min(2.9999872E7D, worldborder.getMaxX() - 16.0D);
			double d3 = Math.min(2.9999872E7D, worldborder.getMaxZ() - 16.0D);
			double d4 = DimensionType.getTeleportationScale(entity.level.dimensionType(), destWorld.dimensionType());
			int maxY = DimensionalConfig.COMMON.netherMaxY.get();
			return new BlockPos(Mth.clamp(entity.getX() * d4, d0, d2), Mth.clamp(entity.getY(), 2, maxY), Mth.clamp(entity.getZ() * d4, d1, d3));
		}
	}

	private static PortalInfo makePortalInfo(Entity entity, double x, double y, double z) {
		return makePortalInfo(entity, new Vec3(x, y, z));
	}

	private static PortalInfo makePortalInfo(Entity entity, Vec3 pos) {
		return new PortalInfo(pos, Vec3.ZERO, entity.getYRot(), entity.getXRot());
	}

	public PaintingTeleporter(ServerLevel worldIn) {
	}

	@Override
	public Entity placeEntity(Entity newEntity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
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