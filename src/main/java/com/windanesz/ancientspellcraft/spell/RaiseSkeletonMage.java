package com.windanesz.ancientspellcraft.spell;

import com.windanesz.ancientspellcraft.entity.living.EntitySkeletonMageMinion;
import com.windanesz.ancientspellcraft.integration.baubles.ASBaublesIntegration;
import com.windanesz.ancientspellcraft.item.AbstractItemArtefactWithSlots;
import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.util.ASUtils;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemCrystal;
import electroblob.wizardry.spell.SpellMinion;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class RaiseSkeletonMage extends SpellMinion<EntitySkeletonMageMinion> {

	public RaiseSkeletonMage(String modID, String name, Function<World, EntitySkeletonMageMinion> minionFactory) {
		super(modID, name, minionFactory);
		//		this.soundValues(7, 0.6f, 0);
	}

	@Override
	protected EntitySkeletonMageMinion createMinion(World world, EntityLivingBase caster, SpellModifiers modifiers) {
		this.playSound(world, caster, 0, -1, modifiers);

		Random rand = new Random();
		Element element = (Element.values()[rand.nextInt(Element.values().length - 1) + 1]);

		if (caster instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer) caster, ASItems.amulet_elemental_offense)) {
			List<ItemStack> amuletz = ASBaublesIntegration.getEquippedArtefactStacks((EntityPlayer) caster, ItemArtefact.Type.AMULET);
			if (amuletz.size() == 1) {
				ItemStack amulet = amuletz.get(0);
				if (AbstractItemArtefactWithSlots.getItemForSlot(amulet, 0).getItem() instanceof ItemCrystal) {
					ItemStack crystal = AbstractItemArtefactWithSlots.getItemForSlot(amulet, 0);
					if (Element.values()[crystal.getMetadata()] != Element.MAGIC) {
						element = Element.values()[crystal.getMetadata()];
					}
				}
			}
		} else
		// Holding a magic crystal in the offhand acts as a focus crystal to spawn a skeleton of that element
		if (caster instanceof EntityPlayer && caster.getHeldItemOffhand().getItem() instanceof ItemCrystal) {
			Element crystalElement = ASUtils.getCrystalElementFromStack(caster.getHeldItemOffhand());
			if (crystalElement != Element.MAGIC) {
				element = crystalElement;
				// 10% chance to break the crystal
				if (rand.nextInt(100) > 90) {
					caster.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.9F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
					caster.getHeldItemOffhand().shrink(1);
					if (!world.isRemote) {
						((EntityPlayer) caster).sendStatusMessage(new TextComponentTranslation("spell." + this.getRegistryName() + ".crystal.break"), true);
					}
				}
			}
		}
		return new EntitySkeletonMageMinion(world, element);
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == ASItems.ancient_spellcraft_spell_book || item == ASItems.ancient_spellcraft_scroll;
	}
}
