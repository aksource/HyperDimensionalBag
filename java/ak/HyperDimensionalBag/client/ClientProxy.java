package ak.HyperDimensionalBag.client;

import ak.HyperDimensionalBag.CommonProxy;
import ak.HyperDimensionalBag.HyperDimensionalBag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static ak.HyperDimensionalBag.HyperDimensionalBag.*;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	private Minecraft mc = Minecraft.getMinecraft();
	@Override
	public void registerClientInfo(){
        MinecraftForge.EVENT_BUS.register(new RenderBlockSelectionBox());
		for (int i = 0; i < 16; i++) {
			registerItemModel(HDBag, "hyperdimensionalbag", i);
		}
		registerItemModel(itemBlockExchanger, "itemblockexchanger", 0);
		ItemColors itemColors = mc.getItemColors();
		itemColors.registerItemColorHandler((stack, tintIndex) -> ItemDye.DYE_COLORS[stack.getItemDamage() % 16], HyperDimensionalBag.HDBag);
	}

	private void registerItemModel(Item item, String registeredName, int damage) {
		ItemModelMesher itemModelMesher = mc.getRenderItem().getItemModelMesher();
		itemModelMesher.register(item, damage, new ModelResourceLocation(MOD_ID + ":" + registeredName, "inventory"));
	}
}