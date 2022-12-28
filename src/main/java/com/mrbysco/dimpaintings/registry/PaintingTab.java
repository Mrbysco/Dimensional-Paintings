package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class PaintingTab {
	private static CreativeModeTab MAIN_TAB;

	@SubscribeEvent
	public void registerCreativeTabs(final CreativeModeTabEvent.Register event) {
		MAIN_TAB = event.registerCreativeModeTab(new ResourceLocation(DimPaintings.MOD_ID, "tab"), builder ->
				builder.icon(() -> new ItemStack(PaintingRegistry.OVERWORLD_PAINTING.get()))
						.title(Component.translatable("itemGroup.dimpaintings"))
						.displayItems((features, output, hasPermissions) -> {
							List<ItemStack> stacks = PaintingRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
							output.acceptAll(stacks);
						}));
	}
}
