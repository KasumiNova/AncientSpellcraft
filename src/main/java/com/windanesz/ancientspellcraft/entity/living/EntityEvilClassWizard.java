package com.windanesz.ancientspellcraft.entity.living;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.entity.ai.EntityAIAttackSpellImproved;
import com.windanesz.ancientspellcraft.entity.ai.EntityAIBattlemageMelee;
import com.windanesz.ancientspellcraft.entity.ai.EntityAIBattlemageSpellcasting;
import com.windanesz.ancientspellcraft.entity.ai.EntityAIBlockWithShield;
import com.windanesz.ancientspellcraft.entity.ai.IShieldUser;
import com.windanesz.ancientspellcraft.item.ItemWarlockOrb;
import com.windanesz.ancientspellcraft.item.WizardClassWeaponHelper;
import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.registry.ASSpells;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityAIAttackSpell;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class EntityEvilClassWizard extends EntityEvilWizard implements ICustomCooldown, IArmourClassWizard, IShieldUser {

	/**
	 * Decremented each tick while greater than 0. When a spell is cast, this is set to that spell's cooldown plus the
	 * base cooldown.
	 */
	private static final DataParameter<Integer> SHIELD_DISABLED_TICK = EntityDataManager.createKey(EntityEvilClassWizard.class, DataSerializers.VARINT);

	private static final ResourceLocation BATTLEMAGE_LOOT_TABLE = new ResourceLocation(AncientSpellcraft.MODID, "entities/evil_battlemage");
	private static final ResourceLocation SAGE_LOOT_TABLE = new ResourceLocation(AncientSpellcraft.MODID, "entities/evil_sage");
	private static final ResourceLocation WARLOCK_LOOT_TABLE = new ResourceLocation(AncientSpellcraft.MODID, "entities/evil_warlock");

	protected int cooldown;

	/**
	 * Data parameter for the wizard's armour class.
	 */
	private static final DataParameter<Integer> EVIL_WIZARD_ARMOUR_CLASS = EntityDataManager.createKey(EntityEvilClassWizard.class, DataSerializers.VARINT);

	// Field implementations
	private List<Spell> spells = new ArrayList<Spell>(4);

	private EntityAIAttackSpellImproved<EntityEvilClassWizard> spellCastingAIImproved = new EntityAIAttackSpellImproved<>(this, 0.5D, 14.0F, 30, 80);
	private final EntityAIBattlemageMelee entityAIBattlemageMelee = new EntityAIBattlemageMelee(this, 0.6D, false);
	private final EntityAIBattlemageSpellcasting entityAIBattlemageSpellcasting = new EntityAIBattlemageSpellcasting(this, 0.6D, 14.0F, 30, 50);

	public int getCooldown() {return cooldown;}

	public void setCooldown(int cooldown) {this.cooldown = cooldown;}

	@Override
	public int incrementCooldown() {return cooldown++;}

	@Override
	public int decrementCooldown() {return cooldown--;}

	public EntityEvilClassWizard(World world) {
		super(world);
		// discard the old AI
		this.tasks.taskEntries.removeIf(t -> t.action instanceof EntityAIAttackSpell);
		this.tasks.addTask(2, new EntityAIBlockWithShield(this));

		this.tasks.addTask(3, this.spellCastingAIImproved);
		this.tasks.addTask(3, this.entityAIBattlemageMelee);
		this.tasks.addTask(3, this.entityAIBattlemageSpellcasting);
	}

	@Override
	protected ResourceLocation getLootTable() {
		// TODO debug
		ResourceLocation loot = super.getLootTable();

		if (getArmourClass() == ItemWizardArmour.ArmourClass.BATTLEMAGE) {
			loot = BATTLEMAGE_LOOT_TABLE;
		} else if (getArmourClass() == ItemWizardArmour.ArmourClass.SAGE) {
			loot = SAGE_LOOT_TABLE;
		} else if (getArmourClass() == ItemWizardArmour.ArmourClass.WARLOCK) {
			loot = WARLOCK_LOOT_TABLE;
		}

		return loot;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(EVIL_WIZARD_ARMOUR_CLASS, 0);
		this.dataManager.register(SHIELD_DISABLED_TICK, 0);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {

		livingdata = super.onInitialSpawn(difficulty, livingdata);

		if (getElement() == null) {
			if (rand.nextInt(10) > 2) {
				this.setElement(Element.values()[rand.nextInt(Element.values().length - 1) + 1]);
			} else {
				this.setElement(Element.MAGIC);
			}
		}

		if (getArmourClass() == ItemWizardArmour.ArmourClass.WIZARD) {
			this.setArmourClass(ItemWizardArmour.ArmourClass.values()[world.rand.nextInt(3) + 1]);
		}

		Element element = this.getElement();

		// Adds armour.
		for (EntityEquipmentSlot slot : InventoryUtils.ARMOUR_SLOTS) {
			this.setItemStackToSlot(slot, new ItemStack(ItemWizardArmour.getArmour(element, this.getArmourClass(), slot)));
		}

		// Default chance is 0.085f, for reference.
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {this.setDropChance(slot, 0.0f);}

		// All wizards know magic missile, even if it is disabled.

		int spellCount;
		switch (this.getArmourClass()) {
			case SAGE:
				spellCount = 9;
			case WARLOCK:
				spellCount = 6;
			default: // BATTLEMAGE
				spellCount = 4;
		}

		Tier maxTier = IArmourClassWizard.populateSpells(this, spells, element, this.getArmourClass() == ItemWizardArmour.ArmourClass.SAGE || this.getArmourClass() == ItemWizardArmour.ArmourClass.WARLOCK, spellCount, rand);

		if (this.getArmourClass() == ItemWizardArmour.ArmourClass.WARLOCK) {
			spells.remove(Spells.magic_missile);
			if (rand.nextBoolean()) {
				spells.add(ASSpells.chaos_blast);
			}
			//spells.add(ASSpells.chaos_orb);
			spells.add(ASSpells.chaos_orb);
		}

		// Now done after the spells so it can take the tier into account.
		ItemStack wand = new ItemStack(WizardryItems.getWand(maxTier, element));
		ArrayList<Spell> list = new ArrayList<>(spells);
		list.add(Spells.heal);
		WandHelper.setSpells(wand, list.toArray(new Spell[5]));

		if (getArmourClass() == ItemWizardArmour.ArmourClass.BATTLEMAGE) {
			ItemStack sword = new ItemStack(ASItems.battlemage_sword_master);
			NBTTagCompound nbt = sword.getTagCompound();
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			nbt.setString(WizardClassWeaponHelper.ELEMENT_TAG, element.name());
			sword.setTagCompound(nbt);

			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, sword);
			this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ASItems.battlemage_shield));
			setAITask(ItemWizardArmour.ArmourClass.BATTLEMAGE);

		} else {
			if (getArmourClass() == ItemWizardArmour.ArmourClass.WARLOCK) {
				wand = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(AncientSpellcraft.MODID, "warlock_orb_" + Tier.values()[rand.nextInt(4)].toString().toLowerCase() + "_" + element.name().toLowerCase())));
				NBTTagCompound nbt = wand.getTagCompound();
				if (nbt == null) {
					nbt = new NBTTagCompound();
				}
				nbt.setString(WizardClassWeaponHelper.ELEMENT_TAG, element.name());
				wand.setTagCompound(nbt);
			}
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, wand);
			setAITask(ItemWizardArmour.ArmourClass.WIZARD);
		}

		// Default chance is 0.085f, for reference.
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {this.setDropChance(slot, 0.0f);}

		return livingdata;
	}

	private void setAITask(ItemWizardArmour.ArmourClass armourClass) {
		if (armourClass == ItemWizardArmour.ArmourClass.BATTLEMAGE) {
			this.tasks.taskEntries.removeIf(t -> t.action == spellCastingAIImproved);

		} else {
			this.tasks.taskEntries.removeIf(t -> t.action == entityAIBattlemageSpellcasting);
			this.tasks.taskEntries.removeIf(t -> t.action == entityAIBattlemageMelee);
		}
	}

	public ItemWizardArmour.ArmourClass getArmourClass() {
		return ItemWizardArmour.ArmourClass.values()[this.dataManager.get(EVIL_WIZARD_ARMOUR_CLASS)];
	}

	public void setArmourClass(ItemWizardArmour.ArmourClass armourClass) {
		this.dataManager.set(EVIL_WIZARD_ARMOUR_CLASS, armourClass.ordinal());
	}

	@Override
	public List<Spell> getSpells() {return spells;}

	@Override
	public ITextComponent getDisplayName() {
		if (this.hasCustomName()) {
			TextComponentString textcomponentstring = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
			textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
			textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
			// don't see why this could happen but it might fix #81
			if (textcomponentstring == null) {
				ITextComponent wizardName = new TextComponentTranslation("class_element." + getElement().getName() + ".wizard");
				ITextComponent className = getArmourClassNameFor(this.getArmourClass());
				new TextComponentTranslation("entity." + this.getEntityString() + "_combined.name", wizardName, className);
			}
			return textcomponentstring;
		}

		// no-element wizards should only display the class name
		if (this.getElement() == Element.MAGIC) {
			return getArmourClassNameFor(this.getArmourClass());
		}

		ITextComponent wizardName = new TextComponentTranslation("class_element." + getElement().getName() + ".wizard");
		ITextComponent className = getArmourClassNameFor(this.getArmourClass());

		return new TextComponentTranslation("entity." + this.getEntityString() + "_combined.name", wizardName, className);
	}

	@Override
	public SpellModifiers getModifiers() {
		return this.getArmourClass() == ItemWizardArmour.ArmourClass.WARLOCK ? this.getWarlockSpellModifiers() : super.getModifiers();
	}

	private SpellModifiers getWarlockSpellModifiers() {
		SpellModifiers modifiers = new SpellModifiers();
		if (this.getHeldItemMainhand().getItem() instanceof ItemWarlockOrb) {
			float potency = (float) (1 + ((((ItemWarlockOrb) this.getHeldItemMainhand().getItem()).tier.ordinal() + 1) * 0.15));
			modifiers.set(SpellModifiers.POTENCY, potency, false);
		}
		return modifiers;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {

		super.writeEntityToNBT(nbt);
		NBTExtras.storeTagSafely(nbt, "spells", NBTExtras.listToNBT(spells, spell -> new NBTTagInt(spell.metadata())));
		nbt.setInteger("armour_class", this.getArmourClass().ordinal());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {

		super.readEntityFromNBT(nbt);
		this.spells = (List<Spell>) NBTExtras.NBTToList(nbt.getTagList("spells", Constants.NBT.TAG_INT), (NBTTagInt tag) -> Spell.byMetadata(tag.getInt()));
		ItemWizardArmour.ArmourClass armourClass = ItemWizardArmour.ArmourClass.values()[nbt.getInteger("armour_class")];
		this.setArmourClass(armourClass);
		setAITask(armourClass);

		// no wandering for wizards with a tent, or they'll never go back to it
		if (hasStructure) {
			this.tasks.taskEntries.removeIf(t -> t.action instanceof EntityAIWander);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (getShieldDisabledTick() > 0) {
			decrementShieldDisabledTick();
		}
	}

	// vanilla method with a check for shield items to ignore canContinueUsing
	@Override
	protected void updateActiveHand() {
		if (isHandActive()) {
			ItemStack itemstack = getHeldItem(getActiveHand());
			if (net.minecraftforge.common.ForgeHooks.canContinueUsing(activeItemStack, itemstack) || itemstack.getItem().isShield(itemstack, this)) {
				activeItemStack = itemstack;
			}

			if (itemstack == activeItemStack) {
				if (!activeItemStack.isEmpty()) {
					activeItemStackUseCount = net.minecraftforge.event.ForgeEventFactory.onItemUseTick(this, activeItemStack, activeItemStackUseCount);
					if (activeItemStackUseCount > 0) {activeItemStack.getItem().onUsingTick(activeItemStack, this, activeItemStackUseCount);}
				}

				if (getItemInUseCount() <= 25 && getItemInUseCount() % 4 == 0) {
					updateItemUse(activeItemStack, 5);
				}

				if ((--activeItemStackUseCount <= 0 || 20000 - activeItemStackUseCount > Math.max(25, rand.nextInt(100))) && !world.isRemote) {
					onItemUseFinish();
				}
			} else {
				resetActiveHand();
			}
		}
	}

	private ItemStack getShieldStack() {
		return getHeldItem(EnumHand.OFF_HAND);
	}

	public void setShieldStack(ItemStack stack) {
		setHeldItem(EnumHand.OFF_HAND, stack);
	}

	public void setShieldDisabledTick(int count) {
		dataManager.set(SHIELD_DISABLED_TICK, count);
	}

	public int getShieldDisabledTick() {
		return dataManager.get(SHIELD_DISABLED_TICK);
	}

	public void decrementShieldDisabledTick() {
		dataManager.set(SHIELD_DISABLED_TICK, (dataManager.get(SHIELD_DISABLED_TICK)) - 1);
	}


	@Override
	protected void blockUsingShield(EntityLivingBase attacker) {
		attacker.knockBack(this, 0.1F, posX - attacker.posX, posZ - attacker.posZ);

		if (attacker.getHeldItemMainhand().getItem().canDisableShield(attacker.getHeldItemMainhand(), getActiveItemStack(), this, attacker)) {
			disableShield();
		}
	}

	private void disableShield() {
		float f = 1F + (float) EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

		if (rand.nextFloat() < f) {
			setShieldDisabledTick(80);
			resetActiveHand();
			world.setEntityState(this, (byte) 30);
		}
	}

	@Override
	protected void damageShield(float damage) {
		if (damage >= 3.0F && activeItemStack.getItem().isShield(activeItemStack, this)) {
			int i = 1 + MathHelper.floor(damage);
			getShieldStack().damageItem(i, this);
			setActiveHand(EnumHand.OFF_HAND);

			if (getShieldStack().isEmpty()) {  //shield breaks
				setShieldStack(ItemStack.EMPTY);
				playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + world.rand.nextFloat() * 0.4F);
			}
		}
	}

}
