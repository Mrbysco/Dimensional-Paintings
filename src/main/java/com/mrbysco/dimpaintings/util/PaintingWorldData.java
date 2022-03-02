package com.mrbysco.dimpaintings.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaintingWorldData extends SavedData {
	private static final String DATA_NAME = DimPaintings.MOD_ID + "_world_data";

	public PaintingWorldData(ListMultimap<ResourceLocation, PaintingLocation> paintingMap) {
		this.paintingPositionMap.clear();
		if (!paintingMap.isEmpty()) {
			this.paintingPositionMap.putAll(paintingMap);
		}
	}

	public PaintingWorldData() {
		this(ArrayListMultimap.create());
	}

	private final ListMultimap<ResourceLocation, PaintingLocation> paintingPositionMap = ArrayListMultimap.create();

	public static PaintingWorldData load(CompoundTag tag) {
		ListMultimap<ResourceLocation, PaintingLocation> paintingMap = ArrayListMultimap.create();
		for (String nbtName : tag.getAllKeys()) {
			ListTag dimensionNBTList = new ListTag();
			if (tag.getTagType(nbtName) == 9) {
				Tag nbt = tag.get(nbtName);
				if (nbt instanceof ListTag listNBT) {
					if (!listNBT.isEmpty() && listNBT.getElementType() != CompoundTag.TAG_COMPOUND) {
						continue;
					}

					dimensionNBTList = listNBT;
				}
			}
			if (!dimensionNBTList.isEmpty()) {
				List<PaintingLocation> posList = new ArrayList<>();
				for (int i = 0; i < dimensionNBTList.size(); ++i) {
					CompoundTag dimTag = dimensionNBTList.getCompound(i);
					if (dimTag.contains("BlockPos") && dimTag.contains("Direction")) {
						BlockPos blockPos = BlockPos.of(dimTag.getLong("BlockPos"));
						int direction2D = dimTag.getInt("Direction");
						posList.add(new PaintingLocation(blockPos, direction2D));
					}
				}
				paintingMap.putAll(ResourceLocation.tryParse(nbtName), posList);
			}
		}
		return new PaintingWorldData(paintingMap);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		for (ResourceLocation dimensionLocation : paintingPositionMap.keySet()) {
			List<PaintingLocation> globalPosList = paintingPositionMap.get(dimensionLocation);

			ListTag dimensionStorage = new ListTag();
			for (PaintingLocation paintLoc : globalPosList) {
				CompoundTag positionTag = new CompoundTag();
				positionTag.putLong("BlockPos", paintLoc.pos.asLong());
				positionTag.putInt("Direction", paintLoc.direction2D);
				dimensionStorage.add(positionTag);
			}
			compound.put(dimensionLocation.toString(), dimensionStorage);
		}
		return compound;
	}

	public List<PaintingLocation> getDimensionPositions(ResourceLocation dimensionLocation) {
		return paintingPositionMap.get(dimensionLocation);
	}

	public void addPositionToDimension(ResourceLocation dimensionLocation, BlockPos pos, Direction direction) {
		BlockPos roundedPos = new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
		PaintingLocation position = new PaintingLocation(roundedPos, direction);
		List<PaintingLocation> similarPos = paintingPositionMap.get(dimensionLocation).stream().filter((loc) -> loc.distanceTo(roundedPos) < 2).collect(Collectors.toList());
		if (similarPos.isEmpty()) {
			paintingPositionMap.get(dimensionLocation)
					.add(position);
		}
		setDirty();
	}

	public void removePositionFromDimension(ResourceLocation dimensionLocation, BlockPos pos) {
		BlockPos roundedPos = new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
		paintingPositionMap.get(dimensionLocation).removeIf((loc) -> loc.distanceTo(roundedPos) < 2);
		setDirty();
	}

	public static PaintingWorldData get(Level world) {
		if (!(world instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);

		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(PaintingWorldData::load, PaintingWorldData::new, DATA_NAME);
	}
}