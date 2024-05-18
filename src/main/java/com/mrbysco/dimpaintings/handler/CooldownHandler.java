package com.mrbysco.dimpaintings.handler;

import com.mrbysco.dimpaintings.config.DimensionalConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent.PortalSpawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class CooldownHandler {
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Level level = player.level();
		if (!level.isClientSide && level.getGameTime() % 20 == 0 && DimensionalConfig.COMMON.teleportCooldown.get() > 0) {
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
