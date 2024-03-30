package com.windanesz.ancientspellcraft.mixin.ebwizardry;

import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.PocketFurnace;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PocketFurnace.class)
public class MixinPocketFurnace {

	@Inject(method = "cast", at = @At("HEAD"), cancellable = true, remap = false)
	private void mixinPerformEffectConsistent(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers, CallbackInfoReturnable<Boolean> ci) {
		if (ItemArtefact.isArtefactActive(caster, ASItems.charm_thousand_anvils)) {
			// Check if the player has at least 9 Blocks.COAL_BLOCK blocks in their inventory
			int coalBlockCount = 0;
			for (int i = 0; i < caster.inventory.getSizeInventory(); i++) {
				ItemStack stack = caster.inventory.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK)) {
					coalBlockCount += stack.getCount();
				}
			}

			if (coalBlockCount >= 9) {
				// Consume 9 Blocks.COAL_BLOCK blocks
				int blocksConsumed = 0;
				for (int i = 0; i < caster.inventory.getSizeInventory(); i++) {
					ItemStack stack = caster.inventory.getStackInSlot(i);
					if (!stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK)) {
						int stackCount = stack.getCount();
						if (stackCount <= 9 - blocksConsumed) {
							blocksConsumed += stackCount;
							caster.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						} else {
							caster.inventory.decrStackSize(i, 9 - blocksConsumed);
							break;
						}
					}
				}

				// Add a diamond to the player's inventory
				caster.inventory.addItemStackToInventory(new ItemStack(Items.DIAMOND));

				world.playSound(null, caster.posX, caster.posY, caster.posZ, new SoundEvent(new ResourceLocation("ebwizardry:spell.pocket_furnace")),
						WizardrySounds.SPELLS, 1, 0);

				if(world.isRemote){
					for(int i = 0; i < 10; i++){
						double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
						double y1 = (double)((float)caster.posY + caster.getEyeHeight() - 0.5F + world.rand.nextFloat());
						double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
						world.spawnParticle(EnumParticleTypes.FLAME, x1, y1, z1, 0, 0.01F, 0);
					}
				}

				// Return true to indicate successful casting and cancel the original method
				ci.setReturnValue(true);
				ci.cancel();
			}
		}
	}
}