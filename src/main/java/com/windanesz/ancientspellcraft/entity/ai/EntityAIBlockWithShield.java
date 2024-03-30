package com.windanesz.ancientspellcraft.entity.ai;

import com.windanesz.ancientspellcraft.entity.living.EntityClassWizard;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class EntityAIBlockWithShield<T extends EntityLiving & IShieldUser> extends EntityAIBase {
	private final T battlemage;
	private EntityLivingBase target;
	private static final float SAFE_MELEE_DISTANCE = 4.5F;
	private final int maxReactionDelay = 5; //TODO turn into setting
	private final int maxReactionDelayBow = 30; //TODO turn into setting
	private int reactionDelayTicks = 0;
	private static final int SHIELD_WITHDRAW_DELAY = 40;
	private int shieldWithdrawTicks = 0;

	public EntityAIBlockWithShield(T shieldUser) {
		super();
		this.battlemage = shieldUser;
	}

	@Override
	public boolean shouldExecute() {
		return canExecute(e -> e.isEntityAlive() && shouldDefendFrom(e));
	}

	private boolean canExecute(Predicate<EntityLivingBase> defendFrom) {
		if (target == null) {
			return hasShieldInOffhand() && battlemage.getShieldDisabledTick() <= 0 && (shieldWithdrawTicks > 0 || getAttackTarget().map(defendFrom::test).orElse(false));
		}
		return hasShieldInOffhand() && battlemage.getShieldDisabledTick() <= 0 && target.isEntityAlive() && getAttackTarget().isPresent() && (shieldWithdrawTicks > 0 || getAttackTarget().map(t -> t.equals(target) && shouldDefendFrom(target)).orElse(false));
	}

	private boolean shouldDefendFrom(@Nullable EntityLivingBase e) {
		return e != null && isPlayerOrTargetsThisNpc(e) && ((e.isSwingInProgress && battlemage.getDistance(e) < 4) || isAimingWithBow(e));
	}

	private boolean isPlayerOrTargetsThisNpc(EntityLivingBase e) {
		if (e instanceof EntityPlayer) {
			return true;
		}
		if (e instanceof EntityLiving) {
			EntityLiving living = (EntityLiving) e;
			return living.getAttackTarget() != null && living.getAttackTarget().equals(battlemage);
		}
		return false;
	}

	@Override
	public void startExecuting() {
		if (!battlemage.isHandActive()) {
			init();
		}
	}

	@SuppressWarnings("java:S2259")
	private void init() {
		target = getAttackTarget().orElse(null);
		reactionDelayTicks = new Random(battlemage.world.getTotalWorldTime()).nextInt(target != null && isAimingWithBow(target) ? maxReactionDelayBow : maxReactionDelay);
		shieldWithdrawTicks = SHIELD_WITHDRAW_DELAY;
	}

	@Override
	public final void resetTask() {
		target = null;
		reactionDelayTicks = 0;
	}

	@Override
	public final void updateTask() {
		if (reactionDelayTicks > 0) {
			reactionDelayTicks--;
			return;
		}
		if (!shouldDefendFrom(target)) {
			shieldWithdrawTicks--;
			return;
		}
		battlemage.setActiveHand(EnumHand.OFF_HAND);

		if (target != null) {
			double distanceToEntity = battlemage.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);

			if (!shouldCloseOnTarget(distanceToEntity) || isAimingWithBow(target)) {
				startBlocking();
				battlemage.getNavigator().tryMoveToXYZ(target.posX, target.posY, target.posZ, 0.7f);
			}
		}
	}

	private void startBlocking() {
		battlemage.setActiveHand(EnumHand.OFF_HAND);
	}

	private Optional<EntityLivingBase> getAttackTarget() {
		return battlemage.getAttackTarget() != null ? Optional.of(battlemage.getAttackTarget()) : Optional.ofNullable(battlemage.getRevengeTarget());
	}

	protected boolean shouldCloseOnTarget(double distanceToEntity) {
		double attackDistance = (battlemage.width / 2D) + (getTarget().width / 2D) + SAFE_MELEE_DISTANCE;
		return (distanceToEntity > (attackDistance * attackDistance)) || !battlemage.getEntitySenses().canSee(getTarget());
	}

	public final EntityLivingBase getTarget() {
		return target;
	}

	private boolean hasShieldInOffhand() {
		return battlemage.getHeldItemOffhand().getItem().isShield(battlemage.getHeldItemOffhand(), battlemage);
	}

	private boolean isAimingWithBow(EntityLivingBase entity) {
		return (isBow(entity.getHeldItemMainhand().getItem()) && entity.isHandActive() && entity.getActiveHand() == EnumHand.MAIN_HAND) ||
				(isBow(entity.getHeldItemOffhand().getItem()) && entity.isHandActive() && entity.getActiveHand() == EnumHand.OFF_HAND);
	}

	public static boolean isBow(Item stack) {
		return stack instanceof ItemBow;
	}
}
