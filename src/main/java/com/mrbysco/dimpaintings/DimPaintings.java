package com.mrbysco.dimpaintings;

import com.mojang.logging.LogUtils;
import com.mrbysco.dimpaintings.client.ClientHandler;
import com.mrbysco.dimpaintings.config.DimensionalConfig;
import com.mrbysco.dimpaintings.handler.CooldownHandler;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DimPaintings.MOD_ID)
public class DimPaintings {
	public static final String MOD_ID = "dimpaintings";
	public static final Logger LOGGER = LogUtils.getLogger();

	public DimPaintings() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(Type.COMMON, DimensionalConfig.commonSpec);
		eventBus.register(DimensionalConfig.class);

		PaintingRegistry.ENTITIES.register(eventBus);
		PaintingRegistry.DIM_PAINTINGS.register(eventBus);
		PaintingRegistry.ITEMS.register(eventBus);

		MinecraftForge.EVENT_BUS.register(new CooldownHandler());

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			eventBus.addListener(ClientHandler::onRegisterReloadListeners);
			eventBus.addListener(ClientHandler::registerEntityRenders);
		});
	}
}