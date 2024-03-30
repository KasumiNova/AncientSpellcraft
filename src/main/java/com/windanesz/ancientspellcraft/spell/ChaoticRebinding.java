package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChaoticRebinding extends SpellRay implements IClassSpell {

	public ChaoticRebinding(){
		super(AncientSpellcraft.MODID, "chaotic_rebinding", SpellActions.POINT, false);
	}

	// The following three methods serve as a good example of how to implement continuous spell sounds (hint: it's easy)
	
	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if (target instanceof EntityItem && ((EntityItem) target).getItem().getItem() instanceof ItemSpellBook && ((EntityItem) target).getItem().getMetadata() != 1) {
			double posX = target.posX;
			double posY = target.posY;
			double posZ = target.posZ;
			if (!world.isRemote) {
				EntityItem newItem = new EntityItem(world);
				newItem.setItem(new ItemStack(WizardryItems.ruined_spell_book));
				((EntityItem) target).setItem(ItemStack.EMPTY);
				newItem.setPosition(target.posX, target.posY, target.posZ);
				world.removeEntityDangerously(target);
				world.createExplosion(newItem, newItem.posX, newItem.posY, newItem.posZ, 3f, false);
				world.spawnEntity(newItem);

			}


			if (world.isRemote) {
				ParticleBuilder.create(ParticleBuilder.Type.FLASH).shaded(true).time(40).scale(1.9f)
						.pos(posX, posY+ 0.5f, posZ).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(Element.MAGIC)[0]).spawn(world);
			}
			return true;
		}
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}

	@Override
	protected void spawnParticleRay(World world, Vec3d origin, Vec3d direction, EntityLivingBase caster, double distance){

		if(caster != null){
			ParticleBuilder.create(Type.BEAM).entity(caster).pos(origin.subtract(caster.getPositionVector()))
					.length(distance).clr(WarlockElementalSpellEffects.PARTICLE_COLOURS.get(Element.MAGIC)[0])
					.scale(MathHelper.sin(caster.ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
		}else{
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
		return false;
	}
}
