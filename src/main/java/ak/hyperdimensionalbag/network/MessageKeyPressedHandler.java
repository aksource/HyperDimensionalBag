package ak.hyperdimensionalbag.network;

import ak.hyperdimensionalbag.item.ItemBlockExchanger;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressedHandler implements BiConsumer<MessageKeyPressed, Supplier<Context>> {

  @Override
  public void accept(MessageKeyPressed message, Supplier<Context> contextSupplier) {
    EntityPlayer player = contextSupplier.get().getSender();
    if (Objects.nonNull(player)) {
      ItemStack itemStack = player.getHeldItemMainhand();
      if (itemStack != ItemStack.EMPTY && itemStack.getItem() instanceof ItemBlockExchanger) {
        ItemBlockExchanger.onRightClickAction(itemStack, player, message.keyCtrlDown);
      }
    }
  }
}
