package com.mrbysco.dimpaintings.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PaintingTab {
	public static final ItemGroup MAIN_TAB = new ItemGroup("paintings") {
		@OnlyIn(Dist.CLIENT)
		public ItemStack makeIcon() {
			return new ItemStack(PaintingRegistry.OVERWORLD_PAINTING.get());
		}
	};
}
