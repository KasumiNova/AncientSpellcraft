package com.windanesz.ancientspellcraft.entity.ai;

import com.windanesz.ancientspellcraft.entity.living.ICustomCooldown;
import com.windanesz.ancientspellcraft.item.ItemBattlemageSword;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAIBattlemageMelee<T extends EntityLiving & ICustomCooldown> extends EntityAIBase {

	protected final T entity;
	World world;
	protected EntityCreature battlemage;
	protected int attackTick;
	double speedTowardsTarget;
	boolean longMemory;
	Path path;
	private int delayCounter;
	private double targetX;
	private double targetY;
	private double targetZ;
	//	protected final int attackInterval = 20;

	public EntityAIBattlemageMelee(T creature, double speedIn, boolean useLongMemory) {
		this.battlemage = (EntityCreature) creature;
		this.entity = creature;
		this.world = creature.world;
		this.speedTowardsTarget = speedIn;
		this.longMemory = useLongMemory;
		this.setMutexBits(3);
	}

	public boolean shouldExecute() {

		if (this.entity.getCooldown() <= 0) return false;

		EntityLivingBase target = this.battlemage.getAttackTarget();

		if (target == null) {
			return false;
		} else if (!target.isEntityAlive()) {
			return false;
		} else {
			this.path = this.battlemage.getNavigator().getPathToEntityLiving(target);

			if (this.path != null) {
				return true;
			} else {
				return this.getAttackReachSqr(target) >= this.battlemage.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
			}
		}
	}

	public void startExecuting() {
		this.battlemage.getNavigator().setPath(this.path, this.speedTowardsTarget);
		this.delayCounter = 0;
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (entity.getCooldown() > 0) {
			entity.decrementCooldown();
		}

		if (entity.getCooldown() == 0) {
			return false;
		}

		EntityLivingBase entitylivingbase = this.battlemage.getAttackTarget();

		if (entitylivingbase == null) {
			return false;
		} else if (!entitylivingbase.isEntityAlive()) {
			return false;
		} else if (!this.longMemory) {
			return !this.battlemage.getNavigator().noPath();
		} else if (!this.battlemage.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))) {
			return false;
		} else {
			return !(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer) entitylivingbase).isSpectator() && !((EntityPlayer) entitylivingbase).isCreative();
		}
	}

	public void resetTask() {
		EntityLivingBase entitylivingbase = this.battlemage.getAttackTarget();

		if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer) entitylivingbase).isSpectator() || ((EntityPlayer) entitylivingbase).isCreative())) {
			this.battlemage.setAttackTarget((EntityLivingBase) null);
		}

		this.battlemage.getNavigator().clearPath();
	}

	public void updateTask() {
		EntityLivingBase entitylivingbase = this.battlemage.getAttackTarget();
		this.battlemage.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
		double d0 = this.battlemage.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
		--this.delayCounter;

		if ((this.longMemory || this.battlemage.getEntitySenses().canSee(entitylivingbase)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.battlemage.getRNG().nextFloat() < 0.05F)) {
			this.targetX = entitylivingbase.posX;
			this.targetY = entitylivingbase.getEntityBoundingBox().minY;
			this.targetZ = entitylivingbase.posZ;
			this.delayCounter = 4 + this.battlemage.getRNG().nextInt(7);

			if (d0 > 1024.0D) {
				this.delayCounter += 10;
			} else if (d0 > 256.0D) {
				this.delayCounter += 5;
			}

			if (!this.battlemage.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) {
				this.delayCounter += 15;
			}
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);
		this.checkAndPerformAttack(entitylivingbase, d0);
	}

	protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
		double d0 = this.getAttackReachSqr(enemy);

		if (distToEnemySqr <= d0 && this.attackTick <= 0) {
			this.attackTick = 20;
			this.battlemage.swingArm(EnumHand.MAIN_HAND);
			this.battlemage.attackEntityAsMob(enemy);
			if (this.battlemage.getHeldItemMainhand().getItem() instanceof ItemBattlemageSword) {
				this.battlemage.getHeldItemMainhand().getItem().hitEntity(this.battlemage.getHeldItemMainhand(), enemy, this.battlemage);
			}
		}
	}

	protected double getAttackReachSqr(EntityLivingBase attackTarget) {
		return (double) (this.battlemage.width * 2.0F * this.battlemage.width * 2.0F + attackTarget.width);
	}
}
