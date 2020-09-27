package com.windanesz.ancientspellcraft.registry;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.entity.EntityMageLight;
import com.windanesz.ancientspellcraft.entity.EntityVolcano;
import com.windanesz.ancientspellcraft.entity.EntityWisp;
import com.windanesz.ancientspellcraft.entity.construct.EntityAntiMagicField;
import com.windanesz.ancientspellcraft.entity.construct.EntitySilencingSigil;
import com.windanesz.ancientspellcraft.entity.construct.EntitySpiritWard;
import com.windanesz.ancientspellcraft.entity.construct.EntityTransportationPortal;
import com.windanesz.ancientspellcraft.entity.construct.EntityVenusFlyTrap;
import com.windanesz.ancientspellcraft.entity.living.EntityFireAnt;
import com.windanesz.ancientspellcraft.entity.living.EntitySkeletonMageMinion;
import com.windanesz.ancientspellcraft.entity.living.EntitySpellBook;
import com.windanesz.ancientspellcraft.entity.living.EntitySpiritBear;
import com.windanesz.ancientspellcraft.entity.living.EntityVoidCreeper;
import com.windanesz.ancientspellcraft.entity.living.EntityWolfMinion;
import com.windanesz.ancientspellcraft.entity.projectile.EntityDispelMagic;
import com.windanesz.ancientspellcraft.entity.projectile.EntityHeart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

//import com.windanesz.ancientspellcraft.entity.EntityMageLight;

@Mod.EventBusSubscriber
public class AncientSpellcraftEntities {

	private AncientSpellcraftEntities() {}

	private final static int VOID_CREEPER_SPAWN_RATE = 30;

	/**
	 * Most entity trackers fall into one of a few categories, so they are defined here for convenience. This
	 * generally follows the values used in vanilla for each entity type.
	 */
	enum TrackingType {

		LIVING(80, 3, true),
		PROJECTILE(64, 1, true),
		CONSTRUCT(160, 10, false);

		int range;
		int interval;
		boolean trackVelocity;

		TrackingType(int range, int interval, boolean trackVelocity) {
			this.range = range;
			this.interval = interval;
			this.trackVelocity = trackVelocity;
		}
	}

	/**
	 * Incrementing index for the mod-specific entity network ID.
	 */
	private static int id = 0;

	@SubscribeEvent
	public static void register(RegistryEvent.Register<EntityEntry> event) {

		IForgeRegistry<EntityEntry> registry = event.getRegistry();

		// projectile entities
		registry.register(createEntry(EntityWisp.class, "will_o_wisp", AncientSpellcraftEntities.TrackingType.PROJECTILE).build());

		registry.register(createEntry(EntityMageLight.class, "mage_light_entity", AncientSpellcraftEntities.TrackingType.PROJECTILE).build());

		registry.register(createEntry(EntityHeart.class, "healing_heart", AncientSpellcraftEntities.TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityDispelMagic.class, "dispel_magic", AncientSpellcraftEntities.TrackingType.PROJECTILE).build());

		//		registry.register(createEntry(EntitySpectralFishHook.class, "spectral_fish_hook", AncientSpellcraftEntities.TrackingType.PROJECTILE).build());

		// mobs

		registry.register(createEntry(EntityVoidCreeper.class, "void_creeper", TrackingType.LIVING)
				.egg(0x271b3b, 0x7f35fb)
				.spawn(EnumCreatureType.MONSTER, 20, 3, 5, ForgeRegistries.BIOMES.getValuesCollection()).build());

		registry.register(createEntry(EntitySkeletonMageMinion.class, "skeleton_mage_minion", TrackingType.LIVING).build());
		registry.register(createEntry(EntityWolfMinion.class, "wolf_minion", TrackingType.LIVING).egg(0xcc6f47, 0x676767).build());
		registry.register(createEntry(EntitySpiritBear.class, "spirit_bear", TrackingType.LIVING).egg(0xbcc2e8, 0xffffff).build());
		registry.register(createEntry(EntityVenusFlyTrap.class, "venus_fly_trap", TrackingType.LIVING).egg(0xbcc2e8, 0xffffff).build());
		registry.register(createEntry(EntityFireAnt.class, "fire_ant", TrackingType.LIVING).build());

		registry.register(createEntry(EntitySpellBook.class, "rouge_spell_book", TrackingType.LIVING).build());

		// constructs
		registry.register(createEntry(EntityTransportationPortal.class, "transportation_portal", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntitySpiritWard.class, "spirit_ward", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntitySilencingSigil.class, "silencing_sigil", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityVolcano.class, "entity_volcano", TrackingType.LIVING).build());

		registry.register(createEntry(EntityAntiMagicField.class, "anti_magic_field", TrackingType.CONSTRUCT).build());

	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id, and accepts a {@link TrackingType} for automatic tracker assignment.
	 *
	 * @param entityClass The entity class to use.
	 * @param name        The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 *                    {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param tracking    The {@link TrackingType} to use for this entity.
	 * @param <T>         The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name, TrackingType tracking) {
		return createEntry(entityClass, name).tracker(tracking.range, tracking.interval, tracking.trackVelocity);
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id.
	 *
	 * @param entityClass The entity class to use.
	 * @param name        The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 *                    {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param <T>         The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name) {
		ResourceLocation registryName = new ResourceLocation(AncientSpellcraft.MODID, name);
		return EntityEntryBuilder.<T>create().entity(entityClass).id(registryName, id++).name(registryName.toString());
	}
}
