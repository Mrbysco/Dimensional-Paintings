package com.mrbysco.dimpaintings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DimensionalPaintingRenderer extends EntityRenderer<DimensionalPainting> {
	public DimensionalPaintingRenderer(Context rendererManager) {
		super(rendererManager);
	}

	public void render(DimensionalPainting dimensionalPainting, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_225623_6_) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

		DimensionPaintingType dimensionType = dimensionalPainting.getDimensionType().value();
		float f = 0.0625F;
		poseStack.scale(f, f, f);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(this.getTextureLocation(dimensionalPainting)));
		DimensionalPaintingTextureManager paintingSpriteUploader = DimensionalPaintingTextureManager.instance();
		this.renderDimensionalPainting(poseStack, consumer, dimensionalPainting, dimensionType.getWidth(), dimensionType.getHeight(), paintingSpriteUploader.get(dimensionType), paintingSpriteUploader.getBackSprite());
		poseStack.popPose();
		super.render(dimensionalPainting, entityYaw, partialTicks, poseStack, bufferSource, p_225623_6_);
	}

	public ResourceLocation getTextureLocation(DimensionalPainting dimensionalPainting) {
		return DimensionalPaintingTextureManager.instance().getBackSprite().atlasLocation();
	}

	private void renderDimensionalPainting(PoseStack poseStack, VertexConsumer consumer, DimensionalPainting dimensionalPainting, int width, int height, TextureAtlasSprite paintingSprite, TextureAtlasSprite backSprite) {
		PoseStack.Pose pose = poseStack.last();
		float f = (float) (-width) / 2.0F;
		float f1 = (float) (-height) / 2.0F;
		float f2 = 0.5F;
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
		int i = width / 16;
		int j = height / 16;
		double d0 = 1.0 / (double) i;
		double d1 = 1.0 / (double) j;

		for (int k = 0; k < i; ++k) {
			for (int l = 0; l < j; ++l) {
				float f15 = f + (float) ((k + 1) * 16);
				float f16 = f + (float) (k * 16);
				float f17 = f1 + (float) ((l + 1) * 16);
				float f18 = f1 + (float) (l * 16);
				int i1 = dimensionalPainting.getBlockX();
				int j1 = Mth.floor(dimensionalPainting.getY() + (double) ((f17 + f18) / 2.0F / 16.0F));
				int k1 = dimensionalPainting.getBlockZ();
				Direction direction = dimensionalPainting.getDirection();
				if (direction == Direction.NORTH) {
					i1 = Mth.floor(dimensionalPainting.getX() + (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.WEST) {
					k1 = Mth.floor(dimensionalPainting.getZ() - (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.SOUTH) {
					i1 = Mth.floor(dimensionalPainting.getX() - (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.EAST) {
					k1 = Mth.floor(dimensionalPainting.getZ() + (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				int l1 = LevelRenderer.getLightColor(dimensionalPainting.level(), new BlockPos(i1, j1, k1));
				float f19 = paintingSprite.getU((float) (d0 * (double) (i - k)));
				float f20 = paintingSprite.getU((float) (d0 * (double) (i - (k + 1))));
				float f21 = paintingSprite.getV((float) (d1 * (double) (j - l)));
				float f22 = paintingSprite.getV((float) (d1 * (double) (j - (l + 1))));
				this.vertex(pose, consumer, f15, f18, f20, f21, -f2, 0, 0, -1, l1);
				this.vertex(pose, consumer, f16, f18, f19, f21, -f2, 0, 0, -1, l1);
				this.vertex(pose, consumer, f16, f17, f19, f22, -f2, 0, 0, -1, l1);
				this.vertex(pose, consumer, f15, f17, f20, f22, -f2, 0, 0, -1, l1);
				this.vertex(pose, consumer, f15, f17, f4, f5, f2, 0, 0, 1, l1);
				this.vertex(pose, consumer, f16, f17, f3, f5, f2, 0, 0, 1, l1);
				this.vertex(pose, consumer, f16, f18, f3, f6, f2, 0, 0, 1, l1);
				this.vertex(pose, consumer, f15, f18, f4, f6, f2, 0, 0, 1, l1);
				this.vertex(pose, consumer, f15, f17, f7, f9, -f2, 0, 1, 0, l1);
				this.vertex(pose, consumer, f16, f17, f8, f9, -f2, 0, 1, 0, l1);
				this.vertex(pose, consumer, f16, f17, f8, f10, f2, 0, 1, 0, l1);
				this.vertex(pose, consumer, f15, f17, f7, f10, f2, 0, 1, 0, l1);
				this.vertex(pose, consumer, f15, f18, f7, f9, f2, 0, -1, 0, l1);
				this.vertex(pose, consumer, f16, f18, f8, f9, f2, 0, -1, 0, l1);
				this.vertex(pose, consumer, f16, f18, f8, f10, -f2, 0, -1, 0, l1);
				this.vertex(pose, consumer, f15, f18, f7, f10, -f2, 0, -1, 0, l1);
				this.vertex(pose, consumer, f15, f17, f12, f13, f2, -1, 0, 0, l1);
				this.vertex(pose, consumer, f15, f18, f12, f14, f2, -1, 0, 0, l1);
				this.vertex(pose, consumer, f15, f18, f11, f14, -f2, -1, 0, 0, l1);
				this.vertex(pose, consumer, f15, f17, f11, f13, -f2, -1, 0, 0, l1);
				this.vertex(pose, consumer, f16, f17, f12, f13, -f2, 1, 0, 0, l1);
				this.vertex(pose, consumer, f16, f18, f12, f14, -f2, 1, 0, 0, l1);
				this.vertex(pose, consumer, f16, f18, f11, f14, f2, 1, 0, 0, l1);
				this.vertex(pose, consumer, f16, f17, f11, f13, f2, 1, 0, 0, l1);
			}
		}
	}

	private void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y,
	                    float u, float v, float z, int normalX, int normalY, int normalZ, int packedLight) {
		consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, (float) normalX, (float) normalY, (float) normalZ);
	}
}