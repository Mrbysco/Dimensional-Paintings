package com.mrbysco.dimpaintings.item;

import com.mrbysco.dimpaintings.entity.DimensionalPaintingEntity;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingTab;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class DimensionalPaintingItem extends Item {
	private final Supplier<DimensionPaintingType> paintingDimensionSupplier;

	public DimensionalPaintingItem(Item.Properties properties, Supplier<DimensionPaintingType> paintingDimension) {
		super(properties.tab(PaintingTab.MAIN_TAB));
		this.paintingDimensionSupplier = paintingDimension;
	}

	public ActionResultType useOn(ItemUseContext useContext) {
		BlockPos blockpos = useContext.getClickedPos();
		Direction direction = useContext.getClickedFace();
		BlockPos blockpos1 = blockpos.relative(direction);
		PlayerEntity playerentity = useContext.getPlayer();
		ItemStack itemstack = useContext.getItemInHand();
		if (playerentity != null && !this.mayPlace(playerentity, direction, itemstack, blockpos1)) {
			return ActionResultType.FAIL;
		} else {
			World world = useContext.getLevel();
			DimensionalPaintingEntity hangingentity = new DimensionalPaintingEntity(world, blockpos1, direction, paintingDimensionSupplier.get());
			hangingentity.setItem(itemstack);

			CompoundNBT compoundnbt = itemstack.getTag();
			if (compoundnbt != null) {
				EntityType.updateCustomEntityTag(world, playerentity, hangingentity, compoundnbt);
			}

			if (hangingentity.survives()) {
				if (!world.isClientSide) {
					hangingentity.playPlacementSound();
					world.addFreshEntity(hangingentity);

					itemstack.shrink(1);
				}

				return ActionResultType.sidedSuccess(world.isClientSide);
			} else {
				return ActionResultType.CONSUME;
			}
		}
	}

	protected boolean mayPlace(PlayerEntity playerEntity, Direction direction, ItemStack stack, BlockPos blockPos) {
		return !direction.getAxis().isVertical() && playerEntity.mayUseItemAt(blockPos, direction, stack);
	}
}