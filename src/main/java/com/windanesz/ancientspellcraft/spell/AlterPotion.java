package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.potion.PotionCurseAS;
import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.potion.Curse;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemLingeringPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.PotionItems;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlterPotion extends Spell implements IClassSpell {

	public AlterPotion() {
		super(AncientSpellcraft.MODID, "alter_potion", SpellActions.IMBUE, false);
	}


	@Override
	public ItemWizardArmour.ArmourClass getArmourClass() {
		return ItemWizardArmour.ArmourClass.WARLOCK;
	}

	public boolean applicableForItem(Item item) {
		return item == ASItems.forbidden_tome;
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser) {
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		if (ItemArtefact.isArtefactActive(caster, ASItems.charm_potion_kit)) {
			if (caster.getHeldItemOffhand().getItem() == Items.GLASS_BOTTLE) {
				if (!caster.getActivePotionEffects().isEmpty()) {
					PotionEffect firstEffect = caster.getActivePotionEffects().stream().findFirst().get();
					String displayName;
					String effectName = AncientSpellcraft.proxy.translate(firstEffect.getPotion().getName());
					String potionTypePrefix = caster.isSneaking() ? "Infused Lingering Potion of " : "Infused Splash Potion of ";

					ItemStack stack = new ItemStack(caster.isSneaking() ? Items.LINGERING_POTION : Items.SPLASH_POTION);
					displayName = potionTypePrefix + effectName;
					stack.setStackDisplayName(displayName);
					NBTTagCompound nbt = stack.getTagCompound();
					if (firstEffect.getPotion() == WizardryPotions.frost) {
						nbt.setInteger("CustomPotionColor", 0x38ddec);
					} else {
						nbt.setInteger("CustomPotionColor", firstEffect.getPotion().getLiquidColor());
					}
					stack.setTagCompound(nbt);
					List<PotionEffect> effectList = new ArrayList<>(PotionUtils.getEffectsFromStack(caster.getHeldItemOffhand()));
					if (caster.isSneaking()) {
						effectList.add(new PotionEffect(firstEffect.getPotion(), (int) (firstEffect.getDuration() * 0.1f)));
					} else {
						effectList.add(firstEffect);
					}

					PotionUtils.appendEffects(stack, effectList);
					caster.setHeldItem(EnumHand.OFF_HAND, stack);
					if (!(firstEffect.getPotion() instanceof Curse)) {
						caster.removePotionEffect(firstEffect.getPotion());
					}
				}
			}

			return true;
		}

		if (caster.getHeldItemOffhand().getItem() instanceof ItemPotion) {
			List<PotionEffect> effectList = new ArrayList<>(PotionUtils.getEffectsFromStack(caster.getHeldItemOffhand()));
			Map<String, String> potionMapping = new HashMap<>();
			boolean updated = false;
			for (String mapping : Settings.generalSettings.alter_potion_mapping) {
				String[] parts = mapping.split("\\|");
				potionMapping.put(parts[0], parts[1]);
				potionMapping.put(parts[1], parts[0]);
			}

			for (PotionEffect effect : PotionUtils.getEffectsFromStack(caster.getHeldItemOffhand())) {
				if (potionMapping.containsKey(effect.getPotion().getRegistryName().toString())) {
					String counterpart = potionMapping.get(effect.getPotion().getRegistryName().toString());
					if (ForgeRegistries.POTIONS.containsKey(new ResourceLocation(counterpart))) {
						Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(counterpart));
						effectList.remove(effect);
						effectList.add(new PotionEffect(potion, effect.getDuration(), effect.getAmplifier()));
						updated = true;
					}
				}
			}
			if (updated) {
				String displayName;
				PotionEffect firstEffect = effectList.get(0);
				String effectName = AncientSpellcraft.proxy.translate(firstEffect.getPotion().getName());
				String potionTypePrefix = "";

				if (caster.getHeldItemOffhand().getItem() instanceof ItemSplashPotion) {
					potionTypePrefix = "Infused Splash Potion of ";
				} else if (caster.getHeldItemOffhand().getItem() instanceof ItemLingeringPotion) {
					potionTypePrefix = "Infused Lingering Potion of ";
				} else {
					potionTypePrefix = "Infused Potion of ";
				}

				ItemStack stack = new ItemStack(caster.getHeldItemOffhand().getItem());
				displayName = potionTypePrefix + effectName;
				stack.setStackDisplayName(displayName);
				NBTTagCompound nbt = stack.getTagCompound();
				nbt.setInteger("CustomPotionColor", firstEffect.getPotion().getLiquidColor());
				stack.setTagCompound(nbt);
				PotionUtils.appendEffects(stack, effectList);
				caster.setHeldItem(EnumHand.OFF_HAND, stack);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override) {
		return false;
	}
}
