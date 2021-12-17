package ak.hyperdimensionalbag.inventory;

import ak.hyperdimensionalbag.item.HDBagItem;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BagSlot extends Slot {

  public BagSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  public boolean mayPlace(ItemStack itemstack) {
    return !(itemstack.getItem() instanceof HDBagItem);
  }
}
