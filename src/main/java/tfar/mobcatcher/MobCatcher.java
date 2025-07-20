package tfar.mobcatcher;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import tfar.mobcatcher.config.ServerConfig;
import tfar.mobcatcher.init.ModDataComponents;
import tfar.mobcatcher.init.ModEntities;
import tfar.mobcatcher.init.ModItems;

@Mod(value = MobCatcher.MODID)
public class MobCatcher {
    public static final String MODID = "mobcatcher";
    public static final String MOD_ID = "mobcatcher";

    public static final TagKey<EntityType<?>> blacklisted = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("mobcatcher", "blacklisted"));

    public MobCatcher(IEventBus bus, ModContainer modContainer) {
        ModDataComponents.DATA_COMPONENT_TYPES.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);

        bus.addListener(this::addItemsToTabs);
        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SPEC, "MobCatcher.toml");
    }


    private void addItemsToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.NET.get());
            event.accept(ModItems.NET_LAUNCHER.get());
        }
    }


    //public void init(FMLCommonSetupEvent event) {
    // DispenserBlock.registerBehavior(ModEntities.NET, new DispenseItemBehavior() {
    /**
     * Return the projectile entity spawned by this dispense behavior.
     */
    // @Nonnull
    //@Override
    //protected Projectile getProjectile(@Nonnull Level world, @Nonnull Position pos, @Nonnull ItemStack stack) {
    //ItemStack newStack = stack.copy();
    //newStack.setCount(1);
    //return new NetEntity(pos.x(), pos.y(), pos.z(), world, newStack);
    // }
    //});
    // }


}
