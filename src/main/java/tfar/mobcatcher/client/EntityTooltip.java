package tfar.mobcatcher.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class EntityTooltip implements TooltipComponent {

    private final CompoundTag entityTag;

    public EntityTooltip(CompoundTag entityTag) {
        this.entityTag = entityTag;
    }

    public CompoundTag getEntityTag() {
        return entityTag;
    }

}