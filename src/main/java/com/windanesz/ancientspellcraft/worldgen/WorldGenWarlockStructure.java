package com.windanesz.ancientspellcraft.worldgen;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.entity.living.EntityEvilClassWizard;
import com.windanesz.ancientspellcraft.integration.antiqueatlas.ASAntiqueAtlasIntegration;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockRunestone;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.worldgen.WorldGenSurfaceStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.Random;

public class WorldGenWarlockStructure extends WorldGenSurfaceStructure {

	private static final String EVIL_WARLOCK_TAG = "warlock";

	@Override
	public String getStructureName(){
		return "warlock_structure";
	}

	@Override
	public long getRandomSeedModifier(){
		return 27675449L;
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ) {
		return ArrayUtils.contains(Settings.worldgenSettings.warlockStructureDimensions, world.provider.getDimension())
				&& Settings.worldgenSettings.warlockStructureRarity > 0 && random.nextInt(Settings.worldgenSettings.warlockStructureRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return new ResourceLocation(AncientSpellcraft.MODID, "warlock_rite_0");
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final Element element = Element.values()[1 + random.nextInt(Element.values().length-1)];

		ITemplateProcessor processor = (w, p, i) -> i.blockState.getBlock() instanceof BlockRunestone ? new Template.BlockInfo(
				i.pos, i.blockState.withProperty(BlockRunestone.ELEMENT, element), i.tileentityData) : i;

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		ASAntiqueAtlasIntegration.markMysteryStructure(world, origin.getX(), origin.getZ());

		// Shrine core
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for(Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()){

			Vec3d vec = GeometryUtils.getCentre(entry.getKey());

			if(entry.getValue().equals(EVIL_WARLOCK_TAG)){

				EntityEvilClassWizard wizard = new EntityEvilClassWizard(world);
				wizard.setElement(element);
				wizard.setArmourClass(ItemWizardArmour.ArmourClass.WARLOCK);
				wizard.hasStructure = true; // Stops it despawning
				wizard.onInitialSpawn(world.getDifficultyForLocation(origin), null);
				wizard.setLocationAndAngles(vec.x, vec.y, vec.z, 0, 0);
				world.spawnEntity(wizard);

			}else{
				// This probably shouldn't happen...
				Wizardry.logger.info("Unrecognised data block value {} in structure {}", entry.getValue(), structureFile);
			}
		}
	}

}
