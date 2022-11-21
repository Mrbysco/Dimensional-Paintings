package com.mrbysco.dimpaintings.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrbysco.dimpaintings.entity.DimensionalPaintingEntity;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class DimensionalPaintingRenderer extends EntityRenderer<DimensionalPaintingEntity> {
	public DimensionalPaintingRenderer(EntityRendererManager rendererManager) {
		super(rendererManager);
	}

	public void render(DimensionalPaintingEntity dimensionalPaintingEntity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer typeBuffer, int p_225623_6_) {
		matrixStack.pushPose();
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityYaw));
		DimensionPaintingType dimensionType = dimensionalPaintingEntity.dimensionType;
		float f = 0.0625F;
		matrixStack.scale(f, f, f);
		IVertexBuilder ivertexbuilder = typeBuffer.getBuffer(RenderType.entitySolid(this.getTextureLocation(dimensionalPaintingEntity)));
		DimensionalPaintingSpriteUploader paintingSpriteUploader = DimensionalPaintingSpriteUploader.instance();
		this.renderDimensionalPainting(matrixStack, ivertexbuilder, dimensionalPaintingEntity, dimensionType.getWidth(), dimensionType.getHeight(), paintingSpriteUploader.get(dimensionType), paintingSpriteUploader.getBackSprite());
		matrixStack.popPose();
		super.render(dimensionalPaintingEntity, entityYaw, partialTicks, matrixStack, typeBuffer, p_225623_6_);
	}

	public ResourceLocation getTextureLocation(DimensionalPaintingEntity dimensionalPaintingEntity) {
		return DimensionalPaintingSpriteUploader.instance()
				.getBackSprite()
				.atlas()
				.location();
	}

	private void renderDimensionalPainting(MatrixStack matrixStack, IVertexBuilder vertexBuilder, DimensionalPaintingEntity dimensionalPaintingEntity, int p_229122_4_, int p_229122_5_, TextureAtlasSprite atlasSprite, TextureAtlasSprite atlasSprite1) {
		MatrixStack.Entry matrixstack$entry = matrixStack.last();
		Matrix4f matrix4f = matrixstack$entry.pose();
		Matrix3f matrix3f = matrixstack$entry.normal();
		float f = (float) (-p_229122_4_) / 2.0F;
		float f1 = (float) (-p_229122_5_) / 2.0F;
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
		double d0 = 16.0D / (double) i;
		double d1 = 16.0D / (double) j;

		for (int k = 0; k < i; ++k) {
			for (int l = 0; l < j; ++l) {
				float f15 = f + (float) ((k + 1) * 16);
				float f16 = f + (float) (k * 16);
				float f17 = f1 + (float) ((l + 1) * 16);
				float f18 = f1 + (float) (l * 16);
				int i1 = MathHelper.floor(dimensionalPaintingEntity.getX());
				int j1 = MathHelper.floor(dimensionalPaintingEntity.getY() + (double) ((f17 + f18) / 2.0F / 16.0F));
				int k1 = MathHelper.floor(dimensionalPaintingEntity.getZ());
				Direction direction = dimensionalPaintingEntity.getDirection();
				if (direction == Direction.NORTH) {
					i1 = MathHelper.floor(dimensionalPaintingEntity.getX() + (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.WEST) {
					k1 = MathHelper.floor(dimensionalPaintingEntity.getZ() - (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.SOUTH) {
					i1 = MathHelper.floor(dimensionalPaintingEntity.getX() - (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				if (direction == Direction.EAST) {
					k1 = MathHelper.floor(dimensionalPaintingEntity.getZ() + (double) ((f15 + f16) / 2.0F / 16.0F));
				}

				int l1 = WorldRenderer.getLightColor(dimensionalPaintingEntity.level, new BlockPos(i1, j1, k1));
				float f19 = atlasSprite.getU(d0 * (double) (i - k));
				float f20 = atlasSprite.getU(d0 * (double) (i - (k + 1)));
				float f21 = atlasSprite.getV(d1 * (double) (j - l));
				float f22 = atlasSprite.getV(d1 * (double) (j - (l + 1)));
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f20, f21, -f2, 0, 0, -1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f19, f21, -f2, 0, 0, -1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f19, f22, -f2, 0, 0, -1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f20, f22, -f2, 0, 0, -1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f3, f5, f2, 0, 0, 1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f4, f5, f2, 0, 0, 1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f4, f6, f2, 0, 0, 1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f3, f6, f2, 0, 0, 1, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f7, f9, -f2, 0, 1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f8, f9, -f2, 0, 1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f8, f10, f2, 0, 1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f7, f10, f2, 0, 1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f7, f9, f2, 0, -1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f8, f9, f2, 0, -1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f8, f10, -f2, 0, -1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f7, f10, -f2, 0, -1, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f12, f13, f2, -1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f12, f14, f2, -1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f18, f11, f14, -f2, -1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f15, f17, f11, f13, -f2, -1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f12, f13, -f2, 1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f12, f14, -f2, 1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f18, f11, f14, f2, 1, 0, 0, l1);
				this.vertex(matrix4f, matrix3f, vertexBuilder, f16, f17, f11, f13, f2, 1, 0, 0, l1);
			}
		}
	}

	private void vertex(Matrix4f matrix4f, Matrix3f matrix3f, IVertexBuilder vertexBuilder, float p_229121_4_, float p_229121_5_, float x, float y, float p_229121_8_, int p_229121_9_, int p_229121_10_, int p_229121_11_, int p_229121_12_) {
		vertexBuilder.vertex(matrix4f, p_229121_4_, p_229121_5_, p_229121_8_).color(255, 255, 255, 255).uv(x, y).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_229121_12_).normal(matrix3f, (float) p_229121_9_, (float) p_229121_10_, (float) p_229121_11_).endVertex();
	}
}