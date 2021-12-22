package ak.hyperdimensionalbag;

import ak.hyperdimensionalbag.capabilities.CapabilityHandler;
import ak.hyperdimensionalbag.client.ClientUtils;
import ak.hyperdimensionalbag.network.PacketHandler;
import ak.hyperdimensionalbag.util.RegistrationHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

@Mod(MOD_ID)
public class HyperDimensionalBag {

  public static final String MOD_ID = "hyperdimensionalbag";
  public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
  public static boolean loadSB = false;

  public HyperDimensionalBag() {
    final var modEventBus =
        FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::preInit);
    modEventBus.addListener(this::clientInit);
    modEventBus.addListener(this::postInit);
    RegistrationHandler.register(modEventBus);

    MinecraftForge.EVENT_BUS.register(PlayerPickHook.class);
    MinecraftForge.EVENT_BUS.register(CapabilityHandler.class);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.configSpec);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

  private void clientInit(final FMLClientSetupEvent event) {
    ClientUtils.registerClientInfo(event);
  }

  private void postInit(InterModEnqueueEvent event) {
    loadSB = ModList.get().isLoaded("storagebox");
  }
}