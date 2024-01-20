package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.item.DimensionalPaintingItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, DimPaintings.MOD_ID);
	public static final DeferredRegister<DimensionPaintingType> DIM_PAINTINGS = DeferredRegister.create(DimensionPaintingType.REGISTRY_KEY, DimPaintings.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DimPaintings.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DimPaintings.MOD_ID);

	public static final Supplier<DimensionPaintingType> OVERWORLD = DIM_PAINTINGS.register("overworld", () -> new DimensionPaintingType(new ResourceLocation("overworld"), 64, 32));
	public static final Supplier<DimensionPaintingType> NETHER = DIM_PAINTINGS.register("nether", () -> new DimensionPaintingType(new ResourceLocation("the_nether"), 64, 32));
	public static final Supplier<DimensionPaintingType> END = DIM_PAINTINGS.register("end", () -> new DimensionPaintingType(new ResourceLocation("the_end"), 64, 32));

	public static final DeferredItem<DimensionalPaintingItem> OVERWORLD_PAINTING = ITEMS.register("overworld_painting", () -> new DimensionalPaintingItem(new Item.Properties(), OVERWORLD));
	public static final DeferredItem<DimensionalPaintingItem> NETHER_PAINTING = ITEMS.register("nether_painting", () -> new DimensionalPaintingItem(new Item.Properties(), NETHER));
	public static final DeferredItem<DimensionalPaintingItem> END_PAINTING = ITEMS.register("end_painting", () -> new DimensionalPaintingItem(new Item.Properties(), END));

	public static final Supplier<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.icon(() -> new ItemStack(PaintingRegistry.OVERWORLD_PAINTING.get()))
			.title(Component.translatable("itemGroup.dimpaintings"))
			.displayItems((displayParameters, output) -> {
				List<ItemStack> stacks = PaintingRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
				output.acceptAll(stacks);
			}).build());

	public static final Supplier<EntityType<DimensionalPainting>> DIMENSIONAL_PAINTING = ENTITY_TYPES.register("dimensional_painting", () ->
			register("dimensional_painting", EntityType.Builder.<DimensionalPainting>of(DimensionalPainting::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)));

	public static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> builder) {
		return builder.build(id);
	}
}