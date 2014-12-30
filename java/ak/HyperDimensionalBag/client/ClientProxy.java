package ak.HyperDimensionalBag.client;

import ak.HyperDimensionalBag.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
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
		registerItemModel(HDBag, "hyperdimentionalbag");
		registerItemModel(itemBlockExchanger, "itemblockexchanger");
	}

	private void registerItemModel(Item item, String registeredName) {
		ItemModelMesher itemModelMesher = mc.getRenderItem().getItemModelMesher();
		itemModelMesher.register(item, 0, new ModelResourceLocation(MOD_ID + ":" + registeredName, "inventory"));
	}
}