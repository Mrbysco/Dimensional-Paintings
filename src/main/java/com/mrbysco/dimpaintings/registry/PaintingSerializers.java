package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PaintingSerializers {
	public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, DimPaintings.MOD_ID);

	public static final Supplier<EntityDataSerializer<Holder<DimensionPaintingType>>> DIMENSION_TYPE = ENTITY_DATA_SERIALIZER.register("dimension", () -> EntityDataSerializer.forValueType(
			ByteBufCodecs.holderRegistry(DimensionPaintingType.REGISTRY_KEY)
	));

}
