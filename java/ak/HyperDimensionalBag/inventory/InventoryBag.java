package ak.HyperDimensionalBag.inventory;

import ak.HyperDimensionalBag.item.ItemHDBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public class InventoryBag implements IInventory {
    private final BagData data;

    public InventoryBag(ItemStack hdbag, World world) {
        data = ItemHDBag.getBagData(hdbag, world);
    }

    @Override
    public int getSizeInventory() {
        return data.items.size();
    }

    @Override
    public boolean isEmpty() {
        return data.hasItem();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return Optional.of(data.items.get(index % data.items.size())).orElse(ItemStack.EMPTY);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        if (data.items.get(index) != ItemStack.EMPTY) {
            ItemStack itemStack;
            if (data.items.get(index).getCount() <= count) {
                itemStack = data.items.get(index);
                data.items.set(index, ItemStack.EMPTY);
                this.markDirty();
                return itemStack;
            } else {
                itemStack = data.items.get(index).splitStack(count);

                if (data.items.get(index).getCount() == 0) {
                    data.items.set(index, ItemStack.EMPTY);
                }

                this.markDirty();
                return itemStack;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack itemStack) {
        data.items.set(index, itemStack);
    }

    @Override
    @Nonnull
    public String getName() {
        return "HDBag";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        data.upDate = true;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {}

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
        this.markDirty();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
        return !(itemstack.getItem() instanceof ItemHDBag);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName() {
        return new TextComponentString("");
    }
}
