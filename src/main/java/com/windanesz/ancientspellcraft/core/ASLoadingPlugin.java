package com.windanesz.ancientspellcraft.core;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("AncientSpellcraftCore")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class ASLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
	public static final boolean isClient = FMLLaunchHandler.side().isClient();

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public List<String> getMixinConfigs() {
		List<String> configs = new ArrayList<>();
		// CLIENT ONLY
		if (isClient) {
			configs.add("ancientspellcraft.minecraft.client.mixins.json");
		}
		// COMMON
		configs.add("ancientspellcraft.minecraft.mixins.json");
		return configs;
	}

	@Override
	public boolean shouldMixinConfigQueue(String mixinConfig) {
		if (isClient) {
			if (mixinConfig.equals("ancientspellcraft.minecraft.client.mixins.json")) {
				return true;
			}
		}
		if (mixinConfig.equals("ancientspellcraft.minecraft.mixins.json")) {
			return true;
		}
		return true;
	}
}
