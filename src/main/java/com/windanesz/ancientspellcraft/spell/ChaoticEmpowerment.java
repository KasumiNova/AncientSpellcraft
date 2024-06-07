package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.spell.SpellAreaEffect;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChaoticEmpowerment extends SpellAreaEffect implements IClassSpell{

	public static final String EXPLOSION_STRENGTH = "explosion_strength";
	public static final String BREAK_BLOCKS = "break_blocks";
	public static final String EXPLOSION_CHANCE = "explosion_chance";

	public ChaoticEmpowerment(){
		super(AncientSpellcraft.MODID, "chaotic_empowerment", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		this.targetAllies(true);
		addProperties(EXPLOSION_STRENGTH, BREAK_BLOCKS, EXPLOSION_CHANCE, EFFECT_DURATION);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable
	EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){
		if (target instanceof ISummonedCreature && ((ISummonedCreature) target).getCaster() == caster) {
			if (world.rand.nextFloat() < getProperty(EXPLOSION_CHANCE).floatValue()) {
				if (!world.isRemote) {
					target.setDead();
				}
				world.createExplosion(target, target.posX, target.posY, target.posZ, getProperty(EXPLOSION_STRENGTH).floatValue(), causeBlockDamage());
			} else {
				if (!world.isRemote) {
					target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, getProperty(EFFECT_DURATION).intValue(), 0));
					target.addPotionEffect(new PotionEffect(MobEffects.SPEED, getProperty(EFFECT_DURATION).intValue(), 1));
				}
			}
		}

		return false; // Only succeeds if something was healed
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		// We're spawning particles above so don't bother with this method
	}

	@Override
	public ItemWizardArmour.ArmourClass getArmourClass() {
		return ItemWizardArmour.ArmourClass.WARLOCK;
	}

	public boolean causeBlockDamage() {
		return getProperty(BREAK_BLOCKS).intValue() == 1;
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == ASItems.forbidden_tome;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override) {
		return canBeCastByClassNPC(npc);
	}
}
