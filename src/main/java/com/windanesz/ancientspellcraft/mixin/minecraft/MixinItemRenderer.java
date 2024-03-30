package com.windanesz.ancientspellcraft.mixin.minecraft;

import com.windanesz.ancientspellcraft.mixin.modrefs.MixinItemRendererRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

	@Inject(at = @At("HEAD"), method = "renderItem", cancellable = true)
	private void ancientspellcraft$renderItem(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType type, CallbackInfo ci) {
		if (MixinItemRendererRef.isTome(stack.getItem())) {
			GlStateManager.pushMatrix();

			IBakedModel bakedModel = MixinItemRendererRef.getModel(stack);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, bakedModel);

			GlStateManager.popMatrix();
			ci.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "renderItemSide", cancellable = true)
	private void ancientspellcraft$renderItemSide(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType type, boolean leftHanded, CallbackInfo ci) {
		if (MixinItemRendererRef.isTome(stack.getItem())) {
			GlStateManager.pushMatrix();

			IBakedModel bakedModel = MixinItemRendererRef.getModel(stack);
			Minecraft.getMinecraft().getRenderItem().renderItemModel(stack, bakedModel, type, leftHanded);

			GlStateManager.popMatrix();
			ci.cancel();
		}
	}

}