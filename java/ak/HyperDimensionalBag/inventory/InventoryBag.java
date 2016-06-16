package ak.HyperDimensionalBag.inventory;

import ak.HyperDimensionalBag.item.ItemHDBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class InventoryBag implements IInventory{
    private BagData data;
    public InventoryBag(ItemStack hdbag, World world)
    {
        data = ItemHDBag.getBagData(hdbag, world);
    }
    @Override
    public int getSizeInventory()
    {
        return data.items.length;
    }

    @Override
    public ItemStack getStackInSlot(int var1)
    {
        return data.items[var1];
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2)
    {
        if(data.items[var1] != null)
        {
            ItemStack var3;
            if(data.items[var1].stackSize <= var2)
            {
                var3 = data.items[var1];
                data.items[var1] = null;
                this.markDirty();
                return var3;
            }
            else
            {
                var3 = data.items[var1].splitStack(var2);

                if (data.items[var1].stackSize == 0)
                {
                    data.items[var1] = null;
                }

                this.markDirty();
                return var3;
            }
        }
        else
            return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2) {
        data.items[var1] = var2;
    }

    @Override
    public String getCommandSenderName() {
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
    public boolean isUseableByPlayer(EntityPlayer var1) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {
        this.markDirty();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
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
    public IChatComponent getDisplayName() {
        return null;
    }
}
