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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemHDBag extends Item {
    public BagData data;

    public ItemHDBag() {
        super();
        this.setHasSubtypes(true);
    }

    public static BagData getBagData(ItemStack item, World world) {
        BagData data = null;
        if (item != null && item.getItem() instanceof ItemHDBag) {
            data = ((ItemHDBag) item.getItem()).getData(item, world);
        }
        return data;
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        int meta = MathHelper.clamp_int(item.getItemDamage(), 0, 15);
        return "item.HDBag." + String.valueOf(meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
        for (int i = 0; i < 16; i++)
            par3List.add(new ItemStack(par1, 1, i));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand enumHand) {
        player.openGui(HyperDimensionalBag.instance, HyperDimensionalBag.guiID, world, player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        return new ActionResult<>(EnumActionResult.SUCCESS, item);
    }

    public void onUpdate(ItemStack item, World world, Entity entity, int par4, boolean par5) {
        if (entity instanceof EntityPlayer && par5) {
            EntityPlayer player = (EntityPlayer) entity;
            if (!world.isRemote) {
                this.data = getData(item, world);
                this.data.onUpdate(world, player);
                this.data.markDirty();
            }
        }
    }

    public BagData getData(ItemStack var1, World var2) {
        String itemName = "Bag";
        int itemDamage = MathHelper.clamp_int(var1.getItemDamage(), 0, 15);
        String var3 = String.format("%s_%s", itemName, itemDamage);
        BagData var4 = (BagData) var2.loadItemData(BagData.class, var3);

        if (var4 == null) {
            var4 = new BagData(var3);
            var4.markDirty();
            var2.setItemData(var3, var4);
        }

        return var4;
    }
}