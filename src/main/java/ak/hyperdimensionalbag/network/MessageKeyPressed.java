package ak.hyperdimensionalbag.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.PacketBuffer;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressed {

  public static BiConsumer<MessageKeyPressed, PacketBuffer> encoder = ((messageKeyPressed, packetBuffer) -> packetBuffer
      .writeBoolean(messageKeyPressed.isKeyCtrlDown()));
  public static Function<PacketBuffer, MessageKeyPressed> decoder = packetBuffer -> new MessageKeyPressed(
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
