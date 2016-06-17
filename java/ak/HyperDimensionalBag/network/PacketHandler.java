package ak.HyperDimensionalBag.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by A.K. on 14/06/02.
 */
public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("hyperdimensionalbag");

    public static void init() {
        INSTANCE.registerMessage(MessageKeyPressedHandler.class, MessageKeyPressed.class, 0, Side.SERVER);
    }
}
