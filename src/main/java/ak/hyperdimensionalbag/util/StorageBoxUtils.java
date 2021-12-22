package ak.hyperdimensionalbag.util;


import ak.hyperdimensionalbag.HyperDimensionalBag;
import ak.hyperdimensionalbag.item.BlockExchangerItem;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by A.K. on 2019/06/01.
 */
public class StorageBoxUtils {
  private static final String CLASS_NAME = "ItemStorageBox";
  private static final String PEEK_ITEM_STACK_ALL = "peekItemStackAll";
  private static final String PEEK_ITEM_STACK = "peekItemStack";
  private static final String REMOVE_ITEM_STACK = "removeItemStack";
  private static final String IS_AUTO_COLLECT = "isAutoCollect";
  private static final String ADD_ITEM_STACK = "addItemStack";

  public static boolean isItemStorageBox(ItemStack itemStack) {
    var item = itemStack.getItem();
    var clazz = item.getClass();
    var className = clazz.getSimpleName();
    return CLASS_NAME.equals(className);
  }

  public static ItemStack peekItemStackAll(ItemStack itemStack) {
    var item = itemStack.getItem();
    var clazz = item.getClass();
    if (!isItemStorageBox(itemStack)) return ItemStack.EMPTY;
    try {
      var method = clazz.getMethod(PEEK_ITEM_STACK_ALL, ItemStack.class);
      return (ItemStack) method.invoke(null, itemStack);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return ItemStack.EMPTY;
    }
  }

  public static ItemStack peekItemStack(ItemStack itemStack, int maxNum) {
    var item = itemStack.getItem();
    var clazz = item.getClass();
    if (!isItemStorageBox(itemStack)) return ItemStack.EMPTY;
    try {
      var method = clazz.getMethod(PEEK_ITEM_STACK, ItemStack.class, int.class);
      return (ItemStack) method.invoke(null, itemStack, maxNum);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return ItemStack.EMPTY;
    }
  }

  @SuppressWarnings("UnusedReturnValue")
  public static ItemStack removeItemStack(ItemStack itemStack, ItemStack removeStack) {
    var item = itemStack.getItem();
    var clazz = item.getClass();
    if (!isItemStorageBox(itemStack)) return ItemStack.EMPTY;
    try {
      var method = clazz.getMethod(REMOVE_ITEM_STACK, ItemStack.class, ItemStack.class);
      return (ItemStack) method.invoke(null, itemStack, removeStack);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return ItemStack.EMPTY;
    }
  }

  public static boolean isAutoCollect(ItemStack itemStack) {
    var item = itemStack.getItem();
    var clazz = item.getClass();
    if (!isItemStorageBox(itemStack)) return false;
    try {
      var method = clazz.getMethod(IS_AUTO_COLLECT, ItemStack.class);
      return (boolean) method.invoke(null, itemStack);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void addItemStack(ItemStack itemStack, ItemStack addStack) {
    var item = itemStack.getItem();
    var clazz = item.getClass();
    if (!isItemStorageBox(itemStack)) return;
    try {
      var method = clazz.getMethod(ADD_ITEM_STACK, ItemStack.class, ItemStack.class);
      method.invoke(null, itemStack, addStack);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  public static boolean checkStorageBox(ItemStack itemStack, ItemStack targetBlockStack, List<ItemStack> drops) {
    return HyperDimensionalBag.loadSB
        && isItemStorageBox(itemStack)
        && !peekItemStackAll(itemStack).isEmpty()
        && BlockExchangerItem.checkValidBlock(targetBlockStack, peekItemStackAll(itemStack), drops)
        && peekItemStackAll(itemStack).getCount() > 1;
  }

  public static void removeStack(ItemStack itemStack) {
    var copy = peekItemStack(itemStack, 1);
    copy.setCount(1);
    removeItemStack(itemStack, copy);
  }
}
