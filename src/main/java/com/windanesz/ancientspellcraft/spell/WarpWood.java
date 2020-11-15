package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.ArcaneLock;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

/**
 * Based on {@link electroblob.wizardry.spell.Mine} - author: Electroblob
 * Author: WinDanesz
 */
public class WarpWood extends SpellRay {

	public WarpWood() {
		super(AncientSpellcraft.MODID, "warp_wood", SpellActions.POINT, false);
		this.ignoreLivingEntities(true);
		this.particleSpacing(0.5);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {

		// Needs to be outside because it gets run on the client-side
		if (caster instanceof EntityPlayer) {
			if (caster.getHeldItemMainhand().getItem() instanceof ISpellCastingItem) {
				caster.swingArm(EnumHand.MAIN_HAND);
			} else if (caster.getHeldItemOffhand().getItem() instanceof ISpellCastingItem) {
				caster.swingArm(EnumHand.OFF_HAND);
			}
		}

		if (!world.isRemote) {

			if (BlockUtils.isBlockUnbreakable(world, pos))
				return false;
			if (!(caster instanceof EntityPlayer) && !EntityUtils.canDamageBlocks(caster, world))
				return false;
			// Can't mine arcane-locked blocks
			if (world.getTileEntity(pos) != null && world.getTileEntity(pos).getTileData().hasUniqueId(ArcaneLock.NBT_KEY))
				return false;

			IBlockState state = world.getBlockState(pos);

			// only works on wood and leaves
			if (world.getBlockState(pos).getMaterial() != Material.WOOD || world.getBlockState(pos).getMaterial() != Material.LEAVES) {
				return false;
			}

			boolean flag = false;

			int blastUpgradeCount = (int) ((modifiers.get(WizardryItems.blast_upgrade) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);
			// Results in the following patterns:
			// 0 blast upgrades: single block
			// 1 blast upgrade: 3x3 without corners or edges
			// 2 blast upgrades: 3x3 with corners
			// 3 blast upgrades: 5x5 without corners or edges
			float radius = 0.5f + 0.73f * blastUpgradeCount;

			List<BlockPos> sphere = BlockUtils.getBlockSphere(pos, radius);

			for (BlockPos pos1 : BlockPos.getAllInBox(pos.offset(EnumFacing.DOWN, 2 + blastUpgradeCount), pos.offset(EnumFacing.UP, 2 + blastUpgradeCount))) {

				if (BlockUtils.isBlockUnbreakable(world, pos1))
					continue;

				IBlockState state1 = world.getBlockState(pos1);

				if (caster instanceof EntityPlayerMP) { // Everything in here is server-side only so this is fine

					boolean silkTouch = state1.getBlock().canSilkHarvest(world, pos1, state1, (EntityPlayer) caster)
							&& ItemArtefact.isArtefactActive((EntityPlayer) caster, WizardryItems.charm_silk_touch);

					// Some protection mods seem to use this event instead so let's trigger it to check
					if (ForgeEventFactory.getBreakSpeed((EntityPlayer) caster, state1, 1, pos1) <= 0)
						continue;

					int xp = ForgeHooks.onBlockBreakEvent(world,
							((EntityPlayerMP) caster).interactionManager.getGameType(), (EntityPlayerMP) caster, pos1);

					if (xp == -1)
						continue; // Event was cancelled

					if (silkTouch) {
						flag = world.destroyBlock(pos1, false);
						if (flag)
							Block.spawnAsEntity(world, pos1, getSilkTouchDrop(state1));
					} else {
						flag = world.destroyBlock(pos1, true);
						if (flag)
							state1.getBlock().dropXpOnBlockBreak(world, pos1, xp);
					}

				} else {
					return false;
				}
			}

			return flag;
		}
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
		ParticleBuilder.create(Type.LEAF).pos(x, y, z).time(20 + world.rand.nextInt(5)).clr(0.9f, 0.95f, 1)
				.shaded(false).spawn(world);
	}

	// Copied from Block, where (for some reason) it's protected
	private static ItemStack getSilkTouchDrop(IBlockState state) {

		Item item = Item.getItemFromBlock(state.getBlock());
		int i = 0;

		if (item.getHasSubtypes()) {
			i = state.getBlock().getMetaFromState(state);
		}

		return new ItemStack(item, 1, i);
	}

}
