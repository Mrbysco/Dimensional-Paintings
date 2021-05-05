package com.mrbysco.dimpaintings.entity;

import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class DimensionalPaintingEntity extends HangingEntity implements IEntityAdditionalSpawnData {
	private static final DataParameter<ItemStack> DATA_ITEM_STACK = EntityDataManager.defineId(DimensionalPaintingEntity.class, DataSerializers.ITEM_STACK);
	public DimensionPaintingType dimensionType;

	public DimensionalPaintingEntity(EntityType<? extends DimensionalPaintingEntity> entityType, World world) {
		super(entityType, world);
	}

	public DimensionalPaintingEntity(World world, BlockPos blockPos, Direction direction, DimensionPaintingType paintingType) {
		super(PaintingRegistry.DIMENSIONAL_PAINTING.get(), world, blockPos);
		this.setDimensionType(paintingType);
		this.setDirection(direction);
	}

	public DimensionalPaintingEntity(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
		super(PaintingRegistry.DIMENSIONAL_PAINTING.get(), worldIn);
	}

	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
	}

	public void setDimensionType(DimensionPaintingType type) {
		this.dimensionType = type;
	}

	public void addAdditionalSaveData(CompoundNBT compoundNBT) {
		compoundNBT.putString("Dimension", PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKey(this.dimensionType).toString());
		compoundNBT.putByte("Facing", (byte)this.direction.get2DDataValue());
		ItemStack itemstack = this.getItemRaw();
		if (!itemstack.isEmpty()) {
			compoundNBT.put("Item", itemstack.save(new CompoundNBT()));
		}
		super.addAdditionalSaveData(compoundNBT);
	}

	public void readAdditionalSaveData(CompoundNBT compoundNBT) {
		this.dimensionType = PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getValue(ResourceLocation.tryParse(compoundNBT.getString("Dimension")));
		this.direction = Direction.from2DDataValue(compoundNBT.getByte("Facing"));
		super.readAdditionalSaveData(compoundNBT);
		this.setDirection(this.direction);
		ItemStack itemstack = ItemStack.of(compoundNBT.getCompound("Item"));
		this.setItem(itemstack);
	}

	public void setItem(ItemStack p_213884_1_) {
		if (p_213884_1_.getItem() != PaintingRegistry.OVERWORLD_PAINTING.get() || p_213884_1_.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(p_213884_1_.copy(), (p_213883_0_) -> {
				p_213883_0_.setCount(1);
			}));
		}

	}

	protected ItemStack getItemRaw() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	public ItemStack getItem() {
		ItemStack itemstack = this.getItemRaw();
		return itemstack.isEmpty() ? new ItemStack(PaintingRegistry.OVERWORLD_PAINTING.get()) : itemstack;
	}

	public int getWidth() {
		return this.dimensionType == null ? 1 : this.dimensionType.getWidth();
	}

	public int getHeight() {
		return this.dimensionType == null ? 1 : this.dimensionType.getHeight();
	}

	public void dropItem(@Nullable Entity entity) {
		if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
			if (entity instanceof PlayerEntity) {
				PlayerEntity playerentity = (PlayerEntity)entity;
				if (playerentity.abilities.instabuild) {
					return;
				}
			}

			this.spawnAtLocation(getItem());
		}
	}

	public void playPlacementSound() {
		this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
	}

	public void moveTo(double posX, double posY, double posZ, float unused1, float unused2) {
		this.setPos(posX, posY, posZ);
	}

	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double xOffset, double yOffset, double zOffset, float unused1, float unused2, int unused3, boolean flag) {
		BlockPos blockpos = this.pos.offset(xOffset - this.getX(), yOffset - this.getY(), zOffset - this.getZ());
		this.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeUtf(this.dimensionType.getRegistryName().toString());
		buffer.writeByte((byte)this.direction.get2DDataValue());
		buffer.writeUtf(getItem().getItem().getRegistryName().toString());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		DimensionPaintingType type = PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getValue(ResourceLocation.tryParse(additionalData.readUtf()));
		Direction direction = Direction.from2DDataValue(additionalData.readByte());
		this.setDimensionType(type);
		this.setDirection(direction);
		Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(additionalData.readUtf()));
		if(item != null) {
			this.setItem(new ItemStack(item));
		}
	}
}