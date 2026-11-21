package com.mrbysco.dimpaintings.datagen.server;

import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class DimensionalRecipeProvider extends RecipeProvider {
	public DimensionalRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
		super(recipeOutput, advancementOutput);
	}

	@Override
	protected void buildRecipes() {
		shaped(RecipeCategory.TRANSPORTATION, PaintingRegistry.OVERWORLD_PAINTING.get())
				.pattern("DDD")
				.pattern("DPD")
				.pattern("DDD")
				.define('D', ItemTags.LOGS)
				.define('P', Items.PAINTING)
				.unlockedBy("has_painting", has(Items.PAINTING))
				.unlockedBy("has_logs", has(ItemTags.LOGS))
				.save(output);

		shaped(RecipeCategory.TRANSPORTATION, PaintingRegistry.NETHER_PAINTING.get())
				.pattern("OOO")
				.pattern("OPO")
				.pattern("OOO")
				.define('O', Tags.BlockItems.OBSIDIANS.item())
				.define('P', Items.PAINTING)
				.unlockedBy("has_painting", has(Items.PAINTING))
				.unlockedBy("has_obsidian", has(Tags.BlockItems.OBSIDIANS.item()))
				.save(output);

		shaped(RecipeCategory.TRANSPORTATION, PaintingRegistry.END_PAINTING.get())
				.pattern("EEE")
				.pattern("EPE")
				.pattern("EEE")
				.define('E', Items.ENDER_EYE)
				.define('P', Items.PAINTING)
				.unlockedBy("has_painting", has(Items.PAINTING))
				.unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
				.save(output);

	}
}
