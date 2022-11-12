package tfar.mobcatcher.datagen.providers.data.tags;

import tfar.mobcatcher.MobCatcher;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, MobCatcher.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(MobCatcher.blacklisted).add(EntityType.PAINTING);
    }
}
