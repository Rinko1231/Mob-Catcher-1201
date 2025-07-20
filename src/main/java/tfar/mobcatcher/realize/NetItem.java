package tfar.mobcatcher.realize;

import com.mojang.logging.LogUtils;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import tfar.mobcatcher.MobCatcher;
import tfar.mobcatcher.config.ServerConfig;
import tfar.mobcatcher.init.ModDataComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NetItem extends Item implements ProjectileItem {

    public static final String KEY = "entity_holder";
    private static final Logger LOGGER = LogUtils.getLogger();
    static Set<String> warned;

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        NetEntity entity = new NetEntity(pos.x(), pos.y(), pos.z(), level, stack.copyWithCount(1));
        return entity;
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        // 默认即可，除非你想调节速度或偏移量
        return ProjectileItem.DispenseConfig.DEFAULT;
    }

    public NetItem(Properties properties) {
        super(properties);
    }

    public static RegistryAccess registryAccess() {
        if (ServerLifecycleHooks.getCurrentServer() != null)
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
        return LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).orElseThrow().registryAccess();
    }

    public static Component getNameFromStoredEntity(ItemStack stack) {
        CompoundTag holder = getEntityData(stack);
        if (holder.contains("CustomName", Tag.TAG_STRING)) {
            String s = holder.getString("CustomName");
            try {
                return Component.Serializer.fromJson(s, registryAccess());
            } catch (Exception exception) {
                if (!warned.contains(s)) {
                    LOGGER.warn("Failed to parse entity custom name {}", s, exception);
                    warned.add(s);
                }
            }
        }
        String id = holder.getString("id");
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
        return type.getDescription();
    }

  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    super.appendHoverText(stack, context, tooltip, flagIn);
    if (containsEntity(stack)) {
      CompoundTag holder = getEntityData(stack);
      String id = holder.getString("id");
        EntityType<?> type = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
      tooltip.add(type.getDescription());
      tooltip.add(Component.translatable("mobcatcher.health").append(": "+ getEntityData(stack).getDouble("Health")));
    }
  }

    public static boolean containsEntity(ItemStack stack) {
        CompoundTag tag = stack.get(ModDataComponents.ENTITY_HOLDER.get());
        return tag != null && !tag.isEmpty();
    }

    public static CompoundTag getEntityData(ItemStack stack) {
        return containsEntity(stack) ? stack.get(ModDataComponents.ENTITY_HOLDER.get()) : new CompoundTag();
    }

    public static String getEntityID(CompoundTag nbt) {
        return nbt.getString("id");
    }

    public static boolean isBlacklisted(EntityType<?> type) {
        return type == EntityType.PLAYER || type.is(MobCatcher.blacklisted);
    }

    //helper methods

    public static Entity getEntityFromNBT(CompoundTag nbt, Level world, boolean withInfo) {
        Entity entity = ((EntityType) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(getEntityID(nbt)))).create(world);
        if (withInfo) entity.load(nbt);
        return entity;
    }

    public static Entity getEntityFromStack(ItemStack stack, Level world, boolean withInfo) {
        return getEntityFromNBT(stack.get(ModDataComponents.ENTITY_HOLDER.get()), world, withInfo);
    }

    public static CompoundTag getNBTfromEntity(Entity entity) {
        CompoundTag nbt = new CompoundTag();
        entity.save(nbt);
        return nbt;
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null) return InteractionResult.FAIL;
        ItemStack stack = context.getItemInHand();
        if (world.isClientSide || !containsEntity(stack)) return InteractionResult.FAIL;
        Entity entity = getEntityFromStack(stack, world, true);
        BlockPos blockPos = context.getClickedPos();
        entity.absMoveTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 0, 0);
        stack.remove(ModDataComponents.ENTITY_HOLDER.get());
        world.addFreshEntity(entity);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target.getCommandSenderWorld().isClientSide || target instanceof Player || !target.isAlive() || containsEntity(stack))
            return InteractionResult.FAIL;

        EntityType<?> entityID = target.getType();
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityID).toString();
        if (isBlacklisted(entityID) || ServerConfig.entityBlacklist.get().contains(entityId))
            return InteractionResult.FAIL;


        CompoundTag nbt = getNBTfromEntity(target);
        if (stack.getCount() == 1) {
            if (player.isCreative()) {
                // 创造模式：生成一个新的 Stack 并替换玩家手中的物品
                ItemStack newStack = stack.copy();
                newStack.set(ModDataComponents.ENTITY_HOLDER.get(), nbt);
                player.setItemInHand(hand, newStack);
            } else {
                // 生存模式：直接写入原来的 stack
                stack.set(ModDataComponents.ENTITY_HOLDER.get(), nbt);
            }
        } else {
            // 多个网：分出一个并放到玩家物品栏
            ItemStack newStack = stack.split(1);
            newStack.set(ModDataComponents.ENTITY_HOLDER.get(), nbt);
            if (!player.addItem(newStack)) {
                ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), newStack);
                player.level().addFreshEntity(itemEntity);
            }
        }

        player.swing(hand);
        target.discard();
        player.getCooldowns().addCooldown(this, 5);
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        Component nameComponent = stack.get(DataComponents.CUSTOM_NAME);
        Component baseName;
        if (nameComponent != null) {
            baseName = nameComponent;
        } else {
            baseName = super.getName(stack);
        }

        if (!containsEntity(stack)) {
            return baseName;
        } else {
            return Component.translatable("item.mobcatcher.net.with_entity", baseName, getNameFromStoredEntity(stack));
        }
    }

    public NetEntity createNet(Level worldIn, LivingEntity shooter, ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        return new NetEntity(shooter.getX(), shooter.getY() + 1.25, shooter.getZ(), worldIn, newStack);
    }
}
