package com.windanesz.ancientspellcraft;

import com.windanesz.ancientspellcraft.client.gui.GuiHandlerAS;
import com.windanesz.ancientspellcraft.command.CommandListBiomes;
import com.windanesz.ancientspellcraft.packet.ASPacketHandler;
import com.windanesz.ancientspellcraft.registry.AncientSpellcraftBlocks;
import com.windanesz.ancientspellcraft.registry.AncientSpellcraftItems;
import com.windanesz.ancientspellcraft.registry.AncientSpellcraftLoot;
import electroblob.wizardry.constants.Element;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(modid = AncientSpellcraft.MODID, name = AncientSpellcraft.NAME, version = AncientSpellcraft.VERSION, acceptedMinecraftVersions = AncientSpellcraft.MC_VERSION, dependencies = "required-after:ebwizardry@[4.2.10,4.3)")
public class AncientSpellcraft {

	public static final String MODID = "ancientspellcraft";
	public static final String NAME = "Ancient Spellcraft by Dan";
	public static final String VERSION = "1.0.2";
	public static final String MC_VERSION = "[1.12.2]";

	public static Element RUNIC;

	public static final Random rand = new Random();

	//	public static final Settings settings = new Settings();

	/**
	 * Static instance of the {@link Settings} object for Wizardry.
	 */
	public static final Settings settings = new Settings();

	public static Logger logger;

	// The instance of wizardry that Forge uses.
	@Mod.Instance(AncientSpellcraft.MODID)
	public static AncientSpellcraft instance;

	// Location of the proxy code, used by Forge.
	@SidedProxy(clientSide = "com.windanesz.ancientspellcraft.client.ClientProxy", serverSide = "com.windanesz.ancientspellcraft.CommonProxy")
	public static CommonProxy proxy;

	/**
	 * Static instance of the {@link electroblob.wizardry.Settings} object for Wizardry.
	 */

	//	public static final Logger LOG = LogManager.getLogger(MODID);
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		proxy.registerRenderers();


		AncientSpellcraftLoot.register();
		AncientSpellcraftBlocks.registerTileEntities();

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		AncientSpellcraftItems.registerDispenseBehaviours();
		MinecraftForge.EVENT_BUS.register(instance); // Since there's already an instance we might as well use it
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerAS());
		ASPacketHandler.initPackets();

		proxy.registerParticles();

		//		Style style = new Style();
//		style.setColor(TextFormatting.GOLD);
//		System.out.println("registering runic element");
//		RUNIC = WizardryEnumHelper.addElement("runic", style, "Runic", AncientSpellcraft.MODID);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandListBiomes());
	}
}
