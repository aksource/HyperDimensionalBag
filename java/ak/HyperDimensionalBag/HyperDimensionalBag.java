package ak.HyperDimensionalBag;

import ak.HyperDimensionalBag.item.ItemBlockExchanger;
import ak.HyperDimensionalBag.item.ItemHDBag;
import ak.HyperDimensionalBag.network.PacketHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

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
    public static final String MOD_DEPENDENCIES = "required-after:Forge@[12.17.0,)";
    public static final String MOD_MC_VERSION = "[1.9.4,1.10.2]";
    public static final Logger LOGGER = Logger.getLogger("HyperDimensionalBag");
    @Mod.Instance(MOD_ID)
    public static HyperDimensionalBag instance;
    @SidedProxy(clientSide = "ak.HyperDimensionalBag.client.ClientProxy", serverSide = "ak.HyperDimensionalBag.CommonProxy")
    public static CommonProxy proxy;
    public static int guiID = 0;
    public static String GuiBagTex = "textures/gui/guibag.png";
    public static String TextureDomain = "hyperdimensionalbag:";
    public static String Assets = "hyperdimensionalbag";
    public static boolean loadSB = false;
    public static boolean hardRecipe;
    public static int maxRange;
    public static boolean exchangeInvisibleBlock;
    public static Item HDBag;
    public static Item itemBlockExchanger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        hardRecipe = config.get(Configuration.CATEGORY_GENERAL, "HardRecipe", false).getBoolean(false);
        maxRange = config.get(Configuration.CATEGORY_GENERAL, "maxBlockExchangeRange", 10).getInt();
        exchangeInvisibleBlock = config.get(Configuration.CATEGORY_GENERAL, "exchangeInvisibleBlock", false, "true : exchange invisible block").getBoolean(false);
        config.save();
        HDBag = new ItemHDBag().setRegistryName("hyperdimensionalbag").setUnlocalizedName(TextureDomain + "Bag").setCreativeTab(CreativeTabs.TOOLS);
        GameRegistry.register(HDBag);
        itemBlockExchanger = new ItemBlockExchanger().setRegistryName("itemblockexchanger").setUnlocalizedName(TextureDomain + "BlockExchanger").setCreativeTab(CreativeTabs.TOOLS);
        GameRegistry.register(itemBlockExchanger);

        PacketHandler.init();
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PlayerPickHook());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        proxy.registerClientInfo();
        for (int i = 0; i < 15; i++)
            GameRegistry.addShapelessRecipe(
                    new ItemStack(HDBag, 1, i),
                    new ItemStack(HDBag, 1, OreDictionary.WILDCARD_VALUE),
                    new ItemStack(Items.DYE, 1, i));
        if (!hardRecipe)
            GameRegistry.addShapedRecipe(
                    new ItemStack(HDBag, 1, 15),
                    "LDL", "DCD", "LDL",
                    'L', Items.LEATHER, 'D', Items.DIAMOND, 'C', Blocks.CHEST);
        else
            GameRegistry.addShapedRecipe(
                    new ItemStack(HDBag, 1, 15),
                    "LDL", "DCD", "LDL",
                    'L', Items.LEATHER, 'D', Items.DIAMOND, 'C', Items.NETHER_STAR);
        GameRegistry.addShapedRecipe(new ItemStack(itemBlockExchanger),
                " DE", " ID", "S  ",
                'E', Blocks.EMERALD_BLOCK, 'D', Blocks.DIAMOND_BLOCK, 'I', Blocks.IRON_BLOCK, 'S', Items.STICK);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        loadSB = Loader.isModLoaded("storagebox");
    }
}