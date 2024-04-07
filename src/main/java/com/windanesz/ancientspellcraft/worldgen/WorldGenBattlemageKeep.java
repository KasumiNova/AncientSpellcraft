package com.windanesz.ancientspellcraft.worldgen;

import com.google.common.collect.ImmutableMap;
import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.integration.antiqueatlas.ASAntiqueAtlasIntegration;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.worldgen.MossifierTemplateProcessor;
import electroblob.wizardry.worldgen.MultiTemplateProcessor;
import electroblob.wizardry.worldgen.WoodTypeTemplateProcessor;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
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
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static net.minecraft.block.BlockStoneSlab.VARIANT;

@Mod.EventBusSubscriber
public class WorldGenBattlemageKeep extends WorldGenSurfaceStructureAS {

	// This requires some careful manipulation of Random objects to replicate the positions exactly for the current
	// world. See the end of ChunkGeneratorOverworld for the relevant methods.

	private static final String WIZARD_DATA_BLOCK_TAG = "wizard";
	private static final String EVIL_WIZARD_DATA_BLOCK_TAG = "evil_wizard";
	private static final String HORSE_DATA_BLOCK_TAG = "horse";

	private final Map<BiomeDictionary.Type, IBlockState> specialWallBlocks;
	private final Map<BiomeDictionary.Type, IBlockState> specialStairBlocks;
	private final Map<BiomeDictionary.Type, IBlockState> specialSlabBlocks;

	public WorldGenBattlemageKeep() {
		// These are initialised here because it's a convenient point after the blocks are registered
		specialWallBlocks = ImmutableMap.of(
				BiomeDictionary.Type.MESA, Blocks.RED_SANDSTONE.getDefaultState(),
				BiomeDictionary.Type.MOUNTAIN, Blocks.STONEBRICK.getDefaultState(),
				BiomeDictionary.Type.NETHER, Blocks.NETHER_BRICK.getDefaultState(),
				BiomeDictionary.Type.SANDY, Blocks.SANDSTONE.getDefaultState()
		);

		specialStairBlocks = ImmutableMap.of(
				BiomeDictionary.Type.MESA, Blocks.SANDSTONE_STAIRS.getDefaultState(),
				BiomeDictionary.Type.MOUNTAIN, Blocks.STONE_BRICK_STAIRS.getDefaultState(),
				BiomeDictionary.Type.NETHER, Blocks.NETHER_BRICK_STAIRS.getDefaultState(),
				BiomeDictionary.Type.SANDY, Blocks.SANDSTONE_STAIRS.getDefaultState()
		);

		specialSlabBlocks = ImmutableMap.of(
				BiomeDictionary.Type.MESA, Blocks.STONE_SLAB2.getDefaultState(),
				BiomeDictionary.Type.MOUNTAIN, Blocks.STONE_SLAB.getDefaultState(),
				BiomeDictionary.Type.NETHER, Blocks.STONE_SLAB.getDefaultState().withProperty(VARIANT, BlockStoneSlab.EnumType.NETHERBRICK),
				BiomeDictionary.Type.SANDY, Blocks.STONE_SLAB.getDefaultState().withProperty(VARIANT, BlockStoneSlab.EnumType.SAND)
		);
	}

	@Override
	public String getStructureName() {
		return "battlemage_camp";
	}

	@Override
	public long getRandomSeedModifier() {
		return 12489238L; // Yep, I literally typed 8 digits at random
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ) {
		return ArrayUtils.contains(Settings.worldgenSettings.battlemageKeepDimensions, world.provider.getDimension())
				&& Settings.worldgenSettings.battlemageKeepRarity > 0 && random.nextInt(Settings.worldgenSettings.battlemageKeepRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random) {
		return AncientSpellcraft.settings.battlemageKeepFiles[random.nextInt(AncientSpellcraft.settings.battlemageKeepFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile) {

		final EnumDyeColor colour = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];
		final Biome biome = world.getBiome(origin);
		IBlockState biomeCover = biome.topBlock;
		final float mossiness = getBiomeMossiness(biome);

		final IBlockState wallMaterial = specialWallBlocks.keySet().stream().filter(t -> BiomeDictionary.hasType(biome, t))
				.findFirst().map(specialWallBlocks::get).orElse(Blocks.COBBLESTONE.getDefaultState());

		final IBlockState stairMaterial = specialStairBlocks.keySet().stream().filter(t -> BiomeDictionary.hasType(biome, t))
				.findFirst().map(specialStairBlocks::get).orElse(Blocks.STONE_STAIRS.getDefaultState());

		final IBlockState slabMaterial = specialSlabBlocks.keySet().stream().filter(t -> BiomeDictionary.hasType(biome, t))
				.findFirst().map(specialSlabBlocks::get).orElse(Blocks.STONE_SLAB.getDefaultState());

		final BlockPlanks.EnumType woodType = BlockUtils.getBiomeWoodVariant(biome);

		final Set<BlockPos> blocksPlaced = new HashSet<>();

		ITemplateProcessor processor = new MultiTemplateProcessor(true,
				// wool colour
				(w, p, i) -> i.blockState.getBlock() == Blocks.WOOL ? new Template.BlockInfo(
						i.pos, Blocks.WOOL.getStateFromMeta(colour.getMetadata()), i.tileentityData) : i,
				// carpet colour
				(w, p, i) -> i.blockState.getBlock() == Blocks.CARPET ? new Template.BlockInfo(
						i.pos, Blocks.CARPET.getStateFromMeta(colour.getMetadata()), i.tileentityData) : i,
				// banner colour
				(w, p, i) -> i.blockState.getBlock() == Blocks.WALL_BANNER ? new Template.BlockInfo(
						i.pos, Blocks.CARPET.getStateFromMeta(colour.getMetadata()), i.tileentityData) : i,
				// Wall material
				(w, p, i) -> i.blockState.getBlock() == Blocks.COBBLESTONE ? new Template.BlockInfo(i.pos,
						wallMaterial, i.tileentityData) : i,
				// Wall material
				(w, p, i) -> i.blockState.getBlock() == Blocks.STONE_STAIRS ? new Template.BlockInfo(i.pos,
						stairMaterial
								.withProperty(BlockStairs.FACING, i.blockState.getValue(BlockStairs.FACING))
								.withProperty(BlockStairs.HALF, i.blockState.getValue(BlockStairs.HALF))
								.withProperty(BlockStairs.SHAPE, i.blockState.getValue(BlockStairs.SHAPE))
						, i.tileentityData) : i,
				// Wall material
				(w, p, i) -> i.blockState.getBlock() == Blocks.STONE_SLAB ? new Template.BlockInfo(i.pos,
						slabMaterial, i.tileentityData) : i,
				// change ground type to biome's cover block
				(w, p, i) -> i.blockState.getBlock() == Blocks.DIRT  || i.blockState.getBlock() == Blocks.GRASS ? new Template.BlockInfo(i.pos,
						biomeCover, i.tileentityData) : i,
				// Wood type
				new WoodTypeTemplateProcessor(woodType),
				// Mossifier
				new MossifierTemplateProcessor(mossiness, 0.04f, origin.getY() + 1),
				// Block recording (the process() method doesn't get called for structure voids)
				(w, p, i) -> {
					if (i.blockState.getBlock() != Blocks.AIR) {blocksPlaced.add(p);}
					return i;
				}
		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		if (settings.getBoundingBox() != null) {
			// Define edge blend factor
			float edgeBlendFactor = 0.3f;

			// Iterate through each position in the bounding box
			for (BlockPos currPos : BlockPos.getAllInBox(
					settings.getBoundingBox().minX, settings.getBoundingBox().minY - 8, settings.getBoundingBox().minZ,
					settings.getBoundingBox().maxX, settings.getBoundingBox().minY, settings.getBoundingBox().maxZ)) {
				// Place top blocks
				if (currPos.getY() == settings.getBoundingBox().minY && world.canSnowAt(currPos, true) && world.isAirBlock(currPos)) {
					world.setBlockState(currPos, Blocks.SNOW_LAYER.getDefaultState(), 2);
				} else {
					// Edge blending
					if (currPos.getY() != settings.getBoundingBox().minY || world.rand.nextFloat() < edgeBlendFactor) {
						// Check if the position is air or not solid
						if (world.getBlockState(currPos).getBlock() instanceof BlockTallGrass || world.isAirBlock(currPos) || world.getBlockState(currPos).getBlock() instanceof BlockBush || world.getBlockState(currPos).getBlock() instanceof BlockLog) {
							// Determine block type based on Y position
							IBlockState blockState = (currPos.getY() == settings.getBoundingBox().minY) ? biome.topBlock : biome.fillerBlock;

							// Set the block state
							world.setBlockState(currPos, blockState);
						}
					}
				}
			}
		}

		ASAntiqueAtlasIntegration.markBattlemageKeep(world, origin.getX(), origin.getZ());

		// Entity spawning
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);
		for (Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()) {
			Vec3d vec = GeometryUtils.getCentre(entry.getKey());
			WorldGenUtils.spawnEntityByType(world, entry.getValue(), ItemWizardArmour.ArmourClass.BATTLEMAGE, origin, vec, blocksPlaced, Element.MAGIC, false);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player instanceof EntityPlayerMP && event.player.ticksExisted % 20 == 0) {
			WizardryAdvancementTriggers.visit_structure.trigger((EntityPlayerMP) event.player);
		}
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
	protected void postGenerate(Random random, World world, PlacementSettings settings) {

	}
}
