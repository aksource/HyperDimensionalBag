package ak.HyperDimensionalBag.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class BagData extends WorldSavedData {
    /** NBTキー */
    private static final String NBT_KEY_ITEMS = "Items";
    /** NBTキー */
    private static final String NBT_KEY_SLOT = "Slot";
    private static final int INVENTORY_SIZE = 54;
    public NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    public boolean upDate;
    private boolean init = false;

    public BagData(String strWorldSavedData) {
        super(strWorldSavedData);
    }

    public void onUpdate(World world, EntityPlayer player) {
        if (!this.init) {
            this.init = true;
        }
        if (world.getWorldTime() % 80l == 0l)
            this.upDate = true;
        if (this.upDate) {
            this.markDirty();
            this.upDate = false;
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbtTagCompound) {
        NBTTagList var2 = nbtTagCompound.getTagList(NBT_KEY_ITEMS, Constants.NBT.TAG_COMPOUND);

        for (int tagIndex = 0; tagIndex < var2.tagCount(); ++tagIndex) {
            NBTTagCompound var4 = var2.getCompoundTagAt(tagIndex);
            int i = var4.getByte(NBT_KEY_SLOT) & 255;

            if (i >= 0 && i < this.items.size()) {
                this.items.set(i, new ItemStack(var4));
            }
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbtTagCompound) {
        NBTTagList nbtTagList = new NBTTagList();

        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i) != ItemStack.EMPTY) {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte(NBT_KEY_SLOT, (byte) i);
                this.items.get(i).writeToNBT(var4);
                nbtTagList.appendTag(var4);
            }
        }
        nbtTagCompound.setTag(NBT_KEY_ITEMS, nbtTagList);
        return nbtTagCompound;
    }

    public boolean hasItem() {
        return this.items.stream().filter((itemStack -> itemStack != ItemStack.EMPTY)).findAny().isPresent();
    }
}