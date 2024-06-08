package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MetamagicProjectile extends Spell {

	public static final IStoredVariable<Boolean> METAMAGIC_PROJECTILE = IStoredVariable.StoredVariable.ofBoolean("metamagic_projectile", Persistence.ALWAYS).setSynced();

	public MetamagicProjectile() {
		super(AncientSpellcraft.MODID, "metamagic_projectile", SpellActions.POINT_UP, false);
		WizardData.registerStoredVariables(METAMAGIC_PROJECTILE);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		if (WizardData.get(caster) != null) {
			WizardData data = WizardData.get(caster);
			// Fixes the sound not playing in first person.
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			data.setVariable(METAMAGIC_PROJECTILE, true);
			if (!world.isRemote)
				data.sync();
			return true;
		}
		return false;
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser) {
		return false;
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == ASItems.ancient_spell_book || item == ASItems.ancient_spellcraft_scroll;
	}

	@SideOnly(Side.CLIENT)
	public String getDisplayNameWithFormatting() {
		return TextFormatting.GOLD + net.minecraft.client.resources.I18n.format(getTranslationKey());
	}
}
