package ak.hyperdimensionalbag.network;

import ak.hyperdimensionalbag.item.BlockExchangerItem;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<NetworkEvent.Context>> {

  @Override
  public void accept(MessageKeyPressed message, Supplier<NetworkEvent.Context> contextSupplier) {
    var player = contextSupplier.get().getSender();
    if (Objects.nonNull(player)) {
      var itemStack = player.getMainHandItem();
      if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockExchangerItem) {
        BlockExchangerItem.onRightClickAction(itemStack, player, message.keyCtrlDown);
      }
    }
  }
}
