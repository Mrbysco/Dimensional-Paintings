package com.mrbysco.dimpaintings.util;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TeleportHelper {

	public static void teleportToGivenDimension(Entity entityIn, ResourceLocation dimensionLocation) {
		ResourceKey<Level> dimensionKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimensionLocation);
		if(entityIn.level.dimension() != dimensionKey) {
			MinecraftServer server = entityIn.getServer();
			ServerLevel destinationWorld = server != null ? server.getLevel(dimensionKey) : null;

			if(destinationWorld == null) {
				DimPaintings.LOGGER.error("Destination of painting invalid {} isn't known", dimensionLocation);
				return;
			}

			PaintingTeleporter teleporter = new PaintingTeleporter(destinationWorld);

			if(entityIn instanceof Player) {
				ServerPlayer playerMP = (ServerPlayer) entityIn;
				playerMP.changeDimension(destinationWorld, teleporter);
			} else {
				entityIn.changeDimension(destinationWorld, teleporter);
			}
		} else {
			if(entityIn instanceof Player) {
				((Player)entityIn).displayClientMessage(new TextComponent("Can't teleport to the same dimension").withStyle(ChatFormatting.YELLOW), true);
			}
		}
	}
}