package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.item.CustomDimensionalPaintingItem;
import com.mrbysco.dimpaintings.item.DimensionalPaintingItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class PaintingRegistry {
	public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(DimPaintings.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DimPaintings.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DimPaintings.MOD_ID);

	public static final DeferredItem<Item> OVERWORLD_PAINTING = ITEMS.registerItem("overworld_painting", (properties) -> new DimensionalPaintingItem(properties, DimPaintings.modLoc("overworld")));
	public static final DeferredItem<Item> NETHER_PAINTING = ITEMS.registerItem("nether_painting", (properties) -> new DimensionalPaintingItem(properties, DimPaintings.modLoc("nether")));
	public static final DeferredItem<Item> END_PAINTING = ITEMS.registerItem("end_painting", (properties) -> new DimensionalPaintingItem(properties, DimPaintings.modLoc("end")));
	public static final DeferredItem<Item> CUSTOM_PAINTING = ITEMS.registerItem("custom_painting", CustomDimensionalPaintingItem::new);

	public static final Supplier<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.icon(() -> new ItemStack(PaintingRegistry.OVERWORLD_PAINTING.get()))
			.title(Component.translatable("itemGroup.dimpaintings"))
			.displayItems((displayParameters, output) -> {
				List<ItemStack> stacks = PaintingRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
				output.acceptAll(stacks);
			}).build());

	public static final Supplier<EntityType<DimensionalPainting>> DIMENSIONAL_PAINTING = ENTITY_TYPES.registerEntityType("dimensional_painting",
			DimensionalPainting::new,
			MobCategory.MISC,
			builder -> builder
					.sized(0.5F, 0.5F)
					.clientTrackingRange(10)
					.updateInterval(Integer.MAX_VALUE)
	);
}