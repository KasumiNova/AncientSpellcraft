package com.windanesz.ancientspellcraft.integration.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.item.AbstractItemArtefactWithSlots;
import com.windanesz.ancientspellcraft.item.ITickableArtefact;
import com.windanesz.ancientspellcraft.registry.ASItems;
import com.windanesz.ancientspellcraft.spell.AbsorbArtefact;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is a replica of Wizardry's Baubles integration {@link electroblob.wizardry.integration.baubles},
 * its necessary to add support to new bauble types till an API exists for {@link electroblob.wizardry.item.ItemArtefact.Type}.
 *
 * @author Electroblob, WinDanesz
 */
public final class ASBaublesIntegration {

	public static final String BAUBLES_MOD_ID = "baubles";

	private static final Map<ItemArtefact.Type, BaubleType> WIZARDRY_ARTEFACT_TYPE_MAP = new EnumMap<>(ItemArtefact.Type.class);
	//private static final Map<ItemNewArtefact.Type, BaubleType> ARTEFACT_TYPE_MAP = new EnumMap<>(ItemNewArtefact.Type.class);

	private static boolean baublesLoaded;

	public static void init() {

		baublesLoaded = Loader.isModLoaded(BAUBLES_MOD_ID);

		if (!enabled()) {return;}

		WIZARDRY_ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.RING, BaubleType.RING);
		WIZARDRY_ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.AMULET, BaubleType.AMULET);
		WIZARDRY_ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.CHARM, BaubleType.CHARM);
		WIZARDRY_ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.BELT, BaubleType.BELT);
		WIZARDRY_ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.HEAD, BaubleType.HEAD);

		//		ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.BELT, BaubleType.BELT);
		//		ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.HEAD, BaubleType.HEAD);

	}

	public static boolean enabled() {
		return Settings.generalSettings.baubles_integration && baublesLoaded;
	}

	// Wrappers for BaublesApi methods

	//	// Shamelessly copied from The Twilight Forest, with a few modifications
	//	@SuppressWarnings("unchecked")
	//	public static final class ArtefactBaubleProvider implements ICapabilityProvider {
	//
	//		private BaubleType type;
	//
	//		public ArtefactBaubleProvider(ItemArtefact.Type type) {
	//			this.type = ARTEFACT_TYPE_MAP.get(type);
	//		}
	//
	//		@Override
	//		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
	//			return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
	//		}
	//
	//		@Override
	//		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
	//			// This lambda expression is an implementation of the entire IBauble interface
	//			return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? (T) (IBauble) itemStack -> type : null;
	//		}
	//	}

	public static List<ItemStack> getEquippedArtefactStacks(EntityPlayer player, ItemArtefact.Type... types) {

		List<ItemStack> artefacts = new ArrayList<>();

		for (ItemArtefact.Type type : types) {
			for (int slot : WIZARDRY_ARTEFACT_TYPE_MAP.get(type).getValidSlots()) {
				ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
				if (stack.getItem() instanceof ItemArtefact) {artefacts.add(stack);}
			}
		}

		return artefacts;
	}

	public static ItemStack getBeltSlotItemStack(EntityPlayer player) {
		IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
		int beltSlot = 3; // from BaubleType.BELT
		ItemStack stack = baubles.getStackInSlot(beltSlot);
		if (stack != null) {
			return stack;
		}
		return ItemStack.EMPTY;
	}

	public static void setArtefactToSlot(EntityPlayer player, ItemStack stack, ItemArtefact.Type type) {
		setArtefactToSlot(player, stack, type, 0);
	}

	public static void setArtefactToSlot(EntityPlayer player, ItemStack stack, ItemArtefact.Type type, int slotId) {
		BaublesApi.getBaublesHandler(player).setStackInSlot(WIZARDRY_ARTEFACT_TYPE_MAP.get(type).getValidSlots()[slotId], stack);
	}

	public static void tickWornArtefacts(EntityPlayer player) {

		IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
		for (int i = 0; i < baubles.getSlots(); i++) {
			ItemStack stack = baubles.getStackInSlot(i);
			if (stack.getItem() instanceof ITickableArtefact) {
				((ITickableArtefact) stack.getItem()).onWornTick(stack, player);
			}
		}
		Optional<Item> artefact = AbsorbArtefact.getArtefact(WizardData.get(player));
		if (artefact.isPresent()) {
			ItemStack stack = new ItemStack(artefact.get());
			if (stack.getItem() instanceof ITickableArtefact) {
				((ITickableArtefact) stack.getItem()).onWornTick(stack, player);
			}
		}
		List<ItemStack> list = ASBaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.BELT);
		if (list.size() == 1) {
			ItemStack belt = list.get(0);
			if (belt.getItem() == ASItems.belt_hook && belt.hasTagCompound() && AbstractItemArtefactWithSlots.getItemForSlot(belt, 0).getItem() instanceof ITickableArtefact) {
				((ITickableArtefact) AbstractItemArtefactWithSlots.getItemForSlot(belt, 0).getItem()).onWornTick(AbstractItemArtefactWithSlots.getItemForSlot(belt, 0), player);
			}
		}
	}
}
