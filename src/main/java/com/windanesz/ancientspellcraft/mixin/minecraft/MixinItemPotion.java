package com.windanesz.ancientspellcraft.mixin.minecraft;

import com.windanesz.ancientspellcraft.mixin.modrefs.HandleDiffuserRef;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPotion.class)
public class MixinItemPotion {

	@Inject(method = "onItemUseFinish", at = @At("HEAD"))
	private void mixinPerformEffectConsistent(ItemStack stack, World world, EntityLivingBase entityLiving, CallbackInfoReturnable<ItemStack> cir) {
		HandleDiffuserRef.diffuser(stack, world, entityLiving);
	}
}