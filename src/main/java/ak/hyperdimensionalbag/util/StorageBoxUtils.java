package ak.hyperdimensionalbag.util;

import net.minecraft.item.ItemStack;

/**
 * Created by A.K. on 2019/06/01.
 */
public class StorageBoxUtils {
  public static boolean checkStorageBox(ItemStack item, ItemStack targetBlockStack) {
    return false/*HyperDimensionalBag.loadSB
        && item.getItem() instanceof ItemStorageBox
        && ItemStorageBox.peekItemStackAll(item) != null
        && checkValidBlock(targetBlockStack, ItemStorageBox.peekItemStackAll(item), drops)
        && ItemStorageBox.peekItemStackAll(item).getCount() > 1*/;
  }

  public static void removeStack(ItemStack item) {
//    ItemStack copy = ItemStorageBox.peekItemStack(item);
//    copy.setCount(1);
//    ItemStorageBox.removeItemStack(item, copy);
  }
}
