package ak.hyperdimensionalbag.capabilities;


import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Created by A.K. on 2021/08/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IBagData extends INBTSerializable<CompoundTag> {

  NonNullList<ItemStack> getItems(int meta);
  List<NonNullList<ItemStack>> getAllItemsList();
  void setItems(int meta, NonNullList<ItemStack> items);
  boolean hasItem(int meta);
}
