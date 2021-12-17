package ak.hyperdimensionalbag.inventory;

import ak.hyperdimensionalbag.item.HDBagItem;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

//@ChestContainer
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BagContainer extends Container {

  private static final int ROW_SIZE = 6;
  public static final ContainerType<BagContainer> BAG_CONTAINER_TYPE = new ContainerType<>(BagContainer::create);
  private final IInventory bagInventory;
  private final int metaDmg;

  public static BagContainer create(int id, PlayerInventory playerInventory) {
    return new BagContainer(id, playerInventory, new Inventory(9 * 6), 0);
  }

  public BagContainer(int id, PlayerInventory playerInventory, IInventory inv, int meta) {
    super(BAG_CONTAINER_TYPE, id);
    bagInventory = inv;
    metaDmg = meta;
    for (int i = 0; i < ROW_SIZE; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlot(new BagSlot(inv, j + i * 9, 8 + j * 18, 18 + i * 18));
      }
    }
    bindPlayerInventory(playerInventory);
  }

  private void bindPlayerInventory(PlayerInventory playerInventory) {
    int offset = (ROW_SIZE - 4) * 18;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        addSlot(new Slot(playerInventory, j + i * 9 + 9,
            8 + j * 18, 103 + i * 18 + offset));
      }
    }

    for (int i = 0; i < 9; i++) {
      addSlot(new Slot(playerInventory, i, 8 + i * 18, 161 + offset));
    }
  }

  @Override
  public boolean stillValid(PlayerEntity playerIn) {
    ItemStack item = playerIn.getMainHandItem();
    return !item.isEmpty() && item.getItem() instanceof HDBagItem
        && ((HDBagItem) item.getItem()).getDyeColor().getId() == metaDmg;
  }

  @Override
  public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot != null && slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();

      if (index < this.bagInventory.getContainerSize()) {
        if (!this
            .moveItemStackTo(itemstack1, this.bagInventory.getContainerSize(), this.slots.size(),
                true)) {
          return ItemStack.EMPTY;
        }
      } else if (itemstack1.getItem() instanceof HDBagItem) {
        return ItemStack.EMPTY;
      } else if (!this.moveItemStackTo(itemstack1, 0, this.bagInventory.getContainerSize(), false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.getCount() == 0) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return itemstack;
  }
}