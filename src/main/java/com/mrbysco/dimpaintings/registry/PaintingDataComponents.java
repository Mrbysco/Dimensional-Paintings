package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PaintingDataComponents {
	public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DimPaintings.MOD_ID);

	public static final Supplier<DataComponentType<Identifier>> DIMENSION_TYPE = DATA_COMPONENT_TYPES.register("dimension_type", () ->
			DataComponentType.<Identifier>builder()
					.persistent(Identifier.CODEC)
					.networkSynchronized(Identifier.STREAM_CODEC)
					.build());
}
