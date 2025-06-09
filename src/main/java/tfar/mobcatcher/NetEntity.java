package tfar.mobcatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class NetEntity extends ThrowableItemProjectile {

  public ItemStack stack = ItemStack.EMPTY;

  public NetEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
    super(entityType, world);
  }

  public NetEntity(double x, double y, double z, Level world, ItemStack newStack) {
    super(Objs.net, x, y, z, world);
    this.stack = newStack;
  }

  @Nonnull
  @Override
  protected Item getDefaultItem() {
    return Objs.net_item;
  }

  /**
   * Called when this EntityThrowable hits a block or entity.
   *
   * @param result
   */
  @Override
  protected void onHit(@Nonnull HitResult result) {
    if (level().isClientSide || !this.isAlive()) return;
    HitResult.Type type = result.getType();
    boolean containsEntity = NetItem.containsEntity(stack);
    if (containsEntity) {
      Entity entity = NetItem.getEntityFromStack(stack, level(), true);
      BlockPos pos;
      if (type == HitResult.Type.ENTITY)
        pos = ((EntityHitResult) result).getEntity().blockPosition();
      else
        pos = ((BlockHitResult) result).getBlockPos();
      entity.absMoveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
      stack.removeTagKey(NetItem.KEY);
      level().addFreshEntity(entity);
      ItemEntity emptynet = createDroppedItemAtEntity(this,stack.copy());
      level().addFreshEntity(emptynet);
      if (stack.isDamageableItem()) {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity) {
          stack.hurtAndBreak(1, (LivingEntity)owner, playerEntity -> {
          });
        }
      }
    } else {
      if (type == HitResult.Type.ENTITY) {
        EntityHitResult entityRayTrace = (EntityHitResult) result;
        Entity target = entityRayTrace.getEntity();

        String entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString();
        // 检查该实体是否在黑名单中
        if (!(target instanceof LivingEntity) || !target.isAlive() || NetItem.isBlacklisted(target.getType()) || MobCatcher.ServerConfig.entityBlacklist.get().contains(entityId)) return;

        CompoundTag nbt = NetItem.getNBTfromEntity(target);
        ItemStack newStack = stack.copy();
        newStack.getOrCreateTag().put(NetItem.KEY,nbt);
        ItemEntity itemEntity = createDroppedItemAtEntity(target,newStack);
        level().addFreshEntity(itemEntity);
        target.discard();
      } else {
        ItemEntity emptynet = createDroppedItemAtEntity(this,stack.copy());
        level().addFreshEntity(emptynet);
      }
    }
    this.discard();
  }

  protected ItemEntity createDroppedItemAtEntity(Entity entity,ItemStack stack) {
    return new ItemEntity(this.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
  }

  public void addAdditionalSaveData(CompoundTag nbt) {
    super.addAdditionalSaveData(nbt);
    if (!stack.isEmpty()) {
      nbt.put(MobCatcher.MODID, stack.save(stack.getOrCreateTag()));
    }

  }

  public void readAdditionalSaveData(CompoundTag nbt) {
    super.readAdditionalSaveData(nbt);
    stack = ItemStack.of(nbt.getCompound(MobCatcher.MODID));
  }

 // @Nonnull
 // @Override
 // //public Packet<?> getAddEntityPacket() {
 //   return NetworkHooks.getEntitySpawningPacket(this);
  //}
}