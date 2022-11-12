package com.tfar.mobcatcher.datagen.providers.data.tags;

import com.tfar.mobcatcher.MobCatcher;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;


import javax.annotation.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, MobCatcher.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
    }
}
