package com.mrbysco.dimpaintings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
		DimensionPaintingType dimensionType = dimensionalPainting.dimensionType;
		float f = 0.0625F;
		poseStack.scale(f, f, f);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(this.getTextureLocation(dimensionalPainting)));
		DimensionalPaintingSpriteUploader paintingSpriteUploader = DimensionalPaintingSpriteUploader.instance();
		this.renderDimensionalPainting(poseStack, consumer, dimensionalPainting, dimensionType.getWidth(), dimensionType.getHeight(), paintingSpriteUploader.get(dimensionType), paintingSpriteUploader.getBackSprite());
		poseStack.popPose();
		super.render(dimensionalPainting, entityYaw, partialTicks, poseStack, bufferSource, p_225623_6_);
	}

	public ResourceLocation getTextureLocation(DimensionalPainting dimensionalPainting) {
		return DimensionalPaintingSpriteUploader.instance()
				.getBackSprite()
				.atlas()
				.location();
	}

	private void renderDimensionalPainting(PoseStack poseStack, VertexConsumer consumer, DimensionalPainting dimensionalPainting, int p_229122_4_, int p_229122_5_, TextureAtlasSprite atlasSprite, TextureAtlasSprite atlasSprite1) {
		PoseStack.Pose last = poseStack.last();
		Matrix4f pose = last.pose();
		Matrix3f normal = last.normal();
		float f = (float)(-p_229122_4_) / 2.0F;
		float f1 = (float)(-p_229122_5_) / 2.0F;
		float f2 = 0.5F;
		float f3 = atlasSprite1.getU0();
		float f4 = atlasSprite1.getU1();
		float f5 = atlasSprite1.getV0();
		float f6 = atlasSprite1.getV1();
		float f7 = atlasSprite1.getU0();
		float f8 = atlasSprite1.getU1();
		float f9 = atlasSprite1.getV0();
		float f10 = atlasSprite1.getV(1.0D);
		float f11 = atlasSprite1.getU0();
		float f12 = atlasSprite1.getU(1.0D);
		float f13 = atlasSprite1.getV0();
		float f14 = atlasSprite1.getV1();
		int i = p_229122_4_ / 16;
		int j = p_229122_5_ / 16;
		double d0 = 16.0D / (double)i;
		double d1 = 16.0D / (double)j;

		for(int k = 0; k < i; ++k) {
			for(int l = 0; l < j; ++l) {
				float f15 = f + (float)((k + 1) * 16);
				float f16 = f + (float)(k * 16);
				float f17 = f1 + (float)((l + 1) * 16);
				float f18 = f1 + (float)(l * 16);
				int i1 = Mth.floor(dimensionalPainting.getX());
				int j1 = Mth.floor(dimensionalPainting.getY() + (double)((f17 + f18) / 2.0F / 16.0F));
				int k1 = Mth.floor(dimensionalPainting.getZ());
				Direction direction = dimensionalPainting.getDirection();
				if (direction == Direction.NORTH) {
					i1 = Mth.floor(dimensionalPainting.getX() + (double)((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.WEST) {
					k1 = Mth.floor(dimensionalPainting.getZ() - (double)((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.SOUTH) {
					i1 = Mth.floor(dimensionalPainting.getX() - (double)((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.EAST) {
					k1 = Mth.floor(dimensionalPainting.getZ() + (double)((f15 + f16) / 2.0F / 16.0F));
				}

				int l1 = LevelRenderer.getLightColor(dimensionalPainting.level, new BlockPos(i1, j1, k1));
				float f19 = atlasSprite.getU(d0 * (double)(i - k));
				float f20 = atlasSprite.getU(d0 * (double)(i - (k + 1)));
				float f21 = atlasSprite.getV(d1 * (double)(j - l));
				float f22 = atlasSprite.getV(d1 * (double)(j - (l + 1)));
				this.vertex(pose, normal, consumer, f15, f18, f20, f21, -f2, 0, 0, -1, l1);
				this.vertex(pose, normal, consumer, f16, f18, f19, f21, -f2, 0, 0, -1, l1);
				this.vertex(pose, normal, consumer, f16, f17, f19, f22, -f2, 0, 0, -1, l1);
				this.vertex(pose, normal, consumer, f15, f17, f20, f22, -f2, 0, 0, -1, l1);
				this.vertex(pose, normal, consumer, f15, f17, f3, f5, f2, 0, 0, 1, l1);
				this.vertex(pose, normal, consumer, f16, f17, f4, f5, f2, 0, 0, 1, l1);
				this.vertex(pose, normal, consumer, f16, f18, f4, f6, f2, 0, 0, 1, l1);
				this.vertex(pose, normal, consumer, f15, f18, f3, f6, f2, 0, 0, 1, l1);
				this.vertex(pose, normal, consumer, f15, f17, f7, f9, -f2, 0, 1, 0, l1);
				this.vertex(pose, normal, consumer, f16, f17, f8, f9, -f2, 0, 1, 0, l1);
				this.vertex(pose, normal, consumer, f16, f17, f8, f10, f2, 0, 1, 0, l1);
				this.vertex(pose, normal, consumer, f15, f17, f7, f10, f2, 0, 1, 0, l1);
				this.vertex(pose, normal, consumer, f15, f18, f7, f9, f2, 0, -1, 0, l1);
				this.vertex(pose, normal, consumer, f16, f18, f8, f9, f2, 0, -1, 0, l1);
				this.vertex(pose, normal, consumer, f16, f18, f8, f10, -f2, 0, -1, 0, l1);
				this.vertex(pose, normal, consumer, f15, f18, f7, f10, -f2, 0, -1, 0, l1);
				this.vertex(pose, normal, consumer, f15, f17, f12, f13, f2, -1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f15, f18, f12, f14, f2, -1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f15, f18, f11, f14, -f2, -1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f15, f17, f11, f13, -f2, -1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f16, f17, f12, f13, -f2, 1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f16, f18, f12, f14, -f2, 1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f16, f18, f11, f14, f2, 1, 0, 0, l1);
				this.vertex(pose, normal, consumer, f16, f17, f11, f13, f2, 1, 0, 0, l1);
			}
		}
	}

	private void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float p_229121_4_, float p_229121_5_, float x, float y, float p_229121_8_, int p_229121_9_, int p_229121_10_, int p_229121_11_, int p_229121_12_) {
		consumer.vertex(pose, p_229121_4_, p_229121_5_, p_229121_8_).color(255, 255, 255, 255).uv(x, y).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_229121_12_).normal(normal, (float)p_229121_9_, (float)p_229121_10_, (float)p_229121_11_).endVertex();
	}
}