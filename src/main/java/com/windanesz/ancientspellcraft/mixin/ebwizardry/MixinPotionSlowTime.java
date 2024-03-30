package com.windanesz.ancientspellcraft.mixin.ebwizardry;

import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.potion.PotionSlowTime;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionSlowTime.class)
public class MixinPotionSlowTime {

	@Inject(method = "performEffectConsistent", at = @At("HEAD"), cancellable = true, remap = false)
	private static void mixinPerformEffectConsistent(EntityLivingBase host, int strength, CallbackInfo ci) {

		boolean stopTime = host instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)host, WizardryItems.charm_stop_time);

		int interval = strength * 4 + 6;

		// Mark all entities within range
		List<Entity> targetsInRange = EntityUtils.getEntitiesWithinRadius(ancientSpellcraft$getEffectRadius(), host.posX, host.posY, host.posZ, host.world, Entity.class);
		targetsInRange.remove(host);
		// Other entities with the slow time effect are unaffected
		targetsInRange.removeIf(t -> t instanceof EntityLivingBase && ((EntityLivingBase)t).isPotionActive(WizardryPotions.slow_time));
		if(!Wizardry.settings.slowTimeAffectsPlayers) targetsInRange.removeIf(t -> t instanceof EntityPlayer);

		//// //// //// //// //// This is the only change //// //// //// //// ////
		targetsInRange.removeIf(t -> t instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer) t, ASItems.belt_temporal_anchor));
		//// //// //// //// //// This is the only change //// //// //// //// ////


		targetsInRange.removeIf(t -> t instanceof EntityArrow && t.isEntityInsideOpaqueBlock());

		for(Entity entity : targetsInRange){

			entity.getEntityData().setBoolean(PotionSlowTime.NBT_KEY, true);

			// If time is stopped, block all updates; otherwise block all updates except every [interval] ticks
			entity.updateBlocked = stopTime || host.ticksExisted % interval != 0;

			if(!stopTime && entity.world.isRemote){

				// Client-side movement interpolation (smoothing)

				if(entity.onGround) entity.motionY = 0; // Don't ask. It just works.

				if(entity.updateBlocked){
					// When the update is blocked, the entity is moved 1/interval times the distance it would have moved
					double x = entity.posX + entity.motionX * 1d / (double)interval;
					double y = entity.posY + entity.motionY * 1d / (double)interval;
					double z = entity.posZ + entity.motionZ * 1d / (double)interval;

					entity.prevPosX = entity.posX;
					entity.prevPosY = entity.posY;
					entity.prevPosZ = entity.posZ;

					entity.posX = x;
					entity.posY = y;
					entity.posZ = z;

				}else{
					// When the update is not blocked, the entity is moved BACK 1-1/interval times the distance it moved
					// This is because the entity already covered most of that distance when its update was blocked
					entity.posX += entity.motionX * 1d / (double)interval;
					entity.posY += entity.motionY * 1d / (double)interval;
					entity.posZ += entity.motionZ * 1d / (double)interval;

					double x = entity.posX - entity.motionX * 1d / (double)interval;
					double y = entity.posY - entity.motionY * 1d / (double)interval;
					double z = entity.posZ - entity.motionZ * 1d / (double)interval;

					entity.prevPosX = x;
					entity.prevPosY = y;
					entity.prevPosZ = z;
				}
			}

			if(entity.world.isRemote && host.ticksExisted % 2 == 0){
				int lifetime = 15;
				double dx = (entity.world.rand.nextDouble() - 0.5D) * 2 * (double)entity.width;
				double dy = (entity.world.rand.nextDouble() - 0.5D) * 2 * (double)entity.width;
				double dz = (entity.world.rand.nextDouble() - 0.5D) * 2 * (double)entity.width;
				double x = entity.posX + dx;
				double y = entity instanceof IProjectile ? entity.posY + dy : entity.posY + entity.height/2 + dy;
				double z = entity.posZ + dz;
				ParticleBuilder.create(ParticleBuilder.Type.DUST)
						.pos(x, y, z)
						.vel(-dx/lifetime, -dy/lifetime, -dz/lifetime)
						.clr(0x5be3bb).time(15).spawn(entity.world);
			}
		}

		// Un-mark all entities that have just left range
		List<Entity> targetsBeyondRange = EntityUtils.getEntitiesWithinRadius(ancientSpellcraft$getEffectRadius() + 3, host.posX, host.posY, host.posZ, host.world, Entity.class);
		targetsBeyondRange.removeAll(targetsInRange);
		targetsBeyondRange.forEach(e -> e.updateBlocked = false);

		ci.cancel();
	}

	@Unique
	private static double ancientSpellcraft$getEffectRadius(){
		return Spells.slow_time.getProperty(Spell.EFFECT_RADIUS).doubleValue();
	}

}