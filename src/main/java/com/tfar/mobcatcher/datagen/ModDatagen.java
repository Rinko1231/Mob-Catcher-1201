package com.tfar.mobcatcher.datagen;

import com.tfar.mobcatcher.datagen.providers.data.tags.ModBlockTagsProvider;
import com.tfar.mobcatcher.datagen.providers.data.tags.ModEntityTypeTagsProvider;
import com.tfar.mobcatcher.datagen.providers.data.tags.ModItemTagsProvider;
import com.tfar.mobcatcher.datagen.providers.ModRecipeProvider;
import com.tfar.mobcatcher.datagen.providers.assets.ModItemModelProvider;
import com.tfar.mobcatcher.datagen.providers.assets.ModLangProvider;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class ModDatagen {
    public static void start(GatherDataEvent e) {
        DataGenerator dataGenerator = e.getGenerator();
        ExistingFileHelper helper = e.getExistingFileHelper();
        boolean client = e.includeClient();
        boolean server = e.includeServer();
        if (client) {
            dataGenerator.addProvider(new ModItemModelProvider(dataGenerator, helper));
            dataGenerator.addProvider(new ModLangProvider(dataGenerator));
        }

        if (server) {
            dataGenerator.addProvider(new ModRecipeProvider(dataGenerator));
            BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(dataGenerator, helper);
            dataGenerator.addProvider(blockTagsProvider);
            dataGenerator.addProvider(new ModItemTagsProvider(dataGenerator, blockTagsProvider, helper));
            dataGenerator.addProvider(new ModEntityTypeTagsProvider(dataGenerator,helper));
        }
    }
}
