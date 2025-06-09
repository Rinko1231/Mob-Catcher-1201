package tfar.mobcatcher.datagen.providers.data.tags;

import tfar.mobcatcher.MobCatcher;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;

import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator pGenerator, BlockTagsProvider pBlockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, pBlockTagsProvider, MobCatcher.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}
