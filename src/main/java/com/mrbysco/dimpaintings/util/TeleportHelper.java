package com.mrbysco.dimpaintings.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;

public class TeleportHelper {

	public static void teleportToGivenDimension(Entity entityIn, ServerLevel destination) {
		if (destination == null) return;
		if (entityIn.level().dimension() != destination.dimension()) {
			TeleportTransition transition = PaintingTeleportHelper.getPaintingTeleportData(destination, entityIn);
			entityIn.teleport(transition);
		} else {
			if (entityIn instanceof Player) {
				((Player) entityIn).sendOverlayMessage(Component.translatable("dimpaintings.same_dimension").withStyle(ChatFormatting.YELLOW));
			}
		}
	}
}