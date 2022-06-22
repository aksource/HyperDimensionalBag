package ak.hyperdimensionalbag.client;

import ak.hyperdimensionalbag.item.HDBagItem;
import ak.hyperdimensionalbag.util.RegistrationHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;

import static ak.hyperdimensionalbag.inventory.BagContainer.BAG_CONTAINER_TYPE;

public class ClientUtils {

  public static final KeyMapping CTRL_KEY = new KeyMapping("key.hyperdimensionalbag.left_control",
          GLFW.GLFW_KEY_LEFT_CONTROL,
          "HyperDimensionalBag");

  public static void registerClientInfo(@SuppressWarnings("unused") final FMLClientSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(new RenderBlockSelectionBox());
    ClientRegistry.registerKeyBinding(CTRL_KEY);
    MenuScreens.register(BAG_CONTAINER_TYPE, BagScreen::new);
    Minecraft mc = Minecraft.getInstance();
    mc.getItemColors().register((itemStack, tintIndex) ->
            ((HDBagItem) itemStack.getItem()).getDyeColor().getMaterialColor().col,
            RegistrationHandler.ITEM_HD_BAG_LIST.stream().map(RegistryObject::get).toArray(Item[]::new));
  }
}