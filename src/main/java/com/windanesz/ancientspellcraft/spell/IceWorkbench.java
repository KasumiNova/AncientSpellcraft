package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.registry.AncientSpellcraftBlocks;
import com.windanesz.ancientspellcraft.registry.AncientSpellcraftItems;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class IceWorkbench extends SpellRay {
	public IceWorkbench(String modID, String name, EnumAction action, boolean isContinuous) {
		super(modID, name, isContinuous, action);
		this.soundValues(0.5f, 1.1f, 0.2f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {

		pos = pos.offset(side);
		Random rand = new Random();
		if (world.isRemote) {
			for(int i=1; i<4; i++){
				double speed = (rand.nextBoolean() ? 1 : -1) * 0.1;// + 0.01 * rand.nextDouble();
				ParticleBuilder.create(ParticleBuilder.Type.ICE).pos(pos.getX() +0.8, pos.getY()+ rand.nextDouble() + 0.2, pos.getZ()+ 0.8).vel(0, 0, 0)
						.time(30).scale(1).spin(rand.nextDouble() * 1.5, speed).spawn(world);
			}
		}

		if (WizardryUtilities.canBlockBeReplaced(world, pos)) {

			if (!world.isRemote) {

				world.setBlockState(pos, AncientSpellcraftBlocks.ICE_CRAFTING_TABLE.getDefaultState());
			}

			return true;
		}

		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == AncientSpellcraftItems.ancient_spellcraft_spell_book || item == AncientSpellcraftItems.ancient_spellcraft_scroll;
	}
}
