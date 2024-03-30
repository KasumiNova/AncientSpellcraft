package com.windanesz.ancientspellcraft.item;

import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.registry.ASBlocks;
import com.windanesz.ancientspellcraft.tileentity.TileArcaneWall;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemVaultKey extends ItemASArtefact implements IManaStoringItem, IWorkbenchItem {

	public ItemVaultKey(EnumRarity rarity, Type type) {
		super(rarity, type);
		this.setMaxDamage(10000);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return !isManaEmpty(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc", Settings.generalSettings.orb_artefact_potency_bonus);

		if (!Settings.isArtefactEnabled(this)) {
			tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":generic.disabled", new Style().setColor(TextFormatting.RED)));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);

		Vec3d look = player.getLookVec();
		Vec3d origin = new Vec3d(player.posX, player.posY + player.getEyeHeight() - 0.25f, player.posZ);
		if (world.isRemote && !Wizardry.proxy.isFirstPerson(player)) {
			origin = origin.add(look.scale(1.2));
		}

		if (getMana(stack) < getManaCost()) {
			return EnumActionResult.PASS;
		}

		double range = 5;
		Vec3d endpoint = origin.add(look.scale(range));

		// Change the filter depending on whether living entities are ignored or not
		RayTraceResult rayTrace = RayTracer.rayTrace(world, origin, endpoint, 0, false,
				false, false, Entity.class, RayTracer.ignoreEntityFilter(player));

		if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
			if (world.getBlockState(rayTrace.getBlockPos()).getBlock() == ASBlocks.arcane_wall) {
				TileEntity tile = world.getTileEntity(rayTrace.getBlockPos());
				if (tile instanceof TileArcaneWall && ((TileArcaneWall) tile).isGenerated()) {
					((TileArcaneWall) tile).setBeingDispelled(true);
					if (!player.isCreative()) {
						consumeMana(stack, getManaCost(), player);
						return EnumActionResult.SUCCESS;
					}
				}
			}
		}

		return EnumActionResult.PASS;
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {
		// Overridden to do nothing to stop repair things from 'repairing' the mana in a wand
	}

	@Override
	public void setMana(ItemStack stack, int mana) {
		// Using super (which can only be done from in here) bypasses the above override
		super.setDamage(stack, getManaCapacity(stack) - mana);
	}

	@Override
	public int getMana(ItemStack stack) {
		return getManaCapacity(stack) - getDamage(stack);
	}

	@Override
	public int getManaCapacity(ItemStack stack) {
		return this.getMaxDamage(stack);
	}

	private int getManaCost() {
		return Settings.generalSettings.vault_key_usage_mana_cost;
	}

	@Override
	public int getSpellSlotCount(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean showTooltip(ItemStack stack) {
		return false;
	}

	@Override
	public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks) {
		return WandHelper.rechargeManaOnApplyButtonPressed(centre, crystals);
	}

}
