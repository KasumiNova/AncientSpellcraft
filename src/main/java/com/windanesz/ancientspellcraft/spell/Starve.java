package com.windanesz.ancientspellcraft.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Starve extends SpellRayAS {

	public Starve() {
		super("starve", SpellActions.POINT, false);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit,
			@Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {

		if (target instanceof EntityPlayer) {

			EntityPlayer targetPlayer = (EntityPlayer) target;

			int foodlevel = targetPlayer.getFoodStats().getFoodLevel();
			targetPlayer.getFoodStats().setFoodLevel(foodlevel / 2);

			float saturation = targetPlayer.getFoodStats().getSaturationLevel();
			targetPlayer.getFoodStats().addExhaustion((int) (saturation / 2));

			return true;
		} else if (target instanceof EntityLivingBase) {
			EntityLivingBase entityLivingBase = (EntityLivingBase) target;
			entityLivingBase.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.HUNGER, 200, 2));
			entityLivingBase.addPotionEffect(new net.minecraft.potion.PotionEffect(MobEffects.SLOWNESS, 200, 1));
			entityLivingBase.addPotionEffect(new net.minecraft.potion.PotionEffect(MobEffects.WEAKNESS, 200, 1));
		}
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit,
			@Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected boolean onMiss(World world, @Nullable EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override) { return true; }

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser) { return true; }

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.SPELL_WITCH, x, y, z, 0, 0, 0);
	}
}
