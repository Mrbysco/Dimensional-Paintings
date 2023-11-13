package com.mrbysco.dimpaintings.config;

import com.mrbysco.dimpaintings.DimPaintings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

public class DimensionalConfig {
	public static class Common {
		public final BooleanValue overworldToBed;
		public final IntValue netherMaxY;
		public final IntValue teleportCooldown;
		public final BooleanValue disableNetherPortal;

		Common(ModConfigSpec.Builder builder) {
			builder.comment("General settings")
					.push("General");

			overworldToBed = builder
					.comment("Dictates if the overworld painting will teleport you back to your spawnpoint / bed (Similar to teleporting from the End to the Overworld)")
					.define("overworldToBed", false);

			netherMaxY = builder
					.comment("Dictates the max Y at which the Nether Painting will place you in the Nether",
							"[For a vanilla nether it's recommended to keep the value between 10 and 120",
							"[51 = Spawn in a bubble at Y 50 if nothing] (Default: 120)")
					.defineInRange("netherMaxY", 120, -2048, 2048);

			teleportCooldown = builder
					.comment("Amount of seconds between being able to teleport (Default: 4)")
					.defineInRange("teleportCooldown", 4, 0, Integer.MAX_VALUE);

			disableNetherPortal = builder
					.comment("Disable nether portal creation (Default: false)")
					.define("disableNetherPortal", false);

			builder.pop();
		}
	}

	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		DimPaintings.LOGGER.debug("Loaded Dimensional Painting's config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		DimPaintings.LOGGER.debug("Dimensional Painting's config just got changed on the file system!");
	}
}
