package com.windanesz.ancientspellcraft.spell;

import com.google.common.collect.Sets;
import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AbsorbObject extends SpellRay implements IClassSpell {

	public AbsorbObject() {
		super(AncientSpellcraft.MODID, "absorb_object", SpellActions.POINT, true);
		ignoreUncollidables = false;
		this.soundValues(1, 1, 0.4f);
	}

	// Finally everything in here is standardised and written in a form that's actually readable - it was long overdue!
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();
		Vec3d origin = new Vec3d(caster.posX, caster.posY + caster.getEyeHeight() - Y_OFFSET, caster.posZ);
		if(!this.isContinuous && world.isRemote && !Wizardry.proxy.isFirstPerson(caster)){
			origin = origin.add(look.scale(1.2));
		}

		if(!shootSpell(world, origin, look, caster, ticksInUse, modifiers)) return false;

		if(casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) caster.swingArm(hand);
		if (ticksInUse == 0 || ticksInUse % 80 == 0) {
			this.playSound(world, caster, ticksInUse, -1, modifiers);
		}
		return true;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		// Fire can damage armour stands, so this includes them
		//		if(target instanceof EntityLivingBase) {
		//
		//			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
		//				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
		//						new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
		//			}else{
		//				target.setFire((int)(getProperty(BURN_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
		//			}
		//
		//			return true;
		//		}

		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		boolean hasStack = false;
		if (caster.getActiveHand() == EnumHand.MAIN_HAND) {
			ItemStack stack = caster.getHeldItemMainhand();
			if (stack.hasTagCompound()) {
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt.hasKey("SpellAbsorbObject")) {
					hasStack = true;
				}
			}
		}

		if (caster.isSneaking() && hasStack) {
			return false;
		}
		if (!world.isRemote) {
			if (ticksInUse == 60) {

				if (!caster.isSneaking()) {

					pos = pos.offset(side);
					if (BlockUtils.canPlaceBlock(caster, world, pos)) {

						if (caster.getActiveHand() == EnumHand.MAIN_HAND) {
							ItemStack stack = caster.getHeldItemMainhand();
							if (stack.hasTagCompound()) {
								NBTTagCompound nbt = stack.getTagCompound();
								if (nbt.hasKey("SpellAbsorbObject")) {
									NBTTagCompound spellAbsorbObject = nbt.getCompoundTag("SpellAbsorbObject");
									if (spellAbsorbObject.hasKey("state")) {
										NBTUtil.readBlockState(spellAbsorbObject.getCompoundTag("state"));
										world.setBlockState(pos, NBTUtil.readBlockState(spellAbsorbObject.getCompoundTag("state")));
										nbt.removeTag("SpellAbsorbObject");
										stack.setTagCompound(nbt);

										if (spellAbsorbObject.hasKey("tile")) {
											NBTTagCompound tileNBT = spellAbsorbObject.getCompoundTag("tile");
											world.setTileEntity(pos, TileEntity.create(world, tileNBT));
										}

										return true;
									}
								}
							}
						}
					}
				}

				if (BlockUtils.isBlockUnbreakable(world, pos)) {return false;}
				List<String> blacklist = new ArrayList<>(Arrays.asList(Settings.generalSettings.absorb_object_block_blacklist));
				if (blacklist.contains(world.getBlockState(pos).getBlock().getRegistryName().toString())) {return false;}
				// Reworked to respect the rules, but since we might break multiple blocks this is left as an optimisation
				if (!EntityUtils.canDamageBlocks(caster, world)) {return false;}


				boolean flag = false;

				IBlockState state1 = world.getBlockState(pos);

				if (caster instanceof EntityPlayerMP) { // Everything in here is server-side only so this is fine

					flag = BlockUtils.canBreakBlock(caster, world, pos);
					if (flag) {
						storeBlockAndTileEntity((EntityPlayer) caster, world, pos);
						((EntityPlayerMP) caster).getCooldownTracker().setCooldown(caster.getHeldItemMainhand().getItem(), 5);
					}

				}

				return flag;
			}
		} else {

			if (!caster.isSneaking()) {
				pos = pos.offset(side);
			} else {
				for (int i = 0; i < 2; i++) {//
					ParticleBuilder.create(ParticleBuilder.Type.FLASH).shaded(true).time(40).scale(1.9f)
							.pos(pos.getX() + 0.5f, pos.getY() + 0.3 + world.rand.nextFloat(), pos.getZ() + 0.5f).clr(Math.min(254, 40 + (ticksInUse * 4)), 10, 10).spin(0.5, 0.1f).spawn(world);
				}
			}

			if (hasStack && ticksInUse % 2 == 0) {
				for (int i = 0; i < 2; i++) {//
					ParticleBuilder.create(ParticleBuilder.Type.FLASH).shaded(true).time(40).scale(1.9f)
							.pos(pos.getX() + 0.5f, pos.getY() + 0.3 + world.rand.nextFloat(), pos.getZ() + 0.5f).clr(Math.min(254, 40 + (ticksInUse * 4)), 10, 10).spin(0.5, 0.1f).spawn(world);
				}
			}
			if (ticksInUse == 39) {
				//	ParticleBuilder.create(ParticleBuilder.Type.CLOUD).shaded(true).time(20).scale(4)
				//			.pos(pos.getX() + 0.5f, pos.getY() + 0.4 + world.rand.nextFloat(), pos.getZ() + 0.5f).clr(Math.min(254, 40 + (ticksInUse * 4)), 10,10).spin(0.1,0.1f).spawn(world);

			}
			//ParticleBuilder.create	(ParticleBuilder.Type.SPHERE).time(40).scale(1f).pos(pos.getX() + 0.5f, pos.getY() + world.rand.nextFloat(),pos.getZ() + 0.5f).clr(0xa30700).spin(1f, 0.1f).spawn(world);
			//	ParticleBuilder.create	(ParticleBuilder.Type.FLASH).time(10).scale(1.5f).pos(pos.getX() + 0.5f, pos.getY() + 1,pos.getZ() + 0.5f).clr(0xa30700).spin(0, 0.1f).spawn(world);
			//	}

		}
		return true;

	}

	public static void storeBlockAndTileEntity(EntityPlayer player, World world, BlockPos pos) {
		// If the player is holding an item in their main hand
		ItemStack heldItem = player.getHeldItemMainhand();
		if (!heldItem.isEmpty()) {
			NBTTagCompound nbt = heldItem.hasTagCompound() ? heldItem.getTagCompound() : new NBTTagCompound();

			NBTTagCompound state = new NBTTagCompound();
			NBTTagCompound blockData = new NBTTagCompound();
			IBlockState state2 = world.getBlockState(pos);
			NBTUtil.writeBlockState(state, world.getBlockState(pos));
			if (world.getTileEntity(pos) != null) {
				NBTTagCompound tile = world.getTileEntity(pos).serializeNBT();
				blockData.setTag("tile", tile);
				world.removeTileEntity(pos);
			}
			blockData.setTag("state", state);
			//noinspection DataFlowIssue
			nbt.setTag("SpellAbsorbObject", blockData);
			player.getHeldItemMainhand().setTagCompound(nbt);

			// Break the block at the specified position
			world.setBlockToAir(pos);

			EntityTippedArrow a = new EntityTippedArrow(world);
			NBTTagCompound nbtTagCompound = a.serializeNBT();
			Set<PotionEffect> customPotionEffects = Sets.<PotionEffect>newHashSet();
			for (PotionEffect potioneffect : PotionUtils.getFullEffectsFromTag(nbtTagCompound)) {
				customPotionEffects.add(potioneffect);
			}
			if (!customPotionEffects.isEmpty()) {
				ItemStack stack = new ItemStack(Items.TIPPED_ARROW);
				PotionUtils.appendEffects(stack, customPotionEffects);
			}
		}
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	// The caster argument is only really useful for spawning targeted particles continuously
	protected void spawnParticleRay(World world, Vec3d origin, Vec3d direction, @Nullable EntityLivingBase caster, double distance) {

		if (caster == null) {
			return;
		}

		Vec3d velocity = direction.scale(particleVelocity);

		for (double d = particleSpacing; d <= distance; d += particleSpacing) {
			double x = origin.x + d * direction.x + particleJitter * (world.rand.nextDouble() * 2 - 1);
			double y = origin.y + d * direction.y + particleJitter * (world.rand.nextDouble() * 2 - 1);
			double z = origin.z + d * direction.z + particleJitter * (world.rand.nextDouble() * 2 - 1);
			spawnParticle(world, x, y, z, velocity.x, velocity.y, velocity.z);
		}
		Vec3d endpoint = origin.add(direction.scale(distance));
		if (caster.isSneaking()) {
			ParticleBuilder.create(ParticleBuilder.Type.BEAM).clr(0xa30700).time(-1).shaded(true).pos(origin).target(endpoint).spawn(world);
		}
	}

	@Override
	public ItemWizardArmour.ArmourClass getArmourClass() {
		return ItemWizardArmour.ArmourClass.WARLOCK;
	}

	public boolean applicableForItem(Item item) {
		return item == ASItems.forbidden_tome;
	}


	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override) {
		return false;
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser) {
		return false;
	}
}
