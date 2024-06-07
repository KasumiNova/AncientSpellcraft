package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.registry.ASPotions;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MightAndMagic extends SpellBuffAS {

	public MightAndMagic() {
		super("might_and_magic", 219, 0, 18, ()-> ASPotions.magical_exhaustion, ()-> MobEffects.HEALTH_BOOST, ()-> MobEffects.STRENGTH);
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
