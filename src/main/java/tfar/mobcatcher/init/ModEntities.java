package tfar.mobcatcher.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import tfar.mobcatcher.realize.NetEntity;

import java.util.function.Supplier;

import static tfar.mobcatcher.MobCatcher.MOD_ID;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);

    public static final Supplier<EntityType<NetEntity>> NET = ENTITIES.register("net", () -> EntityType.Builder.<NetEntity>of(NetEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1).setTrackingRange(128).sized(.6f, .6f).build("net"));

    private static ResourceKey<EntityType<?>> prefix(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
    }

}