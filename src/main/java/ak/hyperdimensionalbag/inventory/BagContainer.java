package ak.hyperdimensionalbag.inventory;

import ak.hyperdimensionalbag.item.HDBagItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

//@ChestContainer
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BagContainer extends AbstractContainerMenu {

  private static final int ROW_SIZE = 6;
  public static final MenuType<BagContainer> BAG_CONTAINER_TYPE = new MenuType<>(BagContainer::create);
  private final Container bagInventory;
  private final int metaDmg;

  public static BagContainer create(int id, Inventory playerInventory) {
    return new BagContainer(id, playerInventory, new SimpleContainer(9 * 6), 0);
  }

  public BagContainer(int id, Inventory playerInventory, Container inv, int meta) {
    super(BAG_CONTAINER_TYPE, id);
    bagInventory = inv;
    metaDmg = meta;
    for (var i = 0; i < ROW_SIZE; i++) {
      for (var j = 0; j < 9; j++) {
        this.addSlot(new BagSlot(inv, j + i * 9, 8 + j * 18, 18 + i * 18));
      }
    }
    bindPlayerInventory(playerInventory);
  }

  private void bindPlayerInventory(Inventory playerInventory) {
    var offset = (ROW_SIZE - 4) * 18;
    for (var i = 0; i < 3; i++) {
      for (var j = 0; j < 9; j++) {
        addSlot(new Slot(playerInventory, j + i * 9 + 9,
            8 + j * 18, 103 + i * 18 + offset));
      }
    }

    for (var i = 0; i < 9; i++) {
      addSlot(new Slot(playerInventory, i, 8 + i * 18, 161 + offset));
    }
  }

  @Override
  public boolean stillValid(Player playerIn) {
    var item = playerIn.getMainHandItem();
    return !item.isEmpty() && item.getItem() instanceof HDBagItem
        && ((HDBagItem) item.getItem()).getDyeColor().getId() == metaDmg;
  }

  @Override
  public ItemStack quickMoveStack(Player playerIn, int index) {
    var itemstack = ItemStack.EMPTY;
    var slot = this.slots.get(index);

    if (slot.hasItem()) {
      var itemstack1 = slot.getItem();
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