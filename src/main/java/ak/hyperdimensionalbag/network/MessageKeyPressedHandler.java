package ak.hyperdimensionalbag.network;

import ak.hyperdimensionalbag.item.BlockExchangerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

  @Override
  public void accept(MessageKeyPressed message, Supplier<Context> contextSupplier) {
    PlayerEntity player = contextSupplier.get().getSender();
    if (Objects.nonNull(player)) {
      ItemStack itemStack = player.getHeldItemMainhand();
      if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockExchangerItem) {
        BlockExchangerItem.onRightClickAction(itemStack, player, message.keyCtrlDown);
      }
    }
  }
}
