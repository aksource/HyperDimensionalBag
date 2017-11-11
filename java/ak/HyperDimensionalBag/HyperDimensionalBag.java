package ak.HyperDimensionalBag;

import ak.HyperDimensionalBag.item.ItemBlockExchanger;
import ak.HyperDimensionalBag.item.ItemHDBag;
import ak.HyperDimensionalBag.network.PacketHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.logging.Logger;

@Mod(modid = HyperDimensionalBag.MOD_ID,
        name = HyperDimensionalBag.MOD_NAME,
        version = HyperDimensionalBag.MOD_VERSION,
        dependencies = HyperDimensionalBag.MOD_DEPENDENCIES,
        useMetadata = true,
        acceptedMinecraftVersions = HyperDimensionalBag.MOD_MC_VERSION)
public class HyperDimensionalBag {
    public static final String MOD_ID = "hyperdimensionalbag";
    public static final String MOD_NAME = "HyperDimensionalBag";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_DEPENDENCIES = "required-after:forge@[14.21.1,)";
    public static final String MOD_MC_VERSION = "[1.12,1.19.99]";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    @Mod.Instance(MOD_ID)
    public static HyperDimensionalBag instance;
    @SidedProxy(clientSide = "ak.HyperDimensionalBag.client.ClientProxy", serverSide = "ak.HyperDimensionalBag.CommonProxy")
    public static CommonProxy proxy;
    public static int guiID = 0;
    public static String GuiBagTex = "textures/gui/bag-gui.png";
    public static String TextureDomain = "hyperdimensionalbag:";
    public static String Assets = "hyperdimensionalbag";
    public static boolean loadSB = false;
    public static boolean hardRecipe;
    public static int maxRange;
    public static boolean exchangeInvisibleBlock;
    public static Item HDBag;
    public static Item itemBlockExchanger;

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        hardRecipe = config.get(Configuration.CATEGORY_GENERAL, "HardRecipe", false).getBoolean(false);
        maxRange = config.get(Configuration.CATEGORY_GENERAL, "maxBlockExchangeRange", 10).getInt();
        exchangeInvisibleBlock = config.get(Configuration.CATEGORY_GENERAL, "exchangeInvisibleBlock", false, "true : exchange invisible block").getBoolean(false);
        config.save();
        HDBag = new ItemHDBag().setRegistryName("hyperdimensionalbag").setUnlocalizedName(TextureDomain + "Bag").setCreativeTab(CreativeTabs.TOOLS);
        itemBlockExchanger = new ItemBlockExchanger().setRegistryName("itemblockexchanger").setUnlocalizedName(TextureDomain + "BlockExchanger").setCreativeTab(CreativeTabs.TOOLS);

        MinecraftForge.EVENT_BUS.register(new RegistrationUtils());
        PacketHandler.init();
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PlayerPickHook());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        proxy.registerClientInfo();
        MinecraftForge.EVENT_BUS.register(new Recipes());
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void postInit(FMLPostInitializationEvent event) {
        loadSB = Loader.isModLoaded("storagebox");
    }
}