package com.windanesz.ancientspellcraft.potion;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import electroblob.wizardry.potion.PotionMagicEffect;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class PotionMageLight extends PotionMagicEffect {
	private float particleSize = 0.7F;

	public PotionMageLight() {
		super(false, 0xFAFCCC, new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_magelight.png"));
		this.setPotionName("potion." + AncientSpellcraft.MODID + ":magelight");
		this.setBeneficial();

	}

	@Override
	public boolean isReady(int duration, int amplifier){
		return true; // Execute the effect every tick
	}

	@Override
	public void performEffect(EntityLivingBase entity, int strength) {
		super.performEffect(entity,strength);
		if (entity instanceof EntityPlayer) {

			if (entity.world.isRemote) {

//					ParticleBuilder.create(ParticleBuilder.Type.FLASH).entity(entity).pos(0, 2.4, 0).time(3).clr(255, 255, 255).collide(false).
//							scale(0.7F).spawn(entity.world);
//				ParticleBuilder.create(ParticleBuilder.Type.SPARKLE).entity(entity).pos(0, 2.8, 0).time(6 + getRandomNumberInRange(0, 4)).vel(entity.world.rand.nextGaussian() / 40, entity.world.rand.nextDouble() / 40,
//						entity.world.rand.nextGaussian() / 40).clr(246, 180 + getRandomNumberInRange(0, 50), 80).collide(false).
//						scale(particleSize / 1.5F).spawn(entity.world);
				ParticleBuilder.create(ParticleBuilder.Type.FLASH).entity(entity).pos(0, 2.8, 0).time(6 + getRandomNumberInRange(0, 4)).vel(entity.world.rand.nextGaussian() / 40, entity.world.rand.nextDouble() / 40,
						entity.world.rand.nextGaussian() / 40).clr(255, 255, 204).collide(false).
						scale(particleSize / 1.5F).spawn(entity.world);
//				ParticleBuilder.create(ParticleBuilder.Type.SPARKLE).entity(entity).pos(0, 2.8, 0).time(6 + getRandomNumberInRange(0, 4)).vel(entity.world.rand.nextGaussian() / 40, entity.world.rand.nextDouble() / 40,
//						entity.world.rand.nextGaussian() / 40).clr(246, 180 + getRandomNumberInRange(0, 50), 80).collide(false).
//						scale(particleSize / 2F).spawn(entity.world);

			}
		}
	}

	private static int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	//		super.performEffect(entitylivingbase, strength);
	//	}
	//
	//
	//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	//	public void onEvent(PlayerTickEvent event)
	//	{
	//		if (event.phase == TickEvent.Phase.START && !event.player.worldObj.isRemote)
	//		{
	//			if (event.player.getCurrentEquippedItem() != null)
	//			{
	//				if (BlockMovingLightSource.isLightEmittingItem(
	//						event.player.getCurrentEquippedItem().getItem()))
	//		{
	//			int blockX = MathHelper.floor(entity.posX);
	//			int blockY = MathHelper.floor(entity.posY - 0.2D -
	//					entity.getYOffset());
	//			int blockZ = MathHelper.floor(entity.posZ);
	//
	//			// place light at head level
	//			BlockPos blockLocation = new BlockPos(blockX, blockY, blockZ).up();
	//
	//			if (entity.world.getBlockState(blockLocation).getBlock() == Blocks.AIR) {
	//				entity.world.setBlockState(
	//						blockLocation,
	//						AncientSpellcraftBlocks.MAGELIGHT.getDefaultState());
	//			} else if (entity.world.getBlockState(
	//					blockLocation.add(
	//							entity.getLookVec().x,
	//							entity.getLookVec().y,
	//							entity.getLookVec().z)).getBlock() == Blocks.AIR) {
	//				entity.world.setBlockState(blockLocation.add(entity.getLookVec().x, entity.getLookVec().y, entity.getLookVec().z),
	//						AncientSpellcraftBlocks.MAGELIGHT.getDefaultState());
	//			}
	//		}
}
