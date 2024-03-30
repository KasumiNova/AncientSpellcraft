package com.windanesz.ancientspellcraft.mixin.ebwizardry;

import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.spell.LifeDrain;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static electroblob.wizardry.spell.LifeDrain.HEAL_FACTOR;
import static electroblob.wizardry.spell.Spell.DAMAGE;

@Mixin(LifeDrain.class)
public class MixinLifeDrain {


	@Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true, remap = false)
	private void mixinPerformEffectConsistent(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers, CallbackInfoReturnable<Boolean> ci) {
		if(EntityUtils.isLiving(target)){

			if(ticksInUse % 12 == 0){

				float damage = ((LifeDrain) (Object) this).getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);

				EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster,
						MagicDamage.DamageType.MAGIC), damage);

				if(caster != null) caster.heal(damage * ((LifeDrain) (Object) this).getProperty(HEAL_FACTOR).floatValue());
				if (caster instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer) caster, ASItems.ring_life_drain)) {
					((EntityPlayer) caster).getFoodStats().addStats(1, 0.1f);
				}
			}
		}
		ci.setReturnValue(true);
		ci.cancel();
	}
}