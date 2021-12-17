package ak.hyperdimensionalbag.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

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

  public void onUpdate(World world, PlayerEntity player) {
    if (!this.init) {
      this.init = true;
    }
    if (world.getGameTime() % 80 == 0) {
      this.upDate = true;
    }
    if (this.upDate) {
      this.setDirty();
      this.upDate = false;
    }
  }

  @Override
  public void load(@Nonnull CompoundNBT nbtTagCompound) {
    ListNBT itemTagList = nbtTagCompound.getList(NBT_KEY_ITEMS, Constants.NBT.TAG_COMPOUND);

    for (int tagIndex = 0; tagIndex < itemTagList.size(); ++tagIndex) {
      CompoundNBT itemNBT = itemTagList.getCompound(tagIndex);
      int i = itemNBT.getByte(NBT_KEY_SLOT) & 255;

      if (i < this.items.size()) {
        this.items.set(i, ItemStack.of(itemNBT));
      }
    }
  }

  @Override
  @Nonnull
  public CompoundNBT save(@Nonnull CompoundNBT nbtTagCompound) {
    ListNBT nbtTagList = new ListNBT();

    for (int i = 0; i < this.items.size(); ++i) {
      if (this.items.get(i) != ItemStack.EMPTY) {
        CompoundNBT item = new CompoundNBT();
        item.putByte(NBT_KEY_SLOT, (byte) i);
        this.items.get(i).save(item);
        nbtTagList.add(item);
      }
    }
    nbtTagCompound.put(NBT_KEY_ITEMS, nbtTagList);
    return nbtTagCompound;
  }

  public boolean hasItem() {
    return this.items.stream().anyMatch(itemStack -> !itemStack.isEmpty());
  }
}