package com.mrbysco.dimpaintings.datagen.server;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.datagen.provider.PaintingProvider;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class DimensionalPaintingProvider extends PaintingProvider {

	public DimensionalPaintingProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider) {
		super(packOutput, lookupProvider, DimPaintings.MOD_ID);
	}

	@Override
	protected void start() {
		addPainting("overworld", Identifier.withDefaultNamespace("overworld"), 4, 2, "overworld");
		addPainting("nether", Identifier.withDefaultNamespace("the_nether"), 4, 2, "nether");
		addPainting("end", Identifier.withDefaultNamespace("the_end"), 4, 2, "end");
	}

	private void addPainting(String name, Identifier dimension, int width, int height, String texture) {
		add(name, new DimensionPaintingType(dimension, width, height,
				DimPaintings.modLoc(texture)));
	}
}
