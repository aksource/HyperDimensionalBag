package ak.HyperDimensionalBag.inventory;

import ak.HyperDimensionalBag.item.ItemHDBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

//@ChestContainer
public class ContainerBag extends Container {
    private final IInventory BagInv;
    private final int metaDmg;

    public ContainerBag(EntityPlayer player, IInventory inv, int meta) {
        BagInv = inv;
        metaDmg = meta;
        inv.openInventory(player);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new SlotBag(inv, j + i * 9, 8 + j * 18, 14 + i * 18));
            }
        }
        bindPlayerInventory(player.inventory);
    }

    private void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, 126 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 184));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
        ItemStack item = entityplayer.getHeldItemMainhand();
        return !item.isEmpty() && item.getItem() instanceof ItemHDBag && item.getItemDamage() == metaDmg;
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(par2);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (par2 < this.BagInv.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, this.BagInv.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.getItem() instanceof ItemHDBag)
                return ItemStack.EMPTY;
            else if (!this.mergeItemStack(itemstack1, 0, this.BagInv.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
}