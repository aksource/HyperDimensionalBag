package ak.HyperDimensionalBag;

import ak.HyperDimensionalBag.item.ItemBlockExchanger;
import ak.HyperDimensionalBag.item.ItemHDBag;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid="HyperDimensionalBag", name="HyperDimensionalBag", version="@VERSION@",dependencies="required-after:Forge@[10.12.0.1056,)", useMetadata = true)

public class HyperDimensionalBag
{
	@Mod.Instance("HyperDimensionalBag")
	public static HyperDimensionalBag instance;
	@SidedProxy(clientSide = "ak.HyperDimensionalBag.client.ClientProxy", serverSide = "ak.HyperDimensionalBag.CommonProxy")
	public static CommonProxy proxy;

	public static int guiID = 0;
	public static String GuiBagTex ="textures/gui/GuiBag.png";
	public static String TextureDomain = "hyperdimensionalbag:";
	public static String Assets = "hyperdimensionalbag";
	public static boolean loadSB = false;
	public static boolean hardRecipe;
    public static int maxRange;
    public static boolean exchangeInvisibleBlock;
	public static Item HDBag;
    public static Item itemBlockExchanger;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		hardRecipe = config.get(Configuration.CATEGORY_GENERAL, "HardRecipe", false).getBoolean(false);
        maxRange = config.get(Configuration.CATEGORY_GENERAL, "maxBlockExchangeRange", 10).getInt();
        exchangeInvisibleBlock = config.get(Configuration.CATEGORY_GENERAL, "exchangeInvisibleBlock", false, "true : exchange invisible block").getBoolean(false);
        config.save();
		HDBag = new ItemHDBag().setUnlocalizedName(TextureDomain + "Bag").setTextureName(TextureDomain + "Bag").setCreativeTab(CreativeTabs.tabTools);
		GameRegistry.registerItem(HDBag, "hyperdimentionalbag");
        itemBlockExchanger = new ItemBlockExchanger().setUnlocalizedName(TextureDomain + "BlockExchanger").setTextureName(TextureDomain + "BlockExchanger").setCreativeTab(CreativeTabs.tabTools);
        GameRegistry.registerItem(itemBlockExchanger, "itemblockexchanger");
	}
	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new PlayerPickHook());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        proxy.registerClientInfo();
		for(int i = 0;i<15;i++)
			GameRegistry.addShapelessRecipe(new ItemStack(HDBag, 1, i), new ItemStack(HDBag, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.dye, 1, i));
		if(!hardRecipe)
			GameRegistry.addShapedRecipe(new ItemStack(HDBag, 1, 15), "LDL","DCD","LDL", 'L',Items.leather,'D',Items.diamond,'C',Blocks.chest);
		else
			GameRegistry.addShapedRecipe(new ItemStack(HDBag, 1, 15), "LDL","DCD","LDL", 'L',Items.leather,'D',Items.diamond,'C',Items.nether_star);
        GameRegistry.addShapedRecipe(new ItemStack(itemBlockExchanger), " DE"," ID","S  ", 'E', Blocks.emerald_block, 'D', Blocks.diamond_block, 'I', Blocks.iron_block, 'S', Items.stick);
    }
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		loadSB = Loader.isModLoaded("net.minecraft.storagebox.mod_StorageBox");
	}
}