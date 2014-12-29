package ak.HyperDimensionalBag.client;

import ak.HyperDimensionalBag.HyperDimensionalBag;
import ak.HyperDimensionalBag.inventory.ContainerBag;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiBag extends GuiContainer
{
	private static final ResourceLocation guiTex = new ResourceLocation(HyperDimensionalBag.Assets, HyperDimensionalBag.GuiBagTex);
	IInventory bagData;
	int metaData;
	public GuiBag(EntityPlayer player, IInventory data, int meta)
	{
		super(new ContainerBag(player, data, meta));
		bagData = data;
		metaData = meta;
        short short1 = 222;
        int i = short1 - 108;
        this.ySize = i + 6 * 18;
	}
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(guiTex);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
	
}