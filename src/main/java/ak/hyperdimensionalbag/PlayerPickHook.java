package ak.hyperdimensionalbag;

import ak.hyperdimensionalbag.inventory.InventoryBag;
import java.util.Collection;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class PlayerPickHook {

  @SubscribeEvent
  @SuppressWarnings("unused")
  public static void pickUpEvent(final EntityItemPickupEvent event) {
    if (HyperDimensionalBag.loadSB) {
      EntityPlayer player = event.getEntityPlayer();
      EntityItem item = event.getItem();
      NonNullList<ItemStack> inv = player.inventory.mainInventory;
      if (pickUpItemInBag(player.getEntityWorld(), inv, item.getItem())) {
        event.setCanceled(true);
        player.getEntityWorld()
            .playSound(player, new BlockPos(player.posX, player.posY, player.posZ),
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
                0.2F,
                ((player.getEntityWorld().rand.nextFloat() - player.getEntityWorld().rand
                    .nextFloat()) * 0.7F + 1.0F) * 2.0F);
        int stackSize = item.getItem().getCount();
        player.onItemPickup(item, stackSize);
      }
    }
  }

  private static boolean pickUpItemInBag(World world, NonNullList<ItemStack> inv, ItemStack item) {
    InventoryBag data;
    ItemStack storageStack;
//    for (ItemStack itemStack : inv) {
//      if (itemStack != null && itemStack.getItem() instanceof ItemHDBag) {
//        data = new InventoryBag(itemStack, world);
//        for (int j = 0; j < data.getSizeInventory(); j++) {
//          if (!data.getStackInSlot(j).isEmpty() && data.getStackInSlot(j)
//              .getItem() instanceof ItemStorageBox
//              && ItemStorageBox.isAutoCollect(data.getStackInSlot(j))) {
//            storageStack = ItemStorageBox.peekItemStackAll(data.getStackInSlot(j));
//            if (storageStack != null && areOreNameEquals(item, storageStack)) {
//              ItemStack copyStack = storageStack.copy();
//              copyStack.setCount(item.getCount());
//              ItemStorageBox.addItemStack(data.getStackInSlot(j), copyStack);
//              item.setCount(0);
//              return true;
//            }
//          }
//        }
//      }
//    }
    return false;
  }

  private boolean areOreNameEquals(ItemStack check, ItemStack target) {
    return check.isItemEqual(target);
/*        List<String> oreNames = getTagNames(target);
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

  private Collection<ResourceLocation> getTagNames(ItemStack itemStack) {
    return ItemTags.getCollection().getOwningTags(itemStack.getItem());
  }
}