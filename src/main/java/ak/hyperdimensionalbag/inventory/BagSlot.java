package ak.hyperdimensionalbag.inventory;

import ak.hyperdimensionalbag.item.HDBagItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BagSlot extends Slot {

  public BagSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean mayPlace(ItemStack itemstack) {
    return !(itemstack.getItem() instanceof HDBagItem);
  }
}
