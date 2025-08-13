package com.mrbysco.dimpaintings.datagen.client;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.Nullable;

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

		addConfig("General", "General", "General Settings");
		addConfig("overworldToBed", "Overworld to Bed", "Dictates if the overworld painting will teleport you back to your spawnpoint / bed (Similar to teleporting from the End to the Overworld)");
		addConfig("netherMaxY", "Nether Max Y", "Dictates the max Y at which the Nether Painting will place you in the Nether\n" +
				"[For a vanilla nether it's recommended to keep the value between 10 and 120\n" +
				"[51 = Spawn in a bubble at Y 50 if nothing] (Default: 120)");
		addConfig("teleportCooldown", "Teleport Cooldown", "Amount of seconds between being able to teleport (Default: 4)");
		addConfig("disableNetherPortal", "Disable Nether Portal", "Disable nether portal creation (Default: false)");
	}

	/**
	 * Add the translation for a config entry
	 *
	 * @param path        The path of the config entry
	 * @param name        The name of the config entry
	 * @param description The description of the config entry (optional in case of targeting "title" or similar entries that have no tooltip)
	 */
	private void addConfig(String path, String name, @Nullable String description) {
		this.add(DimPaintings.MOD_ID + ".configuration." + path, name);
		if (description != null && !description.isEmpty())
			this.add(DimPaintings.MOD_ID + ".configuration." + path + ".tooltip", description);
	}
}
