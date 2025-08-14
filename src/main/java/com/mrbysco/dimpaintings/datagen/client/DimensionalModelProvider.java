package com.mrbysco.dimpaintings.datagen.client;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

public class DimensionalModelProvider extends ModelProvider {
	public DimensionalModelProvider(PackOutput packOutput) {
		super(packOutput, DimPaintings.MOD_ID);
	}

	@Override
	protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
		PaintingRegistry.ITEMS.getEntries()
				.forEach(item -> {
					itemModels.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
				});
	}
}
