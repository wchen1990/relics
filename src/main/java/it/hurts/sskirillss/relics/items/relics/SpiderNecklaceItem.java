package it.hurts.sskirillss.relics.items.relics;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStats;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicLoot;
import it.hurts.sskirillss.relics.items.relics.renderer.SpiderNecklaceModel;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.RelicUtils;
import it.hurts.sskirillss.relics.utils.tooltip.AbilityTooltip;
import it.hurts.sskirillss.relics.utils.tooltip.RelicTooltip;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class SpiderNecklaceItem extends RelicItem<SpiderNecklaceItem.Stats> implements ICurioItem {
    public SpiderNecklaceItem() {
        super(RelicData.builder()
                .rarity(Rarity.RARE)
                .config(Stats.class)
                .loot(RelicLoot.builder()
                        .table(RelicUtils.Worldgen.CAVE)
                        .chance(0.15F)
                        .build())
                .loot(RelicLoot.builder()
                        .table(EntityType.SPIDER.getDefaultLootTable().toString())
                        .chance(0.01F)
                        .build())
                .loot(RelicLoot.builder()
                        .table(EntityType.CAVE_SPIDER.getDefaultLootTable().toString())
                        .chance(0.01F)
                        .build())
                .build());
    }

    @Override
    public RelicTooltip getShiftTooltip(ItemStack stack) {
        return new RelicTooltip.Builder(stack)
                .ability(new AbilityTooltip.Builder()
                        .build())
                .ability(new AbilityTooltip.Builder()
                        .build())
                .build();
    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        if (livingEntity.isSpectator())
            return;

        if (livingEntity.horizontalCollision && livingEntity.zza > 0) {
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().x(),
                    config.climbSpeed, livingEntity.getDeltaMovement().z());
            livingEntity.fallDistance = 0F;
        }
    }

    private final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/items/models/spider_necklace.png");

    @Override
    public void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemStack stack) {
        SpiderNecklaceModel model = new SpiderNecklaceModel();

        matrixStack.pushPose();

        model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
        ICurio.RenderHelper.followBodyRotations(livingEntity, model);
        model.renderToBuffer(matrixStack, renderTypeBuffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStack.popPose();
    }

    @Override
    public boolean canRender(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        return true;
    }

    public static class Stats extends RelicStats {
        public float climbSpeed = 0.2F;
    }
}