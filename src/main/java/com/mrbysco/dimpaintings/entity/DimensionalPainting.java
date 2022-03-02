package com.mrbysco.dimpaintings.entity;

import com.mrbysco.dimpaintings.config.DimensionalConfig;
import com.mrbysco.dimpaintings.registry.DimensionPaintingType;
import com.mrbysco.dimpaintings.registry.PaintingRegistry;
import com.mrbysco.dimpaintings.registry.PaintingTypeRegistry;
import com.mrbysco.dimpaintings.util.PaintingWorldData;
import com.mrbysco.dimpaintings.util.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class DimensionalPainting extends HangingEntity implements IEntityAdditionalSpawnData {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(DimensionalPainting.class, EntityDataSerializers.ITEM_STACK);
	public DimensionPaintingType dimensionType;

	public DimensionalPainting(EntityType<? extends DimensionalPainting> entityType, Level world) {
		super(entityType, world);
	}

	public DimensionalPainting(Level world, BlockPos blockPos, Direction direction, DimensionPaintingType paintingType) {
		super(PaintingRegistry.DIMENSIONAL_PAINTING.get(), world, blockPos);
		this.setDimensionType(paintingType);
		this.setDirection(direction);

		if (!level.isClientSide) {
			ServerLevel serverWorld = (ServerLevel) world;
			PaintingWorldData worldData = PaintingWorldData.get(serverWorld);
			worldData.addPositionToDimension(world.dimension().location(), getPos(), getDirection());
		}
	}

	public DimensionalPainting(SpawnEntity spawnEntity, Level worldIn) {
		this(worldIn, new BlockPos((int) spawnEntity.getPosX(), (int) spawnEntity.getPosY(), (int) spawnEntity.getPosZ()),
				Direction.from2DDataValue(spawnEntity.getAdditionalData().readByte()),
				PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getValue(ResourceLocation.tryParse(spawnEntity.getAdditionalData().readUtf())));

		FriendlyByteBuf additionalData = spawnEntity.getAdditionalData();
		Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(additionalData.readUtf()));
		if (item != null) {
			this.setItem(new ItemStack(item));
		}
	}

	@Override
	protected void setDirection(Direction dir) {
		super.setDirection(dir);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
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
			if (isAlive() && !this.level.isClientSide) {
				this.removeStoredPosition();
				this.kill();
				this.markHurt();
				this.dropItem(damageSource.getEntity());
			}

			return true;
		}
	}

	public void move(MoverType type, Vec3 position) {
		if (!this.level.isClientSide && isAlive() && position.lengthSqr() > 0.0D) {
			this.removeStoredPosition();
			this.kill();
			this.dropItem((Entity) null);
		}

	}

	public void push(double posX, double posY, double posZ) {
		if (!this.level.isClientSide && isAlive() && posX * posX + posY * posY + posZ * posZ > 0.0D) {
			this.removeStoredPosition();
			this.kill();
			this.dropItem((Entity) null);
		}

	}

	private void removeStoredPosition() {
		ServerLevel serverWorld = (ServerLevel) level;
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

		if (!level.isClientSide && isAlive()) {
			List<Entity> nearbyEntities = this.level.getEntitiesOfClass(Entity.class, getBoundingBox());
			if (!nearbyEntities.isEmpty()) {
				for (Iterator<Entity> iterator = nearbyEntities.iterator(); iterator.hasNext(); ) {
					Entity entityIn = iterator.next();
					if (entityIn != this && !(entityIn instanceof FakePlayer) && !(entityIn instanceof Player)) {
						boolean flag = entityIn.distanceTo(this) < 1 && !entityIn.isOnGround();
						if (flag && !entityIn.isPassenger() && !entityIn.isPassenger() && !entityIn.isVehicle() && entityIn.canChangeDimensions()) {
							entityIn.teleportTo((int) this.getX(), (int) this.getY(), (int) this.getZ());
							TeleportHelper.teleportToGivenDimension(entityIn, this.dimensionType.getDimensionLocation());
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
		if (!level.isClientSide && isAlive()) {
			boolean flag = player.distanceTo(this) < 1 && !player.isOnGround();
			if (flag && !player.isPassenger() && !player.isPassenger() && !player.isVehicle() && player.canChangeDimensions()) {
				boolean cooldownFlag = DimensionalConfig.COMMON.teleportCooldown.get() == 0;
				if (cooldownFlag || !player.getPersistentData().contains("PaintingCooldown")) {
					if (!cooldownFlag) {
						player.getPersistentData().putInt("PaintingCooldown", DimensionalConfig.COMMON.teleportCooldown.get());
					}
					player.teleportTo((int) this.getX(), (int) this.getY(), (int) this.getZ());
					TeleportHelper.teleportToGivenDimension(player, this.dimensionType.getDimensionLocation());
				} else {
					player.displayClientMessage(new TranslatableComponent("dimpaintings.cooldown").withStyle(ChatFormatting.GOLD), true);
				}
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

	public void addAdditionalSaveData(CompoundTag compoundNBT) {
		compoundNBT.putString("Dimension", PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getKey(this.dimensionType).toString());
		compoundNBT.putByte("Facing", (byte) this.direction.get2DDataValue());
		ItemStack itemstack = this.getItemRaw();
		if (!itemstack.isEmpty()) {
			compoundNBT.put("Item", itemstack.save(new CompoundTag()));
		}
		super.addAdditionalSaveData(compoundNBT);
	}

	public void readAdditionalSaveData(CompoundTag compoundNBT) {
		this.dimensionType = PaintingTypeRegistry.DIMENSIONAL_PAINTINGS.getValue(ResourceLocation.tryParse(compoundNBT.getString("Dimension")));
		this.direction = Direction.from2DDataValue(compoundNBT.getByte("Facing"));
		super.readAdditionalSaveData(compoundNBT);
		this.setDirection(this.direction);
		ItemStack itemstack = ItemStack.of(compoundNBT.getCompound("Item"));
		this.setItem(itemstack);
	}

	public void setItem(ItemStack p_213884_1_) {
		if (p_213884_1_.getItem() != PaintingRegistry.OVERWORLD_PAINTING.get() || p_213884_1_.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(p_213884_1_.copy(), (stack) -> stack.setCount(1)));
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

	public void moveTo(double posX, double posY, double posZ, float unused1, float unused2) {
		this.setPos(posX, posY, posZ);
	}

	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double xOffset, double yOffset, double zOffset, float unused1, float unused2, int unused3, boolean flag) {
		BlockPos blockpos = this.pos.offset(xOffset - this.getX(), yOffset - this.getY(), zOffset - this.getZ());
		this.setPos((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeByte((byte) this.direction.get2DDataValue());
		buffer.writeUtf(this.dimensionType.getRegistryName().toString());
		buffer.writeUtf(getItem().getItem().getRegistryName().toString());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
	}

	@Override
	protected void recalculateBoundingBox() {
		if (this.direction != null) {
			if (level.isClientSide) {
				if (tickCount == 0) {
					if (direction == Direction.NORTH)
						this.pos = this.pos.offset(0, -0.5D, 0);
					if (direction == Direction.EAST)
						this.pos = this.pos.offset(0, -0.5D, 0);
					if (direction == Direction.SOUTH)
						this.pos = this.pos.offset(-0.5D, -0.5D, 0);
					if (direction == Direction.WEST)
						this.pos = this.pos.offset(0, -0.5D, -0.5D);
				}
			}

			double posX = (double) this.pos.getX() + 0.5D;
			double posY = (double) this.pos.getY() + 0.5D;
			double posZ = (double) this.pos.getZ() + 0.5D;

			double d3 = 0.46875D;
			double offWidth = this.offs(this.getWidth());
			double offHeight = this.offs(this.getHeight());
			posX = posX - (double) this.direction.getStepX() * d3;
			posZ = posZ - (double) this.direction.getStepZ() * d3;
			posY = posY + offHeight;
			Direction direction = this.direction.getCounterClockWise();
			posX = posX + offWidth * (double) direction.getStepX();
			posZ = posZ + offWidth * (double) direction.getStepZ();
			this.setPosRaw(posX, posY, posZ);
			double width = (double) this.getWidth();
			double height = (double) this.getHeight();
			double width2 = (double) this.getWidth();
			if (this.direction.getAxis() == Direction.Axis.Z) {
				width2 = 1.0D;
			} else {
				width = 1.0D;
			}

			width = width / 32.0D;
			height = height / 32.0D;
			width2 = width2 / 32.0D;
			this.setBoundingBox(new AABB(posX - width, posY - height, posZ - width2, posX + width, posY + height, posZ + width2));
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
			BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

			for (int k = 0; k < i; ++k) {
				for (int l = 0; l < j; ++l) {
					int i1 = (i - 1) / -2;
					int j1 = (j - 1) / -2;
					blockpos$mutable.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
					BlockState blockstate = this.level.getBlockState(blockpos$mutable);
					if (net.minecraft.world.level.block.Block.canSupportCenter(this.level, blockpos$mutable, this.direction))
						continue;
					if (!blockstate.getMaterial().isSolid() && !DiodeBlock.isDiode(blockstate)) {
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