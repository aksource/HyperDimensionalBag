package ak.HyperDimensionalBag.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ak.HyperDimensionalBag.HyperDimensionalBag;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.storagebox.ItemStorageBox;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemBlockExchanger extends ItemTool {

	public ItemBlockExchanger() {
		super(1.0F, ToolMaterial.EMERALD, new HashSet());
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int x, int y, int z, int side, float par8, float par9, float par10) {
//		if(par3World.isRemote) return false;
		Block blockId = par3World.getBlock(x, y, z);
		int blockMeta = par3World.getBlockMetadata(x, y, z);
		Block targetId = getTargetBlock(par1ItemStack);
		ItemStack targetBlockStack = new ItemStack(blockId, 1, blockMeta);
		if(targetId == null || targetId == Blocks.air || par2EntityPlayer.isSneaking()) {
			setTargetBlock(par1ItemStack, blockId);
			setTargetBlockMeta(par1ItemStack, blockMeta);
			if(!par3World.isRemote) {
				String registerBlock = String.format("Register block : %s", targetBlockStack.getDisplayName());
				par2EntityPlayer.addChatMessage(new ChatComponentText(registerBlock));
			}
			return true;
		} else {
			//とりあえず、同種のブロックの繋がりを置換。
			searchBlock(par3World, par2EntityPlayer, targetBlockStack, new ChunkPosition(x, y, z), new ChunkPosition(x, y, z), ForgeDirection.VALID_DIRECTIONS[side], ForgeDirection.VALID_DIRECTIONS[side], par1ItemStack);
			getDroppedBlock(par3World, par2EntityPlayer);
			return true;
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		if(par2World.isRemote) return par1ItemStack;
		int nowRange = getRange(par1ItemStack);
		if(par3EntityPlayer.isSneaking()) {
			setAllExchangeMode(par1ItemStack, !isAllExchangeMode(par1ItemStack));
			String allExchangeMode = String.format("All Exchange Mode : %b", isAllExchangeMode(par1ItemStack));
			par3EntityPlayer.addChatMessage(new ChatComponentText(allExchangeMode));
		} else {
			nowRange++;
			setRange(par1ItemStack, nowRange);
			int range = 1 + getRange(par1ItemStack) * 2;
			String blockRange = String.format("Range : %d*%d", range, range);
			par3EntityPlayer.addChatMessage(new ChatComponentText(blockRange));
		}
		return par1ItemStack;
	}

	@Override
    @SuppressWarnings("unchecked")
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		String blockName;
		int range = 1 + getRange(par1ItemStack) * 2;
		String blockRange = String.format("Range : %d*%d", range, range);
		Block block = getTargetBlock(par1ItemStack);
		int meta = getTargetBlockMeta(par1ItemStack);
		if (block != null) {
			ItemStack targetBlockStack = new ItemStack(block, 1, meta);
			blockName = String.format("Target Block : %s",  targetBlockStack.getDisplayName());
		} else {
			blockName = "Target Block is not set";
		}
		par3List.add(blockName);
		par3List.add(blockRange);
	}
	
	private void getDroppedBlock(World world, EntityPlayer player) {
        @SuppressWarnings("unchecked")
		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(5d, 5d, 5d));
		if (list == null) return;
		double d0, d1, d2;
		float f1 = player.rotationYaw * 0.01745329F;
		for (EntityItem eItem : list) {
			eItem.delayBeforeCanPickup = 0;
			d0 = player.posX - MathHelper.sin(f1) * 0.5D;
			d1 = player.posY;
			d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
			eItem.setPosition(d0, d1, d2);
		}
	}
	
	private void searchBlock(World world, EntityPlayer player, ItemStack blockStack, ChunkPosition chunkpos, ChunkPosition origin, ForgeDirection face, ForgeDirection originFace, ItemStack heldItem) {
		if(!isVisibleBlock(world, getNextChunkPosition(chunkpos, originFace)) || !hasTargetBlock(heldItem, player) || !exchangeBlock(world, player, heldItem, chunkpos, blockStack)) return;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if(face.equals(direction) || originFace.equals(direction) || originFace.getOpposite().equals(direction)) continue;
			ChunkPosition newPos = getNextChunkPosition(chunkpos, direction);
			if (checkBlockInRange(heldItem, newPos, origin)) {
				searchBlock(world, player, blockStack, newPos, origin, direction.getOpposite(), originFace, heldItem);
			}
		}
	}
	
	public static ChunkPosition getNextChunkPosition(ChunkPosition chunk, ForgeDirection side)
	{
		int dx = side.offsetX;
		int dy = side.offsetY;
		int dz = side.offsetZ;
		return new ChunkPosition(chunk.chunkPosX + dx,chunk.chunkPosY + dy,chunk.chunkPosZ + dz);
	}
	
	public static  boolean checkBlockInRange(ItemStack item, ChunkPosition check, ChunkPosition origin) {
		return Math.abs(check.chunkPosX - origin.chunkPosX) <= getRange(item) && Math.abs(check.chunkPosY - origin.chunkPosY) <= getRange(item) && Math.abs(check.chunkPosZ - origin.chunkPosZ) <= getRange(item);
	}
	
	public static  boolean isVisibleBlock(World world, ChunkPosition chunk) {
		return HyperDimensionalBag.exchangeInvisibleBlock || world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ) == Blocks.air || !world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ).isOpaqueCube();
	}
	
	private boolean exchangeBlock(World world, EntityPlayer player, ItemStack item, ChunkPosition chunk, ItemStack firstFocusBlock) {
		Block block = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
		if(block == Blocks.air) return false;
		int meta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
		ItemStack nowBlock = new ItemStack(block, 1, meta);
		Block targetBlock = getTargetBlock(item);
		int targetBlockMeta = getTargetBlockMeta(item);
        ItemStack targetBlockStack = new ItemStack(targetBlock, 1, targetBlockMeta);
		if(targetBlockStack.isItemEqual(nowBlock) || (!isAllExchangeMode(item) && !firstFocusBlock.isItemEqual(nowBlock))) return false;
		if(decreaseBlockFromInventory(item, player) && world.setBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, targetBlock, targetBlockMeta, 3)){
			block.onBlockHarvested(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta, player);
			block.onBlockDestroyedByPlayer(world,chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ, meta);
			if(!player.capabilities.isCreativeMode)
				block.harvestBlock(world, player, MathHelper.ceiling_double_int( player.posX), MathHelper.ceiling_double_int( player.posY), MathHelper.ceiling_double_int( player.posZ), meta);
			return true;
		} else return false;
	}
	
	private boolean decreaseBlockFromInventory(ItemStack exchangeItem, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) return true;
		InventoryPlayer inv = player.inventory;
		ItemStack targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1, getTargetBlockMeta(exchangeItem));
        for(int i = 0; i < inv.getSizeInventory();i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item == null) continue;
            if (checkValidBlock(player.worldObj, targetBlockStack, item)) {
                item.stackSize--;
                if(item.stackSize == 0) inv.setInventorySlotContents(i, null);
                return true;
            } else if (HyperDimensionalBag.loadSB && item.getItem() instanceof ItemStorageBox && ItemStorageBox.peekItemStackAll(item) != null && checkValidBlock(player.worldObj, targetBlockStack, ItemStorageBox.peekItemStackAll(item)) && ItemStorageBox.peekItemStackAll(item).stackSize > 1) {
                ItemStack copy = ItemStorageBox.peekItemStack(item);
                copy.stackSize = 1;
                ItemStorageBox.removeItemStack(item, copy);
                return true;
            }
        }
		return false;
	}
	
	private boolean hasTargetBlock(ItemStack exchangeItem, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) return true;
		InventoryPlayer inv = player.inventory;
		ItemStack targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1, getTargetBlockMeta(exchangeItem));
		for(ItemStack item : inv.mainInventory) {
            if(item == null) continue;
            if (checkValidBlock(player.worldObj, targetBlockStack, item)) return true;
			else if (HyperDimensionalBag.loadSB && item.getItem() instanceof ItemStorageBox && ItemStorageBox.peekItemStackAll(item) != null && checkValidBlock(player.worldObj, targetBlockStack, ItemStorageBox.peekItemStackAll(item)) && ItemStorageBox.peekItemStackAll(item).stackSize > 1) {
				return true;
			}
		}
		return false;
	}

    private boolean checkValidBlock(World world, ItemStack target, ItemStack check) {
        if (target.getItem() != check.getItem()) return false;
        else if(!target.getHasSubtypes()) return true;
        else if(target.getItemDamage() == check.getItemDamage()) return true;
        else {
            Block targetBlock = Block.getBlockFromItem(target.getItem());
            ArrayList<ItemStack> drops = targetBlock.getDrops(world, 0, 0, 0, target.getItemDamage(), 0);
            for (ItemStack item : drops) {
                if(item.getItem() instanceof ItemBlock && item.isItemEqual(check)) return true;
            }
            return false;
        }
    }

	private static void setTargetBlock(ItemStack item, Block block) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		nbt.setString("HDB|blockExchangeId", GameRegistry.findUniqueIdentifierFor(block).toString());
		item.setTagCompound(nbt);
	}
	
	public static Block getTargetBlock(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
        String blockId = nbt.getString("HDB|blockExchangeId");
        if(blockId == null || blockId.isEmpty()) return null;
        else {
            GameRegistry.UniqueIdentifier uni = new GameRegistry.UniqueIdentifier(blockId);
            return GameRegistry.findBlock(uni.modId, uni.name);
        }
	}
	
	private static void setTargetBlockMeta(ItemStack item, int meta) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		nbt.setInteger("HDB|blockExchangeMeta", meta);
		item.setTagCompound(nbt);
	}
	
	public static int getTargetBlockMeta(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		return nbt.getInteger("HDB|blockExchangeMeta");
	}
	
	public static int getRange(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		return nbt.getInteger("HDB|blockRange");
	}
	
	private static void setRange(ItemStack item, int newRange) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		newRange = newRange < 0 ? HyperDimensionalBag.maxRange: newRange > HyperDimensionalBag.maxRange ? 0 : newRange;
		nbt.setInteger("HDB|blockRange", newRange);
		item.setTagCompound(nbt);
	}
	
	public static boolean isAllExchangeMode(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		return nbt.getBoolean("HDB|blockAllMode");
	}
	
	private static void setAllExchangeMode(ItemStack item, boolean mode) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		nbt.setBoolean("HDB|blockAllMode", mode);
		item.setTagCompound(nbt);
	}
}