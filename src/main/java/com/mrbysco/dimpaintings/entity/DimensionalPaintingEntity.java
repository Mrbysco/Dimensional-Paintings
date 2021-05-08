package com.mrbysco.dimpaintings.entity;

import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import com.mrbysco.dimpaintings.util.PaintingWorldData;
import com.mrbysco.dimpaintings.util.TeleportHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

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

		if(!level.isClientSide) {
			ServerWorld serverWorld = (ServerWorld) world;
			PaintingWorldData worldData = PaintingWorldData.get(serverWorld);
			worldData.addPositionToDimension(world.dimension().location(), getPos(), getDirection());
		}
	}

	public DimensionalPaintingEntity(FMLPlayMessages.SpawnEntity spawnEntity, World worldIn) {
		this(worldIn, new BlockPos((int)spawnEntity.getPosX(), (int)spawnEntity.getPosY(), (int)spawnEntity.getPosZ()),
				Direction.from2DDataValue(spawnEntity.getAdditionalData().readByte()),
				PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getValue(ResourceLocation.tryParse(spawnEntity.getAdditionalData().readUtf())));

		PacketBuffer additionalData = spawnEntity.getAdditionalData();
		Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(additionalData.readUtf()));
		if(item != null) {
			this.setItem(new ItemStack(item));
		}
	}

	@Override
	protected void setDirection(Direction dir) {
		super.setDirection(dir);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void removeAfterChangingDimensions() {
		this.removed = false;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float amount) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			if (!this.removed && !this.level.isClientSide) {
				removeStoredPosition();
				this.remove();
				this.markHurt();
				this.dropItem(damageSource.getEntity());
			}

			return true;
		}
	}

	public void move(MoverType type, Vector3d position) {
		if (!this.level.isClientSide && !this.removed && position.lengthSqr() > 0.0D) {
			removeStoredPosition();
			this.remove();
			this.dropItem((Entity)null);
		}

	}

	public void push(double posX, double posY, double posZ) {
		if (!this.level.isClientSide && !this.removed && posX * posX + posY * posY + posZ * posZ > 0.0D) {
			removeStoredPosition();
			this.remove();
			this.dropItem((Entity)null);
		}

	}

	private void removeStoredPosition() {
		ServerWorld serverWorld = (ServerWorld) level;
		PaintingWorldData worldData = PaintingWorldData.get(serverWorld);
		worldData.removePositionFromDimension(level.dimension().location(), getPos());
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		if(!level.isClientSide) {
			List<Entity> nearbyEntities = this.level.getEntitiesOfClass(Entity.class, getBoundingBox());
			if(!nearbyEntities.isEmpty()) {
				for (Iterator<Entity> iterator = nearbyEntities.iterator(); iterator.hasNext(); ) {
					Entity entityIn = iterator.next();
					if(entityIn != this && !(entityIn instanceof FakePlayer) && !(entityIn instanceof PlayerEntity)) {
						boolean flag = entityIn.distanceTo(this) < 1 && !entityIn.isOnGround();
						if(flag && !entityIn.isPassenger() && !entityIn.isPassenger() && !entityIn.isVehicle() && entityIn.canChangeDimensions()) {
							TeleportHelper.teleportToGivenDimension(entityIn, this.dimensionType.getDimensionLocation());
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void playerTouch(PlayerEntity player) {
		super.playerTouch(player);
		if(!level.isClientSide) {
			boolean flag = player.distanceTo(this) < 1 && !player.isOnGround();
			if(flag && !player.isPassenger() && !player.isPassenger() && !player.isVehicle() && player.canChangeDimensions()) {
				TeleportHelper.teleportToGivenDimension(player, this.dimensionType.getDimensionLocation());
				return;
			}
		}
	}

	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
	}

	public void setDimensionType(DimensionPaintingType type) {
		this.dimensionType = type;
	}

	public DimensionPaintingType getDimensionType() {
		return dimensionType;
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

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return getItem();
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
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeByte((byte)this.direction.get2DDataValue());
		buffer.writeUtf(this.dimensionType.getRegistryName().toString());
		buffer.writeUtf(getItem().getItem().getRegistryName().toString());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) { }

	@Override
	protected void recalculateBoundingBox() {
		if (this.direction != null) {
			if(level.isClientSide) {
				if(tickCount == 0) {
					System.out.println(direction);
					if(direction == Direction.NORTH)
						this.pos = this.pos.offset(0, -0.5D, 0);
					if(direction == Direction.EAST)
						this.pos = this.pos.offset(0, -0.5D, 0);
					if(direction == Direction.SOUTH)
						this.pos = this.pos.offset(-0.5D, -0.5D, 0);
					if(direction == Direction.WEST)
						this.pos = this.pos.offset(0, -0.5D, -0.5D);
				}
			}

			double posX = (double)this.pos.getX() + 0.5D;
			double posY = (double)this.pos.getY() + 0.5D;
			double posZ = (double)this.pos.getZ() + 0.5D;

			double d3 = 0.46875D;
			double offWidth = this.offs(this.getWidth());
			double offHeight = this.offs(this.getHeight());
			posX = posX - (double)this.direction.getStepX() * d3;
			posZ = posZ - (double)this.direction.getStepZ() * d3;
			posY = posY + offHeight;
			Direction direction = this.direction.getCounterClockWise();
			posX = posX + offWidth * (double)direction.getStepX();
			posZ = posZ + offWidth * (double)direction.getStepZ();
			this.setPosRaw(posX, posY, posZ);
			double width = (double)this.getWidth();
			double height = (double)this.getHeight();
			double width2 = (double)this.getWidth();
			if (this.direction.getAxis() == Direction.Axis.Z) {
				width2 = 1.0D;
			} else {
				width = 1.0D;
			}

			width = width / 32.0D;
			height = height / 32.0D;
			width2 = width2 / 32.0D;
			this.setBoundingBox(new AxisAlignedBB(posX - width, posY - height, posZ - width2, posX + width, posY + height, posZ + width2));
		}
	}

	private double offs(int size) {
		return size % 32 == 0 ? 0.5D : 0.0D;
	}


	@Override
	public boolean survives() {
		if (!this.level.noCollision(this)) {
			return false;
		} else {
			int i = Math.max(1, this.getWidth() / 16);
			int j = Math.max(1, this.getHeight() / 16);
			BlockPos blockpos = this.pos.relative(this.direction.getOpposite());
			Direction direction = this.direction.getCounterClockWise();
			BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

			for(int k = 0; k < i; ++k) {
				for(int l = 0; l < j; ++l) {
					int i1 = (i - 1) / -2;
					int j1 = (j - 1) / -2;
					blockpos$mutable.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
					BlockState blockstate = this.level.getBlockState(blockpos$mutable);
					if (net.minecraft.block.Block.canSupportCenter(this.level, blockpos$mutable, this.direction))
						continue;
					if (!blockstate.getMaterial().isSolid() && !RedstoneDiodeBlock.isDiode(blockstate)) {
						return false;
					}
				}
			}

			return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
		}
	}

	@Override
	public void refreshDimensions() {
		double posX = this.getX();
		double posY = this.getY();
		double posZ = this.getZ();
		super.refreshDimensions();
		this.setPosRaw(posX, posY, posZ);
	}
}