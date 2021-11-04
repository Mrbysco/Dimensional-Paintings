package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class PaintingTab {
	public static final CreativeModeTab MAIN_TAB = new CreativeModeTab(DimPaintings.MOD_ID) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(PaintingRegistry.OVERWORLD_PAINTING.get());
		}
	};
}
