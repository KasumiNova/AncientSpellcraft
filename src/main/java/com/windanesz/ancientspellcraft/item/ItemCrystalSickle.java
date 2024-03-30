package com.windanesz.ancientspellcraft.item;

import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.block.BlockDimensionBoundary;
import com.windanesz.ancientspellcraft.block.BlockDimensionFocus;
import com.windanesz.ancientspellcraft.registry.ASBlocks;
import com.windanesz.ancientspellcraft.registry.ASDimensions;
import com.windanesz.ancientspellcraft.spell.OrbSpace;
import com.windanesz.ancientspellcraft.util.ASUtils;
import electroblob.wizardry.block.BlockCrystalFlower;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCrystalSickle extends ItemASArtefact {

	public ItemCrystalSickle(EnumRarity rarity, Type type) {
		super(rarity, type);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);

		if (Settings.isArtefactEnabled(this) && world.getBlockState(pos).getBlock() instanceof BlockCrystalFlower) {
			if (!world.isRemote && BlockUtils.canBreakBlock(player, world, pos)) {
			ASUtils.giveStackToPlayer(player, new ItemStack(WizardryItems.magic_crystal, 2+  Math.max(1,  + itemRand.nextInt(3))));
			world.setBlockToAir(pos);
			}
			player.swingArm(EnumHand.MAIN_HAND);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;

	}
}
