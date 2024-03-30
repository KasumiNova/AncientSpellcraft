package com.windanesz.ancientspellcraft.item;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.entity.living.EntityClassWizard;
import com.windanesz.ancientspellcraft.registry.ASTabs;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static com.windanesz.ancientspellcraft.spell.Covenant.*;

@Mod.EventBusSubscriber
public class ItemBattlemageContract extends Item {

	public ItemBattlemageContract() {
		super();
		setCreativeTab(ASTabs.ANCIENTSPELLCRAFT);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
		if (target instanceof EntityClassWizard && ((EntityClassWizard) target).armourClass == ItemWizardArmour.ArmourClass.BATTLEMAGE) {
			EntityClassWizard wizard = (EntityClassWizard) target;
			WizardData data = WizardData.get(player);
			World world = player.world;
			boolean changeStatus = true;

			if (!world.isRemote) {
				Entity oldWizard = EntityUtils.getEntityByUUID(world, data.getVariable(ALLIED_WIZARD_UUID_KEY));
				boolean flag1 = oldWizard != null && oldWizard != wizard;
				boolean flag2 = wizard == oldWizard && (isAlreadyFollowing(wizard));
				if (flag1 || flag2) {

					if (flag2) {
						changeStatus = false;
					}

					endAlliance((EntityWizard) oldWizard);
					player.sendStatusMessage(new TextComponentTranslation("spell.ancientspellcraft:covenant.no_longer_following", oldWizard.getDisplayName()), false);
				}

				if (changeStatus && !isAlreadyFollowing(wizard)) {
					allyWithWizard(player, wizard);
					wizard.setBattlemageMercenaryRemainingDuration(168000); // 7 days
					player.sendStatusMessage(new TextComponentTranslation("spell.ancientspellcraft:covenant.following", wizard.getDisplayName()), true);

					return true;
				}
			}
			stack.shrink(1);
			return true;
		}
		return false;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.RARE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag advanced){
		AncientSpellcraft.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
	}

}
