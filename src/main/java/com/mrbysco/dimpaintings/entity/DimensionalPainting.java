package com.mrbysco.dimpaintings.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class DimensionalPainting extends HangingEntity implements IEntityWithComplexSpawn {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(DimensionalPainting.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Holder<DimensionPaintingType>> DIMENSION_TYPE = SynchedEntityData.defineId(
			DimensionalPainting.class, PaintingSerializers.DIMENSION_TYPE.get()
	);
	public static final MapCodec<Holder<DimensionPaintingType>> VARIANT_MAP_CODEC = DimensionPaintingType.CODEC.fieldOf("dimension_type");
	public static final Codec<Holder<DimensionPaintingType>> VARIANT_CODEC = VARIANT_MAP_CODEC.codec();

	public DimensionalPainting(EntityType<? extends DimensionalPainting> entityType, Level world) {
		super(entityType, world);
	}

	public DimensionalPainting(Level level, BlockPos blockPos, Direction direction, Holder<DimensionPaintingType> paintingType) {
		super(PaintingRegistry.DIMENSIONAL_PAINTING.get(), level, blockPos);
		this.setDimensionType(paintingType);
		this.setDirection(direction);

		if (!this.level().isClientSide) {
			ServerLevel serverLevel = (ServerLevel) level;
			PaintingWorldData worldData = PaintingWorldData.get(serverLevel);
			worldData.addPositionToDimension(level.dimension(), getPos(), getDirection());
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
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
		if (this.isInvulnerableToBase(damageSource)) {
			return false;
		} else {
			if (!this.isRemoved()) {
				this.removeStoredPosition(serverLevel);
				this.kill(serverLevel);
				this.markHurt();
				this.dropItem(serverLevel, damageSource.getEntity());
			}

			return true;
		}
	}

	@Override
	public void move(MoverType type, Vec3 position) {
		if (this.level() instanceof ServerLevel serverLevel && isAlive() && position.lengthSqr() > 0.0D) {
			this.removeStoredPosition(serverLevel);
			this.kill(serverLevel);
			this.dropItem(serverLevel, (Entity) null);
		}
	}

	@Override
	public void push(double posX, double posY, double posZ) {
		if (this.level() instanceof ServerLevel serverLevel && isAlive() && posX * posX + posY * posY + posZ * posZ > 0.0D) {
			this.removeStoredPosition(serverLevel);
			this.kill(serverLevel);
			this.dropItem(serverLevel, (Entity) null);
		}
	}

	private void removeStoredPosition(ServerLevel serverLevel) {
		PaintingWorldData worldData = PaintingWorldData.get(serverLevel);
		worldData.removePositionFromDimension(this.level().dimension(), getPos());
	}

	@Override
	public boolean canTeleport(Level oldLevel, Level newLevel) {
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
					if (entityIn != this && !(entityIn instanceof Player)) {
						boolean flag = entityIn.distanceTo(this) < 1 && !entityIn.onGround();
						if (flag && !entityIn.isPassenger() && !entityIn.isVehicle() && entityIn.canTeleport(this.level(), getDimensionLevel())) {
							if (this.getDimensionType() != null) {
								entityIn.teleportTo((int) this.getX(), (int) this.getY(), (int) this.getZ());
								TeleportHelper.teleportToGivenDimension(entityIn, this.getDimensionLevel());
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
			if (flag && !player.isPassenger() && !player.isPassenger() && !player.isVehicle() && player.canTeleport(this.level(), getDimensionLevel())) {
				boolean cooldownFlag = DimensionalConfig.COMMON.teleportCooldown.get() == 0;
				if (cooldownFlag || !player.getPersistentData().contains("PaintingCooldown")) {
					if (this.getDimensionType() != null) {
						if (!cooldownFlag) {
							player.getPersistentData().putInt("PaintingCooldown", DimensionalConfig.COMMON.teleportCooldown.get());
						}
						player.teleportTo((int) this.getX(), (int) this.getY(), (int) this.getZ());
						TeleportHelper.teleportToGivenDimension(player, this.getDimensionLevel());
					}
				} else {
					player.displayClientMessage(Component.translatable("dimpaintings.cooldown").withStyle(ChatFormatting.GOLD), true);
				}
			}
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ITEM_STACK, ItemStack.EMPTY);
		builder.define(DIMENSION_TYPE, this.registryAccess().lookupOrThrow(DimensionPaintingType.REGISTRY_KEY).getAny().orElseThrow());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
		if (DIMENSION_TYPE.equals(pKey)) {
			this.recalculateBoundingBox();
		}
	}

	public void setDimensionType(Holder<DimensionPaintingType> type) {
		if (type != null) {
			this.entityData.set(DIMENSION_TYPE, type);
		}
	}

	public Holder<DimensionPaintingType> getDimensionType() {
		return this.entityData.get(DIMENSION_TYPE);
	}

	public ResourceLocation getDimensionLocation() {
		return this.getDimensionType().value().dimensionId();
	}

	public ServerLevel getDimensionLevel() {
		if (this.level() instanceof ServerLevel serverLevel) {
			ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, getDimensionLocation());
			ServerLevel destination = serverLevel.getServer().getLevel(dimensionKey);
			if (destination == null) {
				DimPaintings.LOGGER.error("Destination of painting invalid {} isn't" +
						" known", getDimensionLocation());
				return null;
			}
			return destination;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		output.store("variant", VARIANT_CODEC, this.getDimensionType());
		output.store("facing", Direction.LEGACY_ID_CODEC_2D, this.getDirection());
		ItemStack itemstack = this.getItemRaw();
		if (!itemstack.isEmpty()) {
			output.store("Item", ItemStack.CODEC, itemstack);
		}
		super.addAdditionalSaveData(output);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void readAdditionalSaveData(ValueInput input) {
		input.read("variant", VARIANT_CODEC).ifPresent(this::setDimensionType);
		ItemStack itemstack = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		super.readAdditionalSaveData(input);
		this.setDirection(input.read("facing", Direction.LEGACY_ID_CODEC_2D).orElse(Direction.SOUTH));
		this.setItem(itemstack);
	}

	public void setItem(ItemStack stack) {
		if (stack.getItem() != PaintingRegistry.OVERWORLD_PAINTING.get()) {
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
		return this.getDimensionType() == null ? 1 : this.getDimensionType().value().width();
	}

	public int getHeight() {
		return this.getDimensionType() == null ? 1 : this.getDimensionType().value().height();
	}

	@Override
	public void dropItem(ServerLevel level, @Nullable Entity entity) {
		if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
			if (entity instanceof Player player) {
				if (player.getAbilities().instabuild) {
					return;
				}
			}

			this.spawnAtLocation(level, getItem());
		}
	}

	@Override
	public ItemStack getPickResult() {
		return getItem();
	}

	public void playPlacementSound() {
		this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
	}

	/**
	 * Sets the location and rotation of the entity in the world.
	 */
	@Override
	public void snapTo(double pX, double pY, double pZ, float pYaw, float pPitch) {
		this.setPos(pX, pY, pZ);
	}

	@Override
	public Vec3 trackingPosition() {
		return Vec3.atLowerCornerOf(this.pos);
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		buffer.writeByte((byte) this.getDirection().get2DDataValue());
		buffer.writeResourceKey(this.getDimensionType().unwrapKey().orElseThrow());
		buffer.writeUtf(BuiltInRegistries.ITEM.getKey(getItem().getItem()).toString());
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		this.setDirection(Direction.from2DDataValue(additionalData.readByte()));
		ResourceKey<DimensionPaintingType> dimension = additionalData.readResourceKey(DimensionPaintingType.REGISTRY_KEY);
		var dimensionValue = PaintingTypeRegistry.getHolder(this.registryAccess(), dimension);
		this.setDimensionType(dimensionValue);
		Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.tryParse(additionalData.readUtf()));
		if (item != null) {
			this.setItem(new ItemStack(item));
		}
	}

	@Override
	protected AABB calculateBoundingBox(BlockPos pos, Direction p_direction) {
		float f = 0.46875F;
		Vec3 vec3 = Vec3.atCenterOf(pos).relative(p_direction, -f);
		DimensionPaintingType paintingType = this.getDimensionType().value();
		double d0 = this.offsetForPaintingSize(paintingType.width());
		double d1 = this.offsetForPaintingSize(paintingType.height());
		Direction direction = p_direction.getCounterClockWise();
		Vec3 vec31 = vec3.relative(direction, d0).relative(Direction.UP, d1);
		Direction.Axis direction$axis = p_direction.getAxis();
		double d2 = direction$axis == Direction.Axis.X ? 0.0625 : (double) paintingType.width();
		double d3 = (double) paintingType.height();
		double d4 = direction$axis == Direction.Axis.Z ? 0.0625 : (double) paintingType.width();
		return AABB.ofSize(vec31, d2, d3, d4);
	}

	private double offsetForPaintingSize(int size) {
		return size % 2 == 0 ? 0.5D : 0.0D;
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
			BlockPos blockpos = this.pos.relative(this.getDirection().getOpposite());
			Direction direction = this.getDirection().getCounterClockWise();
			BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

			for (int k = 0; k < i; ++k) {
				for (int l = 0; l < j; ++l) {
					int i1 = (i - 1) / -2;
					int j1 = (j - 1) / -2;
					blockpos$mutable.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
					BlockState blockstate = this.level().getBlockState(blockpos$mutable);
					if (net.minecraft.world.level.block.Block.canSupportCenter(this.level(), blockpos$mutable, this.getDirection()))
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