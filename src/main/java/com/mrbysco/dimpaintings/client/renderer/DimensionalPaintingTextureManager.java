package com.mrbysco.dimpaintings.client.renderer;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public class DimensionalPaintingTextureManager extends TextureAtlasHolder {
	public static final ResourceLocation LOCATION_DIMENSIONAL_TEXTURES =
			DimPaintings.modLoc("textures/atlas/dimensional_paintings.png");
	public static final ResourceLocation INFO_LOCATION =
			DimPaintings.modLoc("dimensional_paintings");
	private static final ResourceLocation BACK_SPRITE_LOCATION =
			DimPaintings.modLoc("back");

	private static DimensionalPaintingTextureManager spriteUploader;

	public DimensionalPaintingTextureManager(TextureManager textureManager) {
		super(textureManager, LOCATION_DIMENSIONAL_TEXTURES, INFO_LOCATION);
	}

	public TextureAtlasSprite get(DimensionPaintingType paintingVariant) {
		return this.getSprite(paintingVariant.assetId());
	}

	public TextureAtlasSprite getBackSprite() {
		return this.getSprite(BACK_SPRITE_LOCATION);
	}

	public static void initialize(AddClientReloadListenersEvent event) {
		spriteUploader = new DimensionalPaintingTextureManager(Minecraft.getInstance().getTextureManager());
		event.addListener(DimPaintings.modLoc("sprite_uploader"), spriteUploader);
	}

	public static DimensionalPaintingTextureManager instance() {
		return spriteUploader;
	}
}