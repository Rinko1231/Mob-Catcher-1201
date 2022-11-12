package tfar.mobcatcher;

import tfar.mobcatcher.datagen.ModDatagen;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

@Mod(value = MobCatcher.MODID)
public class MobCatcher {
  public static final String MODID = "mobcatcher";

  public static final TagKey<EntityType<?>> blacklisted = create(new ResourceLocation(MobCatcher.MODID,"blacklisted").toString());

  private static TagKey<EntityType<?>> create(String pName) {
    return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(pName));
  }
  public MobCatcher() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(ModDatagen::start);
    bus.addGenericListener(Item.class,this::registerItems);
    bus.addGenericListener(EntityType.class,this::registerEntity);
    bus.addListener(this::init);
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

  public static final ServerConfig SERVER;
  public static final ForgeConfigSpec SERVER_SPEC;

  static {
    final Pair<ServerConfig, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
    SERVER_SPEC = specPair2.getRight();
    SERVER = specPair2.getLeft();
  }

    public void registerItems(RegistryEvent.Register<Item> e) {
      IForgeRegistry<Item> registry = e.getRegistry();
      registerItem(Objs.net_item, "net", registry);
      registerItem(Objs.net_launcher, "net_launcher", registry);
    }

    private static void registerItem(Item item, String name, IForgeRegistry<Item> registry) {
      registry.register(item.setRegistryName(name));
    }

    public void registerEntity(RegistryEvent.Register<EntityType<?>> e) {
      e.getRegistry().register(Objs.net.setRegistryName("net"));
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
