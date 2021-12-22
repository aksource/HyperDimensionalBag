package ak.hyperdimensionalbag.inventory;

import ak.hyperdimensionalbag.capabilities.BagData;
import ak.hyperdimensionalbag.capabilities.IBagData;
import ak.hyperdimensionalbag.item.HDBagItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BagInventory implements Container {

  private final IBagData data;
  private final int meta;

  public BagInventory(ItemStack itemStack, Player playerEntity) {
    this.data = playerEntity.getCapability(BagData.CAPABILITY, null).orElse(new BagData());
    this.meta = ((HDBagItem)itemStack.getItem()).getDyeColor().getId();
  }

  @Override
  public int getContainerSize() {
    return data.getItems(meta).size();
  }

  @Override
  public boolean isEmpty() {
    return data.hasItem(meta);
  }

  @Override
  public ItemStack getItem(int index) {
    return Optional.of(data.getItems(meta).get(index % data.getItems(meta).size())).orElse(ItemStack.EMPTY);
  }

  @Override
  public ItemStack removeItem(int index, int count) {
    var items = data.getItems(meta);
    if (!items.get(index).isEmpty()) {
      ItemStack itemStack;
      if (items.get(index).getCount() <= count) {
        itemStack = items.get(index);
        items.set(index, ItemStack.EMPTY);
      } else {
        itemStack = items.get(index).split(count);

        if (items.get(index).getCount() == 0) {
          items.set(index, ItemStack.EMPTY);
        }

      }
      this.setChanged();
      return itemStack;
    } else {
      return ItemStack.EMPTY;
    }
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    var itemStack = data.getItems(meta).get(index);
    data.getItems(meta).set(index, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public void setItem(int index, ItemStack itemStack) {
    data.getItems(meta).set(index, itemStack);
  }

  @Override
  public void setChanged() {

  }

  @Override
  public boolean stillValid(Player entityPlayer) {
    return true;
  }

  @Override
  public boolean canPlaceItem(int i, ItemStack itemstack) {
    return !(itemstack.getItem() instanceof HDBagItem);
  }

  @Override
  public void clearContent() {

  }
}
