package com.windanesz.ancientspellcraft.block;

import com.windanesz.ancientspellcraft.registry.ASBlocks;
import com.windanesz.ancientspellcraft.registry.ASItems;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IShearable;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockSageFlax extends BlockBush implements IShearable {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.5F - 0.2f, 0.0F, 0.5F - 0.2f, 0.5F + 0.2f,
			0.2f * 3.0F, 0.5F + 0.2f);

	private static final AxisAlignedBB DAY_AABB = new AxisAlignedBB(0.5F - 0.2f, 0.0F, 0.5F - 0.2f, 0.5F + 0.2f,
			0.2f, 0.5F + 0.2f);

	public BlockSageFlax(Material par2Material) {
		super(par2Material);
		this.setLightLevel(0.1f);
		this.setTickRandomly(true);
		this.setSoundType(SoundType.PLANT);
	}

	@Override
	public int tickRate(World worldIn) {
		return 10;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos, state, rand);

		if (!world.isRemote) {
			IBlockState newState = world.isDaytime() ? ASBlocks.sage_flax_day.getDefaultState() : ASBlocks.sage_flax.getDefaultState();
			world.setBlockState(pos, newState);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return this == ASBlocks.sage_flax_day ? DAY_AABB : AABB;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Plains;
	}

	private void notifyNeighbors(World worldIn, BlockPos pos, EnumFacing facing) {
		worldIn.notifyNeighborsOfStateChange(pos, this, false);
		worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this, false);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

	}

	@Override
	public boolean isShearable(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos) {
		return this == ASBlocks.sage_flax && world instanceof World && !((World) world).isDaytime();
	}

	@Override
	public NonNullList<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		NonNullList<ItemStack> drops = NonNullList.create();

		if (world instanceof World) {
			World worldIn = (World) world;
			long timeUntilMidnight = worldIn.getWorldTime() % 24000; // Calculate time until midnight (in ticks)

			// If it's close to midnight (within an hour before or after)
			if (Math.abs(18000 - timeUntilMidnight) < 500) {
				drops.add(new ItemStack(ASItems.enchanted_filament, 2)); // Drop 2
			} else if (!worldIn.isDaytime()) { // During night, but not close to midnight
				drops.add(new ItemStack(ASItems.enchanted_filament)); // Drop 1
			}
			// No drops during daytime
		}

		return drops;
	}
}
