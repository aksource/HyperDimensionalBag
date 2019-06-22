package ak.hyperdimensionalbag.item;

import static ak.hyperdimensionalbag.HyperDimensionalBag.MOD_ID;

import ak.hyperdimensionalbag.inventory.BagData;
import ak.hyperdimensionalbag.inventory.ContainerBag;
import ak.hyperdimensionalbag.inventory.InventoryBag;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedDataStorage;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemHDBag extends Item implements IItemColor {

  private static final String NBT_KEY_ITEM_BAG = "Bag";
  private final EnumDyeColor dyeColor;

  public ItemHDBag(EnumDyeColor dyeColor) {
    super(new Item.Properties().group(ItemGroup.TOOLS));
    this.dyeColor = dyeColor;
  }

  public static BagData getBagData(ItemStack item, World world) {
    BagData data = null;
    if (!item.isEmpty() && item.getItem() instanceof ItemHDBag) {
      data = ((ItemHDBag) item.getItem()).getData(item, world);
    }
    return data;
  }

  @Override
  @Nonnull
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
      @Nonnull EnumHand enumHand) {
    if (!world.isRemote) {
      NetworkHooks.openGui((EntityPlayerMP) player, new InterfaceBag(dyeColor));
    }
    return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(enumHand));
  }

  @Override
  public void inventoryTick(ItemStack itemStack, World worldIn, Entity entityIn, int itemSlot,
      boolean isSelected) {
    if (entityIn instanceof EntityPlayer && isSelected) {
      EntityPlayer player = (EntityPlayer) entityIn;
      if (!worldIn.isRemote) {
        BagData data;
        data = getData(itemStack, worldIn);
        if (Objects.nonNull(data)) {
          data.onUpdate(worldIn, player);
          data.markDirty();
        }
      }
    }
  }

  public BagData getData(ItemStack itemStack, World world) {
    int itemDamage = this.dyeColor.getId();
    String worldSavedDataKey = String.format("%s_%s", NBT_KEY_ITEM_BAG, itemDamage);
    WorldSavedDataStorage storage = world.getMapStorage();
    if (Objects.nonNull(storage)) {
      BagData data = storage.func_212426_a(DimensionType.OVERWORLD, BagData::new, worldSavedDataKey);
      if (Objects.isNull(data)) {
        storage.func_212424_a(DimensionType.OVERWORLD, worldSavedDataKey, new BagData(worldSavedDataKey));
        return storage.func_212426_a(DimensionType.OVERWORLD, BagData::new, worldSavedDataKey);
      }
      return data;
    }

    return null;
  }

  public EnumDyeColor getDyeColor() {
    return dyeColor;
  }

  @Override
  public int getColor(@Nonnull ItemStack itemStack, int i) {
    return dyeColor.getMapColor().colorValue;
  }

  public static class InterfaceBag implements IInteractionObject {

    private final EnumDyeColor color;

    public InterfaceBag(EnumDyeColor color) {
      this.color = color;
    }

    @Override
    @Nonnull
    public Container createContainer(@Nonnull InventoryPlayer inventoryPlayer,
        EntityPlayer entityPlayer) {
      ItemStack heldItem = entityPlayer.getHeldItemMainhand();
      InventoryBag inventorybag = new InventoryBag(heldItem, entityPlayer.world);
      return new ContainerBag(entityPlayer, inventorybag, color.getId());
    }

    @Override
    @Nonnull
    public String getGuiID() {
      return MOD_ID + ":bag" + color.getName();
    }

    @Override
    @Nonnull
    public ITextComponent getName() {
      return new TextComponentTranslation("");
    }

    @Override
    public boolean hasCustomName() {
      return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
      return null;
    }
  }
}