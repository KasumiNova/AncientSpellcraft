package com.windanesz.ancientspellcraft.item;

import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;

public class ItemHookBelt extends AbstractItemArtefactWithSlots {

	public ItemHookBelt(EnumRarity rarity, Type type) {
		super(rarity, type, 1, 1, true);
	}

	@Override
	public boolean isItemValid(Item item) {
		return item instanceof ItemArtefact;
	}


}
