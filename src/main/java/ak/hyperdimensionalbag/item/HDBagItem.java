package ak.hyperdimensionalbag.item;

import ak.hyperdimensionalbag.HyperDimensionalBag;
import ak.hyperdimensionalbag.inventory.BagContainer;
import ak.hyperdimensionalbag.inventory.BagInventory;
import ak.hyperdimensionalbag.util.RegistrationHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HDBagItem extends Item {

  private final DyeColor dyeColor;

  public HDBagItem(DyeColor dyeColor) {
    super(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS));
    this.dyeColor = dyeColor;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player,
                                                InteractionHand enumHand) {
    if (!world.isClientSide) {
      player.openMenu(new InterfaceBag(dyeColor));
    }
    return InteractionResultHolder.success(player.getItemInHand(enumHand));
  }

  public DyeColor getDyeColor() {
    return dyeColor;
  }

  @ParametersAreNonnullByDefault
  @MethodsReturnNonnullByDefault
  public record InterfaceBag(DyeColor color) implements MenuProvider {

    @Override
    public AbstractContainerMenu createMenu(int guiId, Inventory playerInventory,
                                            Player playerEntity) {
      var heldItem = playerEntity.getMainHandItem();
      var bagInventory = new BagInventory(heldItem, playerEntity);
      return new BagContainer(guiId, playerInventory, bagInventory, color.getId());
    }

    @Override
    public Component getDisplayName() {
      return Component.translatable("item."
              + HyperDimensionalBag.MOD_ID + "."
              + RegistrationHandler.H_D_BAG_REGISTER_PREFIX + "_" + color.getName());
    }
  }
}