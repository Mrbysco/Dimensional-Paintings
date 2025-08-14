package com.mrbysco.dimpaintings.item;

import com.mrbysco.dimpaintings.entity.DimensionalPainting;
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

public class DimensionalPaintingItem extends Item {
	private final ResourceLocation paintingDimension;

	public DimensionalPaintingItem(Item.Properties properties, ResourceLocation paintingDimension) {
		super(properties);
		this.paintingDimension = paintingDimension;
	}

	@Override
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
			DimensionalPainting dimensionalPainting = new DimensionalPainting(level, relativePos, direction, PaintingTypeRegistry.getHolder(level.registryAccess(), paintingDimension));			dimensionalPainting.setItem(stack);

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

				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.CONSUME;
			}
		}
	}

	protected boolean mayPlace(Player player, Direction direction, ItemStack hangingEntityStack, BlockPos pos) {
		return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, hangingEntityStack);
	}
}