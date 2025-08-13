package com.mrbysco.dimpaintings.datagen.client;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class DimensionalItemModelProvider extends ItemModelProvider {
	public DimensionalItemModelProvider(PackOutput packOutput, ExistingFileHelper helper) {
		super(packOutput, DimPaintings.MOD_ID, helper);
	}

	@Override
	protected void registerModels() {
		PaintingRegistry.ITEMS.getEntries()
				.forEach(item -> {
					String path = Objects.requireNonNull(item.getId()).getPath();
					singleTexture(path, modLoc("item/base_painting"),
							"layer0", modLoc("item/" + path));
				});
	}
}
