package ak.HyperDimensionalBag;

import ak.HyperDimensionalBag.client.GuiBag;
import ak.HyperDimensionalBag.inventory.ContainerBag;
import ak.HyperDimensionalBag.inventory.InventoryBag;
import ak.HyperDimensionalBag.item.ItemHDBag;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler {
    public void registerClientInfo() {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == HyperDimensionalBag.guiID) {
            ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem != ItemStack.EMPTY && heldItem.getItem() instanceof ItemHDBag) {
                InventoryBag inventorybag = new InventoryBag(heldItem, world);
                return new ContainerBag(player, inventorybag, heldItem.getItemDamage());
            } else return null;
        } else return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == HyperDimensionalBag.guiID) {
            ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem != ItemStack.EMPTY && heldItem.getItem() instanceof ItemHDBag) {
                InventoryBag inventorybag = new InventoryBag(heldItem, world);
                return new GuiBag(player, inventorybag, heldItem.getItemDamage());
            } else return null;
        } else return null;
    }

}