package com.mrbysco.dimpaintings.datagen.provider;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class PaintingProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Logger LOGGER = LogManager.getLogger();
	private final PackOutput output;
	private final String modid;
	private final Map<String, JsonElement> toSerializePainting = new HashMap<>();

	public PaintingProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modid) {
		this.output = packOutput;
		this.modid = modid;
	}

	public CompletableFuture<?> run(CachedOutput cache) {
		start();

		ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

		Path biomeFolderPath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(this.modid).resolve(DimPaintings.MOD_ID).resolve("dimension_painting");
		toSerializePainting.forEach(LamdbaExceptionUtils.rethrowBiConsumer((name, json) -> {
			Path modifierPath = biomeFolderPath.resolve(name + ".json");
			futuresBuilder.add(DataProvider.saveStable(cache, json, modifierPath));
		}));

		return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
	}

	protected abstract void start();

	public <T extends DimensionPaintingType> void add(String paintingID, T instance) {
		JsonElement json = DimensionPaintingType.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow(false, s -> {
		});
		this.toSerializePainting.put(paintingID, json);
	}

	@Override
	public String getName() {
		return "Dimensional Paintings: " + modid;
	}
}
