package com.mrbysco.dimpaintings.datagen.client;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class DimensionalLanguageProvider extends LanguageProvider {
	public DimensionalLanguageProvider(PackOutput packOutput) {
		super(packOutput, DimPaintings.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("itemGroup.dimpaintings", "Dimensional Paintings");
		addItem(PaintingRegistry.OVERWORLD_PAINTING, "Overworld Painting");
		addItem(PaintingRegistry.NETHER_PAINTING, "Nether Painting");
		addItem(PaintingRegistry.END_PAINTING, "End Painting");
		addItem(PaintingRegistry.CUSTOM_PAINTING, "Custom Painting");

		add("dimpaintings.same_dimension", "Can't teleport to the same dimension");
		add("dimpaintings.cooldown", "Teleportation on cooldown");
		addEntityType(PaintingRegistry.DIMENSIONAL_PAINTING, "Dimensional Painting");
	}
}
