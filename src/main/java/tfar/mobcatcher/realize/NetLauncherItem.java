package tfar.mobcatcher.realize;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tfar.mobcatcher.init.ModDataComponents;
import tfar.mobcatcher.init.ModItems;

import javax.annotation.Nonnull;

public class NetLauncherItem extends Item {

    public static final Component CAPTURE = Component.translatable("mobcatcher.capturing");
    public static final Component RELEASE = Component.translatable("mobcatcher.releasing");

    public NetLauncherItem(Properties properties) {
        super(properties);
    }

    /**
     * Gets the velocity of the net entity from the launcher's charge
     */
    public static float getNetVelocity(int charge) {
        float f = (float) charge / 20;
        f = (f * f + f * 2) / 3;
        f = Math.min(f, 1.5f);
        return f;
    }

    //helpers
    public static boolean isCaptureMode(ItemStack stack) {
        // 使用 .get() 获取 CAPTURE_MODE 对应的 Boolean 值
        Boolean value = stack.get(ModDataComponents.CAPTURE_MODE.get());

        // 如果为 null，默认返回 false，否则返回实际值
        return value != null && value;
    }

    public static boolean isEmptyNet(ItemStack stack) {
        return stack.getItem() instanceof NetItem && !NetItem.containsEntity(stack);
    }

    public static boolean isFilledNet(ItemStack stack) {
        return stack.getItem() instanceof NetItem && NetItem.containsEntity(stack);
    }

    public static boolean getCaptureMode(ItemStack stack) {
        Boolean value = stack.get(ModDataComponents.CAPTURE_MODE.get());
        return value != null && value;
    }

    protected ItemStack findNet(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (isCaptureMode(stack)) {
            if (isEmptyNet(player.getItemInHand(InteractionHand.OFF_HAND))) {
                return player.getItemInHand(InteractionHand.OFF_HAND);
            } else if (isEmptyNet(player.getItemInHand(InteractionHand.MAIN_HAND))) {
                return player.getItemInHand(InteractionHand.MAIN_HAND);
            } else {
                for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                    ItemStack itemstack = player.getInventory().getItem(i);

                    if (isEmptyNet(itemstack)) {
                        return itemstack;
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        if (isFilledNet(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return player.getItemInHand(InteractionHand.OFF_HAND);
        } else if (isFilledNet(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            return player.getItemInHand(InteractionHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack itemstack = player.getInventory().getItem(i);

                if (isFilledNet(itemstack)) {
                    return itemstack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;

        // 找玩家物品栏里符合当前模式的捕捉网
        ItemStack netStack = findNet(player);
        if (netStack.isEmpty()) return;

        int useTime = getUseDuration(netStack, player) - timeLeft;
        if (useTime < 0) return;

        float velocity = getNetVelocity(useTime);
        if (velocity < 0.1D) return;

        if (!worldIn.isClientSide) {
            // 传递网的副本，避免影响玩家物品栏的原始ItemStack
            ItemStack netStackCopy = netStack.copy();
            // 确保副本不为空
            if (netStackCopy.isEmpty()) {
                netStackCopy = new ItemStack(ModItems.NET.get());
            }

            NetEntity netEntity = new NetEntity(worldIn, player, netStackCopy);
            netEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity * 1.5F, 0.0F);
            worldIn.addFreshEntity(netEntity);

            // 调试输出
            System.out.println("NetEntity created and added to world: " + netEntity + " with stack: " + netEntity.getItem());

            if (!player.getAbilities().instabuild) {
                // 减少玩家持有的捕捉网数量
                netStack.shrink(1);
                if (netStack.isEmpty()) {
                    player.getInventory().removeItem(netStack);
                }
            }
        }

        worldIn.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    /**
     * Called when the equipped item is right clicked.
     */
    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            boolean current = isCaptureMode(stack); // 获取当前状态
            stack.set(ModDataComponents.CAPTURE_MODE.get(), !current);
            player.displayClientMessage(current ? RELEASE : CAPTURE, true);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        boolean hasAmmo = !this.findNet(player).isEmpty();

        if (!player.getAbilities().instabuild && !hasAmmo) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        } else {
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        MutableComponent base = (MutableComponent) super.getName(stack);
        return base.append(" (").append(isCaptureMode(stack) ? CAPTURE : RELEASE).append(")");
    }

}
