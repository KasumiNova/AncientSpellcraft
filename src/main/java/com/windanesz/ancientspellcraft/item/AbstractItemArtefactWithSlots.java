package com.windanesz.ancientspellcraft.item;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.client.gui.GuiHandlerAS;
import com.windanesz.ancientspellcraft.registry.ASTabs;
import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public abstract class AbstractItemArtefactWithSlots extends ItemArtefact implements IItemWithSlots {

	private final int slotCount;

	private final int rows;
	private final int columns;
	private final boolean hasGUI;

	public AbstractItemArtefactWithSlots(EnumRarity rarity, Type type, int rows, int columns, boolean hasGUI) {
		super(rarity, type);
		this.rows = rows;
		this.columns = columns;
		this.slotCount = rows * columns;
		this.hasGUI = hasGUI;
		this.setCreativeTab(ASTabs.ANCIENTSPELLCRAFT_GEAR);
	}


	public int getRowCount() {
		return rows;
	}

	public int getColumnCount() {
		return columns;
	}

	@Override
	public int getSlotCount() {
		return this.slotCount;
	}

	@Override
	public boolean hasGUI() {
		return hasGUI;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStack = player.getHeldItem(hand);

		player.openGui(AncientSpellcraft.MODID, GuiHandlerAS.GUI_1_SLOT, world, hand.ordinal(), 0, 0);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
	}

	public static boolean isSlotEmpty(ItemStack stack, int slot) {
		if (stack.getItem() instanceof AbstractItemArtefactWithSlots) {
			int maxCount = ((AbstractItemArtefactWithSlots) stack.getItem()).getSlotCount();

			if (slot <= maxCount) {
				if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Items")) {
					NBTTagList items = stack.getTagCompound().getTagList("Items", 10);

					NBTTagCompound nbttagcompound = items.getCompoundTagAt(slot);
					return (new ItemStack(nbttagcompound)).isEmpty();
				}
			}
		}
		return true;
	}

	public static ItemStack getItemForSlot(ItemStack stack, int slot) {
		if (stack.getItem() instanceof AbstractItemArtefactWithSlots) {
			int maxCount = ((AbstractItemArtefactWithSlots) stack.getItem()).getSlotCount();

			if (slot <= maxCount) {
				if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Items")) {
					NBTTagList items = stack.getTagCompound().getTagList("Items", 10);

					NBTTagCompound nbttagcompound = items.getCompoundTagAt(slot);
					return new ItemStack(nbttagcompound);
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public static void setItemForSlot(ItemStack bag, ItemStack itemStack, int slot) {
		if (bag.getItem() instanceof AbstractItemArtefactWithSlots) {
			int maxCount = ((AbstractItemArtefactWithSlots) bag.getItem()).getSlotCount();

			if (slot <= maxCount) {
				NBTTagCompound nbt = bag.getTagCompound();
				if (nbt == null) {
					nbt = new NBTTagCompound();
				}

				NBTTagList items = new NBTTagList();
				if (nbt.hasKey("Items")) {
					items = bag.getTagCompound().getTagList("Items", 10);
				}
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) slot);
				itemStack.writeToNBT(nbttagcompound);
				items.appendTag(nbttagcompound);

				nbt.setTag("Items", items);
				bag.setTagCompound(nbt);
			}
		}
	}
}
