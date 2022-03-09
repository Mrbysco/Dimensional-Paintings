package com.mrbysco.dimpaintings.client.renderer;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.util.stream.Stream;

public class DimensionalPaintingTextureManager extends TextureAtlasHolder {
	public static final ResourceLocation LOCATION_DIMENSIONAL_TEXTURES = new ResourceLocation(DimPaintings.MOD_ID, "textures/atlas/dimensional_paintings.png");
	private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation(DimPaintings.MOD_ID, "back");

	private static DimensionalPaintingTextureManager spriteUploader;

	public DimensionalPaintingTextureManager(TextureManager textureManager) {
		super(textureManager, LOCATION_DIMENSIONAL_TEXTURES, "dimensional_painting");
	}

	protected Stream<ResourceLocation> getResourcesToLoad() {
		return Stream.concat(PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKeys().stream(), Stream.of(BACK_SPRITE_LOCATION));
	}

	public TextureAtlasSprite get(DimensionPaintingType paintingType) {
		return this.getSprite(PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKey(paintingType));
	}

	public TextureAtlasSprite getBackSprite() {
		return this.getSprite(BACK_SPRITE_LOCATION);
	}

	public static void initialize(RegisterClientReloadListenersEvent event) {
		spriteUploader = new DimensionalPaintingTextureManager(Minecraft.getInstance().getTextureManager());
		event.registerReloadListener(spriteUploader);
	}

	public static DimensionalPaintingTextureManager instance() {
		return spriteUploader;
	}
}