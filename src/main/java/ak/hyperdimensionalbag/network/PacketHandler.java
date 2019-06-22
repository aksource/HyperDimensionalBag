package ak.hyperdimensionalbag.network;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Created by A.K. on 14/06/02.
 */
public class PacketHandler {

  public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
      .named(new ResourceLocation(MOD_ID.toLowerCase(), "channel"))
      .networkProtocolVersion(() -> "1").clientAcceptedVersions(e -> true)
      .serverAcceptedVersions(e -> true).simpleChannel();

  public static void init() {
    INSTANCE
        .registerMessage(0, MessageKeyPressed.class, MessageKeyPressed.encoder,
            MessageKeyPressed.decoder, new MessageKeyPressedHandler());
  }
}
