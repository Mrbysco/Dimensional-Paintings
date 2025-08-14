package com.mrbysco.dimpaintings.util;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaintingWorldData extends SavedData {
	private static final String DATA_NAME = DimPaintings.MOD_ID + "_world_data";


	public static final Codec<PaintingWorldData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
					Codec.unboundedMap(
									Level.RESOURCE_KEY_CODEC,
									PaintingLocation.CODEC.listOf())
							.fieldOf("paintingPositions").forGetter(data -> data.paintingPositionMap))
			.apply(inst, PaintingWorldData::new));

	private final Map<ResourceKey<Level>, List<PaintingLocation>> paintingPositionMap;

	public PaintingWorldData() {
		this(Maps.newHashMap());
	}

	public PaintingWorldData(Map<ResourceKey<Level>, List<PaintingLocation>> infoMap) {
		this.paintingPositionMap = Maps.newHashMap(infoMap);
	}

	public List<PaintingLocation> getDimensionPositions(ResourceKey<Level> dimensionLocation) {
		return paintingPositionMap.getOrDefault(dimensionLocation, new ArrayList<>());
	}

	public void addPositionToDimension(ResourceKey<Level> dimensionLocation, BlockPos pos, Direction direction) {
		BlockPos roundedPos = new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
		PaintingLocation position = new PaintingLocation(roundedPos, direction);
		List<PaintingLocation> similarPos = paintingPositionMap.getOrDefault(dimensionLocation, new ArrayList<>()).stream()
				.filter((loc) -> loc.distanceTo(roundedPos) < 2).collect(Collectors.toList());
		if (similarPos.isEmpty()) {
			List<PaintingLocation> positions = new ArrayList<>();
			positions.add(position);
			paintingPositionMap.put(dimensionLocation, positions);
		}
		setDirty();
	}

	public void removePositionFromDimension(ResourceKey<Level> dimensionLocation, BlockPos pos) {
		BlockPos roundedPos = new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
		List<PaintingLocation> paintings = new ArrayList<>(paintingPositionMap.getOrDefault(dimensionLocation, new ArrayList<>()));
		paintings.removeIf((loc) -> loc.distanceTo(roundedPos) < 2);
		paintingPositionMap.put(dimensionLocation, paintings);
		setDirty();
	}

	public static SavedDataType<PaintingWorldData> type() {
		return new SavedDataType<>(DATA_NAME, PaintingWorldData::new, CODEC, null);
	}

	public static PaintingWorldData get(Level world) {
		if (!(world instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);

		assert overworld != null;
		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(type());
	}
}