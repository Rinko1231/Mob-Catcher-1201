package tfar.mobcatcher;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import tfar.mobcatcher.client.EntityTooltip;
import tfar.mobcatcher.client.EntityTooltipComponent;
import tfar.mobcatcher.init.ModEntities;

@EventBusSubscriber(modid = MobCatcher.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NET.get(), ThrownItemRenderer::new);
    }
    
    @SubscribeEvent
    public static void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(EntityTooltip.class, entityTooltip -> new EntityTooltipComponent(entityTooltip.getEntityTag()));
    }
}
