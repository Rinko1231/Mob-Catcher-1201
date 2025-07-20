package tfar.mobcatcher.realize;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.NotNull;
import tfar.mobcatcher.config.ServerConfig;
import tfar.mobcatcher.init.ModDataComponents;
import tfar.mobcatcher.init.ModEntities;
import tfar.mobcatcher.init.ModItems;

import javax.annotation.Nonnull;


public class NetEntity extends ThrowableItemProjectile {

    public ItemStack stack;

    public NetEntity(EntityType<? extends NetEntity> type, Level world) {
        super(type, world);
        this.stack = new ItemStack(ModItems.NET.get()); // 确保有默认ItemStack
    }

    public NetEntity(Level world, LivingEntity shooter, ItemStack newStack) {
        super(ModEntities.NET.get(), shooter, world);
        this.stack = getValidStack(newStack);
    }

    public NetEntity(double x, double y, double z, Level world, ItemStack newStack) {
        super(ModEntities.NET.get(), x, y, z, world);
        this.stack = getValidStack(newStack);
    }

    private ItemStack getValidStack(ItemStack inputStack) {
        return inputStack.isEmpty() ? new ItemStack(ModItems.NET.get()) : inputStack.copy();
    }


    public static boolean containsEntity(ItemStack stack) {
        return NetItem.containsEntity(stack);
    }

    @Nonnull
    @Override
    protected Item getDefaultItem() {
        return ModItems.NET.get();
    }

    @Override
    public @NotNull ItemStack getItem() {
        if (this.stack.isEmpty()) {
            return new ItemStack(ModItems.NET.get());
        }
        return this.stack;
    }


    @Override
    protected void onHit(@Nonnull HitResult result) {
        if (level().isClientSide || !this.isAlive()) return;
        HitResult.Type type = result.getType();
        boolean containsEntity = containsEntity(stack);
        if (containsEntity) {
            Entity entity = NetItem.getEntityFromStack(stack, level(), true);
            BlockPos pos;
            if (type == HitResult.Type.ENTITY) pos = ((EntityHitResult) result).getEntity().blockPosition();
            else pos = ((BlockHitResult) result).getBlockPos();
            entity.absMoveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
            stack.remove(ModDataComponents.ENTITY_HOLDER.get());
            level().addFreshEntity(entity);
            ItemEntity emptynet = createDroppedItemAtEntity(this, stack.copy());
            level().addFreshEntity(emptynet);


        } else {
            if (type == HitResult.Type.ENTITY) {
                EntityHitResult entityRayTrace = (EntityHitResult) result;
                Entity target = entityRayTrace.getEntity();
                String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
                // 检查该实体是否在黑名单中
                if (!(target instanceof LivingEntity) || !target.isAlive() || NetItem.isBlacklisted(target.getType()) || ServerConfig.entityBlacklist.get().contains(entityId))
                    return;
                CompoundTag nbt = NetItem.getNBTfromEntity(target);
                ItemStack newStack = this.stack.copyWithCount(1);
                newStack.set(ModDataComponents.ENTITY_HOLDER.get(), nbt);
                ItemEntity itemEntity = createDroppedItemAtEntity(target, newStack);
                level().addFreshEntity(itemEntity);
                target.discard();
            } else {
                ItemEntity emptynet = createDroppedItemAtEntity(this, stack.copyWithCount(1));
                level().addFreshEntity(emptynet);
            }
        }
        this.discard();
    }

    protected ItemEntity createDroppedItemAtEntity(Entity entity, ItemStack stack) {
        return new ItemEntity(this.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (!this.stack.isEmpty()) nbt.put("mobcatcher", this.stack.save(level().registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("mobcatcher", Tag.TAG_COMPOUND)) {
            this.stack = ItemStack.parse(level().registryAccess(), nbt.getCompound("mobcatcher")).orElse(new ItemStack(ModItems.NET.get()));
        } else {
            this.stack = new ItemStack(ModItems.NET.get());
        }
    }
}
