package ak.hyperdimensionalbag.client;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

import ak.hyperdimensionalbag.CommonProxy;
import ak.hyperdimensionalbag.inventory.InventoryBag;
import ak.hyperdimensionalbag.item.ItemHDBag;
import ak.hyperdimensionalbag.util.RegistrationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.FMLPlayMessages.OpenContainer;
import org.lwjgl.glfw.GLFW;

public class ClientProxy extends CommonProxy {

  public static final KeyBinding CTRL_KEY = new KeyBinding("key.hyperdimensionalbag.left_control",
      GLFW.GLFW_KEY_LEFT_CONTROL,
      "HyperDimensionalBag");

  public static GuiScreen openGui(OpenContainer openContainer) {
    ResourceLocation guiId = openContainer.getId();
    if (MOD_ID.equals(guiId.getNamespace())) {
      EntityPlayer player = Minecraft.getInstance().player;
      ItemStack itemStack = player.getHeldItemMainhand();
      if (itemStack.getItem() instanceof ItemHDBag) {
        return openBagGui(player, itemStack);
      }
    }
    return null;
  }

  public static GuiScreen openBagGui(EntityPlayer player, ItemStack itemStack) {
    ItemHDBag bag = (ItemHDBag) itemStack.getItem();
    EnumDyeColor type = bag.getDyeColor();
    InventoryBag inventorybag = new InventoryBag(itemStack, player.world);
    return new GuiBag(player, inventorybag, type.getId());
  }

  @Override
  public void registerClientInfo(final FMLClientSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(new RenderBlockSelectionBox());
    ClientRegistry.registerKeyBinding(CTRL_KEY);
  }

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void onColorHandler(final ColorHandlerEvent.Item event) {
    event.getItemColors().register(new ItemHDBagColorManager(),
        RegistrationUtils.itemHDBagList.toArray(new Item[]{}));
  }

  public static class ItemHDBagColorManager implements IItemColor {

    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
      Item item = itemStack.getItem();
      if (item instanceof ItemHDBag) {
        return ((ItemHDBag) item).getDyeColor().getMapColor().colorValue;
      }
      return 0;
    }
  }
}