package ak.HyperDimensionalBag.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ak.HyperDimensionalBag.HyperDimensionalBag;
import ak.HyperDimensionalBag.network.MessageKeyPressed;
import ak.HyperDimensionalBag.network.PacketHandler;
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
import org.lwjgl.input.Keyboard;

import javax.swing.text.html.parser.Entity;

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
		}
        int mode = getBuildMode(par1ItemStack);
        if (EnumBuildMode.getMode(mode) == EnumBuildMode.exchange) {
			//とりあえず、同種のブロックの繋がりを置換。
			searchExchangeableBlock(par3World, par2EntityPlayer, targetBlockStack, new ChunkPosition(x, y, z), new ChunkPosition(x, y, z), ForgeDirection.VALID_DIRECTIONS[side], ForgeDirection.VALID_DIRECTIONS[side], par1ItemStack);
			getDroppedBlock(par3World, par2EntityPlayer);
		}
        List<ChunkPosition> chunkPositionList;
        boolean allMode = isAllExchangeMode(par1ItemStack);
        int range = getRange(par1ItemStack);
        if (EnumBuildMode.getMode(mode) == EnumBuildMode.wall) {
            chunkPositionList = getNextWallChunkPositionList(par3World, par2EntityPlayer, new ChunkPosition(x, y, z), ForgeDirection.VALID_DIRECTIONS[side], range, allMode);
            putBlockToChunkPositionList(par3World, par2EntityPlayer, chunkPositionList, par1ItemStack, targetBlockStack, allMode);
        }

        if (EnumBuildMode.getMode(mode) == EnumBuildMode.pillar) {
            chunkPositionList = getNextPillarChunkPositionList(par3World, new ChunkPosition(x, y, z), ForgeDirection.VALID_DIRECTIONS[side], range, allMode);
            putBlockToChunkPositionList(par3World, par2EntityPlayer, chunkPositionList, par1ItemStack, targetBlockStack, allMode);
        }

        if (EnumBuildMode.getMode(mode) == EnumBuildMode.cube) {
            chunkPositionList = getNextCubeChunkPositionList(par3World, par2EntityPlayer, new ChunkPosition(x, y, z), ForgeDirection.VALID_DIRECTIONS[side], range, allMode);
            putBlockToChunkPositionList(par3World, par2EntityPlayer, chunkPositionList, par1ItemStack, targetBlockStack, allMode);
        }
        return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if(world.isRemote) {
            PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)));
            return itemStack;
        }
		return itemStack;
	}

    public static void onRightClickAction(ItemStack itemStack, EntityPlayer player, boolean keyPressed) {
        if(keyPressed) {
            if (player.isSneaking()) {
                setAllExchangeMode(itemStack, !isAllExchangeMode(itemStack));
                String allExchangeMode = String.format("All Exchange Mode : %b", isAllExchangeMode(itemStack));
                player.addChatMessage(new ChatComponentText(allExchangeMode));
            } else {
                int nowMode = getBuildMode(itemStack);
                nowMode++;
                setBuildMode(itemStack, nowMode);
                String buildMode = String.format("Builder Mode : %s", EnumBuildMode.getMode(getBuildMode(itemStack)).name());
                player.addChatMessage(new ChatComponentText(buildMode));
            }
        } else {
            int nowRange = getRange(itemStack);
            int var1 = (player.isSneaking())? -1 : 1 ;
            nowRange += var1;
            setRange(itemStack, nowRange);
            int range = 1 + getRange(itemStack) * 2;
            String blockRange = String.format("Range : %d*%d", range, range);
            player.addChatMessage(new ChatComponentText(blockRange));
        }
    }

	@Override
    @SuppressWarnings("unchecked")
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		int range = 1 + getRange(par1ItemStack) * 2;
		String blockRange = String.format("Range : %d*%d", range, range);
		Block block = getTargetBlock(par1ItemStack);
		int meta = getTargetBlockMeta(par1ItemStack);
        String blockName;
		if (block != null) {
			ItemStack targetBlockStack = new ItemStack(block, 1, meta);
			blockName = String.format("Target Block : %s",  targetBlockStack.getDisplayName());
		} else {
			blockName = "Target Block is not set";
		}
        String mode = String.format("Build Mode : %s", EnumBuildMode.getMode(getBuildMode(par1ItemStack)).name());

		par3List.add(blockName);
		par3List.add(blockRange);
        par3List.add(mode);
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
	
	private void searchExchangeableBlock(World world, EntityPlayer player, ItemStack blockStack, ChunkPosition chunkpos, ChunkPosition origin, ForgeDirection face, ForgeDirection originFace, ItemStack heldItem) {
		if(!isVisibleBlock(world, getNextChunkPosition(chunkpos, originFace)) || !hasTargetBlock(heldItem, player) || !exchangeBlock(world, player, heldItem, chunkpos, blockStack)) return;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if(face.equals(direction) || originFace.equals(direction) || originFace.getOpposite().equals(direction)) continue;
			ChunkPosition newPos = getNextChunkPosition(chunkpos, direction);
			if (checkBlockInRange(heldItem, newPos, origin)) {
				searchExchangeableBlock(world, player, blockStack, newPos, origin, direction.getOpposite(), originFace, heldItem);
			}
		}
	}

    public static List<ChunkPosition> getNextWallChunkPositionList(World world, EntityPlayer player, ChunkPosition originPosition, ForgeDirection side, int range, boolean allMode) {
        List<ChunkPosition> list = new ArrayList<>();
        int offsetX = side.offsetX;
        int offsetY = side.offsetY;
        int offsetZ = side.offsetZ;
        int basePositionX = originPosition.chunkPosX + offsetX;
        int basePositionY = originPosition.chunkPosY + offsetY;
        int basePositionZ = originPosition.chunkPosZ + offsetZ;
        int dx = 1 - Math.abs(offsetX);
        int dy = 1 - Math.abs(offsetY);
        int dz = 1 - Math.abs(offsetZ);

        int start = 0;
        int end = range * 2;
        if (side == ForgeDirection.DOWN || side == ForgeDirection.UP) {
            double centerDifX = Math.abs(originPosition.chunkPosX + 0.5D - player.posX);
            //double baseCenterY = originPosition.chunkPosY + 0.5D;
            double centerDifZ = Math.abs(originPosition.chunkPosZ + 0.5D - player.posZ);

            if (centerDifX < centerDifZ) dz = 0;
            else dx = 0;

            if (centerDifX < 0.5D && centerDifZ < 0.5D) {
                start = -range;
                end = range;
                offsetX = offsetY;
                offsetY = 0;
            }
        } else {
            dy = 0;
        }

        for (int axis1 = start; axis1 <= end; axis1++) {
            for (int axis2 = -range; axis2 <= range; axis2++) {
                int x1 = basePositionX + offsetX * axis1 + dx * axis2;
                int y1 = basePositionY + offsetY * axis1 + dy * axis2;
                int z1 = basePositionZ + offsetZ * axis1 + dz * axis2;
                if (world.getBlock(x1, y1, z1) == Blocks.air || allMode) {
                    list.add(new ChunkPosition(x1, y1, z1));
                }
            }
        }
        return list;
    }

    public static List<ChunkPosition> getNextPillarChunkPositionList(World world, ChunkPosition originPosition, ForgeDirection side, int range, boolean allMode) {
        List<ChunkPosition> list = new ArrayList<>();
        int basePositionX = originPosition.chunkPosX + side.offsetX;
        int basePositionY = originPosition.chunkPosY + side.offsetY;
        int basePositionZ = originPosition.chunkPosZ + side.offsetZ;

        for (int axis1 = 0; axis1 <= range * 2; axis1++) {
            int x1 = basePositionX + side.offsetX * axis1;
            int y1 = basePositionY + side.offsetY * axis1;
            int z1 = basePositionZ + side.offsetZ * axis1;
            if (world.getBlock(x1, y1, z1) == Blocks.air || allMode) {
                list.add(new ChunkPosition(x1, y1, z1));
            }
        }
        return list;
    }

    public static List<ChunkPosition> getNextCubeChunkPositionList(World world, EntityPlayer player, ChunkPosition originPosition, ForgeDirection side, int range, boolean allMode) {
        List<ChunkPosition> list = new ArrayList<>();
        int offsetX = side.offsetX;
        int offsetY = side.offsetY;
        int offsetZ = side.offsetZ;
        int basePositionX = originPosition.chunkPosX + offsetX;
        int basePositionY = originPosition.chunkPosY + offsetY;
        int basePositionZ = originPosition.chunkPosZ + offsetZ;
        int dx = 1 - Math.abs(offsetX);
        int dy = 1 - Math.abs(offsetY);
        int dz = 1 - Math.abs(offsetZ);
        int dx1 = 1 - Math.abs(offsetZ);
        int dy1 = 1 - Math.abs(offsetY);
        int dz1 = 1 - Math.abs(offsetX);

        int start = 0;
        int end = range * 2;
        if (side == ForgeDirection.DOWN || side == ForgeDirection.UP) {
            double centerDifX = Math.abs(originPosition.chunkPosX + 0.5D - player.posX);
            //double baseCenterY = originPosition.chunkPosY + 0.5D;
            double centerDifZ = Math.abs(originPosition.chunkPosZ + 0.5D - player.posZ);

            if (centerDifX < centerDifZ) {
                dz = dx1 = 0;
            } else {
                dx = dz1 = 0;
            }

            if (centerDifX < 0.5D && centerDifZ < 0.5D) {
                start = -range;
                end = range;
                offsetX = offsetY;
                offsetY = 0;
            }
        } else {
            dx1 = dz1 = 0;
            dy = 0;
        }

        for (int axis0 = start; axis0 <= end; axis0++) {
            for (int axis1 = start; axis1 <= end; axis1++) {
                for (int axis2 = -range; axis2 <= range; axis2++) {
                    int x1 = basePositionX + offsetX * axis1 + dx * axis2 + dx1 * axis0;
                    int y1 = basePositionY + offsetY * axis1 + dy * axis2 + dy1 * axis0;
                    int z1 = basePositionZ + offsetZ * axis1 + dz * axis2 + dz1 * axis0;
                    if (world.getBlock(x1, y1, z1) == Blocks.air || allMode) {
                        list.add(new ChunkPosition(x1, y1, z1));
                    }
                }
            }
        }
        return list;
    }

    private static void putBlockToChunkPositionList(World world, EntityPlayer player, List<ChunkPosition> list, ItemStack exchangeItem, ItemStack target, boolean allMode) {
        for (ChunkPosition chunkPosition : list) {
            int x = chunkPosition.chunkPosX;
            int y = chunkPosition.chunkPosY;
            int z = chunkPosition.chunkPosZ;
            Block block = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);
            ItemStack nowBlock = new ItemStack(block, 1, meta);
            if (target.isItemEqual(nowBlock) || (block != Blocks.air && !allMode)) continue;

            if (decreaseBlockFromInventory(exchangeItem, player)) {
                Block targetBlock = getTargetBlock(exchangeItem);
                int targetMeta = getTargetBlockMeta(exchangeItem);
                world.setBlock(x, y, z, targetBlock, targetMeta, 3);
                if (block != Blocks.air) {
                    block.onBlockHarvested(world, x, y, z, meta, player);
                    block.onBlockDestroyedByPlayer(world, x, y, z, meta);
                    if(!player.capabilities.isCreativeMode)
                        block.harvestBlock(world, player, MathHelper.ceiling_double_int( player.posX), MathHelper.ceiling_double_int( player.posY), MathHelper.ceiling_double_int( player.posZ), meta);

                }
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
	
	private static boolean exchangeBlock(World world, EntityPlayer player, ItemStack item, ChunkPosition chunk, ItemStack firstFocusBlock) {
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
	
	private static boolean decreaseBlockFromInventory(ItemStack exchangeItem, EntityPlayer player) {
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

    private static boolean checkValidBlock(World world, ItemStack target, ItemStack check) {
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
		nbt.setString("HDB|targetBlockId", GameRegistry.findUniqueIdentifierFor(block).toString());
		item.setTagCompound(nbt);
	}
	
	public static Block getTargetBlock(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
        String blockId = nbt.getString("HDB|targetBlockId");
        if(blockId == null || blockId.isEmpty()) return null;
        else {
            GameRegistry.UniqueIdentifier uni = new GameRegistry.UniqueIdentifier(blockId);
            return GameRegistry.findBlock(uni.modId, uni.name);
        }
	}
	
	private static void setTargetBlockMeta(ItemStack item, int meta) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		nbt.setInteger("HDB|targetBlockMeta", meta);
		item.setTagCompound(nbt);
	}
	
	public static int getTargetBlockMeta(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		return nbt.getInteger("HDB|targetBlockMeta");
	}
	
	public static int getRange(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		return nbt.getInteger("HDB|blockRange");
	}
	
	private static void setRange(ItemStack item, int newRange) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) nbt = new NBTTagCompound();
		newRange = (HyperDimensionalBag.maxRange + 1 + newRange) % (HyperDimensionalBag.maxRange + 1);
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

    public static int getBuildMode(ItemStack item) {
        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) nbt = new NBTTagCompound();
        return nbt.getInteger("HDB|buildMode");
    }

    private static void setBuildMode(ItemStack item, int mode) {
        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) nbt = new NBTTagCompound();
        mode = (EnumBuildMode.getMODESLength() + mode) % (EnumBuildMode.getMODESLength());
        nbt.setInteger("HDB|buildMode", mode);
        item.setTagCompound(nbt);
    }

    public enum EnumBuildMode {
        exchange,
        wall,
        pillar,
        cube;
        public static final EnumBuildMode[] MODES = {exchange, wall, pillar, cube};
        public static EnumBuildMode getMode(int par1) {
            return MODES[par1 % MODES.length];
        }
        public static int getMODESLength() {
            return MODES.length;
        }
    }
}