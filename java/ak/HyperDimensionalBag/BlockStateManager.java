package ak.HyperDimensionalBag;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

/**
 * BlockState用マネージャークラス
 * Created by A.K. on 14/12/29.
 */
public class BlockStateManager {

    public static void addBlockStateToNBT(ItemStack itemStack, IBlockState blockState) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        NBTTagList nbtTagList = getTagList(blockState);
        itemStack.getTagCompound().setTag("HDB|blockstate", nbtTagList);
    }
    
    public static NBTTagList getTagList(IBlockState blockState) {
        NBTTagList nbtTagList = new NBTTagList();
        for (IProperty property : blockState.getProperties().keySet()) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("propertyName", property.getName());
            nbt.setString("propertyValue", property.getName(blockState.getValue(property)));
            nbtTagList.appendTag(nbt);
        }
        return nbtTagList;
    }

    public static IBlockState setBlockStateFromNBT(ItemStack itemStack, IBlockState defaultState) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("HDB|blockstate")) {
            return setProperty(defaultState, itemStack.getTagCompound().getTagList("HDB|blockstate", Constants.NBT.TAG_COMPOUND));
        }
        return defaultState;
    }
    
    public static IBlockState setProperty(IBlockState defaultState, NBTTagList nbtTagList) {
        String propertyName, propertyValue;
        for (int i = 0; i < nbtTagList.tagCount(); i++) {
            propertyName = nbtTagList.getCompoundTagAt(i).getString("propertyName");
            propertyValue = nbtTagList.getCompoundTagAt(i).getString("propertyValue");
            defaultState = setPropertyWithString(defaultState, propertyName, propertyValue);
        }
        return defaultState;
    }

    public static IBlockState setPropertyWithString(IBlockState blockState, String propertyName, String propertyValue) {
        IProperty property = getProperty(blockState, propertyName);
        if (property != null) {
            Comparable value = getPropertyValue(property, propertyValue, blockState.getValue(property));
            return blockState.withProperty(property, value);
        }
        return blockState;
    }

    @SuppressWarnings("unchecked")
    public static Comparable getPropertyValue(IProperty property, String valueName, Comparable defaultValue) {
        Class valueClass = property.getValueClass();
        for (Comparable comparable : (Set<Comparable>)property.getAllowedValues()) {
            if (comparable.getClass() == valueClass && valueName.equals(property.getName(comparable))) {
                return comparable;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static IProperty getProperty(IBlockState blockState, String propertyName) {
        for (IProperty property : blockState.getProperties().keySet()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        HyperDimensionalBag.LOGGER.warning("[HyperDimensional Bag] Illegal Property Name !");
        return null;
    }
}
