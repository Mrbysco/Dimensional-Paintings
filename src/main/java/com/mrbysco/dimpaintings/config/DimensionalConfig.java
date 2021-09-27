package com.mrbysco.dimpaintings.config;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class DimensionalConfig {
	public static class Common {
		public final BooleanValue overworldToBed;
		public final IntValue netherMaxY;
		public final IntValue teleportCooldown;

		Common(ForgeConfigSpec.Builder builder) {
			builder.comment("General settings")
					.push("General");

			overworldToBed = builder
					.comment("Dictates if the overworld painting will teleport you back to your spawnpoint / bed (Similar to teleporting from the End to the Overworld)")
					.define("overworldToBed", false);

			netherMaxY = builder
					.comment("Dictates the max Y at which the Nether Painting will place you in the Nether [51 = Spawn in a bubble at Y 50 if nothing] (Default: 120)")
					.defineInRange("netherMaxY", 120, 10, 120);

			teleportCooldown = builder
					.comment("Amount of seconds between being able to teleport (Default: 4)")
					.defineInRange("teleportCooldown", 4, 0, Integer.MAX_VALUE);

			builder.pop();
		}
	}

	public static final ForgeConfigSpec serverSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		serverSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		DimPaintings.LOGGER.debug("Loaded Dimensional Painting's config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		DimPaintings.LOGGER.debug("Dimensional Painting's config just got changed on the file system!");
	}
}
