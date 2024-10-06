package com.mrbysco.dimpaintings.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;

public class TeleportHelper {

	public static void teleportToGivenDimension(Entity entityIn, ServerLevel destination) {
		if (destination == null) return;
		if (entityIn.level().dimension() != destination.dimension()) {
			DimensionTransition transition = PaintingTeleportHelper.getPaintingTeleportData(destination, entityIn);
			entityIn.changeDimension(transition);
		} else {
			if (entityIn instanceof Player) {
				((Player) entityIn).displayClientMessage(Component.translatable("dimpaintings.same_dimension").withStyle(ChatFormatting.YELLOW), true);
			}
		}
	}
}