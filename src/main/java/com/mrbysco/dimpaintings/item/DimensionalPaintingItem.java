package com.mrbysco.dimpaintings.item;

import com.mrbysco.dimpaintings.entity.DimensionalPainting;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DimensionalPaintingItem extends Item {
	private final Identifier paintingDimension;

	public DimensionalPaintingItem(Item.Properties properties, Identifier paintingDimension) {
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
			DimensionalPainting dimensionalPainting = new DimensionalPainting(level, relativePos, direction, PaintingTypeRegistry.getHolder(level.registryAccess(), paintingDimension));
			dimensionalPainting.setItem(stack);

			EntityType.<HangingEntity>createDefaultStackConfig(level, stack, player).accept(dimensionalPainting);
			if (dimensionalPainting.survives()) {
				if (!level.isClientSide()) {
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