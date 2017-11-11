package ak.HyperDimensionalBag.item;

import ak.HyperDimensionalBag.HyperDimensionalBag;
import ak.HyperDimensionalBag.inventory.BagData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemHDBag extends Item {
    private static final String NBT_KEY_ITEM_BAG = "Bag";

    public ItemHDBag() {
        super();
        this.setHasSubtypes(true);
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
    public String getUnlocalizedName(ItemStack item) {
        int meta = MathHelper.clamp(item.getItemDamage(), 0, 15);
        return "item.HDBag." + String.valueOf(meta);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.TOOLS) {
            for (int i = 0; i < 16; i++)
                items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand enumHand) {
        player.openGui(HyperDimensionalBag.instance, HyperDimensionalBag.guiID, world, player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(enumHand));
    }

    public void onUpdate(ItemStack itemStack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityPlayer && isSelected) {
            EntityPlayer player = (EntityPlayer) entityIn;
            if (!worldIn.isRemote) {
                BagData data;
                data = getData(itemStack, worldIn);
                data.onUpdate(worldIn, player);
                data.markDirty();
            }
        }
    }

    public BagData getData(ItemStack itemStack, World world) {
        int itemDamage = MathHelper.clamp(itemStack.getItemDamage(), 0, 15);
        String worldSavedDataKey = String.format("%s_%s", NBT_KEY_ITEM_BAG, itemDamage);
        BagData bagData = (BagData) world.loadData(BagData.class, worldSavedDataKey);

        if (bagData == null) {
            bagData = new BagData(worldSavedDataKey);
            bagData.markDirty();
            world.setData(worldSavedDataKey, bagData);
        }

        return bagData;
    }
}