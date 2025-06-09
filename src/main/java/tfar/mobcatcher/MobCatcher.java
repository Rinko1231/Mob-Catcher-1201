package tfar.mobcatcher;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

@Mod(value = MobCatcher.MODID)
public class MobCatcher {
  public static final String MODID = "mobcatcher";

  public static final TagKey<EntityType<?>> blacklisted = create(new ResourceLocation(MobCatcher.MODID,"blacklisted").toString());

  private static TagKey<EntityType<?>> create(String pName) {
    return TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(pName));
  }
  public MobCatcher() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::registerItems);
    bus.addListener(this::init);
    bus.addListener(this::addItemsToTabs);
    bus.addListener(this::configChange);

  }
  private void configChange(ModConfigEvent e) {
    if (e.getConfig().getModId().equals(MODID)) {
      int durability = ServerConfig.net_durability.get();
      if (durability > -1) {
        Objs.net_item.maxStackSize = 1;
        Objs.net_item.maxDamage = durability;
      }
    }
  }

  private void addItemsToTabs(BuildCreativeModeTabContentsEvent event)
  {
    if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
    {
      event.accept( Objs.net_item);
      event.accept( Objs.net_launcher);
    }
  }


  public static final ServerConfig SERVER;
  public static final ForgeConfigSpec SERVER_SPEC;

  static {
    final Pair<ServerConfig, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
    SERVER_SPEC = specPair2.getRight();
    SERVER = specPair2.getLeft();
  }

  public void registerItems(RegisterEvent e) {
    registerOb(e, ForgeRegistries.ITEMS.getRegistryKey(),"net", Objs.net_item);
    registerOb(e,ForgeRegistries.ITEMS.getRegistryKey(),"net_launcher", Objs.net_launcher);
    registerOb(e,ForgeRegistries.ENTITY_TYPES.getRegistryKey(),"net",Objs.net);
  }

    private static <T> void registerOb(RegisterEvent e, ResourceKey<? extends Registry<T>>resourceKey, String name, T obj) {
      e.register(resourceKey,new ResourceLocation(MODID,name),() ->obj);
    }

    public void init(FMLCommonSetupEvent event) {
      DispenserBlock.registerBehavior(Objs.net_item, new AbstractProjectileDispenseBehavior() {
        /**
         * Return the projectile entity spawned by this dispense behavior.
         */
        @Nonnull
        @Override
        protected Projectile getProjectile(@Nonnull Level world, @Nonnull Position pos, @Nonnull ItemStack stack) {
          ItemStack newStack = stack.copy();
          newStack.setCount(1);
          return new NetEntity(pos.x(), pos.y(), pos.z(), world, newStack);
        }
      });
    }


  public static class ServerConfig {

    public static ForgeConfigSpec.IntValue net_durability;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
      builder.push("general");
      net_durability = builder.comment("Number of uses before mob catcher breaks, damaged every time a mob is released, -1 disables durability, numbers above will set stack size to 1")
              .defineInRange("net_durability", -1, -1, Integer.MAX_VALUE);
      builder.pop();
    }
  }

}
