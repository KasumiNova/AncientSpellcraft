package com.windanesz.ancientspellcraft.item;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ItemFocusStone extends ItemASArtefact {

	public static final String CHARGE = "charge";
	public ItemFocusStone(EnumRarity rarity, Type type) {
		super(rarity, type);
	}

	public static float getCharge(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(CHARGE)) {
			return stack.getTagCompound().getFloat(CHARGE);
		}
		return  0f;
	}

	public static void resetCharge(ItemStack stack) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (stack.hasTagCompound()) {
			nbt = stack.getTagCompound();
		}
		nbt.setFloat(CHARGE, 0);
	}

	public static ItemStack addCharge(ItemStack stack, float charge) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (stack.hasTagCompound()) {
			nbt = stack.getTagCompound();
			if (nbt.hasKey(CHARGE)) {
				charge = Math.min(1f, nbt.getFloat(CHARGE) + charge);
			}
		}
		nbt.setFloat(CHARGE, charge);
		stack.setTagCompound(nbt);
		return stack;
	}
}
