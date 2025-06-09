package tfar.mobcatcher.datagen;

import net.minecraftforge.data.event.GatherDataEvent;
import tfar.mobcatcher.datagen.providers.data.tags.ModBlockTagsProvider;
import tfar.mobcatcher.datagen.providers.data.tags.ModEntityTypeTagsProvider;
import tfar.mobcatcher.datagen.providers.data.tags.ModItemTagsProvider;
import tfar.mobcatcher.datagen.providers.ModRecipeProvider;
import tfar.mobcatcher.datagen.providers.assets.ModItemModelProvider;
import tfar.mobcatcher.datagen.providers.assets.ModLangProvider;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModDatagen {
    public static void start(GatherDataEvent e) {
        DataGenerator dataGenerator = e.getGenerator();
        ExistingFileHelper helper = e.getExistingFileHelper();
        boolean client = e.includeClient();
        boolean server = e.includeServer();
        dataGenerator.addProvider(client, new ModItemModelProvider(dataGenerator, helper));
        dataGenerator.addProvider(client, new ModLangProvider(dataGenerator));

        dataGenerator.addProvider(server, new ModRecipeProvider(dataGenerator));
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(dataGenerator, helper);
        dataGenerator.addProvider(server, blockTagsProvider);
        dataGenerator.addProvider(server, new ModItemTagsProvider(dataGenerator, blockTagsProvider, helper));
        dataGenerator.addProvider(server, new ModEntityTypeTagsProvider(dataGenerator, helper));
    }
}
