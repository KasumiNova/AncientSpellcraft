package com.windanesz.ancientspellcraft.worldgen;

import com.google.common.collect.ImmutableMap;
import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.integration.antiqueatlas.ASAntiqueAtlasIntegration;
import com.windanesz.ancientspellcraft.tileentity.TileSageLectern;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.worldgen.MossifierTemplateProcessor;
import electroblob.wizardry.worldgen.MultiTemplateProcessor;
import electroblob.wizardry.worldgen.WoodTypeTemplateProcessor;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStoneBrick;
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

@Mod.EventBusSubscriber
public class WorldGenSageHill extends WorldGenSurfaceStructureAS {

	private final Map<BiomeDictionary.Type, IBlockState> specialWallBlocks;

	public WorldGenSageHill() {
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
		return "sage_hill";
	}

	@Override
	public long getRandomSeedModifier() {
		return 28738742L; // Yep, I literally typed 8 digits at random
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ) {
		return ArrayUtils.contains(Settings.worldgenSettings.sageHillDimensions, world.provider.getDimension())
				&& Settings.worldgenSettings.sageHillRarity > 0 && random.nextInt(Settings.worldgenSettings.sageHillRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random) {
		return AncientSpellcraft.settings.sageHillWithChestFiles[random.nextInt(AncientSpellcraft.settings.sageHillWithChestFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile) {

		final EnumDyeColor colour = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];
		final Biome biome = world.getBiome(origin);
		IBlockState biomeCover = biome.topBlock;
		final float mossiness = getBiomeMossiness(biome);

		final IBlockState wallMaterial = specialWallBlocks.keySet().stream().filter(t -> BiomeDictionary.hasType(biome, t))
				.findFirst().map(specialWallBlocks::get).orElse(Blocks.COBBLESTONE.getDefaultState());

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
				// change ground type to biome's cover block
				(w, p, i) -> i.blockState.getBlock() == Blocks.DIRT ? new Template.BlockInfo(i.pos,
						biomeCover, i.tileentityData) : i,
				// change ground type to biome's cover block
				(w, p, i) -> i.blockState.getBlock() == Blocks.GRASS ? new Template.BlockInfo(i.pos,
						biomeCover, i.tileentityData) : i,
				// Wood type
				new WoodTypeTemplateProcessor(woodType),
				// Mossifier
				new MossifierTemplateProcessor(mossiness, 0.04f, origin.getY() + 1),
				// Stone brick smasher-upper
				(w, p, i) -> i.blockState.getBlock() == Blocks.STONEBRICK && w.rand.nextFloat() < 0.1f ?
						new Template.BlockInfo(i.pos, Blocks.STONEBRICK.getDefaultState().withProperty(
								BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), i.tileentityData) : i,
				// Bookshelf marker
				(w, p, i) -> {
					TileEntityBookshelf.markAsNatural(i.tileentityData);
					return i;
				},
				(w, p, i) -> {
					TileSageLectern.markAsNatural(i.tileentityData);
					return i;
				},
				// Block recording (the process() method doesn't get called for structure voids)
				(w, p, i) -> {
					if (i.blockState.getBlock() != Blocks.AIR) {blocksPlaced.add(p);}
					return i;
				}

		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		ASAntiqueAtlasIntegration.markSageHill(world, origin.getX(), origin.getZ());

		// Entity spawning
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);
		for (Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()) {
			Vec3d vec = GeometryUtils.getCentre(entry.getKey());
			WorldGenUtils.spawnEntityByType(world, entry.getValue(), ItemWizardArmour.ArmourClass.SAGE, origin, vec, blocksPlaced,
					Element.values()[1 + random.nextInt(Element.values().length - 1)], false);
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
}
