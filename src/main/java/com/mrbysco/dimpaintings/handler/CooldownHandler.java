package com.mrbysco.dimpaintings.handler;

import com.mrbysco.dimpaintings.config.DimensionalConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.level.BlockEvent.PortalSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CooldownHandler {
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START)
			return;

		Level level = event.player.level();
		if (!level.isClientSide && level.getGameTime() % 20 == 0 && DimensionalConfig.COMMON.teleportCooldown.get() > 0) {
			Player player = event.player;
			CompoundTag persistentData = player.getPersistentData();
			if (persistentData.contains("PaintingCooldown")) {
				int currentCooldown = persistentData.getInt("PaintingCooldown") - 1;
				if (currentCooldown == 0) {
					persistentData.remove("PaintingCooldown");
				} else {
					persistentData.putInt("PaintingCooldown", currentCooldown);
				}
			}
		}
	}

	@SubscribeEvent
	public void onNetherPortal(PortalSpawnEvent event) {
		if (DimensionalConfig.COMMON.disableNetherPortal.get()) {
			event.setCanceled(true);
		}
	}
}
