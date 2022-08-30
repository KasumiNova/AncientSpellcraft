package com.windanesz.ancientspellcraft.item;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.material.IDevoritium;
import com.windanesz.ancientspellcraft.registry.ASTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemDevoritiumSword extends ItemSword implements IDevoritium {

	@SuppressWarnings("ConstantConditions")
	public ItemDevoritiumSword() {
		super(AncientSpellcraft.DEVORITIUM_TOOL_MATERIAL);
		setCreativeTab(ASTabs.ANCIENTSPELLCRAFT_GEAR);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		hitEntityDelegate(attacker, target);
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) { return false; }

	@Override
	public int getItemEnchantability() { return 0; }

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		onUpdateDelegate(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag advanced){
		super.addInformation(itemstack,world,tooltip, advanced);

		if (GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak)) {
			AncientSpellcraft.proxy.addMultiLineDescription(tooltip, I18n.format("tooltip.ancientspellcraft:devoritium.more_info"));
		} else {
			tooltip.add(I18n.format("tooltip.ancientspellcraft:more_info"));
		}
	}
}



