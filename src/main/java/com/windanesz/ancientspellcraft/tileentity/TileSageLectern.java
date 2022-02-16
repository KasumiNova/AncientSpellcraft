package com.windanesz.ancientspellcraft.tileentity;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.block.BlockSageLectern;
import com.windanesz.ancientspellcraft.item.ItemSageTome;
import com.windanesz.ancientspellcraft.registry.AncientSpellcraftItems;
import com.windanesz.ancientspellcraft.util.WizardArmourUtils;
import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.Random;

/**
 * Controls the book animations and remembers the GUI state when not in use.
 */
public class TileSageLectern extends TileEntity implements ITickable, IInventory {
	public static final double BOOK_OPEN_DISTANCE = 1;
	public static final int BOOK_SLOT = 0;
	private static final Random rand = new Random();
	public int ticksExisted;
	public float pageFlip;
	public float pageFlipPrev;
	public float flipT;
	public float flipA;
	public float bookSpread;
	public float bookSpreadPrev;
	private NonNullList<ItemStack> inventory;
	private boolean inUse = false;
	private EntityPlayer currentPlayer;

	public TileSageLectern() {
		this.inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public EntityPlayer getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(EntityPlayer currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public String getName() {
		return "container." + AncientSpellcraft.MODID + ":sage_lectern";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public void update() {

		this.bookSpreadPrev = this.bookSpread;
		BlockPos pos = new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ());
		EnumFacing facing = world.getBlockState(this.pos).getBlock() instanceof BlockSageLectern ? world.getBlockState(this.pos).getValue(BlockHorizontal.FACING) : null;
		if (facing != null) {
			pos = pos.offset(facing);
		}
		EntityPlayer player = this.world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, BOOK_OPEN_DISTANCE, false);

		if (player != null && shouldBookOpen(player)) {

			this.bookSpread += 0.1f;

			if (this.bookSpread < 0.5f || rand.nextInt(40) == 0) {
				float f1 = this.flipT;
				while (f1 == flipT) { this.flipT += (float) (rand.nextInt(4) - rand.nextInt(4)); }
			}

			if (world.isRemote) {

				int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(this.getBookElement());

				Element element = WizardArmourUtils.getFullSetElementForClass(player, ItemWizardArmour.ArmourClass.SAGE);

				if (element != Element.MAGIC && element == this.getBookElement()) {
					for (int i = 0; i < 2; i++) {
						ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(this.pos.getX() + rand.nextFloat(), this.pos.getY() + 1, this.pos.getZ() + rand.nextFloat())
								.vel(0, 0.05 + (rand.nextFloat() * 0.1), 0).clr(colours[1]).fade(colours[2]).time(40).shaded(false).spawn(world);
					}
				}
			}

		} else {
			this.bookSpread -= 0.1f;
		}

		this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0f, 1.0f);

		this.ticksExisted++;

		this.pageFlipPrev = this.pageFlip;
		float f = (this.flipT - this.pageFlip) * 0.4f;
		f = MathHelper.clamp(f, -0.2f, 0.2f);
		this.flipA += (f - this.flipA) * 0.9f;
		this.pageFlip += this.flipA;

	}

	/**
	 * Returns true if the lectern has an item (a book)
	 */
	public boolean hasItem() { return !getStackInSlot(BOOK_SLOT).isEmpty(); }

	public ItemStack getBookSlotItem() { return getStackInSlot(BOOK_SLOT) == null ? ItemStack.EMPTY : getStackInSlot(BOOK_SLOT); }

	public Element getBookElement() {
		if (getBookSlotItem().getItem() instanceof ItemSageTome) {
			return ((ItemSageTome) getBookSlotItem().getItem()).element;
		}
		return Element.MAGIC;
	}

	///////////////////////// IInventory field implementations /////////////////////////

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return 3;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < getSizeInventory(); i++) {
			if (!getStackInSlot(i).isEmpty()) { return false; }
		}
		return true;
	}

	public boolean shouldBookOpen(EntityPlayer player) {
		if (!this.hasItem()) { return true; }

		if (this.getBookSlotItem().getItem() instanceof ItemSageTome) {
			return WizardArmourUtils.isWearingFullSet(player, null, ItemWizardArmour.ArmourClass.SAGE);
		}

		return true;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, index, count);

		if (!itemstack.isEmpty()) {
			this.markDirty();
		}

		return itemstack;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.get(slot);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.inventory, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}

		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		boolean withinDistance = world.getTileEntity(pos) == this && player.getDistanceSqToCenter(pos) < 64;
		return withinDistance && (!inUse || (player == getCurrentPlayer()));
	}

	@Override
	public void openInventory(EntityPlayer player) {
		this.setInUse(true);
		this.setCurrentPlayer(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		this.setInUse(false);
		this.setCurrentPlayer(null);
	}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack stack) {
		if (stack == ItemStack.EMPTY) { return true; }

		if (slotNumber == 0 && stack.getItem() instanceof ItemSageTome) {
			return true;
		} else if (slotNumber == 1) {
			return stack.getItem() == AncientSpellcraftItems.enchanted_page;
		}
		return false;
	}

	public int getFieldCount() {
		return 0;
	}

	public int getField(int id) {
		return 0;
	}

	public void setField(int id, int value) {
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++) {
			setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	/**
	 * Called to manually sync the tile entity with clients.
	 */
	public void sync() {
		this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound); // Confusingly, this method both writes to the supplied compound and returns it

		NBTTagList inventoryList = new NBTTagList();
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack stack = getStackInSlot(i);
			if (!stack.isEmpty()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				inventoryList.appendTag(tag);
			}
		}

		NBTExtras.storeTagSafely(compound, "Inventory", inventoryList);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		NBTTagList inventoryList = compound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < inventoryList.tagCount(); i++) {
			NBTTagCompound tag = inventoryList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < getSizeInventory()) {
				setInventorySlotContents(slot, new ItemStack(tag));
			}
		}
	}

	@Override
	public final NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

}
