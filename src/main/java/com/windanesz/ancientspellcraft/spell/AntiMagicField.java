package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.entity.construct.EntityAntiMagicField;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellConstructRanged;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AntiMagicField extends SpellConstructRanged<EntityAntiMagicField> {

	public AntiMagicField() {
		super(AncientSpellcraft.MODID, "antimagic_field", EntityAntiMagicField::new, false);
		addProperties(Spell.EFFECT_RADIUS);
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
