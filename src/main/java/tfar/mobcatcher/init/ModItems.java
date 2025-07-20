package tfar.mobcatcher.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tfar.mobcatcher.realize.NetItem;
import tfar.mobcatcher.realize.NetLauncherItem;

import static tfar.mobcatcher.MobCatcher.MOD_ID;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredHolder<Item, Item> NET_LAUNCHER = ITEMS.register("net_launcher", () -> new NetLauncherItem(new Item.Properties().stacksTo(1)));
    public static int netStackSize = 64;
    public static final DeferredHolder<Item, Item> NET = ITEMS.register("net", () -> new NetItem(new Item.Properties().stacksTo(netStackSize)));
}
