package com.windanesz.ancientspellcraft.tileentity;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.client.gui.ContainerCrystalBallCognizance;
import com.windanesz.ancientspellcraft.registry.AncientSpellcraftBlocks;
import com.windanesz.ancientspellcraft.util.ASUtils;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.item.ItemCrystal;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class TileCrystalBallCognizance extends TileEntity implements IInventory, ITickable {
	private NonNullList<ItemStack> inventory;

	private int researchDuration;
	private int researchProgress;
	//	private int researchCompleted = 0;
	public int currentHintId = 0;
	public int currentHintTypeId = 0;

	private boolean changedResearchState = false;

	private boolean inUse = false;
	private EntityPlayer currentPlayer;

	public WizardData getPlayerWizardData() {
		return playerWizardData;
	}

	private WizardData playerWizardData;

	/**
	 * The number of ticks that the furnace will keep burning
	 */
	//	private int furnaceBurnTime;

	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);

	public int tickCount;
	public float pageFlip;
	public float pageFlipPrev;
	public float flipT;
	public float flipA;
	public float bookSpread;
	public float skullRotation;
	public float skullRotationPrev;
	public float tRot;

	//	private static final Random rand = new Random();
	//	private String customName;

	public TileCrystalBallCognizance() {
		inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
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

	@Override
	public String getName() {
		return "container." + AncientSpellcraft.MODID + ":crystal_ball_cognizance";
	}

	@Override
	public boolean hasCustomName() {
		return false;
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

		researchDuration = compound.getShort("researchDuration");
		researchProgress = compound.getShort("researchProgress");
		currentHintId = compound.getShort("currentHintId");
		currentHintTypeId = compound.getShort("currentHintTypeId");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);

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

		compound.setInteger("researchDuration", (short) this.researchDuration); // calculated
		compound.setInteger("researchProgress", (short) this.researchProgress);
		compound.setInteger("currentHintId", (short) this.currentHintId);
		compound.setInteger("currentHintTypeId", (short) this.currentHintTypeId);

		NBTExtras.storeTagSafely(compound, "Inventory", inventoryList);
		return compound;
	}

	/**
	 * Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
	 * Warning - although our getUpdatePacket() uses this method, vanilla also calls it directly, so don't remove it.
	 */
	@Override
	public final NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockType();
		if (type == AncientSpellcraftBlocks.CRYSTAL_BALL_COGNIZANCE) {
			bb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
		} else if (type != null) {
			AxisAlignedBB cbb = this.getWorld().getBlockState(pos).getBoundingBox(world, pos);
			if (cbb != null) {
				bb = cbb;
			}
		}
		return bb;
	}

	private boolean canBeginResearch() {
		return (isResearchFinished() || researchProgress == 0) && hasBookOrScrollToResearch() && hasCrystalForFuel();
	}

	private boolean isResearchFinished() {
		return (researchProgress != 0 && researchProgress == researchDuration);
	}

	private boolean canResearch() {
		//		System.out.println("canresearch called");
		//		System.out.println("hascrystalfuel: " + hasCrystalForFuel());
		//		System.out.println("hasBookOrScrollToResearch(): " + hasBookOrScrollToResearch());
		//		if (hasCrystalForFuel() && hasBookOrScrollToResearch()) {
		if (hasBookOrScrollToResearch()) {
			//			System.out.println("can research this");
			return true;
		}
		return false;
	}

	private boolean isCurrentBookKnown() {
		if (getCurrentSpell() != null) {
			//			System.out.println("current spell is known: " + playerWizardData.hasSpellBeenDiscovered(getCurrentSpell()));
			return playerWizardData.hasSpellBeenDiscovered(getCurrentSpell());
		}
		return false;
	}

	public Spell getCurrentSpell() {
		if (getBookStack().getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			return Spell.byMetadata(getBookStack().getItemDamage());
		}
		return null;
	}

	public ItemStack getCrystalStack() {
		return inventory.get(0);
	}

	public ItemStack getBookStack() {
		return inventory.get(1);
	}

	// this function indicates whether container texture should be drawn
	@SideOnly(Side.CLIENT)
	public static boolean func_174903_a(IInventory parIInventory) {
		return true;
	}

	@SuppressWarnings("Duplicates")
	public void update() {
		//		System.out.println("researchProgress: " + this.researchProgress);
		this.skullRotationPrev = this.skullRotation;
		EntityPlayer entityplayer = this.world.getClosestPlayer((double) ((float) this.pos.getX() + 0.5F), (double) ((float) this.pos.getY() + 0.5F), (double) ((float) this.pos.getZ() + 0.5F), 2.5D, false);

		//		if (entityplayer != null && entityplayer.openContainer != null && entityplayer.openContainer instanceof ContainerCrystalBallCognizance &&
		//				((ContainerCrystalBallCognizance) entityplayer.openContainer).getTileCrystalBall().getPos() == this.getPos()) {
		//			//			System.out.println("nearest player has the tile gui open ...");
		//		}

		this.tRot += 0.02F;
		this.bookSpread -= 0.1F;
		//		}

		while (this.skullRotation >= (float) Math.PI) {
			this.skullRotation -= ((float) Math.PI * 2F);
		}

		while (this.skullRotation < -(float) Math.PI) {
			this.skullRotation += ((float) Math.PI * 2F);
		}

		while (this.tRot >= (float) Math.PI) {
			this.tRot -= ((float) Math.PI * 2F);
		}

		while (this.tRot < -(float) Math.PI) {
			this.tRot += ((float) Math.PI * 2F);
		}

		float f2;

		for (f2 = this.tRot - this.skullRotation; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) {
			;
		}

		while (f2 < -(float) Math.PI) {
			f2 += ((float) Math.PI * 2F);
		}

		this.skullRotation += f2 * 0.4F;
		this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
		++this.tickCount;
		this.pageFlipPrev = this.pageFlip;
		float f = (this.flipT - this.pageFlip) * 0.4F;
		float f3 = 0.2F;
		f = MathHelper.clamp(f, -0.2F, 0.2F);
		this.flipA += (f - this.flipA) * 0.9F;
		this.pageFlip += this.flipA;

		//		if (researchDuration == 0 && hasBookOrScroll()) {
		//			System.out.println("setting res duration");
		//			setResearchDuration();
		//		}

		if (getBookStack().isEmpty()) {
			//			System.out.println("stack empty");
			researchProgress = 0;
			setResearchDuration(0);
		}

		// button was activated
		//		if (buttonClicked == 1) {
		//			onResearchButtonClicked();
		//
		//		}

		if (!world.isRemote) {

			//			System.out.println("researchprogress: " + researchProgress);
			//			System.out.println("buttonclicked: " + buttonClicked);
			//
			//			System.out.println("in use: " + inUse);
			//			if (!inUse && currentPlayer != null) {
			//				currentPlayer = null;
			//			}
			/// todo lose progress if new player opens it ... NOPE

			if (inUse && currentPlayer != null) {
				if (canResearch()) {

					//					System.out.println("researchduration before: " + researchDuration);
					//					setResearchDuration();
					//					System.out.println("researchduration after: " + researchDuration);

					if (shouldReseach()) {
						progressResearch();
						if (researchProgress >= researchDuration) {
							onResearchComplete();
						}
						changedResearchState = true;
						//						System.out.println("researchprogress: " + researchProgress);
					}

				} else {
					researchProgress = 0;
				}
				if (shouldDisplayHint()) {

				}
			}
		}

		if (changedResearchState) {
			markDirty();
		}
	}

	public void setResearchDuration() {
		//		System.out.println("setResearchDuration() called..");
		researchDuration = getResearchDuration(getCurrentSpell());
		this.markDirty();
	}

	public void setResearchDuration(int duration) {
		researchDuration = duration;
		this.markDirty();
	}

	public boolean shouldReseach() {
		//		System.out.println("ch");
		return (researchProgress != 0 && researchDuration > researchProgress);
	}

	public void progressResearch() {
		//		System.out.println("progressResearch()");
		researchProgress++;
	}

	public void onResearchComplete() {
		double special = AncientSpellcraft.rand.nextDouble();
		if (special < 0.2) {
			// discover spell

			this.currentHintTypeId = 2; // discovered
			int count = ContainerCrystalBallCognizance.HINTS_COUNT.get("discovered");
			int id = ASUtils.randIntBetween(1, count);
			this.currentHintId = id;

			if (!MinecraftForge.EVENT_BUS.post(new DiscoverSpellEvent(getCurrentPlayer(), getCurrentSpell(),
					DiscoverSpellEvent.Source.IDENTIFICATION_SCROLL))) {
				// Identification scrolls give the chat readout in creative mode, otherwise it looks like
				// nothing happens!
				if (getPlayerWizardData().discoverSpell(getCurrentSpell()) && !world.isRemote) {
					playerWizardData.sync();
				}

				getCurrentPlayer().playSound(WizardrySounds.MISC_DISCOVER_SPELL, 1.25f, 1);
				if (!world.isRemote)
					getCurrentPlayer().sendMessage(new TextComponentTranslation("spell.discover",
							getCurrentSpell().getNameForTranslationFormatted()));
					setPlayerWizardData(currentPlayer);
			}

		} else if (special < 0.4) {
			// failed attempt
			this.currentHintTypeId = 1; // failed
			int count = ContainerCrystalBallCognizance.HINTS_COUNT.get("failed");
			int id = ASUtils.randIntBetween(1, count);

			this.currentHintId = id;
		} else {

			//		System.out.println("onResearchComplete()");
			Spell spell = getCurrentSpell();
			String name = spell.getUnlocalisedName();
			String type = spell.getType().getUnlocalisedName();
			String element = spell.getElement().getName();
			System.out.println("name: " + spell.getUnlocalisedName());
			System.out.println("type: " + spell.getType().getUnlocalisedName());
			System.out.println("element: " + spell.getElement().getName());

			boolean t = ContainerCrystalBallCognizance.HINT_TYPES.contains(type);
			boolean n = ContainerCrystalBallCognizance.HINT_TYPES.contains(name);
			boolean e = ContainerCrystalBallCognizance.HINT_TYPES.contains(element);

			List<String> list = new ArrayList<String>() {};

			if (n) {
				int i = ContainerCrystalBallCognizance.HINTS_COUNT.get(name);
				//			System.out.println("count for name: " + i);
				list.add(name);
			}
			if (t) {
				int i = ContainerCrystalBallCognizance.HINTS_COUNT.get(type);
				//			System.out.println("count for type: " + i);
				list.add(type);
			}
			if (e) {
				int i = ContainerCrystalBallCognizance.HINTS_COUNT.get(element);
				//			System.out.println("count for element: " + i);
				list.add(element);
			}

			String selected = ASUtils.getRandomListItem(list);
			int count = ContainerCrystalBallCognizance.HINTS_COUNT.get(selected);
			int id = ASUtils.randIntBetween(1, count);
			String string = "gui.ancientspellcraft:crystal_ball_cognizance.hint." + selected + "." + id;
			//		System.out.println("this will be the final stuff: " + string);
			this.currentHintTypeId = ContainerCrystalBallCognizance.HINT_TYPES.indexOf(selected);
			this.currentHintId = id;

		}

		//		System.out.println("currentHintTypeId : " + currentHintTypeId);
		//		System.out.println("currentHintId : " + currentHintId);

		//		System.out.println("the string: " + I18n.format(string));
		//
		//		HashMap<Integer, Boolean> test = new LinkedHashMap<Integer, Boolean>() {
		//			{
		//				put(0, t);
		//				put(1, n);
		//				put(2, e);
		//			}
		//		};

		//		if (test.values().contains(true)) {
		//			int i = ASUtils.getRandomMapId(test);
		//			while (test.get(i)) {
		//				System.out.println("looping ...");
		//				i = ASUtils.getRandomMapId(test);
		//			}
		//
		//			if ()
		//
		//			System.out.println("new final key: " + i + " (0 = type, 1 = name, 2 = element)");
		//
		//			int index = 0;
		//			if (i == 0) {
		//				int count = ContainerCrystalBallCognizance.HINTS_COUNT.get(type);
		//				System.out.println("type count: " + count);
		////				index = ContainerCrystalBallCognizance.HINT_TYPES.indexOf(type);
		//			}
		////			else if (i == 1) {
		////				int count = ContainerCrystalBallCognizance.HINTS_COUNT.get(name);
		////				System.out.println("name count: " + count);
		//////				index = ContainerCrystalBallCognizance.HINT_TYPES.indexOf(name);
		////
		////			}
		//			else if (i == 2) {
		//				int count = ContainerCrystalBallCognizance.HINTS_COUNT.get(element);
		//				System.out.println("element count: "+ count);
		////				index = ContainerCrystalBallCognizance.HINT_TYPES.indexOf(element);
		//			}
		//			System.out.println("index: " + index);

		//
		//			Object firstKey = ContainerCrystalBallCognizance.HINTS_COUNT.keySet().toArray()[index];
		//			System.out.println("the key: " + (String) firstKey);

		//			ContainerCrystalBallCognizance.HINTS_COUNT.keySet().iterator()
		//			int count = (int) ContainerCrystalBallCognizance.HINTS_COUNT.values().toArray()[index];
		//			System.out.println("count: " + count);

		//
		//		} else {
		//			System.out.println("no true value! this shouldnt happen");
		//		}

	}

	/**
	 * @return true if there is at least one crystal in the crystal inventory slot
	 */
	public boolean hasCrystalForFuel() {
		boolean b = (getCrystalStack() != null && !getCrystalStack().isEmpty() && getCrystalStack().getItem() instanceof ItemCrystal);
		if (b && getCrystalStack().getCount() >= getResearchCost(getCurrentSpell())) {
			return true;
		}
		return false;
	}

	//	public boolean hasBookOrScroll() {
	//		return (!getBookStack().isEmpty() && (getBookStack().getItem() instanceof ItemSpellBook || getBookStack().getItem() instanceof ItemScroll));
	//	}

	public boolean hasBookOrScrollToResearch() {
		return (!getBookStack().isEmpty() && (getBookStack().getItem() instanceof ItemSpellBook || getBookStack().getItem() instanceof ItemScroll) && !isCurrentBookKnown());
	}

	//	public boolean researchingSomething() {
	//		return true;
	//	}

	//////////////////////////

	public int getResearchCost(Spell spell) {
		switch (spell.getTier()) {
			case NOVICE:
				return 1;
			case APPRENTICE:
				return 1;
			case ADVANCED:
				return 1;
			case MASTER:
				return 2;
			default:
				return 1;
		}
	}

	public static int getResearchDuration(Spell spell) {
		switch (spell.getTier()) {
			case NOVICE:
				return 10;
			case APPRENTICE:
				return 15;
			case ADVANCED:
				return 20;
			case MASTER:
				return 25;
			default:
				return 10;
		}
	}

	//	private void onResearchButtonClicked() {
	//		System.out.println("buttonclicked!!!!");
	//		System.out.println("setting back to 0");
	//		if (canBeginResearch() && researchProgress == 0) {
	//		researchProgress = 1;
	//		}
	//		// flipping back value
	//		buttonClicked = 0;
	//	}

	public void attemptBeginResearch() {
		if (canBeginResearch()) {
			System.out.println("called");
			researchProgress = 1;
			int researchcost = getResearchCost(getCurrentSpell());
			System.out.println("cost: " + researchcost);
			System.out.println("shrink ... ");
			getCrystalStack().shrink(researchcost);
			System.out.println("shrinked ... ");
		}
	}

	public boolean shouldDisplayHint() {
		return researchDuration != 0 && researchDuration == researchProgress;
	}

	//	public int getCurrentHintTypeId() {
	//		return currentHintTypeId;
	//	}

	///////////////////////// IInventory field implementations /////////////////////////

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < getSizeInventory(); i++) {
			if (!getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.get(slot);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 */
	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, slot, amount);
		if (!itemstack.isEmpty()) {
			this.markDirty();
		}
		return itemstack;
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {

		ItemStack itemstack = inventory.get(slot);
		boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
		inventory.set(slot, stack);

		if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}
		if (slot == ContainerCrystalBallCognizance.CRYSTAL_SLOT) {
			//NOOP
			//			System.out.println("slot 0 called");
			//			researchDuration = 0;
		}
		if (slot == ContainerCrystalBallCognizance.BOOK_SLOT) {
			//			System.out.println("findme set res dur");
			researchProgress = 0;
			setResearchDuration();
			this.currentHintId = 0;
			this.currentHintTypeId = 0;
			//			System.out.println("slot 1 called");
			//			researchProgress = 0;
		}

		if (slot == 2) {
			//			System.out.println("slot 2 called");
			//			no slot 2 TODO remove
		}
		markDirty();
		//		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		boolean withinDistance = world.getTileEntity(pos) == this && player.getDistanceSqToCenter(pos) < 64;
		return withinDistance && (!inUse || (player == getCurrentPlayer()));
	}

	/**
	 * Sets the current player user and limits usage to one player at a time. Sets the wizarddata to the current user data.
	 *
	 * @param player
	 */
	@Override
	public void openInventory(EntityPlayer player) {
		System.out.println("gui opened, setting player and use to true");
		this.setInUse(true);
		this.setCurrentPlayer(player);
		setPlayerWizardData(player);
	}

	public void setPlayerWizardData(EntityPlayer player) {
		this.playerWizardData = WizardData.get(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		System.out.println("inventory closed, setting inuse and player to null/false");
		this.setInUse(false);
		this.setCurrentPlayer(null);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
	 * guis use Slot.isItemValid
	 */
	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack stack) {

		if (stack == ItemStack.EMPTY)
			return true;

		if (slotNumber == ContainerCrystalBallCognizance.CRYSTAL_SLOT) {
			return stack.getItem() instanceof ItemCrystal;
		} else if (slotNumber == ContainerCrystalBallCognizance.BOOK_SLOT) {
			return stack.getItem() instanceof ItemSpellBook || stack.getItem() instanceof ItemScroll;
		}
		return false;
	}

	public int getFieldCount() {
		return 4;
	}

	public int getField(int id) {
		//		System.out.println("getfield called");
		switch (id) {
			case 0:
				return this.researchProgress;
			case 1:
				//				System.out.println("case 1: researchduration: " + researchDuration);
				return this.researchDuration;
			case 2:
				//				System.out.println("get currentHintTypeId");
				return this.currentHintTypeId;
			case 3:
				//				System.out.println("get currentHintId");
				return this.currentHintId;
			default:
				return 0;
		}
	}

	public void setField(int id, int value) {
		switch (id) {
			case 0:
				this.researchProgress = value;
				break;
			case 1:
				this.researchDuration = value;
				break;
			case 2:
				this.currentHintTypeId = value;
				break;
			case 3:
				this.currentHintId = value;
				break;
		}
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++) {
			setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	///////////////////////// IInventory field implementations /////////////////////////
}
