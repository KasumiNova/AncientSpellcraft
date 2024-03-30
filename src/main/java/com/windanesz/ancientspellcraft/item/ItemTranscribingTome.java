package com.windanesz.ancientspellcraft.item;

import com.windanesz.ancientspellcraft.block.BlockDimensionFocus;
import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.registry.ASSounds;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ItemTranscribingTome extends ItemASArtefact {

	public ItemTranscribingTome(EnumRarity rarity, Type type) {
		super(rarity, type);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
		if (!entity.world.isRemote) {return;}

		World world = entity.world;
		Random rand = entity.world.rand;
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;

		ParticleBuilder.create(ParticleBuilder.Type.DUST).clr(0xf5ad42).vel(0, 0.03f, 0).spin(0.8f, 0.03f)
				.time(60).entity(entity).pos(0, 0.1f, 0).scale(1.2f).spawn(world);
		ParticleBuilder.create(ParticleBuilder.Type.DUST).clr(0xbb28c9).vel(0, 0.03f, 0).spin(0.8f, 0.03f)
				.time(60).entity(entity).pos(0, 0.1f, 0).scale(1.2f).spawn(world);
		ParticleBuilder.create(ParticleBuilder.Type.FLASH).face(EnumFacing.DOWN).clr(0xbb28c9).pos(0,0.1f,0).time(20).entity(entity).scale(1.2f).spawn(world);


	}

	/**
	 * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
	 * the Item before the action is complete.
	 */
	@Override
	public ItemStack onItemUseFinish(ItemStack scrollStack, World world, EntityLivingBase entityLiving) {
		return onTransmutationFinish(scrollStack, world, entityLiving);
	}

	public ItemStack onTransmutationFinish(ItemStack scrollStack, World world, EntityLivingBase entityLiving) {
		if (entityLiving instanceof EntityPlayer) {

			EntityPlayer player = (EntityPlayer) entityLiving;
			ItemStack offhandStack = player.getHeldItemOffhand();

			if (offhandStack.isEmpty() && !world.isRemote) {
				player.sendStatusMessage(new TextComponentTranslation("item.ancientspellcraft:charm_transcribing_tome.no_items_to_transcribe"), false);
				return scrollStack;
			}

			if (offhandStack.getItem() instanceof ItemScroll && Spell.byMetadata(offhandStack.getItemDamage()).isEnabled(SpellProperties.Context.BOOK)) {
				if (!world.isRemote) {
					Spell spell = Spell.byMetadata(offhandStack.getItemDamage());
					List<Item> bookTypeList = ForgeRegistries.ITEMS.getValuesCollection().stream()
							.filter(i -> i instanceof ItemSpellBook)
							.filter(spell::applicableForItem)
							.distinct().collect(Collectors.toList());

					if (!bookTypeList.isEmpty()) {
						//player.getHeldItemMainhand().shrink(1);
						scrollStack = new ItemStack(bookTypeList.get(0), 1, spell.metadata());
					//	player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(book.get(), spell.metadata()));
						player.getHeldItemOffhand().shrink(1);
					}

				}
			} else {
				if (!world.isRemote) {player.sendStatusMessage(new TextComponentTranslation("item.ancientspellcraft:charm_transcribing_tome.invalid_item"), false);}
			}

			player.getCooldownTracker().setCooldown(this, 60);
		}

		world.playSound(entityLiving.posX, entityLiving.posY, entityLiving.posZ, ASSounds.TRANSMUTATION, WizardrySounds.SPELLS, 1, 1, false);

		return scrollStack;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {return SpellActions.IMBUE;}

	/**
	 * Called when the equipped item is right clicked.
	 */
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		playerIn.setActiveHand(handIn);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack scrollStack, World world, EntityLivingBase entityLiving, int timeLeft) {
		// this is not getting called
		if (entityLiving instanceof EntityPlayer && !world.isRemote) {
			((EntityPlayer) entityLiving).sendStatusMessage(new TextComponentTranslation("item." + this.getRegistryName() + ".interrupted"), true);
			((EntityPlayer) entityLiving).getCooldownTracker().setCooldown(this, 40);
		}

	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {return 60;}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getEntity() instanceof EntityPlayer && event.getHand() == EnumHand.OFF_HAND
				&& ((EntityPlayer) event.getEntity()).getHeldItemMainhand().getItem() == ASItems.transmutation_scroll) {
			event.setCanceled(true);
		}

	}
}
