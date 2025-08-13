package com.mrbysco.dimpaintings.datagen.server;

import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class DimensionalRecipeProvider extends RecipeProvider {
	public DimensionalRecipeProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void buildRecipes(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, PaintingRegistry.OVERWORLD_PAINTING.get())
				.pattern("DDD")
				.pattern("DPD")
				.pattern("DDD")
				.define('D', ItemTags.LOGS)
				.define('P', Items.PAINTING)
				.unlockedBy("has_painting", has(Items.PAINTING))
				.unlockedBy("has_logs", has(ItemTags.LOGS))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, PaintingRegistry.NETHER_PAINTING.get())
				.pattern("OOO")
				.pattern("OPO")
				.pattern("OOO")
				.define('O', Tags.Items.OBSIDIANS)
				.define('P', Items.PAINTING)
				.unlockedBy("has_painting", has(Items.PAINTING))
				.unlockedBy("has_obsidian", has(Tags.Items.OBSIDIANS))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, PaintingRegistry.END_PAINTING.get())
				.pattern("EEE")
				.pattern("EPE")
				.pattern("EEE")
				.define('E', Items.ENDER_EYE)
				.define('P', Items.PAINTING)
				.unlockedBy("has_painting", has(Items.PAINTING))
				.unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
				.save(consumer);

	}
}
