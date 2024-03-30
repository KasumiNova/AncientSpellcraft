package com.windanesz.ancientspellcraft.worldgen;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.Settings;
import com.windanesz.ancientspellcraft.entity.living.EntityStoneGuardian;
import com.windanesz.ancientspellcraft.integration.antiqueatlas.ASAntiqueAtlasIntegration;
import com.windanesz.ancientspellcraft.tileentity.TileSageLectern;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
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
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber
public class WorldGenBookVault extends WorldGenSurfaceStructureAS {

	// TODO: Add wizard towers to the /locate command
	// This requires some careful manipulation of Random objects to replicate the positions exactly for the current
	// world. See the end of ChunkGeneratorOverworld for the relevant methods.

	private static final String STONE_GUARDIAN = "stone_guardian";

	public WorldGenBookVault() {
	}

	@Override
	public String getStructureName() {
		return "bookVaultRarity";
	}

	@Override
	public long getRandomSeedModifier() {
		return 444212215L; // Yep, I literally typed 8 digits at random
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ) {
		return ArrayUtils.contains(Settings.worldgenSettings.battlemageKeepDimensions, world.provider.getDimension())
				&& Settings.worldgenSettings.bookVaultRarity > 0 && random.nextInt(Settings.worldgenSettings.bookVaultRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random) {
		return AncientSpellcraft.settings.bookVaultFiles[0];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile) {
		final Element element = Element.values()[1 + random.nextInt(Element.values().length-1)];

		final EnumDyeColor colour = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];
		final Biome biome = world.getBiome(origin);
		IBlockState biomeCover = biome.topBlock;
		final float mossiness = getBiomeMossiness(biome);

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
				(w, p, i) -> (i.blockState.getBlock() == WizardryBlocks.receptacle ? new Template.BlockInfo(
						i.pos, i.blockState, setElement(i.tileentityData, element)) : i)
		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);
		ASAntiqueAtlasIntegration.markMysteryStructure(world, origin.getX(), origin.getZ());

		// Wizard spawning
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for (
				Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()) {

			Vec3d vec = GeometryUtils.getCentre(entry.getKey());

			if (entry.getValue().equals(STONE_GUARDIAN)) {

				EntityStoneGuardian guardian = new EntityStoneGuardian(world);
				guardian.setLocationAndAngles(vec.x, vec.y, vec.z, 0, 0);
				guardian.onInitialSpawn(world.getDifficultyForLocation(origin), null);
				guardian.setSeal(Optional.of(new BlockPos(vec.x, vec.y - 1, vec.z)));
				guardian.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 40));
				world.spawnEntity(guardian);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player instanceof EntityPlayerMP && event.player.ticksExisted % 20 == 0) {
			WizardryAdvancementTriggers.visit_structure.trigger((EntityPlayerMP) event.player);
		}
	}

	private static float getBiomeMossiness(Biome biome) {
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE)) { return 0.7f; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) { return 0.7f; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)) { return 0.5f; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) { return 0.5f; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)) { return 0.3f; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.LUSH)) { return 0.3f; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)) { return 0; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) { return 0; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DEAD)) { return 0; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WASTELAND)) { return 0; }
		if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)) { return 0; }
		return 0.1f; // Everything else (plains, etc.) has a small amount of moss
	}

	NBTTagCompound setElement(NBTTagCompound compound, Element element) {
		compound.setInteger("Element", element.ordinal());
		return compound;
	}


}
