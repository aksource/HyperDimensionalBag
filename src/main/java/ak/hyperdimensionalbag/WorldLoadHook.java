package ak.hyperdimensionalbag;

import ak.hyperdimensionalbag.item.ItemHDBag;
import ak.hyperdimensionalbag.util.RegistrationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Created by A.K. on 2019/06/22.
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class WorldLoadHook {

  @SubscribeEvent
  @SuppressWarnings("unused")
  public static void loadWorld(final Load event) {
    IWorld world = event.getWorld();
    if (world.isRemote()) {
      Minecraft.getInstance().getItemColors().register(new ItemHDBagColorManager(),
          RegistrationUtils.itemHDBagList.toArray(new Item[]{}));
    }
  }

  public static class ItemHDBagColorManager implements IItemColor {

    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
      Item item = itemStack.getItem();
      if (item instanceof ItemHDBag) {
        return ((ItemHDBag) item).getDyeColor().getMapColor().colorValue;
      }
      return 0;
    }
  }
}
