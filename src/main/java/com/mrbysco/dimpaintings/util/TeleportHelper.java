package com.mrbysco.dimpaintings.util;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TeleportHelper {

	public static void teleportToGivenDimension(PlayerEntity player, ResourceLocation dimensionLocation) {
		RegistryKey<World> dimensionKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, dimensionLocation);
		if(player.level.dimension() != dimensionKey) {
			ServerPlayerEntity playerMP = (ServerPlayerEntity) player;

			MinecraftServer server = player.getServer();
			ServerWorld destinationWorld = server != null ? server.getLevel(dimensionKey) : null;
			if(destinationWorld == null) {
				DimPaintings.LOGGER.error("Destination of painting invalid {} isn't known", dimensionLocation);
				return;
			}

			PaintingTeleporter teleporter = new PaintingTeleporter(destinationWorld);
			playerMP.changeDimension(destinationWorld, teleporter);
		} else {
			player.displayClientMessage(new StringTextComponent("Can't teleport to the same dimension").withStyle(TextFormatting.YELLOW), true);
		}
	}
}
