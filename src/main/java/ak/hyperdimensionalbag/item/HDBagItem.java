package ak.hyperdimensionalbag.item;

import ak.hyperdimensionalbag.HyperDimensionalBag;
import ak.hyperdimensionalbag.capabilities.BagData;
import ak.hyperdimensionalbag.capabilities.IBagData;
import ak.hyperdimensionalbag.inventory.BagContainer;
import ak.hyperdimensionalbag.inventory.BagInventory;
import ak.hyperdimensionalbag.util.RegistrationHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HDBagItem extends Item {

  private final DyeColor dyeColor;

  public HDBagItem(DyeColor dyeColor) {
    super(new Item.Properties().tab(ItemGroup.TAB_TOOLS));
    this.dyeColor = dyeColor;
  }

  @Override
  public ActionResult<ItemStack> use(World world, PlayerEntity player,
                                                  Hand enumHand) {
    if (!world.isClientSide) {
//      NetworkHooks.openGui((ServerPlayerEntity) player, new InterfaceBag(dyeColor));
      player.openMenu(new InterfaceBag(dyeColor));
    }
    return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(enumHand));
  }

//  @Override
//  public void inventoryTick(ItemStack itemStack, World worldIn, Entity entityIn, int itemSlot,
//      boolean isSelected) {
//    if (entityIn instanceof PlayerEntity && isSelected) {
//      PlayerEntity player = (PlayerEntity) entityIn;
//      if (!worldIn.isRemote) {
//        IBagData data = getData(player);
//        if (Objects.nonNull(data)) {
//          data.onUpdate(worldIn, player);
//          data.markDirty();
//        }
//      }
//    }
//  }

  public IBagData getData(PlayerEntity player) {
    LazyOptional<IBagData> lazyOptional = player.getCapability(BagData.CAPABILITY, null);
    return lazyOptional.orElse(new ak.hyperdimensionalbag.capabilities.BagData());
  }

  public DyeColor getDyeColor() {
    return dyeColor;
  }

  @ParametersAreNonnullByDefault
  @MethodsReturnNonnullByDefault
  public static class InterfaceBag implements INamedContainerProvider {

    private final DyeColor color;

    public InterfaceBag(DyeColor color) {
      this.color = color;
    }

    @Override
    public Container createMenu(int guiId, PlayerInventory playerInventory,
                                PlayerEntity playerEntity) {
      ItemStack heldItem = playerEntity.getMainHandItem();
      BagInventory bagInventory = new BagInventory(heldItem, playerEntity);
      return new BagContainer(guiId, playerInventory, bagInventory, color.getId());
    }

    @Override
    public ITextComponent getDisplayName() {
      return new TranslationTextComponent("item."
              + HyperDimensionalBag.MOD_ID + "."
              + RegistrationHandler.H_D_BAG_REGISTER_PREFIX + "_" + color.getName());
    }
  }
}