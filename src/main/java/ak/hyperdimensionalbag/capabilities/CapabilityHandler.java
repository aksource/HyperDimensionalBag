package ak.hyperdimensionalbag.capabilities;

import ak.hyperdimensionalbag.HyperDimensionalBag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

/**
 * Created by A.K. on 2021/08/25.
 */
public class CapabilityHandler {
  private final static ResourceLocation BAG_DATA = new ResourceLocation(HyperDimensionalBag.MOD_ID, "bag_data");
  public static void register() {
    CapabilityManager.INSTANCE.register(IBagData.class, new Capability.IStorage<IBagData>() {
      @Nullable
      @Override
      public INBT writeNBT(Capability<IBagData> capability, IBagData instance, Direction side) {
        return BagData.writeNBT(instance);
      }

      @Override
      public void readNBT(Capability<IBagData> capability, IBagData instance, Direction side, INBT nbt) {
        BagData.readNBT(instance, nbt);
      }
    }, BagData::new);
  }
  @SubscribeEvent
  @SuppressWarnings("unused")
  public void onAttachingEntity(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof PlayerEntity) {
      event.addCapability(BAG_DATA, new BagData());
    }
  }

  @SubscribeEvent
  public void onDeathPlayer(PlayerEvent.Clone event) {
    if (event.isWasDeath()) {
      //noinspection ConstantConditions
      assert BagData.CAPABILITY != null;
      LazyOptional<IBagData> oldCapability = event.getOriginal().getCapability(BagData.CAPABILITY, null);
      LazyOptional<IBagData> newCapability = event.getPlayer().getCapability(BagData.CAPABILITY, null);
      INBT nbt = BagData.CAPABILITY.writeNBT(oldCapability.orElse(new BagData()), null);
      BagData.CAPABILITY.readNBT(newCapability.orElse(new BagData()), null, nbt);
    }
  }
}
