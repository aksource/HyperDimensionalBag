package ak.hyperdimensionalbag.capabilities;

import ak.hyperdimensionalbag.HyperDimensionalBag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by A.K. on 2021/08/25.
 */
public class CapabilityHandler {
  private final static ResourceLocation BAG_DATA = new ResourceLocation(HyperDimensionalBag.MOD_ID, "bag_data");

  @SubscribeEvent
  public static void registerCapability(RegisterCapabilitiesEvent event) {
    event.register(IBagData.class);
  }

  @SubscribeEvent
  public static void onAttachingEntity(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(BAG_DATA, new BagDataCapabilityProvider());
    }
  }

  @SubscribeEvent
  public static void onDeathPlayer(PlayerEvent.Clone event) {
    if (event.isWasDeath()) {
      var oldCapability = event.getOriginal().getCapability(BagData.CAPABILITY, null);
      var newCapability = event.getPlayer().getCapability(BagData.CAPABILITY, null);
      oldCapability.ifPresent(oldData -> {
        var nbt = oldData.serializeNBT();
        newCapability.ifPresent(newData -> newData.deserializeNBT(nbt));
      });
    }
  }
}
