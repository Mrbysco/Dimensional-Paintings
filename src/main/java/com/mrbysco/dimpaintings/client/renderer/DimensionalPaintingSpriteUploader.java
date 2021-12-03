package com.mrbysco.dimpaintings.client.renderer;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.stream.Stream;

public class DimensionalPaintingSpriteUploader extends TextureAtlasHolder {
	public static final ResourceLocation LOCATION_DIMENSIONAL_TEXTURES = new ResourceLocation(DimPaintings.MOD_ID, "textures/atlas/dimensional_paintings.png");
	private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation(DimPaintings.MOD_ID, "back");

	private static DimensionalPaintingSpriteUploader spriteUploader;

	public DimensionalPaintingSpriteUploader(TextureManager textureManager) {
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

	public static void initialize() {
		spriteUploader = new DimensionalPaintingSpriteUploader(Minecraft.getInstance().getTextureManager());
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (resourceManager instanceof ReloadableResourceManager) {
			((ReloadableResourceManager) resourceManager).registerReloadListener(spriteUploader);
		}
	}

	public static DimensionalPaintingSpriteUploader instance() {
		return spriteUploader;
	}
}