package com.windanesz.ancientspellcraft.core;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

public class ASMixinLoader implements ILateMixinLoader
{
	@Override
	public List<String> getMixinConfigs()
	{
		List<String> configs = new ArrayList<>();
		// CLIENT ONLY
		if (ASLoadingPlugin.isClient)
		{
			configs.add("ancientspellcraft.ebwizardry.client.mixins.json");
		}
		// COMMON
		configs.add("ancientspellcraft.ebwizardry.mixins.json");
		return configs;
	}

	@Override
	public boolean shouldMixinConfigQueue(String mixinConfig)
	{
		if (ASLoadingPlugin.isClient)
		{
			if (mixinConfig.equals("ancientspellcraft.ebwizardry.client.mixins.json")) {
				return Loader.isModLoaded("ebwizardry");
			}
		}
		if (mixinConfig.equals("ancientspellcraft.ebwizardry.mixins.json")) {
			return Loader.isModLoaded("ebwizardry");
		}
		return true;
	}
}