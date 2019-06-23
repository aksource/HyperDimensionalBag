package ak.hyperdimensionalbag;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

import ak.hyperdimensionalbag.client.ClientProxy;
import ak.hyperdimensionalbag.network.PacketHandler;
import ak.hyperdimensionalbag.util.RegistrationUtils;
import java.util.logging.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MOD_ID)
public class HyperDimensionalBag {

  public static final String MOD_ID = "hyperdimensionalbag";
  public static final Logger LOGGER = Logger.getLogger(MOD_ID);
  public static String GuiBagTex = "textures/gui/bag-gui.png";
  public static String TextureDomain = "hyperdimensionalbag:";
  public static String Assets = "hyperdimensionalbag";
  public static boolean loadSB = false;
  private ClientProxy proxy = new ClientProxy();

  public HyperDimensionalBag() {
    final IEventBus modEventBus =
        FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::preInit);
    modEventBus.addListener(this::clientInit);
    modEventBus.addListener(this::postInit);

    MinecraftForge.EVENT_BUS.register(new RegistrationUtils());
    MinecraftForge.EVENT_BUS.register(new PlayerPickHook());
    MinecraftForge.EVENT_BUS.register(proxy);
    ModLoadingContext.get()
        .registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> ClientProxy::openGui);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.configSpec);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

  private void clientInit(final FMLClientSetupEvent event) {
    proxy.registerClientInfo(event);
  }

  private void postInit(InterModEnqueueEvent event) {
    loadSB = ModList.get().isLoaded("storagebox");
  }
}