package com.mrbysco.dimpaintings.datagen;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PaintingDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if (event.includeServer()) {
			generator.addProvider(true, new PaintingRecipeProvider(packOutput, lookupProvider));
		}
		if (event.includeClient()) {
			generator.addProvider(true, new PaintingLanguageProvider(packOutput));
			generator.addProvider(true, new PaintingItemModelProvider(packOutput, helper));
		}
	}

	private static class PaintingRecipeProvider extends RecipeProvider {
		public PaintingRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
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
					.define('O', Tags.Items.OBSIDIAN)
					.define('P', Items.PAINTING)
					.unlockedBy("has_painting", has(Items.PAINTING))
					.unlockedBy("has_obsidian", has(Tags.Items.OBSIDIAN))
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

	private static class PaintingLanguageProvider extends LanguageProvider {
		public PaintingLanguageProvider(PackOutput packOutput) {
			super(packOutput, DimPaintings.MOD_ID, "en_us");
		}

		@Override
		protected void addTranslations() {
			add("itemGroup.dimpaintings", "Dimensional Paintings");
			addItem(PaintingRegistry.OVERWORLD_PAINTING, "Overworld Painting");
			addItem(PaintingRegistry.NETHER_PAINTING, "Nether Painting");
			addItem(PaintingRegistry.END_PAINTING, "End Painting");

			add("dimpaintings.same_dimension", "Can't teleport to the same dimension");
			add("dimpaintings.cooldown", "Teleportation on cooldown");
			addEntityType(PaintingRegistry.DIMENSIONAL_PAINTING, "Dimensional Painting");
		}
	}

	private static class PaintingItemModelProvider extends ItemModelProvider {
		public PaintingItemModelProvider(PackOutput packOutput, ExistingFileHelper helper) {
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
}
