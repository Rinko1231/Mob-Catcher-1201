package tfar.mobcatcher.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

import static tfar.mobcatcher.MobCatcher.MOD_ID;

public class ModDataComponents {
    //将entity_whitelist改名为normal_nbt就是模拟旧版本通用nbt了
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ENTITY_HOLDER;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CAPTURE_MODE;

    static {
        ENTITY_HOLDER = register("entity_holder", builder -> builder.persistent(CompoundTag.CODEC));
        CAPTURE_MODE = register("capture_mode", builder -> builder.persistent(Codec.BOOL));
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.<T>builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }

}