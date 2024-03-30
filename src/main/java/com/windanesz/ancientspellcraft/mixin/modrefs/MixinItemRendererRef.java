package com.windanesz.ancientspellcraft.mixin.modrefs;

import com.windanesz.ancientspellcraft.item.ItemSageTome;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MixinItemRendererRef {

	public static boolean isTome(Item item) {
		return item instanceof ItemSageTome;

	}
	public static IBakedModel getModel(ItemStack stack) {
		return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(((ItemSageTome) stack.getItem()).getSpecialModel());
	}

}
