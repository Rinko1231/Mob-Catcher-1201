package com.tfar.mobcatcher;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class NetItem extends Item {

  public static final String KEY = "entity_holder";


  public NetItem(Properties properties) {
    super(properties);
  }

  @Override
  @Nonnull
  public ActionResultType useOn(ItemUseContext context) {
    PlayerEntity player = context.getPlayer();
    World world = context.getLevel();
    if (player == null)return ActionResultType.FAIL;
    ItemStack stack = context.getItemInHand();
    if (world.isClientSide || !containsEntity(stack)) return ActionResultType.FAIL;
    Entity entity = getEntityFromStack(stack, world, true);
    BlockPos blockPos = context.getClickedPos();
    entity.absMoveTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 0, 0);
    stack.setTag(null);
    world.addFreshEntity(entity);
    if (this.canBeDepleted()) {
      stack.hurtAndBreak(1,player,playerEntity -> playerEntity.broadcastBreakEvent(context.getHand()));
    }
    return ActionResultType.SUCCESS;
  }

  @Override
  public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
    if (target.getCommandSenderWorld().isClientSide || target instanceof PlayerEntity || !target.isAlive() || containsEntity(stack))
      return ActionResultType.FAIL;
    EntityType<?> entityID = target.getType();
    if (isBlacklisted(entityID)) return ActionResultType.FAIL;
    ItemStack newStack = stack.copy();
    CompoundNBT nbt = getNBTfromEntity(target);
    ItemStack newerStack = newStack.split(1);
    newerStack.getOrCreateTag().put(KEY,nbt);
    player.swing(hand);
    player.setItemInHand(hand, newStack);
    if(!player.addItem(newerStack)){
      ItemEntity itemEntity = new ItemEntity(player.level,player.getX(),player.getY(),player.getZ(),newerStack);
      player.level.addFreshEntity(itemEntity);
    }
    target.remove();
    player.getCooldowns().addCooldown(this, 5);
    return ActionResultType.SUCCESS;
  }


  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    super.appendHoverText(stack, worldIn, tooltip, flagIn);

   // tooltip.add(new StringTextComponent(stack.getOrCreateTag().toString()));
    if (containsEntity(stack))
      if (!getEntityID(stack).isEmpty()) {
        String s0 = "entity." + getEntityID(stack);
        String s1 = s0.replace(':','.');
        tooltip.add(new StringTextComponent(I18n.get(s1)));
        tooltip.add(new StringTextComponent("Health: " + stack.getTag().getCompound(KEY).getDouble("Health")));
      }
  }

  @Override
  @Nonnull
  public ITextComponent getName(@Nonnull ItemStack stack) {
    if (!containsEntity(stack))
      return super.getName(stack);
    else {
      String s0 = "entity." + getEntityID(stack);
      String s1 = s0.replace(':', '.');
      return ((TranslationTextComponent)super.getName(stack))
              .append(" (")
              .append(new TranslationTextComponent(s1))
              .append(")")
      ;
    }
  }

  public NetEntity createNet(World worldIn, LivingEntity shooter, ItemStack stack)
  {
    ItemStack newStack = stack.copy();
    newStack.setCount(1);
    return new NetEntity(shooter.getX(), shooter.getY() + 1.25, shooter.getZ(), worldIn, newStack);
  }

  //helper methods

  public static boolean containsEntity(@Nonnull ItemStack stack) {
    return stack.hasTag() && stack.getTag().contains(KEY);
  }

  public static String getEntityID(ItemStack stack) {
    return getEntityID(stack.getTag().getCompound(KEY));
  }

  public static String getEntityID(CompoundNBT nbt) {
    return nbt.getString("entity");
  }

  public static boolean isBlacklisted(EntityType<?> type) {
    return type == EntityType.PLAYER || MobCatcher.blacklisted.contains(type);
  }

  public static Entity getEntityFromNBT(CompoundNBT nbt, World world, boolean withInfo) {
    Entity entity = Registry.ENTITY_TYPE.get(new ResourceLocation(getEntityID(nbt))).create(world);
    if (withInfo) entity.load(nbt);
    return entity;
  }

  public static Entity getEntityFromStack(ItemStack stack, World world, boolean withInfo) {
    return getEntityFromNBT(stack.getOrCreateTag().getCompound(KEY),world,withInfo);
  }

  public static CompoundNBT getNBTfromEntity(Entity entity) {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putString("entity", entity.getType().getRegistryName().toString());
    entity.save(nbt);
    return nbt;
  }
}
