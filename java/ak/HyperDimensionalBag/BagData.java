package ak.HyperDimensionalBag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class BagData extends WorldSavedData
{
	public ItemStack[] items = new ItemStack[54];
	private boolean init = false;
	public boolean upDate;

	public BagData(String par1Str)
	{
		super(par1Str);
	}
	public void onUpdate(World var1, EntityPlayer var2)
	{
		if(!this.init)
		{
			this.init = true;
		}
		if(var1.getWorldTime() % 80l == 0l)
			this.upDate = true;
		if(this.upDate)
		{
			this.markDirty();
			this.upDate = false;
		}
	}

    @Override
    public void readFromNBT(NBTTagCompound var1) {
        NBTTagList var2 = var1.getTagList("Items", 10);
        this.items = new ItemStack[54];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;

            if (var5 >= 0 && var5 < this.items.length)
            {
                this.items[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
    }

	@Override
	public void writeToNBT(NBTTagCompound var1) {
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < this.items.length; ++var3)
		{
			if (this.items[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte)var3);
				this.items[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}
		}
		var1.setTag("Items", var2);
	}

}