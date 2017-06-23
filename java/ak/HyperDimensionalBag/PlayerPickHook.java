package ak.HyperDimensionalBag;

import ak.HyperDimensionalBag.inventory.InventoryBag;
import ak.HyperDimensionalBag.item.ItemHDBag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.storagebox.ItemStorageBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class PlayerPickHook {
    @SubscribeEvent
    public void pickUpEvent(EntityItemPickupEvent event) {
        if (HyperDimensionalBag.loadSB) {
            EntityPlayer player = event.getEntityPlayer();
            EntityItem item = event.getItem();
            NonNullList<ItemStack> inv = player.inventory.mainInventory;
            if (pickUpItemInBag(player.getEntityWorld(), inv, item.getItem())) {
                event.setCanceled(true);
                player.getEntityWorld().playSound(player, new BlockPos(player.posX, player.posY, player.posZ),
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
                        0.2F,
                        ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                int stackSize = item.getItem().getCount();
                player.onItemPickup(item, stackSize);
            }
        }
    }

    private boolean pickUpItemInBag(World world, NonNullList<ItemStack> inv, ItemStack item) {
        InventoryBag data;
        ItemStack storageStack;
        for (ItemStack itemStack : inv) {
            if (itemStack != null && itemStack.getItem() instanceof ItemHDBag) {
                data = new InventoryBag(itemStack, world);
                for (int j = 0; j < data.getSizeInventory(); j++) {
                    if (!data.getStackInSlot(j).isEmpty() && data.getStackInSlot(j).getItem() instanceof ItemStorageBox
                            && ItemStorageBox.isAutoCollect(data.getStackInSlot(j))) {
                        storageStack = ItemStorageBox.peekItemStackAll(data.getStackInSlot(j));
                        if (storageStack != null && areOreNameEquals(item, storageStack)) {
                            ItemStack copyStack = storageStack.copy();
                            copyStack.setCount(item.getCount());
                            ItemStorageBox.addItemStack(data.getStackInSlot(j), copyStack);
                            item.setCount(0);
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
/*        List<String> oreNames = getOreNames(target);
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

    private List<String> getOreNames(ItemStack itemStack) {
        int[] oreIDs = OreDictionary.getOreIDs(itemStack);
        if (oreIDs.length > 0) {
            List<String> oreNames = new ArrayList<>(oreIDs.length);
            for (int id : oreIDs) {
                oreNames.add(OreDictionary.getOreName(id));
            }
            return oreNames;
        }
        return null;
    }
}