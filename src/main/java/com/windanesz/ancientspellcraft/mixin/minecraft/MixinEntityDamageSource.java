package com.windanesz.ancientspellcraft.mixin.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityDamageSource.class)
public class MixinEntityDamageSource {

	@Inject(at = @At("HEAD"), method = "getDeathMessage", cancellable = true)
	private void ancientspellcraft$getDeathMessage(EntityLivingBase entityLivingBaseIn, CallbackInfoReturnable<ITextComponent> cir) {
		String s = "death.attack." + ((EntityDamageSource) (Object) this).damageType;
		if (((EntityDamageSource) (Object) this).getTrueSource() == null) {
			cir.setReturnValue(new TextComponentTranslation(s, new Object[] {entityLivingBaseIn.getDisplayName(), "?"}));
		}
	}

}