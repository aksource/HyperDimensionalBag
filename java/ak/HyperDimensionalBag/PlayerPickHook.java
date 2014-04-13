package ak.HyperDimensionalBag;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.storagebox.ItemStorageBox;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerPickHook
{
	@SubscribeEvent
	public void pickUpEvent(EntityItemPickupEvent event){
		if(HyperDimensionalBag.loadSB)
		{
			EntityPlayer player = event.entityPlayer;
			EntityItem item = event.item;
			int stackSize = item.getEntityItem().stackSize;
			ItemStack[] inv = player.inventory.mainInventory;
			if(pickUpItemInBag(player.worldObj,inv,item.getEntityItem()))
			{
				event.setCanceled(true);
				player.worldObj.playSoundAtEntity(item, "random.pop", 0.2F, ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				player.onItemPickup(item, stackSize);
			}
		}
	}
	private boolean pickUpItemInBag(World world,ItemStack[] inv, ItemStack item)
	{
		InventoryBag data;
		ItemStack storageStack;
		for(int i=0;i<inv.length;i++)
		{
			if(inv[i] != null && inv[i].getItem() instanceof ItemHDBag)
			{
                data = new InventoryBag(inv[i], world);
				if(data != null)
				{
					for(int j=0;j<data.getSizeInventory();j++)
					{
						if(data.getStackInSlot(j) != null && data.getStackInSlot(j).getItem() instanceof ItemStorageBox
								&& ItemStorageBox.isAutoCollect(data.getStackInSlot(j)))
						{
							storageStack = ItemStorageBox.peekItemStackAll(data.getStackInSlot(j));
							if(storageStack != null && item.isItemEqual(storageStack))
							{
								ItemStorageBox.addItemStack(data.getStackInSlot(j), item);
								item.stackSize = 0;
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}