package com.windanesz.ancientspellcraft.tileentity;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.block.BlockSageLectern;
import com.windanesz.ancientspellcraft.client.gui.ContainerSageLectern;
import com.windanesz.ancientspellcraft.item.ItemSageTome;
import com.windanesz.ancientspellcraft.item.ItemTransmutationScroll;
import com.windanesz.ancientspellcraft.item.WizardClassWeaponHelper;
import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.spell.IClassSpell;
import com.windanesz.ancientspellcraft.spell.SpellLecternInteract;
import com.windanesz.ancientspellcraft.util.WizardArmourUtils;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellProperties;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Controls the book animations and remembers the GUI state when not in use.
 */
public class TileSageLectern extends TileEntity implements ITickable, IInventory {

	private static final String NATURAL_NBT_KEY = "NaturallyGenerated";

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

	public void setNatural(boolean natural) {
		this.natural = natural;
	}

	public boolean isNatural() {
		return natural;
	}

	private boolean natural = false;
	private List<SpellLecternInteract> spellEffects = new ArrayList<>();

	public TileSageLectern() {
		this.inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
	}

	public List<SpellLecternInteract> getSpellEffects() {
		return spellEffects;
	}

	// remove any non-persistent effect if the slot item changes
	private void resetEffects() {
		spellEffects.removeIf(effect -> effect instanceof SpellLecternInteract && !effect.persistsOnBookRemoval());
		markDirty();
	}

	public void removeSpellEffect(SpellLecternInteract spell) {
		spellEffects.remove(spell);
		markDirty();
	}

	public void addSpellEffect(SpellLecternInteract spellEffects) {
		if (!this.spellEffects.contains(spellEffects)) {
			this.spellEffects.add(spellEffects);
			markDirty();
		}
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
				while (f1 == flipT) {this.flipT += (float) (rand.nextInt(4) - rand.nextInt(4));}
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
	public boolean hasItem() {return !getStackInSlot(BOOK_SLOT).isEmpty();}

	public ItemStack getBookSlotItem() {return getStackInSlot(BOOK_SLOT) == null ? ItemStack.EMPTY : getStackInSlot(BOOK_SLOT);}

	public Element getBookElement() {
		if (getBookSlotItem().getItem() instanceof ItemSageTome) {
			return ((ItemSageTome) getBookSlotItem().getItem()).element;
		}
		return Element.MAGIC;
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

		if (slot == 0 || slot == 1) {
			inventory.set(ContainerSageLectern.RESULT_SLOT, getResultItem());
		}

		resetEffects();
		markDirty();
	}

	private ItemStack getResultItem() {
		ItemStack stack0 = this.inventory.get(ContainerSageLectern.INPUT_SLOT_0);
		ItemStack stack1 = this.inventory.get(ContainerSageLectern.INPUT_SLOT_1);

		// tome progression
		if (stack0.getItem() instanceof ItemSageTome && stack1.getItem() == ASItems.enchanted_page) {

			Tier tier = ((ItemSageTome) stack0.getItem()).tier;

			if (tier.ordinal() < Tier.MASTER.ordinal()) {

				int progression = WandHelper.getProgression(stack0);

				if (progression >= tier.next().getProgression()) {
					int requiredPages = (tier.next().ordinal()) * 5;
					if (stack1.getCount() >= requiredPages) {
						return getProgressedTome(stack0);
					}
				}
			}

			// sage tome levelling
			//			if (tierLevel >= 0) {
			//				Tier tier = Tier.values()[tierLevel];
			//
			//			}
			//			if (true) { throw new IllegalArgumentException("Incomplete feature.."); }

		}

		return ItemStack.EMPTY;
	}

	private ItemStack getProgressedTome(ItemStack tome) {
		Tier tier = ((ItemSageTome) tome.getItem()).tier;
		Element element = ((ItemSageTome) tome.getItem()).element;
		ItemStack copy = tome.copy();

		// Next tier tome
		ItemStack progressedTome = new ItemStack(WizardClassWeaponHelper.getClassItemForTier(tier.next(), ItemWizardArmour.ArmourClass.SAGE, element));
		progressedTome.setTagCompound(copy.getTagCompound());
		return progressedTome;
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
			if (!getStackInSlot(i).isEmpty()) {return false;}
		}
		return true;
	}

	public boolean shouldBookOpen(EntityPlayer player) {
		if (!this.hasItem()) {return true;}

		if (this.getBookSlotItem().getItem() instanceof ItemSageTome) {
			return WizardArmourUtils.isWearingFullSet(player, null, ItemWizardArmour.ArmourClass.SAGE);
		}

		return true;
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, slot, count);

		if (slot != 2 && !itemstack.isEmpty()) {
			this.markDirty();
		}
		if (slot == 2 && !itemstack.isEmpty()) {
			ItemStack stack0 = this.inventory.get(0).copy();
			int requiredPages = ((ItemSageTome) stack0.getItem()).tier.next().ordinal() * 5;
			stack0.shrink(1);
			ItemStack stack1 = this.inventory.get(1).copy();

			stack1.shrink(requiredPages);

			this.inventory.set(0, stack0);
			this.inventory.set(1, stack1);
			this.markDirty();
		}

		return itemstack;
	}

	public int getRequiredEnchantedPageCount() {
		ItemStack tome = this.inventory.get(0);
		if (!tome.isEmpty() && tome.getItem() instanceof ItemSageTome) {
			Tier tier = ((ItemSageTome) tome.getItem()).tier;

			if (tier.ordinal() < Tier.MASTER.ordinal()) {
				int progression = WandHelper.getProgression(tome);
				if (progression >= tier.next().getProgression()) {
					return (tier.next().ordinal()) * 5;
				}
			}
		}
		return -1;
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

	//	/**
	//	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	//	 */
	//	@Override
	//	public void setInventorySlotContents(int slot, ItemStack stack) {
	//		this.inventory.set(slot, stack);
	//
	//		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
	//			stack.setCount(this.getInventoryStackLimit());
	//		}
	//
	//		this.markDirty();
	//	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
		if (stack == ItemStack.EMPTY) {return true;}

		if (slotNumber == 0 && stack.getItem() instanceof ItemSageTome && stack.getCount() == 1) {
			return true;
		} else if (slotNumber == 1) {
			return stack.getItem() == ASItems.enchanted_page;
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

		NBTExtras.storeTagSafely(compound, "SpellEffects", NBTExtras.listToNBT(spellEffects, s -> new NBTTagString(s.getRegistryName().toString())));
		NBTExtras.storeTagSafely(compound, "Inventory", inventoryList);
		compound.setBoolean(NATURAL_NBT_KEY, natural);
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

		if (compound.hasKey("SpellEffects")) {
			this.spellEffects = (List<SpellLecternInteract>) NBTExtras.NBTToList(compound.getTagList("SpellEffects", Constants.NBT.TAG_STRING),
					(NBTTagString tag) -> (SpellLecternInteract) Spell.get(tag.getString()));
		}

		if (compound.hasKey(NATURAL_NBT_KEY)) {
			this.setNatural(compound.getBoolean(NATURAL_NBT_KEY));
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

	public static void markAsNatural(NBTTagCompound nbt) {
		if (nbt != null && nbt.getString("id").equals(TileEntity.getKey(TileSageLectern.class).toString())) {
			nbt.setBoolean(NATURAL_NBT_KEY, true);
		}
	}

	public ItemStack pickRandomSpell(Random random, EntityPlayer player){
		Optional<Element> element;
		ItemWizardArmour.ArmourClass armourClass = WizardArmourUtils.getArmourClass(player);
		element = WizardArmourUtils.getFullSetElementForClassOptional(player, armourClass);
		List<Tier> possibleTiers = Arrays.asList(Tier.APPRENTICE, Tier.APPRENTICE, Tier.ADVANCED,  Tier.ADVANCED, Tier.MASTER);

		Optional<Element> finalElement = element;
		List<Spell> possibleSpells = Spell.getSpells(// Remove excluded tiers/elements immediately (mainly because empty tier checks should account for excluded elements)
				s -> possibleTiers.contains(s.getTier()) && !(s instanceof IClassSpell) && !s.applicableForItem(ASItems.ancient_spellcraft_scroll)
						&& (!finalElement.isPresent() || s.getElement() == finalElement.get()));

		possibleSpells.removeIf(s -> !s.isEnabled(SpellProperties.Context.BOOK));

		// Select a tier...

		// Remove all empty tiers
		possibleTiers.removeIf(t -> possibleSpells.stream().noneMatch(s -> s.getTier() == t)); // Lambdaception!

		if(possibleTiers.isEmpty()) return ItemStack.EMPTY;

		Tier tier = possibleTiers.get(random.nextInt(possibleTiers.size()));

		// Remove all spells that aren't of the selected tier
		possibleSpells.removeIf(s -> s.getTier() != tier);
		if(possibleSpells.isEmpty()) return ItemStack.EMPTY;


		if(player != null){

			float bias = 0.9f;
			// Archivist's eyeglass increases undiscovered bias by 0.4 up to a maximum of 0.9
			if(ItemArtefact.isArtefactActive(player, WizardryItems.charm_spell_discovery))
				bias = 0.99f;

			// Remove either the undiscovered spells or the discovered ones, depending on the bias
			WizardData data = WizardData.get(player);

			int discoveredCount = (int)possibleSpells.stream().filter(data::hasSpellBeenDiscovered).count();
			// If none have been discovered or they've all been discovered, don't bother!
			if(discoveredCount > 0 && discoveredCount < possibleSpells.size()){
				// Kinda unintuitive but it's very neat!
				boolean keepDiscovered = random.nextFloat() > 0.5f + 0.5f * bias;
				possibleSpells.removeIf(s -> keepDiscovered != data.hasSpellBeenDiscovered(s));
			}
		}

		Spell spell = possibleSpells.get(random.nextInt(possibleSpells.size())); // Finally pick a spell

		List<Item> bookTypeList = ForgeRegistries.ITEMS.getValuesCollection().stream().filter(i -> i instanceof ItemSpellBook).collect(Collectors.toList());
		Item book = Items.AIR;
		for (int i = 0; i < bookTypeList.size(); i++) {
			Item currentBook = bookTypeList.get(i);
			if (spell.applicableForItem(currentBook)) {
				book = currentBook;
			}
		}
		return new ItemStack(book, 1, spell.metadata());
	}
}
