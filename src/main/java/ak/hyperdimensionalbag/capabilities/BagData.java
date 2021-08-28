package ak.hyperdimensionalbag.capabilities;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by A.K. on 2021/08/25.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BagData implements IBagData,
        ICapabilitySerializable<CompoundNBT> {

  @CapabilityInject(IBagData.class)
  public static final Capability<IBagData> CAPABILITY = null;
  private static final int META_SIZE = 16;
  private static final int INVENTORY_SIZE = 9 * 6;
  /**
   * NBTキー
   */
  private static final String NBT_KEY_ITEMS = "Items";
  private final List<NonNullList<ItemStack>> itemsList = IntStream.range(0, META_SIZE)
          .mapToObj(i -> NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY)).collect(Collectors.toList());

  public static INBT writeNBT(IBagData instance) {
    CompoundNBT nbt = new CompoundNBT();
    ListNBT nbtList = new ListNBT();
    instance.getAllItemsList().forEach(items -> {
      CompoundNBT nbt1 = new CompoundNBT();
      ItemStackHelper.saveAllItems(nbt1, items);
      nbtList.add(nbt1);
    });
    nbt.put(NBT_KEY_ITEMS, nbtList);
    return nbt;
  }

  public static void readNBT(IBagData instance, INBT inbt) {
    if (inbt instanceof CompoundNBT) {
      CompoundNBT nbt = (CompoundNBT) inbt;
      ListNBT nbtList = nbt.getList(NBT_KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
      for (int i = 0; i < nbtList.size(); i++) {
        CompoundNBT nbt1 = nbtList.getCompound(i);
        ItemStackHelper.loadAllItems(nbt1, instance.getAllItemsList().get(i));
      }
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
    for (int i = 0; i < INVENTORY_SIZE; i++) {
      this.itemsList.get(meta % META_SIZE).set(i, items.get(i));
    }
  }

  @Override
  public boolean hasItem(int meta) {
    return itemsList.get(meta % META_SIZE).stream().anyMatch(itemStack -> !itemStack.isEmpty());
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    //noinspection ConditionalExpression
    return Objects.nonNull(CAPABILITY) ? CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this)) : LazyOptional.empty();
  }

  @Override
  public CompoundNBT serializeNBT() {
    return (CompoundNBT) BagData.writeNBT(this);
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    BagData.readNBT(this, nbt);
  }
}
