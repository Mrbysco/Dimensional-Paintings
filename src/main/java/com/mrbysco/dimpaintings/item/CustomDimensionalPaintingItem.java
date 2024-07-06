package com.mrbysco.dimpaintings.item;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.registry.PaintingDataComponents;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class CustomDimensionalPaintingItem extends Item {
	public CustomDimensionalPaintingItem(Properties properties) {
		super(properties);
	}

	public InteractionResult useOn(UseOnContext useContext) {
		BlockPos pos = useContext.getClickedPos();
		Direction direction = useContext.getClickedFace();
		BlockPos relativePos = pos.relative(direction);
		Player player = useContext.getPlayer();
		ItemStack stack = useContext.getItemInHand();
		if (player != null && !this.mayPlace(player, direction, stack, relativePos)) {
			return InteractionResult.FAIL;
		} else {
			Level level = useContext.getLevel();
			ResourceLocation paintingDimension = ResourceLocation.fromNamespaceAndPath(DimPaintings.MOD_ID, "overworld");
			if (stack.has(PaintingDataComponents.DIMENSION_TYPE)) {
				paintingDimension = stack.get(PaintingDataComponents.DIMENSION_TYPE);
			}
			if (paintingDimension != null) {
				DimensionalPainting dimensionalPainting = new DimensionalPainting(level, relativePos, direction,
						PaintingTypeRegistry.getHolder(level.registryAccess(), paintingDimension));
				dimensionalPainting.setItem(stack);

				CustomData customdata = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
				if (!customdata.isEmpty()) {
					EntityType.updateCustomEntityTag(level, player, dimensionalPainting, customdata);
				}

				if (dimensionalPainting.survives()) {
					if (!level.isClientSide) {
						dimensionalPainting.playPlacementSound();
						level.addFreshEntity(dimensionalPainting);

						stack.shrink(1);
					}

					return InteractionResult.sidedSuccess(level.isClientSide);
				} else {
					return InteractionResult.CONSUME;
				}
			} else {
				return InteractionResult.FAIL;
			}
		}
	}

	protected boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
		return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, stack);
	}
}
