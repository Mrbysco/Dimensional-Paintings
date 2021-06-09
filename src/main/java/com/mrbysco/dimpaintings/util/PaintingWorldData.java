package com.mrbysco.dimpaintings.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaintingWorldData extends WorldSavedData {
	private static final String DATA_NAME = DimPaintings.MOD_ID + "_world_data";
	public PaintingWorldData() {
		super(DATA_NAME);
	}

	private final ListMultimap<ResourceLocation, PaintingLocation> paintingPositionMap = ArrayListMultimap.create();

	@Override
	public void load(CompoundNBT compound) {
		for(String nbtName : compound.getAllKeys()) {
			ListNBT dimensionNBTList = new ListNBT();
			if(compound.getTagType(nbtName) == 9) {
				INBT nbt = compound.get(nbtName);
				if(nbt instanceof ListNBT) {
					ListNBT listNBT = (ListNBT) nbt;
					if (!listNBT.isEmpty() && listNBT.getElementType() != Constants.NBT.TAG_COMPOUND) {
						return;
					}

					dimensionNBTList = listNBT;
				}
			}
			if(!dimensionNBTList.isEmpty()) {
				List<PaintingLocation> posList = new ArrayList<>();
				for (int i = 0; i < dimensionNBTList.size(); ++i) {
					CompoundNBT tag = dimensionNBTList.getCompound(i);
					if(tag.contains("BlockPos") && tag.contains("Direction")) {
						BlockPos blockPos = BlockPos.of(tag.getLong("BlockPos"));
						int direction2D = tag.getInt("Direction");
						posList.add(new PaintingLocation(blockPos, direction2D));
					}
				}
				paintingPositionMap.putAll(ResourceLocation.tryParse(nbtName), posList);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		for (ResourceLocation dimensionLocation: paintingPositionMap.keySet()) {
			List<PaintingLocation> globalPosList = paintingPositionMap.get(dimensionLocation);

			ListNBT dimensionStorage = new ListNBT();
			for (PaintingLocation paintLoc : globalPosList) {
				CompoundNBT positionTag = new CompoundNBT();
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
		BlockPos roundedPos = new BlockPos((int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
		PaintingLocation position = new PaintingLocation(roundedPos, direction);
		List<PaintingLocation> similarPos = paintingPositionMap.get(dimensionLocation).stream().filter((loc) -> loc.distanceTo(roundedPos) < 2).collect(Collectors.toList());
		if(similarPos.isEmpty()) {
			paintingPositionMap.get(dimensionLocation)
					.add(position);
		}
		setDirty();
	}

	public void removePositionFromDimension(ResourceLocation dimensionLocation, BlockPos pos) {
		BlockPos roundedPos = new BlockPos((int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
		paintingPositionMap.get(dimensionLocation).removeIf((loc) -> loc.distanceTo(roundedPos) < 2);
		setDirty();
	}

	public static PaintingWorldData get(World world) {
		if (!(world instanceof ServerWorld)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerWorld overworld = world.getServer().getLevel(World.OVERWORLD);

		DimensionSavedDataManager storage = overworld.getDataStorage();
		return storage.computeIfAbsent(PaintingWorldData::new, DATA_NAME);
	}
}