package com.windanesz.ancientspellcraft.registry;

import com.windanesz.ancientspellcraft.AncientSpellcraft;
import com.windanesz.ancientspellcraft.potion.PotionCandleLight;
import com.windanesz.ancientspellcraft.potion.PotionCurseAS;
import com.windanesz.ancientspellcraft.potion.PotionCurseDeath;
import com.windanesz.ancientspellcraft.potion.PotionCurseEnder;
import com.windanesz.ancientspellcraft.potion.PotionCurseWard;
import com.windanesz.ancientspellcraft.potion.PotionFeatherFall;
import com.windanesz.ancientspellcraft.potion.PotionMageLight;
import com.windanesz.ancientspellcraft.potion.PotionMagicEffectAS;
import com.windanesz.ancientspellcraft.potion.PotionMetamagicEffect;
import com.windanesz.ancientspellcraft.potion.PotionProjectileWard;
import com.windanesz.ancientspellcraft.potion.PotionTimeKnot;
import com.windanesz.ancientspellcraft.potion.PotionUnlimitedPower;
import com.windanesz.ancientspellcraft.potion.PotionWaterWalking;
import com.windanesz.ancientspellcraft.spell.FortifiedArchery;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(AncientSpellcraft.MODID)
@Mod.EventBusSubscriber
public class AncientSpellcraftPotions {

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder() { return null; }

	public static final Potion curse_of_ender = placeholder();
	public static final Potion unlimited_power = placeholder();
	public static final Potion martyr = placeholder();
	public static final Potion martyr_beneficial = placeholder();
	public static final Potion aquatic_agility = placeholder();
	public static final Potion lava_vision = placeholder();
	public static final Potion magma_strider = placeholder();
	public static final Potion magelight = placeholder();
	public static final Potion candlelight = placeholder();
	public static final Potion curse_ward = placeholder();
	public static final Potion curse_of_death = placeholder();
	public static final Potion time_knot = placeholder();
	public static final Potion curse_temporal_casualty = placeholder();
	public static final Potion feather_fall = placeholder();
	public static final Potion water_walking = placeholder();

	// metamagic
	public static final Potion arcane_augmentation = placeholder();
	public static final Potion intensifying_focus = placeholder();
	public static final Potion continuity_charm = placeholder();
	//

	public static final Potion projectile_ward = placeholder();
	public static final Potion bulwark = placeholder();
	public static final Potion arcane_aegis = placeholder();
	public static final Potion fortified_archery = placeholder();

	public static void registerPotion(IForgeRegistry<Potion> registry, String name, Potion potion) {
		potion.setRegistryName(AncientSpellcraft.MODID, name);
		potion.setPotionName("potion." + potion.getRegistryName().toString());
		registry.register(potion);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Potion> event) {

		IForgeRegistry<Potion> registry = event.getRegistry();

		// Interestingly, setting the colour to black stops the particles from rendering.

		registerPotion(registry, "curse_of_ender", new PotionCurseEnder());

		// AS 1.1
		registerPotion(registry, "unlimited_power", new PotionUnlimitedPower());

		registerPotion(registry, "martyr", new PotionMagicEffectAS("martyr", true, 0x0f000f,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_martyr.png")));

		registerPotion(registry, "martyr_beneficial", new PotionMagicEffectAS("martyr_beneficial", true, 0x0f000f,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_martyr_beneficial.png")));

		registerPotion(registry, "aquatic_agility", new PotionMagicEffectAS("aquatic_agility", false, 0x0EB6B1,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_aquatic_agility.png")).setBeneficial());

		registerPotion(registry, "lava_vision", new PotionMagicEffectAS("lava_vision", false, 0xF04900,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_lava_vision.png")).setBeneficial());

		registerPotion(registry, "magma_strider", new PotionMagicEffectAS("magma_strider", false, 0xD81A0B,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_magma_strider.png")).setBeneficial());

		registerPotion(registry, "magelight", new PotionMageLight());

		registerPotion(registry, "candlelight", new PotionCandleLight());

		registerPotion(registry, "curse_ward", new PotionCurseWard());

		registerPotion(registry, "curse_of_death", new PotionCurseDeath());

		registerPotion(registry, "time_knot", new PotionTimeKnot());

		registerPotion(registry, "curse_temporal_casualty", new PotionCurseAS("curse_temporal_casualty", true, 0xD81A0B,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_curse_temporal_casualty.png")));

		registerPotion(registry, "feather_fall", new PotionFeatherFall());

		registerPotion(registry, "water_walking", new PotionWaterWalking());

		// metamagic
		registerPotion(registry, "arcane_augmentation", new PotionMetamagicEffect("arcane_augmentation", false, 0xf0fafa,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_arcane_augmentation.png")).setBeneficial());

		registerPotion(registry, "intensifying_focus", new PotionMetamagicEffect("intensifying_focus", false, 0xf0fafa,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_intensifying_focus.png")).setBeneficial());

		registerPotion(registry, "continuity_charm", new PotionMetamagicEffect("continuity_charm", false, 0xf0fafa,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_icon_continuity_charm.png")).setBeneficial());

		registerPotion(registry, "projectile_ward", new PotionProjectileWard(projectile_ward, "projectile_ward", 0xf0fafa, false, 0.3f));
		registerPotion(registry, "bulwark", new PotionProjectileWard(bulwark, "bulwark", 0xf0fafa, true, 0.6f));
		registerPotion(registry, "arcane_aegis", new PotionProjectileWard(arcane_aegis, "arcane_aegis", 0xf0fafa, true, 0.8f));

		registerPotion(registry, "fortified_archery", new FortifiedArchery("fortified_archery", false, 0x166630,
				new ResourceLocation(AncientSpellcraft.MODID, "textures/gui/potion_fortified_archery.png")));

	}

}
