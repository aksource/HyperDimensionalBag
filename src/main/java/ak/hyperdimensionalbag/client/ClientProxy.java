package ak.hyperdimensionalbag.client;

import ak.hyperdimensionalbag.CommonProxy;
import ak.hyperdimensionalbag.item.HDBagItem;
import ak.hyperdimensionalbag.util.RegistrationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import static ak.hyperdimensionalbag.inventory.BagContainer.BAG_CONTAINER_TYPE;

public class ClientProxy extends CommonProxy {

  public static final KeyBinding CTRL_KEY = new KeyBinding("key.hyperdimensionalbag.left_control",
          GLFW.GLFW_KEY_LEFT_CONTROL,
          "HyperDimensionalBag");

//  public static Screen openGui(OpenContainer openContainer) {
//    ResourceLocation guiId = openContainer.getId();
//    if (MOD_ID.equals(guiId.getNamespace())) {
//      PlayerEntity player = Minecraft.getInstance().player;
//      ItemStack itemStack = player.getHeldItemMainhand();
//      if (itemStack.getItem() instanceof ItemHDBag) {
//        return openBagGui(player, itemStack);
//      }
//    }
//    return null;
//  }

//  public static Screen openBagGui(PlayerEntity player, ItemStack itemStack) {
//    ItemHDBag bag = (ItemHDBag) itemStack.getItem();
//    DyeColor type = bag.getDyeColor();
//    BagInventory inventorybag = new BagInventory(itemStack, player);
//    return new BagScreen(player, inventorybag, type.getId());
//  }

  @Override
  public void registerClientInfo(final FMLClientSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(new RenderBlockSelectionBox());
    ClientRegistry.registerKeyBinding(CTRL_KEY);
    ScreenManager.register(BAG_CONTAINER_TYPE, BagScreen::new);
    Minecraft mc = event.getMinecraftSupplier().get();
    mc.getItemColors().register((itemStack, tintIndex) ->
            ((HDBagItem) itemStack.getItem()).getDyeColor().getMaterialColor().col,
            RegistrationHandler.ITEM_HD_BAG_LIST.toArray(new Item[]{}));
  }
//
//  @SubscribeEvent
//  @SuppressWarnings("unused")
//  public void onColorHandler(final ColorHandlerEvent.Item event) {
//    event.getItemColors().register(new ItemHDBagColorManager(),
//            RegistrationHandler.ITEM_HD_BAG_LIST.toArray(new Item[]{}));
//  }
//
//  public static class ItemHDBagColorManager implements IItemColor {
//
//    @Override
//    public int getColor(ItemStack itemStack, int tintIndex) {
//      Item item = itemStack.getItem();
//      if (item instanceof HDBagItem) {
//        return ((HDBagItem) item).getDyeColor().getMapColor().colorValue;
//      }
//      return 0;
//    }
//  }
}