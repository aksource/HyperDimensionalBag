package ak.hyperdimensionalbag.capabilities;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Created by A.K. on 2021/08/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IBagData {

  NonNullList<ItemStack> getItems(int meta);
  List<NonNullList<ItemStack>> getAllItemsList();
  void setItems(int meta, NonNullList<ItemStack> items);
  boolean hasItem(int meta);
}
