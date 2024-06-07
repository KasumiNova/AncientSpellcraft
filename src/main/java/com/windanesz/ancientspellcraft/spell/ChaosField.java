package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.entity.construct.EntityChaosField;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellConstructRanged;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;

public class ChaosField extends SpellConstructRanged<EntityChaosField> implements IClassSpell {

	public ChaosField() {
		super(AncientSpellcraft.MODID, "chaos_field", EntityChaosField::new, false);
		addProperties(Spell.EFFECT_RADIUS);
	}

	@Override
	public ItemWizardArmour.ArmourClass getArmourClass() {
		return ItemWizardArmour.ArmourClass.WARLOCK;
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
