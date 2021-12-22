package ak.hyperdimensionalbag.capabilities;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by A.K. on 2021/08/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BagData implements IBagData {
  public static final Capability<IBagData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
  private static final int META_SIZE = 16;
  private static final int INVENTORY_SIZE = 9 * 6;
  /**
   * NBTキー
   */
  private static final String NBT_KEY_ITEMS = "Items";
  private final List<NonNullList<ItemStack>> itemsList = IntStream.range(0, META_SIZE)
          .mapToObj(i -> NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY)).collect(Collectors.toList());

  public static CompoundTag writeNBT(IBagData instance) {
    var nbt = new CompoundTag();
    var nbtList = new ListTag();
    instance.getAllItemsList().forEach(items -> {
      var nbt1 = new CompoundTag();
      ContainerHelper.saveAllItems(nbt1, items);
      nbtList.add(nbt1);
    });
    nbt.put(NBT_KEY_ITEMS, nbtList);
    return nbt;
  }

  public static void readNBT(IBagData instance, CompoundTag nbt) {
    var nbtList = nbt.getList(NBT_KEY_ITEMS, Tag.TAG_COMPOUND);
    for (var i = 0; i < nbtList.size(); i++) {
      var nbt1 = nbtList.getCompound(i);
      ContainerHelper.loadAllItems(nbt1, instance.getAllItemsList().get(i));
    }
  }

  @Nonnull
  @Override
  public NonNullList<ItemStack> getItems(int meta) {
    return itemsList.get(meta % META_SIZE);
  }

  @Nonnull
  @Override
  public List<NonNullList<ItemStack>> getAllItemsList() {
    return itemsList;
  }

  @Override
  public void setItems(int meta, NonNullList<ItemStack> items) {
    for (var i = 0; i < INVENTORY_SIZE; i++) {
      this.itemsList.get(meta % META_SIZE).set(i, items.get(i));
    }
  }

  @Override
  public boolean hasItem(int meta) {
    return itemsList.get(meta % META_SIZE).stream().anyMatch(itemStack -> !itemStack.isEmpty());
  }

  @Override
  public CompoundTag serializeNBT() {
    return BagData.writeNBT(this);
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    BagData.readNBT(this, nbt);
  }
}
