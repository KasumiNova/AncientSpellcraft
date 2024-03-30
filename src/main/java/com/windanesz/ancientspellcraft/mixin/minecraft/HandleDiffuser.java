package com.windanesz.ancientspellcraft.mixin.minecraft;

import com.windanesz.ancientspellcraft.mixin.modrefs.HandleDiffuserRef;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class HandleDiffuser {
	public static void diffuserLogic(ItemStack stack, World world, EntityLivingBase entityLiving) {
		if (entityLiving instanceof EntityPlayer) {
			HandleDiffuserRef.diffuser(stack, world, entityLiving);
		}
	}
}
