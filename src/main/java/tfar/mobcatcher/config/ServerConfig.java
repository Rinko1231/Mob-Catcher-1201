package tfar.mobcatcher.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<List<? extends String>> entityBlacklist;
    public static ModConfigSpec.DoubleValue launcherVelocityMultiplier;
    public static ModConfigSpec.DoubleValue dispenserVelocity;
    public static ModConfigSpec.DoubleValue dispenserInaccuracy;
    static {
        BUILDER.push("Config");
        entityBlacklist = BUILDER.comment("Living Entity Blacklist")
                .comment("Entities that will not be captured, you can also use tag")
                .defineList("Entity Blacklist", List.of("corpse:corpse", "minecraft:wither", "minecraft:ender_dragon"),
                        element -> element instanceof String);
        launcherVelocityMultiplier = BUILDER
                .defineInRange("Velocity Multiplier of NetEntity shot by NetLauncher",1.5,0,10);
        dispenserVelocity = BUILDER
                .defineInRange("Velocity of NetEntity shot by Dispenser",1.1F,0,255);
        dispenserInaccuracy = BUILDER
                .defineInRange("Inaccuracy of NetEntity shot by Dispenser", 4F,0,10F);
        SPEC = BUILDER.build();
    }
}