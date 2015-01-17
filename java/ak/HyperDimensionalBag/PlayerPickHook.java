package ak.HyperDimensionalBag;

import ak.HyperDimensionalBag.inventory.InventoryBag;
import ak.HyperDimensionalBag.item.ItemHDBag;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.storagebox.ItemStorageBox;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.LinkedHashSet;

public class PlayerPickHook
{
	@SubscribeEvent
	public void pickUpEvent(EntityItemPickupEvent event){
		if(HyperDimensionalBag.loadSB)
		{
			EntityPlayer player = event.entityPlayer;
			EntityItem item = event.item;
			ItemStack[] inv = player.inventory.mainInventory;
			if(pickUpItemInBag(player.worldObj,inv,item.getEntityItem()))
			{
				event.setCanceled(true);
				player.worldObj.playSoundAtEntity(item, "random.pop", 0.2F, ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                int stackSize = item.getEntityItem().stackSize;
                player.onItemPickup(item, stackSize);
			}
		}
	}
	private boolean pickUpItemInBag(World world,ItemStack[] inv, ItemStack item)
	{
		InventoryBag data;
		ItemStack storageStack;
		for(ItemStack itemStack : inv)
		{
			if(itemStack != null && itemStack.getItem() instanceof ItemHDBag)
			{
                data = new InventoryBag(itemStack, world);
                for(int j = 0 ; j < data.getSizeInventory();j++)
                {
                    if(data.getStackInSlot(j) != null && data.getStackInSlot(j).getItem() instanceof ItemStorageBox
                            && ItemStorageBox.isAutoCollect(data.getStackInSlot(j)))
                    {
                        storageStack = ItemStorageBox.peekItemStackAll(data.getStackInSlot(j));
                        if(storageStack != null && areOreNameEquals(item, storageStack))
                        {
                            ItemStack copyStack = storageStack.copy();
                            copyStack.stackSize = item.stackSize;
                            ItemStorageBox.addItemStack(data.getStackInSlot(j), copyStack);
                            item.stackSize = 0;
                            return true;
                        }
                    }
                }
			}
		}
		return false;
	}

    private boolean areOreNameEquals(ItemStack check, ItemStack target) {
        return check.isItemEqual(target);
/*        LinkedHashSet<String> oreNames = getOreNames(target);
        if (oreNames != null && oreNames.size() > 0) {
            for (String oreName : oreNames) {
                for (ItemStack itemStack : OreDictionary.getOres(oreName)) {
                    if (check.isItemEqual(itemStack)) return true;
                }
            }
            return false;
        } else {
            return check.isItemEqual(target);
        }*/
    }

    private LinkedHashSet<String> getOreNames(ItemStack itemStack) {
        int[] oreIDs = OreDictionary.getOreIDs(itemStack);
        if (oreIDs.length > 0) {
            LinkedHashSet<String> oreNames = Sets.newLinkedHashSet();
            for (int id : oreIDs) {
                oreNames.add(OreDictionary.getOreName(id));
            }
            return oreNames;
        }
        return null;
    }
}