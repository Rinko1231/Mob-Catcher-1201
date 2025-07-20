package tfar.mobcatcher.init;

import net.minecraft.world.level.block.DispenserBlock;
import tfar.mobcatcher.realize.NetDispenserBehavior;

public class ModBehaviors {
    public static void registerDispenserBehaviors() {
        DispenserBlock.registerBehavior(ModItems.NET.get(), new NetDispenserBehavior());
    }
} 