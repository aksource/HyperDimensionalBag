package ak.hyperdimensionalbag.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;


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
