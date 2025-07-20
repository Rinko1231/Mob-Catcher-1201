package tfar.mobcatcher.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EntityTooltipComponent implements ClientTooltipComponent {
    
    private final CompoundTag entityTag;
    
    public EntityTooltipComponent(CompoundTag entityTag) {
        this.entityTag = entityTag;
    }
    
    @Override
    public int getHeight() {
        return 90;
    }
    
    @Override
    public int getWidth(@NotNull Font font) {
        return 120;
    }
    
    @Override
    public void renderImage(Font font, int pX, int pY, GuiGraphics guiGraphics) {
        String id = this.entityTag.getString("id");
        EntityType<?> type = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
        Component entityName = type.getDescription();
        double health = this.entityTag.getDouble("Health");
        guiGraphics.drawString(font, entityName, pX, pY, 0xFFFFFF);
        Component healthText = Component.translatable("mobcatcher.health").append(": " + String.format("%.1f", health));
        guiGraphics.drawString(font, healthText, pX, pY + 10, 0xAAAAFF);
        int width = getWidth(font);
        int posX = pX + width / 2;
        int posY = pY + 80;
        double rot = System.currentTimeMillis() / 25.0D % 360.0D;
        Quaternionf pose = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf rotation = (new Quaternionf()).rotateY((float)Math.toRadians(rot));
        pose.mul((Quaternionfc)rotation);
        guiGraphics.enableScissor(pX, posY - 50, pX + width, posY + 10);
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return; 
        LivingEntity livingEntity = (LivingEntity)type.create((Level)clientLevel);
        if (livingEntity != null) {
            livingEntity.setOnGround(true);
            livingEntity.load(this.entityTag);
            InventoryScreen.renderEntityInInventory(guiGraphics, posX, posY, 25.0F, new Vector3f(), pose, null, livingEntity);
            guiGraphics.disableScissor();
        } 
    }
} 