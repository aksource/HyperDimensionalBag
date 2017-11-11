package ak.HyperDimensionalBag;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
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
        for (int i = 0; i < 15; i++)
            registry.register(new ShapelessOreRecipe(HDBag.getRegistryName(),
                    new ItemStack(HDBag, 1, i),
                    new ItemStack(HDBag, 1, OreDictionary.WILDCARD_VALUE),
                    new ItemStack(Items.DYE, 1, i)));
        if (!hardRecipe)
            registry.register(new ShapedOreRecipe(HDBag.getRegistryName(),
                    new ItemStack(HDBag, 1, 15),
                    "LDL", "DCD", "LDL",
                    'L', Items.LEATHER, 'D', Items.DIAMOND, 'C', Blocks.CHEST));
        else
            registry.register(new ShapedOreRecipe(HDBag.getRegistryName(),
                    new ItemStack(HDBag, 1, 15),
                    "LDL", "DCD", "LDL",
                    'L', Items.LEATHER, 'D', Items.DIAMOND, 'C', Items.NETHER_STAR));
        registry.register(new ShapedOreRecipe(itemBlockExchanger.getRegistryName(),
                new ItemStack(itemBlockExchanger),
                " DE", " ID", "S  ",
                'E', Blocks.EMERALD_BLOCK, 'D', Blocks.DIAMOND_BLOCK, 'I', Blocks.IRON_BLOCK, 'S', Items.STICK));
    }
}
