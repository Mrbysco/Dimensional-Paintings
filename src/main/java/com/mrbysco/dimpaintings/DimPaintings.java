package com.mrbysco.dimpaintings;

import com.mojang.logging.LogUtils;
import com.mrbysco.dimpaintings.client.ClientHandler;
import com.mrbysco.dimpaintings.config.DimensionalConfig;
import com.mrbysco.dimpaintings.handler.CooldownHandler;
import com.mrbysco.dimpaintings.registry.PaintingDataComponents;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import com.mrbysco.dimpaintings.registry.PaintingSerializers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(DimPaintings.MOD_ID)
public class DimPaintings {
	public static final String MOD_ID = "dimpaintings";
	public static final Logger LOGGER = LogUtils.getLogger();

	public DimPaintings(IEventBus eventBus, Dist dist, ModContainer container) {
		container.registerConfig(Type.COMMON, DimensionalConfig.commonSpec);
		eventBus.register(DimensionalConfig.class);

		PaintingSerializers.ENTITY_DATA_SERIALIZER.register(eventBus);
		PaintingRegistry.ENTITY_TYPES.register(eventBus);
		PaintingRegistry.ITEMS.register(eventBus);
		PaintingRegistry.CREATIVE_MODE_TABS.register(eventBus);
		PaintingDataComponents.DATA_COMPONENT_TYPES.register(eventBus);

		NeoForge.EVENT_BUS.register(new CooldownHandler());

		if (dist.isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
			eventBus.addListener(ClientHandler::onRegisterReloadListeners);
			eventBus.addListener(ClientHandler::registerEntityRenders);
		}
	}
}