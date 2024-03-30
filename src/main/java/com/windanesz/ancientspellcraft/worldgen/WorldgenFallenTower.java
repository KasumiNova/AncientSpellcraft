package com.windanesz.ancientspellcraft.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.entity.living.EntitySkeletonMageMinion;
import com.windanesz.ancientspellcraft.integration.antiqueatlas.ASAntiqueAtlasIntegration;
import com.windanesz.ancientspellcraft.tileentity.TileSageLectern;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.worldgen.MossifierTemplateProcessor;
import electroblob.wizardry.worldgen.MultiTemplateProcessor;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WorldgenFallenTower extends WorldGenSurfaceStructure {

	// TODO: Add wizard towers to the /locate command
	// This requires some careful manipulation of Random objects to replicate the positions exactly for the current
	// world. See the end of ChunkGeneratorOverworld for the relevant methods.

	private static final String SKELETON_MAGE_DATA_BLOCK_TAG = "skeleton_mage_ghost";
	private static final List<BiomeDictionary.Type> BIOME_TYPES = ImmutableList.of(BiomeDictionary.Type.FOREST);

	private final Map<BiomeDictionary.Type, IBlockState> specialWallBlocks;

	public WorldgenFallenTower() {
		// These are initialised here because it's a convenient point after the blocks are registered
		specialWallBlocks = ImmutableMap.of(
				BiomeDictionary.Type.MESA, Blocks.RED_SANDSTONE.getDefaultState(),
				BiomeDictionary.Type.MOUNTAIN, Blocks.STONEBRICK.getDefaultState(),
				BiomeDictionary.Type.NETHER, Blocks.NETHER_BRICK.getDefaultState(),
				BiomeDictionary.Type.SANDY, Blocks.SANDSTONE.getDefaultState()
		);
	}

	@Override
	public String getStructureName() {
		return "fallen_tower";
	}

	@Override
	public long getRandomSeedModifier() {
		return 13428681L; // Yep, I literally typed 8 digits at random
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ) {
		return ArrayUtils.contains(Settings.worldgenSettings.ancientVaultDimensions, world.provider.getDimension())
				&& Settings.worldgenSettings.fallenTowerRarity > 0 && random.nextInt(Settings.worldgenSettings.fallenTowerRarity) == 0 &&
				BiomeDictionary.getTypes(world.getBiome(new BlockPos(chunkX * 16 + 16, 0, chunkZ * 16 + 16)))
						.stream().anyMatch(BIOME_TYPES::contains);
	}

	@Override
	public ResourceLocation getStructureFile(Random random) {
		return AncientSpellcraft.settings.fallenTowerFiles[0];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile) {

		final EnumDyeColor colour = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];

		final Element element = Element.values()[1 + random.nextInt(Element.values().length - 1)];
		final Biome biome = world.getBiome(origin);
		IBlockState biomeCover = biome.topBlock;
		final float mossiness = getBiomeMossiness(biome);

		final IBlockState wallMaterial = specialWallBlocks.keySet().stream().filter(t -> BiomeDictionary.hasType(biome, t))
				.findFirst().map(specialWallBlocks::get).orElse(Blocks.COBBLESTONE.getDefaultState());

		final BlockPlanks.EnumType woodType = BlockUtils.getBiomeWoodVariant(biome);

		final Set<BlockPos> blocksPlaced = new HashSet<>();

		ITemplateProcessor processor = new MultiTemplateProcessor(true,
				// Roof colour
				(w, p, i) -> i.blockState.getBlock() instanceof BlockStainedHardenedClay ? new Template.BlockInfo(
						i.pos, i.blockState.withProperty(BlockStainedHardenedClay.COLOR, colour), i.tileentityData) : i,
				// change ground type to biome's cover block
				(w, p, i) -> i.blockState.getBlock() == Blocks.DIRT ? new Template.BlockInfo(i.pos,
						biomeCover, i.tileentityData) : i,
				// Mossifier
				new MossifierTemplateProcessor(mossiness, 0.04f, origin.getY() + 1),
				// change ground type to biome's cover block
				(w, p, i) -> i.blockState.getBlock() == Blocks.GRASS ? new Template.BlockInfo(i.pos,
						biomeCover, i.tileentityData) : i,
				// receptacle colour
				(w, p, i) -> (i.blockState.getBlock() == WizardryBlocks.receptacle ? new Template.BlockInfo(
						i.pos, i.blockState, setElement(i.tileentityData, element)) : i),
				// Bookshelf marker
				(w, p, i) -> {
					TileEntityBookshelf.markAsNatural(i.tileentityData);
					return i;
				},
				(w, p, i) -> {
					TileSageLectern.markAsNatural(i.tileentityData);
					return i;
				}

		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);
		if (settings.getBoundingBox() != null) {
			// Define edge blend factor
			float edgeBlendFactor = 0.3f;

			// Iterate through each position in the bounding box
			for (BlockPos currPos : BlockPos.getAllInBox(
					settings.getBoundingBox().minX, settings.getBoundingBox().maxY, settings.getBoundingBox().minZ,
					settings.getBoundingBox().maxX, settings.getBoundingBox().maxY + 8, settings.getBoundingBox().maxZ)) {
				if (currPos.getY() != settings.getBoundingBox().minY || world.rand.nextFloat() < edgeBlendFactor) {
					if (!world.isAirBlock(currPos)) {
						// Determine block type based on Y position
						// Set the block state
						world.setBlockToAir(currPos);
					}
				}
			}

		}

		ASAntiqueAtlasIntegration.markMysteryStructure(world, origin.getX(), origin.getZ());

		// Wizard spawning
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for (
				Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()) {

			Vec3d vec = GeometryUtils.getCentre(entry.getKey());

			if (entry.getValue().equals(SKELETON_MAGE_DATA_BLOCK_TAG)) {

				EntitySkeletonMageMinion skeleton = new EntitySkeletonMageMinion(world);
				skeleton.setElement(element);
				skeleton.setLocationAndAngles(vec.x, vec.y, vec.z, 0, 0);
				skeleton.setLifetime(-1); // Stops it despawning
				skeleton.onInitialSpawn(world.getDifficultyForLocation(origin), null);
				if (element == Element.HEALING) {
					skeleton.populateSpellList(Element.HEALING, Spells.ray_of_purification);
				}

				world.spawnEntity(skeleton);
				world.setBlockToAir(entry.getKey());
			} else {
				// This probably shouldn't happen...
				Wizardry.logger.info("Unrecognised data block value {} in structure {}", entry.getValue(), structureFile);
			}

		}

	}

	NBTTagCompound setElement(NBTTagCompound compound, Element element) {
		compound.setInteger("Element", element.ordinal());
		return compound;
	}

	private static float getBiomeMossiness(Biome biome) {
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE)) {return 0.7f;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) {return 0.7f;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)) {return 0.5f;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {return 0.5f;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)) {return 0.3f;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.LUSH)) {return 0.3f;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)) {return 0;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) {return 0;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DEAD)) {return 0;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WASTELAND)) {return 0;}
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) {return 0;}
		return 0.1f; // Everything else (plains, etc.) has a small amount of moss
	}

	@Override
	protected void postGenerate(Random random, World world, PlacementSettings settings) {}
}
