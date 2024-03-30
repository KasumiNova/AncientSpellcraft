package com.windanesz.ancientspellcraft.client.renderer.entity;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.client.model.ModelClassWizard;
import com.windanesz.ancientspellcraft.entity.living.EntityClassWizard;
import electroblob.wizardry.item.ItemWizardArmour;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

//@SideOnly(Side.CLIENT)
public class RenderClassWizard extends RenderBiped<EntityClassWizard> {

	static final ResourceLocation[] TEXTURES = new ResourceLocation[24];

	public RenderClassWizard(RenderManager renderManager) {

		super(renderManager, new ModelClassWizard(), 0.5F);

		int totalCount = 0;
		for (int i = 0; i < 4; i++) { // 4 class types
			for (int j = 0; j < 6; j++) { // 6 skin per type
				TEXTURES[totalCount] = new ResourceLocation(AncientSpellcraft.MODID, "textures/entity/class_wizard/"
						+ ((ItemWizardArmour.ArmourClass.values()[i].name().toLowerCase() + "_" + j) + ".png"));
				totalCount++;
			}
		}
		// Just using the default without overriding models, since the armour sets its own model anyway.
		this.addLayer(new LayerBipedArmor(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityClassWizard wizard) {
		int i = wizard.getArmourClass().ordinal();
		int j = i * 6;
		int k = wizard.textureIndex;
		int res = j + k;
		return TEXTURES[(wizard.getArmourClass().ordinal() * 6 + wizard.textureIndex)];
	}

}
