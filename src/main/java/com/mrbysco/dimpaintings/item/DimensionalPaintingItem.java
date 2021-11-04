package com.mrbysco.dimpaintings.item;

import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class DimensionalPaintingItem extends Item {
	private final Supplier<DimensionPaintingType> paintingDimensionSupplier;

	public DimensionalPaintingItem(Item.Properties properties, Supplier<DimensionPaintingType> paintingDimension) {
		super(properties.tab(PaintingTab.MAIN_TAB));
		this.paintingDimensionSupplier = paintingDimension;
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
			DimensionalPainting dimensionalPainting = new DimensionalPainting(level, relativePos, direction, paintingDimensionSupplier.get());
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
		}
	}

	protected boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
		return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, stack);
	}
}