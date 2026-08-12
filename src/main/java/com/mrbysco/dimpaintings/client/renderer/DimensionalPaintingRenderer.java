package com.mrbysco.dimpaintings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.client.ClientHandler;
import com.mrbysco.dimpaintings.client.state.DimensionalPaintingRenderState;
import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class DimensionalPaintingRenderer extends EntityRenderer<DimensionalPainting, DimensionalPaintingRenderState> {
	private static final Identifier BACK_SPRITE_LOCATION = DimPaintings.modLoc("back");

	private final TextureAtlas dimensionalPaintingsAtlas;

	public DimensionalPaintingRenderer(Context context) {
		super(context);
		this.dimensionalPaintingsAtlas = context.getAtlas(ClientHandler.DIMENSIONAL_PAINTINGS);
	}

	public void submit(DimensionalPaintingRenderState renderState, PoseStack poseStack,
	                   SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180 - renderState.direction.get2DDataValue() * 90));
		DimensionPaintingType dimensionType = renderState.dimensionType;
		TextureAtlasSprite sprite = this.dimensionalPaintingsAtlas.getSprite(dimensionType.assetId());
		TextureAtlasSprite backSprite = this.dimensionalPaintingsAtlas.getSprite(BACK_SPRITE_LOCATION);
		this.renderDimensionalPainting(
				poseStack,
				nodeCollector,
				RenderTypes.entitySolidZOffsetForward(backSprite.atlasLocation()),
				renderState.lightCoordsPerBlock,
				dimensionType.width(),
				dimensionType.height(),
				sprite,
				backSprite
		);
		poseStack.popPose();
		super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
	}

	@Override
	public DimensionalPaintingRenderState createRenderState() {
		return new DimensionalPaintingRenderState();
	}

	@Override
	public void extractRenderState(DimensionalPainting painting, DimensionalPaintingRenderState reusedState, float partialTick) {
		super.extractRenderState(painting, reusedState, partialTick);
		Direction direction = painting.getDirection();
		reusedState.dimensionType = painting.getDimensionType().value();
		reusedState.direction = direction;
		int width = reusedState.dimensionType.width();
		int height = reusedState.dimensionType.height();
		if (reusedState.lightCoordsPerBlock.length != width * height) {
			reusedState.lightCoordsPerBlock = new int[width * height];
		}

		float f = (float) (-width) / 2.0F;
		float f1 = (float) (-height) / 2.0F;
		Level level = painting.level();

		for (int k = 0; k < height; k++) {
			for (int l = 0; l < width; l++) {
				float f2 = (float) l + f + 0.5F;
				float f3 = (float) k + f1 + 0.5F;
				int i1 = painting.getBlockX();
				int j1 = Mth.floor(painting.getY() + (double) f3);
				int k1 = painting.getBlockZ();
				switch (direction) {
					case NORTH:
						i1 = Mth.floor(painting.getX() + (double) f2);
						break;
					case WEST:
						k1 = Mth.floor(painting.getZ() - (double) f2);
						break;
					case SOUTH:
						i1 = Mth.floor(painting.getX() - (double) f2);
						break;
					case EAST:
						k1 = Mth.floor(painting.getZ() + (double) f2);
				}

				reusedState.lightCoordsPerBlock[l + k * width] = LightCoordsUtil.getLightCoords(level, new BlockPos(i1, j1, k1));
			}
		}
	}

	private void renderDimensionalPainting(
			PoseStack poseStack,
			SubmitNodeCollector nodeCollector,
			RenderType renderType,
			int[] lightCoordsPerBlock,
			int width,
			int height,
			TextureAtlasSprite frontSprite,
			TextureAtlasSprite backSprite
	) {
		nodeCollector.submitCustomGeometry(poseStack, renderType, (p_435197_, p_434479_) -> {
			float f = -width / 2.0F;
			float f1 = -height / 2.0F;
			float f2 = 0.03125F;
			float f3 = backSprite.getU0();
			float f4 = backSprite.getU1();
			float f5 = backSprite.getV0();
			float f6 = backSprite.getV1();
			float f7 = backSprite.getU0();
			float f8 = backSprite.getU1();
			float f9 = backSprite.getV0();
			float f10 = backSprite.getV(0.0625F);
			float f11 = backSprite.getU0();
			float f12 = backSprite.getU(0.0625F);
			float f13 = backSprite.getV0();
			float f14 = backSprite.getV1();
			double d0 = 1.0 / width;
			double d1 = 1.0 / height;

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					float f15 = f + (i + 1);
					float f16 = f + i;
					float f17 = f1 + (j + 1);
					float f18 = f1 + j;
					int k = lightCoordsPerBlock[i + j * width];
					float f19 = frontSprite.getU((float) (d0 * (width - i)));
					float f20 = frontSprite.getU((float) (d0 * (width - (i + 1))));
					float f21 = frontSprite.getV((float) (d1 * (height - j)));
					float f22 = frontSprite.getV((float) (d1 * (height - (j + 1))));
					this.vertex(p_435197_, p_434479_, f15, f18, f20, f21, -0.03125F, 0, 0, -1, k);
					this.vertex(p_435197_, p_434479_, f16, f18, f19, f21, -0.03125F, 0, 0, -1, k);
					this.vertex(p_435197_, p_434479_, f16, f17, f19, f22, -0.03125F, 0, 0, -1, k);
					this.vertex(p_435197_, p_434479_, f15, f17, f20, f22, -0.03125F, 0, 0, -1, k);
					this.vertex(p_435197_, p_434479_, f15, f17, f4, f5, 0.03125F, 0, 0, 1, k);
					this.vertex(p_435197_, p_434479_, f16, f17, f3, f5, 0.03125F, 0, 0, 1, k);
					this.vertex(p_435197_, p_434479_, f16, f18, f3, f6, 0.03125F, 0, 0, 1, k);
					this.vertex(p_435197_, p_434479_, f15, f18, f4, f6, 0.03125F, 0, 0, 1, k);
					this.vertex(p_435197_, p_434479_, f15, f17, f7, f9, -0.03125F, 0, 1, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f17, f8, f9, -0.03125F, 0, 1, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f17, f8, f10, 0.03125F, 0, 1, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f17, f7, f10, 0.03125F, 0, 1, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f18, f7, f9, 0.03125F, 0, -1, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f18, f8, f9, 0.03125F, 0, -1, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f18, f8, f10, -0.03125F, 0, -1, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f18, f7, f10, -0.03125F, 0, -1, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f17, f12, f13, 0.03125F, -1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f18, f12, f14, 0.03125F, -1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f18, f11, f14, -0.03125F, -1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f15, f17, f11, f13, -0.03125F, -1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f17, f12, f13, -0.03125F, 1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f18, f12, f14, -0.03125F, 1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f18, f11, f14, 0.03125F, 1, 0, 0, k);
					this.vertex(p_435197_, p_434479_, f16, f17, f11, f13, 0.03125F, 1, 0, 0, k);
				}
			}
		});
	}

	private void vertex(
			PoseStack.Pose pose,
			VertexConsumer consumer,
			float x,
			float y,
			float u,
			float v,
			float z,
			int normalX,
			int normalY,
			int normalZ,
			int packedLight
	) {
		consumer.addVertex(pose, x, y, z)
				.setColor(-1)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(packedLight)
				.setNormal(pose, normalX, normalY, normalZ);
	}
}