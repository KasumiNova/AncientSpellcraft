package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class Obliteration extends Spell implements IClassSpell {
	public static final String CHARGING_TIME = "charging_time";

	public Obliteration() {
		super(AncientSpellcraft.MODID, "obliteration", SpellActions.SUMMON, true);
		this.soundValues(1, 1, 0.4f);
		addProperties(DAMAGE, EFFECT_RADIUS, CHARGING_TIME);
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers) {
		return doSpellTick(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		return doSpellTick(world, caster, hand, ticksInUse, modifiers);
	}


	private boolean doSpellTick(World world, EntityLivingBase caster, EnumHand hand, int ticksInUse,
			SpellModifiers modifiers) {
		int chargeup = getProperty(CHARGING_TIME).intValue();
		Element element = getElementOrMagicElement(caster);
		if (ticksInUse < chargeup) {
			if (world.isRemote && caster.ticksExisted % 1 == 0) {
				for (int i = 0; i < 10; i++) {
					ParticleBuilder.create(ParticleBuilder.Type.FLASH).entity(caster).spin((Math.min(3, 1 / ((0.1 + ticksInUse) * 0.05f))), 0.006f)
							.scale(Math.min(1.0f, 2 / (Math.max(1.2f, ticksInUse * 0.2f) * 0.7f))).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(element)[world.rand.nextInt(2)]).pos(0, world.rand.nextFloat() + 0.2f, 0).time(40).spawn(world);
				}
			}
		} else if (ticksInUse == chargeup + 1) {

			if (world.isRemote) {
				Vec3d origin = caster.getPositionVector();
				double particleX, particleZ;

				for(int i = 0; i < 40 * modifiers.get(WizardryItems.blast_upgrade); i++){

					particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
					particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();
					ParticleBuilder.create(WarlockElementalSpellEffects.getElementalParticle(element)).pos(particleX, origin.y + 0.1f, particleZ)
							.vel(particleX - origin.x, 0, particleZ - origin.z).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(element)[0]).spawn(world);

					particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
					particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();
					ParticleBuilder.create(WarlockElementalSpellEffects.getElementalParticle(element)).pos(particleX, origin.y + 0.1f, particleZ)
							.vel(particleX - origin.x, 0, particleZ - origin.z).time(30).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(element)[0]).spawn(world);

					particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
					particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();

					IBlockState block = world.getBlockState(new BlockPos(origin.x, origin.y - 0.5, origin.z));

					if(block != null){
						world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, origin.y + 0.1f,
								particleZ, particleX - origin.x, 0, particleZ - origin.z, Block.getStateId(block));
					}
				}

				ParticleBuilder.create(ParticleBuilder.Type.SPHERE).time(10).pos(origin.add(0, 0.1, 0)).scale((float)getProperty(EFFECT_RADIUS).floatValue() * 0.8f).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(element)[0]).spawn(world);
				ParticleBuilder.create(ParticleBuilder.Type.SPHERE).time(20).pos(origin.add(0, 0.1, 0)).scale((float)getProperty(EFFECT_RADIUS).floatValue() * 0.8f).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(element)[0]).spawn(world);
				ParticleBuilder.create(ParticleBuilder.Type.FLASH)
						.pos(origin.add(0, 0.1, 0))
						.scale((float)getProperty(EFFECT_RADIUS).floatValue() * 0.8f)
						.clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(element)[0])
						.face(EnumFacing.DOWN)
						.time(60)
						.spawn(world);
			} else {
				List<BlockPos> list = BlockUtils.getBlockSphere(caster.getPosition(), getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade));
				//list.removeIf(pos -> pos.getY() < caster.posY);
				for (BlockPos pos : list) {
					if(true){

						EntityLevitatingBlock target = new EntityLevitatingBlock(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						world.getBlockState(pos));

						target.fallTime = 2;
						target.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
						target.setCaster(caster);
						//((EntityLevitatingBlock)target).suspend();
			//			world.setBlockToAir(pos);

						double velocityFactor = 4;

						double dx = target.posX - caster.getPositionVector().x;
						double dy = target.posY + 1 - caster.getPositionVector().y;
						double dz = target.posZ - caster.getPositionVector().z;

						target.motionX = 1;
						target.motionY = 1;
						target.motionZ = 1;

						world.spawnEntity(target);
						}

						return true;
					}
				}
			}
		return true;
	}

	boolean affectEntity(@Nullable EntityLivingBase caster, EntityLivingBase target, SpellModifiers modifiers, Element element) {
		WarlockElementalSpellEffects.affectEntity(target, element, caster, modifiers, true);
		return true;
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
