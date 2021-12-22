package ak.hyperdimensionalbag.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by A.K. on 2021/12/18.
 */
public class BagDataCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
  private final BagData bagData = new BagData();
  private final LazyOptional<IBagData> optional = LazyOptional.of(() -> bagData);
  private final Capability<IBagData> capability = BagData.CAPABILITY;
  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == capability) return optional.cast();
    return LazyOptional.empty();
  }

  @Override
  public CompoundTag serializeNBT() {
    return bagData.serializeNBT();
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    bagData.deserializeNBT(nbt);
  }
}
