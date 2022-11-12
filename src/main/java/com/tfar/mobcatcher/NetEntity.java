package com.tfar.mobcatcher;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;

import static com.tfar.mobcatcher.NetItem.containsEntity;

public class NetEntity extends ProjectileItemEntity {

  public ItemStack stack = ItemStack.EMPTY;

  public NetEntity(EntityType<? extends ProjectileItemEntity> entityType, World world) {
    super(entityType, world);
  }

  public NetEntity(double x, double y, double z, World world, ItemStack newStack) {
    super(MobCatcher.net, x, y, z, world);
    this.stack = newStack;
  }

  @Nonnull
  @Override
  protected Item getDefaultItem() {
    return MobCatcher.net_item;
  }

  /**
   * Called when this EntityThrowable hits a block or entity.
   *
   * @param result
   */
  @Override
  protected void onHit(@Nonnull RayTraceResult result) {
    if (level.isClientSide || !this.isAlive()) return;
    RayTraceResult.Type type = result.getType();
    boolean containsEntity = containsEntity(stack);
    if (containsEntity) {
      Entity entity = NetItem.getEntityFromStack(stack, level, true);
      BlockPos pos;
      if (type == RayTraceResult.Type.ENTITY)
        pos = ((EntityRayTraceResult) result).getEntity().blockPosition();
      else
        pos = ((BlockRayTraceResult) result).getBlockPos();
      entity.absMoveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
      stack.removeTagKey(NetItem.KEY);
      level.addFreshEntity(entity);
      ItemEntity emptynet = createDroppedItemAtEntity(this,stack.copy());
      level.addFreshEntity(emptynet);
      if (stack.isDamageableItem()) {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity) {
          stack.hurtAndBreak(1, (LivingEntity)owner, playerEntity -> {
          });
        }
      }
    } else {
      if (type == RayTraceResult.Type.ENTITY) {
        EntityRayTraceResult entityRayTrace = (EntityRayTraceResult) result;
        Entity target = entityRayTrace.getEntity();
        if (!target.isAlive() || NetItem.isBlacklisted(target.getType())) return;

        CompoundNBT nbt = NetItem.getNBTfromEntity(target);
        ItemStack newStack = stack.copy();
        newStack.getOrCreateTag().put(NetItem.KEY,nbt);
        ItemEntity itemEntity = createDroppedItemAtEntity(target,newStack);
        level.addFreshEntity(itemEntity);
        target.remove();
      } else {
        ItemEntity emptynet = createDroppedItemAtEntity(this,stack.copy());
        level.addFreshEntity(emptynet);
      }
    }
    this.remove();
  }

  protected ItemEntity createDroppedItemAtEntity(Entity entity,ItemStack stack) {
    return new ItemEntity(this.level, entity.getX(), entity.getY(), entity.getZ(), stack);
  }

  public void addAdditionalSaveData(CompoundNBT nbt) {
    super.addAdditionalSaveData(nbt);
    if (!stack.isEmpty()) {
      nbt.put("mobcatcher", stack.save(stack.getOrCreateTag()));
    }

  }

  public void readAdditionalSaveData(CompoundNBT nbt) {
    super.readAdditionalSaveData(nbt);
    stack = ItemStack.of(nbt.getCompound("mobcatcher"));
  }

  @Nonnull
  @Override
  public IPacket<?> getAddEntityPacket() {
    return NetworkHooks.getEntitySpawningPacket(this);
  }
}