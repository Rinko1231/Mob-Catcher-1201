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
  protected void onImpact(@Nonnull RayTraceResult result) {
    if (world.isRemote || !this.isAlive()) return;
    RayTraceResult.Type type = result.getType();
    boolean containsEntity = containsEntity(stack);
    if (containsEntity) {
      Entity entity = NetItem.getEntityFromStack(stack, world, true);
      BlockPos pos;
      if (type == RayTraceResult.Type.ENTITY)
        pos = ((EntityRayTraceResult) result).getEntity().getPosition();
      else
        pos = ((BlockRayTraceResult) result).getPos();
      entity.setPositionAndRotation(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
      stack.removeChildTag(NetItem.KEY);
      world.addEntity(entity);
      ItemEntity emptynet = createDroppedItemAtEntity(this,stack.copy());
      world.addEntity(emptynet);
      if (stack.isDamageable()) {
        Entity owner = this.func_234616_v_();
        if (owner instanceof LivingEntity) {
          stack.damageItem(1, (LivingEntity)owner, playerEntity -> {
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
        world.addEntity(itemEntity);
        target.remove();
      } else {
        ItemEntity emptynet = createDroppedItemAtEntity(this,stack.copy());
        world.addEntity(emptynet);
      }
    }
    this.remove();
  }

  protected ItemEntity createDroppedItemAtEntity(Entity entity,ItemStack stack) {
    return new ItemEntity(this.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), stack);
  }

  public void writeAdditional(CompoundNBT nbt) {
    super.writeAdditional(nbt);
    if (!stack.isEmpty()) {
      nbt.put("mobcatcher", stack.write(stack.getOrCreateTag()));
    }

  }

  public void readAdditional(CompoundNBT nbt) {
    super.readAdditional(nbt);
    stack = ItemStack.read(nbt.getCompound("mobcatcher"));
  }

  @Nonnull
  @Override
  public IPacket<?> createSpawnPacket() {
    return NetworkHooks.getEntitySpawningPacket(this);
  }
}