package tfar.mobcatcher.realize;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

public class NetDispenserBehavior extends DefaultDispenseItemBehavior {

    @Override
    public @NotNull ItemStack execute(BlockSource blockSource, ItemStack stack) {
        Level level = blockSource.level();
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        double x = blockSource.pos().getX() + direction.getStepX() * 1.1D;
        double y = blockSource.pos().getY() + direction.getStepY() * 1.1D;
        double z = blockSource.pos().getZ() + direction.getStepZ() * 1.1D;
        ItemStack netStack = stack.copy();
        netStack.setCount(1);
        NetEntity netEntity = new NetEntity(x, y, z, level, netStack);
        netEntity.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 1.1F, 6.0F);
        level.addFreshEntity(netEntity);
        stack.shrink(1);
        return stack;
    }
} 