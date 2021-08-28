package ak.hyperdimensionalbag.util;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;

import static ak.hyperdimensionalbag.item.BlockExchangerItem.NBT_KEY_BLOCK_STATE;

/**
 * BlockState用マネージャークラス Created by A.K. on 14/12/29.
 */
public class BlockStateManager {

  public static void addBlockStateToNBT(ItemStack itemStack, BlockState blockState) {
    itemStack.getOrCreateTag().put(NBT_KEY_BLOCK_STATE, NBTUtil.writeBlockState(blockState));
  }

//  public static NBTTagList getTagList(IBlockState blockState) {
//    NBTTagList nbtTagList = new NBTTagList();
//    blockState.getProperties().forEach(property -> {
//      NBTTagCompound nbt = new NBTTagCompound();
//      nbt.setString("propertyName", property.getName());
//      nbt.setString("propertyValue", getValueName(property, blockState::get));
//      nbtTagList.add(nbt);
//    });
//    return nbtTagList;
//  }

  public static BlockState setBlockStateFromNBT(ItemStack itemStack, BlockState defaultState) {
    if (itemStack.hasTag() && itemStack.getTag().contains(NBT_KEY_BLOCK_STATE)) {
      return NBTUtil.readBlockState(itemStack.getTag().getCompound(NBT_KEY_BLOCK_STATE));
    }
    return defaultState;
  }

//  public static IBlockState setProperty(IBlockState defaultState, NBTTagList nbtTagList) {
//    String propertyName, valueName;
//    for (int i = 0; i < nbtTagList.size(); i++) {
//      propertyName = nbtTagList.getCompound(i).getString("propertyName");
//      valueName = nbtTagList.getCompound(i).getString("propertyValue");
//      defaultState = setPropertyWithString(defaultState, propertyName, valueName);
//    }
//    return defaultState;
//  }
//
//  public static IBlockState setPropertyWithString(IBlockState blockState, String propertyName,
//      String valueName) {
//    IProperty<? extends Comparable<?>> property = getProperty(blockState, propertyName);
//    if (property != null) {
//      Comparable value = getPropertyValue(property, valueName, blockState::get);
//      return blockState.with(property, value);
//    }
//    return blockState;
//  }
//
//  public static <T extends Comparable<T>> T getPropertyValue(IProperty<T> property, String valueName,
//      Function<IProperty<T>, T> defaultValueGetter) {
//    Class valueClass = property.getValueClass();
//    for (T comparable : property.getAllowedValues()) {
//      if (comparable.getClass() == valueClass && valueName.equals(property.getName(comparable))) {
//        return comparable;
//      }
//    }
//    return defaultValueGetter.apply(property);
//  }
//
//  public static <T extends Comparable<T>> String getValueName(IProperty<T> property, Function<IProperty<T>, T>getter) {
//    return property.getName(getter.apply(property));
//  }
//
//  public static IProperty<? extends Comparable<?>> getProperty(IBlockState blockState, String propertyName) {
//    for (IProperty<?> property : blockState.getProperties()) {
//      if (property.getName().equals(propertyName)) {
//        return property;
//      }
//    }
//    HyperDimensionalBag.LOGGER.warning("[HyperDimensional Bag] Illegal Property Name !");
//    return null;
//  }
}
