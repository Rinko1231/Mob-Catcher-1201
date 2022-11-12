package com.tfar.mobcatcher.datagen.providers.assets;

import com.tfar.mobcatcher.MobCatcher;
import net.minecraft.data.DataGenerator;

import net.minecraft.item.Item;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(DataGenerator generator,  ExistingFileHelper existingFileHelper) {
        super(generator, MobCatcher.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }


    protected void makeSimpleBlockItem(Item item, ResourceLocation loc) {
        getBuilder(Registry.ITEM.getKey(item).toString())
                .parent(getExistingFile(loc));
    }

    protected void makeSimpleBlockItem(Item item) {
        makeSimpleBlockItem(item, new ResourceLocation(MobCatcher.MODID, "block/" + Registry.ITEM.getKey(item).getPath()));
    }


    protected void makeOneLayerItem(Item item, ResourceLocation texture) {
        String path = Registry.ITEM.getKey(item).getPath();
        if (existingFileHelper.exists(new ResourceLocation(texture.getNamespace(), "item/" + texture.getPath())
                , ResourcePackType.CLIENT_RESOURCES, ".png", "textures")) {
            getBuilder(path).parent(getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", new ResourceLocation(texture.getNamespace(), "item/" + texture.getPath()));
        } else {
            System.out.println("no texture for " + item + " found, skipping");
        }
    }

    protected void makeOneLayerItem(Item item) {
        ResourceLocation texture = Registry.ITEM.getKey(item);
        makeOneLayerItem(item, texture);
    }

    //wood_to_iron_frame_upgrade
    protected void registerUpgrade(Item item) {
        String name = Registry.ITEM.getKey(item).getPath();
        registerUpgrade(name);
    }
    protected void registerUpgrade(String name) {
        String[] strings = name.split("_");
        getBuilder(name)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0","item/frame_upgrade/from_"+strings[0])
                .texture("layer1","item/frame_upgrade/to_"+strings[2]);
    }

}
