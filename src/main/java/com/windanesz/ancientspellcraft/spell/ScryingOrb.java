package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.registry.ASPotions;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ScryingOrb extends SpellRay implements IClassSpell {

	public static final IStoredVariable<BlockPos> SCRYING_ORB_POS = IStoredVariable.StoredVariable.ofBlockPos("ancientSpellcraftScryingOrbPos", Persistence.NEVER).setSynced();
	public static final IStoredVariable<Boolean> IS_SCRYING = IStoredVariable.StoredVariable.ofBoolean("ancientSpellcraftIsScryingOrb", Persistence.NEVER).setSynced();

	public ScryingOrb() {
		super(AncientSpellcraft.MODID, "scrying_orb", SpellActions.POINT, false);
		WizardData.registerStoredVariables(SCRYING_ORB_POS, IS_SCRYING);
		addProperties(EFFECT_DURATION);
	}

	// The following three methods serve as a good example of how to implement continuous spell sounds (hint: it's easy)

	public static boolean isScrying(EntityPlayer player) {
		Boolean b = WizardData.get(player).getVariable(IS_SCRYING);
		return b != null && b;
	}

	public static BlockPos getBlockPos(EntityPlayer player) {
		return WizardData.get(player).getVariable(SCRYING_ORB_POS) != null ? WizardData.get(player).getVariable(SCRYING_ORB_POS) : BlockPos.ORIGIN;
	}

	public static void setScrying(EntityPlayer player, boolean scrying) {
		WizardData data = WizardData.get(player);
		data.setVariable(IS_SCRYING, scrying);
		data.sync();
	}

	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		if (!world.isRemote && isScrying(caster)) {
			setScrying(caster, false);
			caster.removePotionEffect(WizardryPotions.sixth_sense);
			caster.removePotionEffect(ASPotions.astral_projection);
			return true;
		}

		if (!caster.isSneaking()) {
			if (getBlockPos(caster) != null) {
				BlockPos pos = getBlockPos(caster);
				if (world.isRemote) {
					int[] colours = WarlockElementalSpellEffects.PARTICLE_COLOURS.get(Element.MAGIC);
					ParticleBuilder.create(Type.FLASH).scale(3.5f).pos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).scale(0.35F).time(160).clr(colours[0]).spawn(world);
					ParticleBuilder.create(Type.FLASH).scale(2.5f).pos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).scale(0.35F).time(160).clr(colours[0]).spawn(world);
					double r = 0.12;

					for (int i = 0; i < 3; ++i) {
						double x = r * (world.rand.nextDouble() * 2.0 - 1.0);
						double y = r * (world.rand.nextDouble() * 2.0 - 1.0);
						double z = r * (world.rand.nextDouble() * 2.0 - 1.0);
						ParticleBuilder.create(Type.DUST).pos(pos.getX() + 0.5, pos.getY() + 0.5f, pos.getZ() + 0.5)
								.vel(x * -0.03, -0.02, z * -0.03).scale(2).time(160 + world.rand.nextInt(8)).clr(colours[0]).fade(colours[1]).spawn(world);
					}
				} else {
					caster.addPotionEffect(new PotionEffect(ASPotions.astral_projection, 120, 0));
					caster.addPotionEffect(new PotionEffect(WizardryPotions.sixth_sense, 120, 0));
				}
				setScrying(caster, true);
			}
			return true;
		}

		Vec3d look = caster.getLookVec();
		Vec3d origin = new Vec3d(caster.posX, caster.posY + (double) caster.getEyeHeight() - 0.25, caster.posZ);
		if (!this.isContinuous && world.isRemote && !Wizardry.proxy.isFirstPerson(caster)) {
			origin = origin.add(look.scale(1.2));
		}

		if (!this.shootSpell(world, origin, look, caster, ticksInUse, modifiers)) {
			return false;
		} else {
			if (this.casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) {
				caster.swingArm(hand);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers, new String[0]);
			return true;
		}
	}


	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		EntityPlayer player = (EntityPlayer) caster;
		WizardData data = WizardData.get(player);
		data.setVariable(SCRYING_ORB_POS, pos.offset(side, 2));
		data.sync();
		return true;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return true;
	}

	@Override
	protected void spawnParticleRay(World world, Vec3d origin, Vec3d direction, EntityLivingBase caster, double distance) {

		if (caster != null) {
			ParticleBuilder.create(Type.BEAM).entity(caster).pos(origin.subtract(caster.getPositionVector()))
					.length(distance).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(Element.MAGIC)[0])
					.scale(MathHelper.sin(caster.ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
		} else {
			ParticleBuilder.create(Type.BEAM).pos(origin).target(origin.add(direction.scale(distance)))
					.clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(Element.MAGIC)[0])
					.scale(MathHelper.sin(Wizardry.proxy.getThePlayer().ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
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
	public boolean canBeCastBy(TileEntityDispenser dispenser) {
		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override) {
		return canBeCastByClassNPC(npc);
	}
}
