package com.windanesz.ancientspellcraft.mixin.ebwizardry;

import com.windanesz.ancientspellcraft.registry.ASItems;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.ConjureBlock;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConjureBlock.class)
public class MixinConjureBlock {

	@Unique
	private static final String BLOCK_LIFETIME = "block_lifetime";

	@Inject(method = "onMiss", at = @At("HEAD"), cancellable = true, remap = false)
	private void mixinPerformEffectConsistent(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers, CallbackInfoReturnable<Boolean> ci) {
		if (caster instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer) caster, ASItems.ring_solid_air)) {
			// Calculate new position 2 blocks further in the direction the player is looking
			Vec3d newPosition = origin.add(direction.scale(2.0));
			BlockPos pos = new BlockPos(newPosition.x, newPosition.y, newPosition.z);

			if(BlockUtils.canBlockBeReplaced(world, pos)){

				if(!world.isRemote){

					world.setBlockState(pos, WizardryBlocks.spectral_block.getDefaultState());

					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(((ConjureBlock) (Object) this).getProperty(BLOCK_LIFETIME).floatValue()
								* modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				ci.setReturnValue(true);
				ci.cancel();
			}
		}
	}
}