package com.mrbysco.dimpaintings.util;

import com.mojang.datafixers.util.Pair;
import com.mrbysco.dimpaintings.config.DimensionalConfig;
import it.unimi.dsi.fastutil.longs.Long2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class PaintingTeleportHelper {
	private static final Object2ObjectMap<ResourceKey<Level>, LevelTeleportFinder> LEVEL_TELEPORTERS = Util.make(new Object2ObjectOpenHashMap<>(), map ->
			map.put(ServerLevel.END, (entity, destWorld, minMaxBounds, cacheMap) -> toEnd(entity, destWorld))
	);

	@Nullable
	public static DimensionTransition getPaintingTeleportData(ServerLevel destWorld, Entity entity) {
		entity.fallDistance = 0;
		if (entity instanceof LivingEntity livingEntity) { //Give resistance
			livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 200, false, false));
		}
		BlockPos spawnPos = entity.blockPosition();

		// Cache map so that we don't need to run checking the specific block position each time
		Long2BooleanArrayMap safeLocation = new Long2BooleanArrayMap();

		// Add bounds for checking the y-positions the entity can spawn
		var minMaxBounds = Pair.of(destWorld.getMinBuildHeight(), destWorld.getMaxBuildHeight());
		if (destWorld.dimension() == Level.NETHER) {
			minMaxBounds = Pair.of(DimensionalConfig.COMMON.netherMaxY.get(), destWorld.getMaxBuildHeight());
		}

		// If spawn position exists, verify position is safe
		if (spawnPos != null && destWorld.getBlockState(spawnPos.relative(Direction.DOWN)).isSolid() &&
				isPositionSafe(entity, destWorld, spawnPos, safeLocation, minMaxBounds)) {
			return postProcessAndMake(destWorld, spawnPos, entity);
		}

		// Check level teleporter to determine portal info
		@Nullable
		DimensionTransition levelInfo = LEVEL_TELEPORTERS.getOrDefault(destWorld.dimension(),
				PaintingTeleportHelper::searchAroundAndDown).determineTeleportLocation(entity, destWorld, minMaxBounds, safeLocation);
		if (levelInfo != null) {
			return levelInfo;
		}


		// If none of these positions work, use the entity's current position and spawn and safety ring around them
		// If the entity's position isn't within the world bounds, use default coordinates instead (0, 70, 0)

		BlockPos teleportPos = destWorld.getWorldBorder().isWithinBounds(entity.blockPosition())
				&& minMaxBounds.getFirst() < entity.blockPosition().getY() - 1
				&& minMaxBounds.getSecond() > entity.blockPosition().getY() + entity.getBbHeight() + 1
				? entity.blockPosition() : new BlockPos(0, Math.max(minMaxBounds.getFirst(), 70), 0);

		makePlatform(destWorld, teleportPos, true);

		// Create info
		return postProcessAndMake(destWorld, teleportPos, entity);
	}

	/**
	 * Make safety platform around the entity's position.
	 *
	 * @param serverLevel the level the entity is teleporting to
	 * @param pos         the position the entity is trying to be spawned at
	 * @param withGlass   if the platform should have glass around it
	 */
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

	/**
	 * Search eight blocks around the current block and check down to determine where to teleport.
	 *
	 * @param entity       the entity attempting to spawn at the location
	 * @param destWorld    the level the entity is teleporting to
	 * @param cacheMap     a cache to prevent additional lookups to the position
	 * @param minMaxBounds the bounds of the y position the entity can spawn within
	 * @return the portal information to teleport to, or {@code null} if there is none
	 */
	@Nullable
	private static DimensionTransition searchAroundAndDown(Entity entity, ServerLevel destWorld, Pair<Integer, Integer> minMaxBounds, Long2BooleanArrayMap cacheMap) {
		// Set y position to max possible
		double dimensionScale = DimensionType.getTeleportationScale(entity.level().dimensionType(), destWorld.dimensionType());
		BlockPos spawnPos = destWorld.getWorldBorder().clampToBounds(entity.blockPosition().getX() * dimensionScale, entity.blockPosition().getY(), entity.blockPosition().getZ() * dimensionScale)
				.atY(Math.min(minMaxBounds.getSecond(), destWorld.getMinBuildHeight() + destWorld.getLogicalHeight()) - 1);

		// No spawn position or isn't valid, so loop around location
		for (var checkPos : BlockPos.spiralAround(spawnPos, 16, Direction.EAST, Direction.SOUTH)) {
			// Load chunk to actually check the location
			destWorld.getChunk(checkPos);

			// Cycle through positions from top to bottom
			for (int heightY = Math.min(spawnPos.getY(), destWorld.getHeight(Heightmap.Types.MOTION_BLOCKING, checkPos.getX(), checkPos.getZ())); heightY > minMaxBounds.getFirst(); --heightY) {
				checkPos.setY(heightY);

				// Since we are checking going down, we want to verify the player is on the floor
				// Check the player position afterward
				if (!destWorld.getBlockState(checkPos.immutable().relative(Direction.DOWN)).isSolid()
						|| !isPositionSafe(entity, destWorld, checkPos, cacheMap, minMaxBounds)
				) continue;

				// All positions the entity is in is safe, so spawn in that location
				return postProcessAndMake(destWorld, checkPos, entity);
			}
		}

		// If it fails, return null
		return null;
	}

	/**
	 * Set the portal information to the end's spawn point.
	 *
	 * @param entity    the entity attempting to spawn at the location
	 * @param destWorld the level the entity is teleporting to
	 * @return the portal information to teleport to, or {@code null} if there is none
	 * @deprecated this should be removed in favor of a datagen solution
	 */
	@Deprecated
	private static DimensionTransition toEnd(Entity entity, ServerLevel destWorld) {
		// Get teleport position
		BlockPos teleportPos = ServerLevel.END_SPAWN_POINT;
		Vec3 vec3 = teleportPos.getBottomCenter();
		EndPlatformFeature.createEndPlatform(destWorld, BlockPos.containing(vec3).below(), true);

		return postProcessAndMake(destWorld, teleportPos, entity);
	}

	/**
	 * Checks if all blocks within the entity's bounding box is safe to spawn in.
	 *
	 * @param entity       the entity attempting to spawn at the location
	 * @param destWorld    the level the entity is teleporting to
	 * @param checkPos     the position the entity is trying to be spawned at
	 * @param cacheMap     a cache to prevent additional lookups to the position
	 * @param minMaxBounds the bounds of the y position the entity can spawn within
	 * @return {@code true} if it is safe for the entity to spawn here, {@code false} otherwise
	 */
	private static boolean isPositionSafe(Entity entity, ServerLevel destWorld, BlockPos checkPos, Long2BooleanArrayMap cacheMap, Pair<Integer, Integer> minMaxBounds) {
		var halfWidth = entity.getBbWidth() / 2;
		// We construct the position based on the entity radius
		// We could use the AABB method; however we want to account fo edge cases where the entity is touching a corner with
		// the box, causing the safety check to fail and change the spawn position.
		// This is also why we round to the higher or lower value
		for (var entityBoxPos : BlockPos.betweenClosed(
				Math.round(checkPos.getX() - halfWidth),
				checkPos.getY(),
				Math.round(checkPos.getZ() - halfWidth),
				Math.round(checkPos.getX() + halfWidth),
				Math.round(checkPos.getY() + entity.getBbHeight()),
				Math.round(checkPos.getZ() + halfWidth)
		)) {
			// If a safe position isn't found in the entity is in, move to next spot to check
			if (!cacheMap.computeIfAbsent(
					entityBoxPos.asLong(),
					c -> {
						// Check if position is within bounds or that the position's min build height is higher than the spawning position
						if (!destWorld.getWorldBorder().isWithinBounds(entityBoxPos) || minMaxBounds.getFirst() >= entityBoxPos.getY())
							return false;
						// Get block state and check if it is possible to respawn
						BlockState entityBoxState = destWorld.getBlockState(entityBoxPos);
						return entityBoxState.getBlock().isPossibleToRespawnInThis(entityBoxState);
					}
			)) {
				return false;
			}
		}

		// If nothing fails, it is a safe location
		return true;
	}

	/**
	 * Sets the spawn location of the entity in compatible levels.
	 *
	 * @param destWorld the level the entity is teleporting to
	 * @param pos       the position the entity is trying to be spawned at
	 * @param entity    the entity attempting to spawn at the location
	 */
	private static DimensionTransition postProcessAndMake(ServerLevel destWorld, BlockPos pos, Entity entity) {
		// Set overworld back to respawn position when using painting.
		if (destWorld.dimension() == Level.OVERWORLD) {
			if (entity instanceof ServerPlayer serverPlayer) {
				serverPlayer.setRespawnPosition(Level.OVERWORLD, pos, serverPlayer.getYRot(), true, false);
			}
		}

		if (ModList.get().isLoaded("twilightforest")) {
			ResourceKey<Level> twilightKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("twilightforest", "twilight_forest"));
			if (destWorld.dimension() == twilightKey) {
				if (entity instanceof ServerPlayer serverPlayer) {
					serverPlayer.setRespawnPosition(twilightKey, pos, serverPlayer.getYRot(), true, false);
				}
			}
		}

		if (ModList.get().isLoaded("lostcities")) {
			ResourceKey<Level> lostCityKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("lostcities", "lostcity"));
			if (destWorld.dimension() == lostCityKey) {
				if (entity instanceof ServerPlayer serverPlayer) {
					serverPlayer.setRespawnPosition(lostCityKey, pos, serverPlayer.getYRot(), true, false);
				}
			}
		}

		return makePortalInfo(destWorld, entity, pos.getX(), pos.getY(), pos.getZ());
	}

	private static DimensionTransition makePortalInfo(ServerLevel destination, Entity entity, double x, double y, double z) {
		return makePortalInfo(destination, entity, new Vec3(x, y, z));
	}

	private static DimensionTransition makePortalInfo(ServerLevel destination, Entity entity, Vec3 pos) {
		return new DimensionTransition(destination, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING);
	}

	/**
	 * A location finder for determining where to teleport the entity in a given level.
	 */
	@FunctionalInterface
	interface LevelTeleportFinder {

		/**
		 * Determine where to teleport the entity for the given level.
		 *
		 * @param entity       the entity attempting to spawn at the location
		 * @param destWorld    the level the entity is teleporting to
		 * @param cacheMap     a cache to prevent additional lookups to the position
		 * @param minMaxBounds the bounds of the y position the entity can spawn within
		 * @return the portal information to teleport to, or {@code null} if there is none
		 */
		@Nullable
		DimensionTransition determineTeleportLocation(Entity entity, ServerLevel destWorld, Pair<Integer, Integer> minMaxBounds, Long2BooleanArrayMap cacheMap);
	}
}
