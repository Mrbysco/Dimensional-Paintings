package com.mrbysco.dimpaintings.registry;

import com.mrbysco.dimpaintings.DimPaintings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PaintingSerializers {
	public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, DimPaintings.MOD_ID);

	public static final Supplier<EntityDataSerializer<DimensionPaintingType>> DIMENSION_TYPE = ENTITY_DATA_SERIALIZER.register("dimension", () -> new EntityDataSerializer<DimensionPaintingType>() {
		public void write(FriendlyByteBuf friendlyByteBuf, DimensionPaintingType paintingType) {
			friendlyByteBuf.writeUtf(PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKey(paintingType).toString());
		}

		public DimensionPaintingType read(FriendlyByteBuf friendlyByteBuf) {
			return PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.get(ResourceLocation.tryParse(friendlyByteBuf.readUtf()));
		}

		public DimensionPaintingType copy(DimensionPaintingType paintingType) {
			return paintingType;
		}
	});

}
