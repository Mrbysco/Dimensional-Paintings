package com.mrbysco.dimpaintings.entity;

import com.mrbysco.dimpaintings.DimPaintings;
import com.mrbysco.dimpaintings.config.DimensionalConfig;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import com.mrbysco.dimpaintings.registry.PaintingSerializers;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import com.mrbysco.dimpaintings.util.PaintingWorldData;
import com.mrbysco.dimpaintings.util.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class DimensionalPainting extends HangingEntity implements IEntityWithComplexSpawn {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(DimensionalPainting.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<DimensionPaintingType> DIMENSION_TYPE = SynchedEntityData.defineId(DimensionalPainting.class, PaintingSerializers.DIMENSION_TYPE.get());

	public DimensionalPainting(EntityType<? extends DimensionalPainting> entityType, Level world) {
		super(entityType, world);
	}

	public DimensionalPainting(Level level, BlockPos blockPos, Direction direction, DimensionPaintingType paintingType) {
		super(PaintingRegistry.DIMENSIONAL_PAINTING.get(), level, blockPos);
		this.setDimensionType(paintingType);
		this.setDirection(direction);

		if (!this.level().isClientSide) {
			ServerLevel serverLevel = (ServerLevel) level;
			PaintingWorldData worldData = PaintingWorldData.get(serverLevel);
			worldData.addPositionToDimension(level.dimension().location(), getPos(), getDirection());
		}
	}

	@Override
	protected void setDirection(Direction dir) {
		super.setDirection(dir);
	}

	@Override
	protected void removeAfterChangingDimensions() {
		this.unsetRemoved();
	}

	@Override
	public boolean hurt(DamageSource damageSource, float amount) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			if (isAlive() && !this.level().isClientSide) {
				this.removeStoredPosition();
				this.kill();
				this.markHurt();
				this.dropItem(damageSource.getEntity());
			}

			return true;
		}
	}

	public void move(MoverType type, Vec3 position) {
		if (!this.level().isClientSide && isAlive() && position.lengthSqr() > 0.0D) {
			this.removeStoredPosition();
			this.kill();
			this.dropItem((Entity) null);
		}

	}

	public void push(double posX, double posY, double posZ) {
		if (!this.level().isClientSide && isAlive() && posX * posX + posY * posY + posZ * posZ > 0.0D) {
			this.removeStoredPosition();
			this.kill();
			this.dropItem((Entity) null);
		}

	}

	private void removeStoredPosition() {
		ServerLevel serverWorld = (ServerLevel) this.level();
		PaintingWorldData worldData = PaintingWorldData.get(serverWorld);
		worldData.removePositionFromDimension(this.level().dimension().location(), getPos());
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide && isAlive()) {
			List<Entity> nearbyEntities = this.level().getEntitiesOfClass(Entity.class, getBoundingBox());
			if (!nearbyEntities.isEmpty()) {
				for (Iterator<Entity> iterator = nearbyEntities.iterator(); iterator.hasNext(); ) {
					Entity entityIn = iterator.next();
					if (entityIn != this && !(entityIn instanceof FakePlayer) && !(entityIn instanceof Player)) {
						boolean flag = entityIn.distanceTo(this) < 1 && !entityIn.onGround();
						if (flag && !entityIn.isPassenger() && !entityIn.isPassenger() && !entityIn.isVehicle() && entityIn.canChangeDimensions()) {
							if (this.getDimensionType() != null) {
								entityIn.teleportTo((int) this.getX(), (int) this.getY(), (int) this.getZ());
								TeleportHelper.teleportToGivenDimension(entityIn, this.getDimensionType().getDimensionLocation());
							}
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void playerTouch(Player player) {
		super.playerTouch(player);
		if (!this.level().isClientSide && isAlive()) {
			boolean flag = player.distanceTo(this) < 1 && !player.onGround();
			if (flag && !player.isPassenger() && !player.isPassenger() && !player.isVehicle() && player.canChangeDimensions()) {
				boolean cooldownFlag = DimensionalConfig.COMMON.teleportCooldown.get() == 0;
				if (cooldownFlag || !player.getPersistentData().contains("PaintingCooldown")) {
					if (this.getDimensionType() != null) {
						if (!cooldownFlag) {
							player.getPersistentData().putInt("PaintingCooldown", DimensionalConfig.COMMON.teleportCooldown.get());
						}
						player.teleportTo((int) this.getX(), (int) this.getY(), (int) this.getZ());
						TeleportHelper.teleportToGivenDimension(player, this.getDimensionType().getDimensionLocation());
					}
				} else {
					player.displayClientMessage(Component.translatable("dimpaintings.cooldown").withStyle(ChatFormatting.GOLD), true);
				}
			}
		}
	}

	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
		this.getEntityData().define(DIMENSION_TYPE, PaintingRegistry.OVERWORLD.get());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
		if (DIMENSION_TYPE.equals(pKey)) {
			this.recalculateBoundingBox();
		}
	}

	public void setDimensionType(DimensionPaintingType type) {
		if (type == null) {
			DimPaintings.LOGGER.error("Can not set Dimension type to null");
		} else {
			this.entityData.set(DIMENSION_TYPE, type);
		}
	}

	public DimensionPaintingType getDimensionType() {
		return this.entityData.get(DIMENSION_TYPE);
	}

	public void addAdditionalSaveData(CompoundTag compoundNBT) {
		compoundNBT.putString("Dimension", PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKey(this.getDimensionType()).toString());
		compoundNBT.putByte("Facing", (byte) this.direction.get2DDataValue());
		ItemStack itemstack = this.getItemRaw();
		if (!itemstack.isEmpty()) {
			compoundNBT.put("Item", itemstack.save(new CompoundTag()));
		}
		super.addAdditionalSaveData(compoundNBT);
	}

	public void readAdditionalSaveData(CompoundTag compoundNBT) {
		this.setDimensionType(PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.get(ResourceLocation.tryParse(compoundNBT.getString("Dimension"))));
		this.direction = Direction.from2DDataValue(compoundNBT.getByte("Facing"));
		super.readAdditionalSaveData(compoundNBT);
		this.setDirection(this.direction);
		ItemStack itemstack = ItemStack.of(compoundNBT.getCompound("Item"));
		this.setItem(itemstack);
	}

	public void setItem(ItemStack stack) {
		if (stack.getItem() != PaintingRegistry.OVERWORLD_PAINTING.get() || stack.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(stack.copy(), (itemStack) -> itemStack.setCount(1)));
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
		return this.getDimensionType() == null ? 1 : this.getDimensionType().getWidth();
	}

	public int getHeight() {
		return this.getDimensionType() == null ? 1 : this.getDimensionType().getHeight();
	}

	public void dropItem(@Nullable Entity entity) {
		if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
			if (entity instanceof Player player) {
				if (player.getAbilities().instabuild) {
					return;
				}
			}

			this.spawnAtLocation(getItem());
		}
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		return getItem();
	}

	public void playPlacementSound() {
		this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
	}

	/**
	 * Sets the location and rotation of the entity in the world.
	 */
	@Override
	public void moveTo(double pX, double pY, double pZ, float pYaw, float pPitch) {
		this.setPos(pX, pY, pZ);
	}

	@Override
	public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pSteps) {
		this.setPos(pX, pY, pZ);
	}

	@Override
	public Vec3 trackingPosition() {
		return Vec3.atLowerCornerOf(this.pos);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeByte((byte) this.direction.get2DDataValue());
		buffer.writeUtf(PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKey(this.getDimensionType()).toString());
		buffer.writeUtf(BuiltInRegistries.ITEM.getKey(getItem().getItem()).toString());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		this.setDirection(Direction.from2DDataValue(additionalData.readByte()));
		this.setDimensionType(PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.get(ResourceLocation.tryParse(additionalData.readUtf())));
		Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(additionalData.readUtf()));
		if (item != null) {
			this.setItem(new ItemStack(item));
		}
	}

	@Override
	protected void recalculateBoundingBox() {
		if (this.direction != null) {
			double posX = (double) this.pos.getX() + 0.5;
			double posY = (double) this.pos.getY() + 0.5;
			double posZ = (double) this.pos.getZ() + 0.5;

			if (this.level().isClientSide) {
				if (tickCount == 0) {
					if (direction == Direction.NORTH)
						posY -= 1;
					if (direction == Direction.EAST)
						posY -= 1;
					if (direction == Direction.SOUTH) {
						posX -= 1;
						posY -= 1;
					}
					if (direction == Direction.WEST) {
						posY -= 1;
						posZ -= 1;
					}
				}
			}

			double d3 = 0.46875;
			double offWidth = this.offs(this.getWidth());
			double offHeight = this.offs(this.getHeight());
			posX -= (double) this.direction.getStepX() * d3;
			posZ -= (double) this.direction.getStepZ() * d3;
			posY += offHeight;
			Direction direction = this.direction.getCounterClockWise();
			posX += offWidth * (double) direction.getStepX();
			posZ += offWidth * (double) direction.getStepZ();
			this.setPosRaw(posX, posY, posZ);
			double width = (double) this.getWidth();
			double height = (double) this.getHeight();
			double width2 = (double) this.getWidth();
			if (this.direction.getAxis() == Direction.Axis.Z) {
				width2 = 1.0;
			} else {
				width = 1.0;
			}

			width /= 32.0;
			height /= 32.0;
			width2 /= 32.0;
			this.setBoundingBox(new AABB(posX - width, posY - height, posZ - width2, posX + width, posY + height, posZ + width2));
		}
	}

	private double offs(int size) {
		return size % 32 == 0 ? 0.5D : 0.0D;
	}

	@Override
	public Component getName() {
		return this.getItem().getDisplayName();
	}

	@Override
	public boolean survives() {
		if (!this.level().noCollision(this)) {
			return false;
		} else {
			int i = Math.max(1, this.getWidth() / 16);
			int j = Math.max(1, this.getHeight() / 16);
			BlockPos blockpos = this.pos.relative(this.direction.getOpposite());
			Direction direction = this.direction.getCounterClockWise();
			BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

			for (int k = 0; k < i; ++k) {
				for (int l = 0; l < j; ++l) {
					int i1 = (i - 1) / -2;
					int j1 = (j - 1) / -2;
					blockpos$mutable.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
					BlockState blockstate = this.level().getBlockState(blockpos$mutable);
					if (net.minecraft.world.level.block.Block.canSupportCenter(this.level(), blockpos$mutable, this.direction))
						continue;
					if (!blockstate.isSolid() && !DiodeBlock.isDiode(blockstate)) {
						return false;
					}
				}
			}

			return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
		}
	}

	@Override
	public void refreshDimensions() {
		double posX = this.getX();
		double posY = this.getY();
		double posZ = this.getZ();
		this.setPosRaw(posX, posY, posZ);
	}
}