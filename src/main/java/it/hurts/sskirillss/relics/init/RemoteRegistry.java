package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.entities.renderer.ShadowGlaiveRenderer;
import it.hurts.sskirillss.relics.entities.renderer.SpaceDissectorRenderer;
import it.hurts.sskirillss.relics.entities.renderer.StellarCatalystProjectileRenderer;
import it.hurts.sskirillss.relics.items.RelicContractItem;
import it.hurts.sskirillss.relics.items.relics.InfinityHamItem;
import it.hurts.sskirillss.relics.items.relics.ShadowGlaiveItem;
import it.hurts.sskirillss.relics.items.relics.SlimeHeartItem;
import it.hurts.sskirillss.relics.items.relics.SpaceDissectorItem;
import it.hurts.sskirillss.relics.tiles.renderer.BloodyLecternTileRenderer;
import it.hurts.sskirillss.relics.tiles.renderer.PedestalTileRenderer;
import it.hurts.sskirillss.relics.tiles.renderer.RunicAltarTileRenderer;
import it.hurts.sskirillss.relics.tiles.renderer.RunicAnvilTileRenderer;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RemoteRegistry {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.STELLAR_CATALYST_PROJECTILE.get(), new StellarCatalystProjectileRenderer.RenderFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SPACE_DISSECTOR.get(), new SpaceDissectorRenderer.RenderFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SHADOW_GLAIVE.get(), new ShadowGlaiveRenderer.RenderFactory());

        ClientRegistry.bindTileEntityRenderer(TileRegistry.PEDESTAL_TILE.get(), PedestalTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(TileRegistry.RUNIC_ALTAR_TILE.get(), RunicAltarTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(TileRegistry.RUNIC_ANVIL_TILE.get(), RunicAnvilTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(TileRegistry.BLOODY_LECTERN_TILE.get(), BloodyLecternTileRenderer::new);

        RenderTypeLookup.setRenderLayer(BlockRegistry.CHALK_BLOCK.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockRegistry.RUNIC_ALTAR_BLOCK.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockRegistry.RUNIC_ANVIL_BLOCK.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockRegistry.BLOODY_LECTERN_BLOCK.get(), RenderType.cutout());

        HotkeyRegistry.register();

        ItemModelsProperties.register(ItemRegistry.SPACE_DISSECTOR.get(), new ResourceLocation(Reference.MODID, "mode"),
                (stack, world, entity) -> NBTUtils.getBoolean(stack, SpaceDissectorItem.TAG_IS_THROWN, false) ? 1 : 0);
        ItemModelsProperties.register(ItemRegistry.INFINITY_HAM.get(), new ResourceLocation(Reference.MODID, "pieces"),
                (stack, world, entity) -> Math.min(3, NBTUtils.getInt(stack, InfinityHamItem.TAG_PIECES, 0)));
        ItemModelsProperties.register(ItemRegistry.SHADOW_GLAIVE.get(), new ResourceLocation(Reference.MODID, "charges"),
                (stack, world, entity) -> Math.min(8, NBTUtils.getInt(stack, ShadowGlaiveItem.TAG_CHARGES, 0)));
        ItemModelsProperties.register(ItemRegistry.RELIC_CONTRACT.get(), new ResourceLocation(Reference.MODID, "blood"), (stack, world, entity) ->
                NBTUtils.getInt(stack, RelicContractItem.TAG_BLOOD, 0));
        ItemModelsProperties.register(ItemRegistry.SLIME_HEART.get(), new ResourceLocation(Reference.MODID, "mode"), (stack, world, entity) ->
                NBTUtils.getInt(stack, SlimeHeartItem.TAG_SLIME_AMOUNT, 0) > 0 ? 1 : 0);
    }
}