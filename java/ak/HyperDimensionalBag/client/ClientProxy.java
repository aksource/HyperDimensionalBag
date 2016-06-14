package ak.HyperDimensionalBag.client;

import ak.HyperDimensionalBag.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerClientInfo(){
        MinecraftForge.EVENT_BUS.register(new RenderBlockSelectionBox());
	}
}