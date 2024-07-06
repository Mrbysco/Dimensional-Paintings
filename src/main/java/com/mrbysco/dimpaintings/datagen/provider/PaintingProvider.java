package com.mrbysco.dimpaintings.datagen.provider;

import com.google.common.collect.ImmutableList;
import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class PaintingProvider implements DataProvider {
	private final PackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registries;
	private final String modid;
	private final Map<String, WithConditions<DimensionPaintingType>> toSerialize = new HashMap<>();

	public PaintingProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, String modid) {
		this.output = packOutput;
		this.registries = registries;
		this.modid = modid;
	}

	@Override
	public final CompletableFuture<?> run(CachedOutput cache) {
		return this.registries.thenCompose(registries -> this.run(cache, registries));
	}

	protected CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider registries) {
		start();

		ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

		Path biomeFolderPath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(this.modid).resolve(DimPaintings.MOD_ID).resolve("dimension_painting");
		for (var entry : toSerialize.entrySet()) {
			var name = entry.getKey();
			var lootModifier = entry.getValue();
			Path modifierPath = biomeFolderPath.resolve(name + ".json");
			futuresBuilder.add(DataProvider.saveStable(cache, registries, DimensionPaintingType.CONDITIONAL_CODEC, Optional.of(lootModifier), modifierPath));
		}

		return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
	}

	protected abstract void start();

	public <T extends DimensionPaintingType> void add(String modifier, T instance, List<ICondition> conditions) {
		this.toSerialize.put(modifier, new WithConditions<>(conditions, instance));
	}

	public <T extends DimensionPaintingType> void add(String modifier, T instance, ICondition... conditions) {
		add(modifier, instance, Arrays.asList(conditions));
	}

	@Override
	public String getName() {
		return "Dimensional Paintings: " + modid;
	}
}