package ak.HyperDimensionalBag;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import static ak.HyperDimensionalBag.HyperDimensionalBag.HDBag;
import static ak.HyperDimensionalBag.HyperDimensionalBag.hardRecipe;
import static ak.HyperDimensionalBag.HyperDimensionalBag.itemBlockExchanger;

public class Recipes {
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();
        ResourceLocation hdBagRl = HDBag.getRegistryName();
        for (int i = 0; i < 15; i++)
            registry.register(new ShapelessOreRecipe(hdBagRl,
                    new ItemStack(HDBag, 1, i),
                    new ItemStack(HDBag, 1, OreDictionary.WILDCARD_VALUE),
                    new ItemStack(Items.DYE, 1, i)).setRegistryName(hdBagRl));
        if (!hardRecipe)
            registry.register(new ShapedOreRecipe(hdBagRl,
                    new ItemStack(HDBag, 1, 15),
                    "LDL", "DCD", "LDL",
                    'L', new ItemStack(Items.LEATHER, 1, 0),
                    'D', new ItemStack(Items.DIAMOND, 1, 0),
                    'C', new ItemStack(Blocks.CHEST, 1, 0)).setRegistryName(hdBagRl));
        else
            registry.register(new ShapedOreRecipe(hdBagRl,
                    new ItemStack(HDBag, 1, 15),
                    "LDL", "DCD", "LDL",
                    'L', new ItemStack(Items.LEATHER, 1, 0),
                    'D', new ItemStack(Items.DIAMOND, 1, 0),
                    'C', new ItemStack(Items.NETHER_STAR,1, 0)).setRegistryName(hdBagRl));
        registry.register(new ShapedOreRecipe(itemBlockExchanger.getRegistryName(),
                new ItemStack(itemBlockExchanger),
                " DE", " ID", "S  ",
                'E', new ItemStack(Blocks.EMERALD_BLOCK, 1, 0),
                'D', new ItemStack(Blocks.DIAMOND_BLOCK, 1, 0),
                'I', new ItemStack(Blocks.IRON_BLOCK,1, 0),
                'S', new ItemStack(Items.STICK, 1, 0)).setRegistryName(itemBlockExchanger.getRegistryName()));
    }
}
