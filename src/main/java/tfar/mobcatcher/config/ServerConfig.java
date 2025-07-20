package tfar.mobcatcher.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<List<? extends String>> entityBlacklist;

    static {
        BUILDER.push("Config");
        entityBlacklist = BUILDER.comment("Living Entity Blacklist").comment("Entities that will not be captured, you can also use tag").defineList("Entity Blacklist", List.of("corpse:corpse", "minecraft:wither", "minecraft:ender_dragon"), element -> element instanceof String);
        SPEC = BUILDER.build();
    }
}