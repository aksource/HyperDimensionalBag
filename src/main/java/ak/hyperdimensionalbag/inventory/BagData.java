package ak.hyperdimensionalbag.inventory;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class BagData extends WorldSavedData {

  /**
   * NBTキー
   */
  private static final String NBT_KEY_ITEMS = "Items";
  /**
   * NBTキー
   */
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
    if (world.getGameTime() % 80 == 0) {
      this.upDate = true;
    }
    if (this.upDate) {
      this.markDirty();
      this.upDate = false;
    }
  }

  @Override
  public void read(@Nonnull NBTTagCompound nbtTagCompound) {
    NBTTagList itemTagList = nbtTagCompound.getList(NBT_KEY_ITEMS, Constants.NBT.TAG_COMPOUND);

    for (int tagIndex = 0; tagIndex < itemTagList.size(); ++tagIndex) {
      NBTTagCompound itemNBT = itemTagList.getCompound(tagIndex);
      int i = itemNBT.getByte(NBT_KEY_SLOT) & 255;

      if (i < this.items.size()) {
        this.items.set(i, ItemStack.read(itemNBT));
      }
    }
  }

  @Override
  @Nonnull
  public NBTTagCompound write(@Nonnull NBTTagCompound nbtTagCompound) {
    NBTTagList nbtTagList = new NBTTagList();

    for (int i = 0; i < this.items.size(); ++i) {
      if (this.items.get(i) != ItemStack.EMPTY) {
        NBTTagCompound item = new NBTTagCompound();
        item.setByte(NBT_KEY_SLOT, (byte) i);
        this.items.get(i).write(item);
        nbtTagList.add(item);
      }
    }
    nbtTagCompound.setTag(NBT_KEY_ITEMS, nbtTagList);
    return nbtTagCompound;
  }

  public boolean hasItem() {
    return this.items.stream().anyMatch(itemStack -> !itemStack.isEmpty());
  }
}