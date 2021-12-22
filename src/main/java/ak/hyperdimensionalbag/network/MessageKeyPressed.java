package ak.hyperdimensionalbag.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressed {

  public static final BiConsumer<MessageKeyPressed, FriendlyByteBuf> encoder = ((messageKeyPressed, packetBuffer) -> packetBuffer
      .writeBoolean(messageKeyPressed.isKeyCtrlDown()));
  public static final Function<FriendlyByteBuf, MessageKeyPressed> decoder = packetBuffer -> new MessageKeyPressed(
      packetBuffer.readBoolean());
  public boolean keyCtrlDown;

  public MessageKeyPressed() {
  }

  public MessageKeyPressed(boolean pressed) {
    this.keyCtrlDown = pressed;
  }

  public boolean isKeyCtrlDown() {
    return keyCtrlDown;
  }
}
