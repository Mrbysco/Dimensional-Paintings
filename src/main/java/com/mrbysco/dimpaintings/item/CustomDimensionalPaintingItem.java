package com.mrbysco.dimpaintings.item;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
			ResourceLocation paintingDimension = new ResourceLocation(DimPaintings.MOD_ID, "overworld");
			if (stack.hasTag()) {
				CompoundTag tag = stack.getTag();
				if (tag.contains("dimension_painting")) {
					paintingDimension = ResourceLocation.tryParse(tag.getString("dimension_painting"));
				}
			}
			if (paintingDimension != null) {
				DimensionalPainting dimensionalPainting = new DimensionalPainting(level, relativePos, direction, PaintingTypeRegistry.getValue(level, paintingDimension));
				dimensionalPainting.setItem(stack);

				CompoundTag tag = stack.getTag();
				if (tag != null) {
					EntityType.updateCustomEntityTag(level, player, dimensionalPainting, tag);
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

	@Override
	public Component getName(ItemStack stack) {
		if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains("painting_name")) {
			CompoundTag tag = stack.getTag();
			String painting = tag.getString("painting_name");
			if (!painting.isEmpty()) {
				return Component.literal(painting + " ").append(Component.translatable(this.getDescriptionId(stack)));
			}
		}
		return Component.literal("Custom ").append(super.getName(stack));
	}
}