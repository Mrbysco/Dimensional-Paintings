package com.mrbysco.dimpaintings.handler;

import com.mrbysco.dimpaintings.config.DimensionalConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CooldownHandler {
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.phase == TickEvent.Phase.START)
			return;

		if(!event.player.level.isClientSide && event.player.level.getGameTime() % 20 == 0 && DimensionalConfig.COMMON.teleportCooldown.get() > 0) {
			Player player = event.player;
			CompoundTag persistentData = player.getPersistentData();
			if(persistentData.contains("PaintingCooldown")) {
				int currentCooldown = persistentData.getInt("PaintingCooldown") - 1;
				if(currentCooldown == 0) {
					persistentData.remove("PaintingCooldown");
				} else {
					persistentData.putInt("PaintingCooldown", currentCooldown);
				}
			}
		}
	}
}
