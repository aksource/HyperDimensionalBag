package ak.HyperDimensionalBag.item;

import ak.HyperDimensionalBag.HyperDimensionalBag;
import ak.HyperDimensionalBag.network.MessageKeyPressed;
import ak.HyperDimensionalBag.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.storagebox.ItemStorageBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ItemBlockExchanger extends ItemTool {

	public ItemBlockExchanger() {
		super(1.0F, 1.0F, ToolMaterial.DIAMOND, new HashSet<>());
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos blockPos, EnumHand hand, EnumFacing side, float par8, float par9, float par10) {
//		if(par3World.isRemote) return false;
        IBlockState state = par3World.getBlockState(blockPos);
		int blockMeta = state.getBlock().getMetaFromState(state);
		Block targetId = getTargetBlock(par1ItemStack);
		ItemStack targetBlockStack = new ItemStack(state.getBlock(), 1, blockMeta);
		if(targetId == null || targetId == Blocks.AIR || par2EntityPlayer.isSneaking()) {
			setTargetBlock(par1ItemStack, state.getBlock());
			setTargetItemStackMeta(par1ItemStack, blockMeta);
			if(!par3World.isRemote) {
				String registerBlock = String.format("Register block : %s", state.getBlock().getLocalizedName());
				par2EntityPlayer.addChatMessage(new TextComponentString(registerBlock));
			}
            return EnumActionResult.SUCCESS;
		}
        int mode = getBuildMode(par1ItemStack);
        if (EnumBuildMode.getMode(mode) == EnumBuildMode.exchange) {
			//とりあえず、同種のブロックの繋がりを置換。
			searchExchangeableBlock(par3World, par2EntityPlayer, targetBlockStack, blockPos, blockPos, side, side, par1ItemStack);
			getDroppedBlock(par3World, par2EntityPlayer);
		}
        List<BlockPos> blockPosList;
        boolean allMode = isAllExchangeMode(par1ItemStack);
        int range = getRange(par1ItemStack);
        if (EnumBuildMode.getMode(mode) == EnumBuildMode.wall) {
            blockPosList = getNextWallBlockPosList(par3World, par2EntityPlayer, blockPos, side, range, allMode);
            putBlockToBlockPosList(par3World, par2EntityPlayer, blockPosList, par1ItemStack, targetBlockStack, allMode);
        }

        if (EnumBuildMode.getMode(mode) == EnumBuildMode.pillar) {
            blockPosList = getNextPillarBlockPosList(par3World, blockPos, side, range, allMode);
            putBlockToBlockPosList(par3World, par2EntityPlayer, blockPosList, par1ItemStack, targetBlockStack, allMode);
        }

        if (EnumBuildMode.getMode(mode) == EnumBuildMode.cube) {
            blockPosList = getNextCubeBlockPosList(par3World, par2EntityPlayer, blockPos, side, range, allMode);
            putBlockToBlockPosList(par3World, par2EntityPlayer, blockPosList, par1ItemStack, targetBlockStack, allMode);
        }
        return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
		if(world.isRemote) {
            PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)));
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }
		return new ActionResult(EnumActionResult.SUCCESS, itemStack);
	}

    public static void onRightClickAction(ItemStack itemStack, EntityPlayer player, boolean keyPressed) {
        if(keyPressed) {
            if (player.isSneaking()) {
                setAllExchangeMode(itemStack, !isAllExchangeMode(itemStack));
                String allExchangeMode = String.format("All Exchange Mode : %b", isAllExchangeMode(itemStack));
                player.addChatMessage(new TextComponentString(allExchangeMode));
            } else {
                int nowMode = getBuildMode(itemStack);
                nowMode++;
                setBuildMode(itemStack, nowMode);
                String buildMode = String.format("Builder Mode : %s", EnumBuildMode.getMode(getBuildMode(itemStack)).name());
                player.addChatMessage(new TextComponentString(buildMode));
            }
        } else {
            int nowRange = getRange(itemStack);
            int var1 = (player.isSneaking())? -1 : 1 ;
            nowRange += var1;
            setRange(itemStack, nowRange);
            int range = 1 + getRange(itemStack) * 2;
            String blockRange = String.format("Range : %d*%d", range, range);
            player.addChatMessage(new TextComponentString(blockRange));
        }
    }

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4) {
		int range = 1 + getRange(par1ItemStack) * 2;
		String blockRange = String.format("Range : %d*%d", range, range);
		Block block = getTargetBlock(par1ItemStack);
		int meta = getTargetItemStackMeta(par1ItemStack);
        String blockName;
		if (block != null && block != Blocks.AIR) {
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
		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, player.getEntityBoundingBox().expand(5d, 5d, 5d));
		double d0, d1, d2;
		float f1 = player.rotationYaw * 0.01745329F;
		for (EntityItem eItem : list) {
			eItem.setNoPickupDelay();
			d0 = player.posX - MathHelper.sin(f1) * 0.5D;
			d1 = player.posY;
			d2 = player.posZ + MathHelper.cos(f1) * 0.5D;
			eItem.setPosition(d0, d1, d2);
		}
	}
	
	private void searchExchangeableBlock(World world, EntityPlayer player, ItemStack blockStack, BlockPos chunkpos, BlockPos origin, EnumFacing face, EnumFacing originFace, ItemStack heldItem) {
		if(!isVisibleBlock(world, getNextBlockPos(chunkpos, originFace)) || !hasTargetBlock(heldItem, player, chunkpos) || !exchangeBlock(world, player, heldItem, chunkpos, blockStack)) return;
		for (EnumFacing enumFacing : EnumFacing.values()) {
			if(face.equals(enumFacing) || originFace.equals(enumFacing) || originFace.getOpposite().equals(enumFacing)) continue;
			BlockPos newPos = getNextBlockPos(chunkpos, enumFacing);
			if (checkBlockInRange(heldItem, newPos, origin)) {
				searchExchangeableBlock(world, player, blockStack, newPos, origin, enumFacing.getOpposite(), originFace, heldItem);
			}
		}
	}

    public static List<BlockPos> getNextWallBlockPosList(World world, EntityPlayer player, BlockPos originPosition, EnumFacing side, int range, boolean allMode) {
        List<BlockPos> list = new ArrayList<>();
        //ターゲットしたブロックの面に接するブロックの座標
        BlockPos blockPos = originPosition.offset(side);
        int offsetX = side.getFrontOffsetX();
        int offsetY = side.getFrontOffsetY();
        int offsetZ = side.getFrontOffsetZ();
//        int basePositionX = originPosition.chunkPosX + offsetX;
//        int basePositionY = originPosition.chunkPosY + offsetY;
//        int basePositionZ = originPosition.chunkPosZ + offsetZ;
        int dx = 1 - Math.abs(offsetX);
        int dy = 1 - Math.abs(offsetY);
        int dz = 1 - Math.abs(offsetZ);

        int start = 0;
        int end = range * 2;
        if (side == EnumFacing.DOWN || side == EnumFacing.UP) {
            double centerDifX = Math.abs(originPosition.getX() + 0.5D - player.posX);
            //double baseCenterY = originPosition.chunkPosY + 0.5D;
            double centerDifZ = Math.abs(originPosition.getZ() + 0.5D - player.posZ);

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
        BlockPos blockPos1;
        for (int axis1 = start; axis1 <= end; axis1++) {
            for (int axis2 = -range; axis2 <= range; axis2++) {
                blockPos1 = blockPos.add(offsetX * axis1 + dx * axis2, offsetY * axis1 + dy * axis2, offsetZ * axis1 + dz * axis2);
//                int x1 = basePositionX + offsetX * axis1 + dx * axis2;
//                int y1 = basePositionY + offsetY * axis1 + dy * axis2;
//                int z1 = basePositionZ + offsetZ * axis1 + dz * axis2;
                if (world.getBlockState(blockPos1).getBlock() == Blocks.AIR || allMode) {
                    list.add(blockPos1);
                }
            }
        }
        return list;
    }

    public static List<BlockPos> getNextPillarBlockPosList(World world, BlockPos originPosition, EnumFacing side, int range, boolean allMode) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos blockPos = originPosition.offset(side);
        BlockPos blockPos1;
        for (int axis1 = 0; axis1 <= range * 2; axis1++) {
            blockPos1 = blockPos.offset(side, axis1);
            if (world.getBlockState(blockPos1).getBlock() == Blocks.AIR || allMode) {
                list.add(blockPos1);
            }
        }
        return list;
    }

    public static List<BlockPos> getNextCubeBlockPosList(World world, EntityPlayer player, BlockPos originPosition, EnumFacing side, int range, boolean allMode) {
        List<BlockPos> list = new ArrayList<>();
        BlockPos blockPos = originPosition.offset(side);
        int offsetX = side.getFrontOffsetX();
        int offsetY = side.getFrontOffsetY();
        int offsetZ = side.getFrontOffsetZ();
//        int basePositionX = originPosition.chunkPosX + offsetX;
//        int basePositionY = originPosition.chunkPosY + offsetY;
//        int basePositionZ = originPosition.chunkPosZ + offsetZ;
        int dx = 1 - Math.abs(offsetX);
        int dy = 1 - Math.abs(offsetY);
        int dz = 1 - Math.abs(offsetZ);
        int dx1 = 1 - Math.abs(offsetZ);
        int dy1 = 1 - Math.abs(offsetY);
        int dz1 = 1 - Math.abs(offsetX);

        int start = 0;
        int end = range * 2;
        int start1 = 0;
        int end1 = range * 2;
        if (side == EnumFacing.DOWN || side == EnumFacing.UP) {
            double centerDifX = Math.abs(originPosition.getX() + 0.5D - player.posX);
            double signX = Math.signum(originPosition.getX() + 0.5D - player.posX);
            //double baseCenterY = originPosition.chunkPosY + 0.5D;
            double centerDifZ = Math.abs(originPosition.getZ() + 0.5D - player.posZ);
            double signZ = Math.signum(originPosition.getZ() + 0.5D - player.posZ);

            if (centerDifX < centerDifZ) {
                dz = dx1 = 0;
                if (signZ <= 0) {
                    start1 = - range * 2;
                    end1 = 0;
                }
            } else {
                dx = dz1 = 0;
                if (signX <= 0) {
                    start1 = - range * 2;
                    end1 = 0;
                }
            }

            if (centerDifX < 0.5D && centerDifZ < 0.5D) {
                start = -range;
                end = range;
                start1 = -range;
                end1 = range;
                offsetX = offsetY;
                offsetY = 0;
            }
        } else {
            dx1 = dz1 = 0;
            dy = 0;
        }

        BlockPos blockPos1;
        for (int axis0 = start1; axis0 <= end1; axis0++) {
            for (int axis1 = start; axis1 <= end; axis1++) {
                for (int axis2 = -range; axis2 <= range; axis2++) {
                    blockPos1 = blockPos.add(offsetX * axis1 + dx * axis2 + dx1 * axis0, offsetY * axis1 + dy * axis2 + dy1 * axis0, offsetZ * axis1 + dz * axis2 + dz1 * axis0);
//                    int x1 = basePositionX + offsetX * axis1 + dx * axis2 + dx1 * axis0;
//                    int y1 = basePositionY + offsetY * axis1 + dy * axis2 + dy1 * axis0;
//                    int z1 = basePositionZ + offsetZ * axis1 + dz * axis2 + dz1 * axis0;
                    if (world.getBlockState(blockPos1).getBlock() == Blocks.AIR || allMode) {
                        list.add(blockPos1);
                    }
                }
            }
        }
        return list;
    }

    private static void putBlockToBlockPosList(World world, EntityPlayer player, List<BlockPos> list, ItemStack exchangeItem, ItemStack target, boolean allMode) {
        for (BlockPos blockPos : list) {
//            int x = blockPos.chunkPosX;
//            int y = blockPos.chunkPosY;
//            int z = blockPos.chunkPosZ;
            IBlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);
            ItemStack nowBlock = new ItemStack(block, 1, meta);
            if (target.isItemEqual(nowBlock) || (block != Blocks.AIR && !allMode)) continue;

            if (decreaseBlockFromInventory(exchangeItem, player, blockPos)) {
                Block targetBlock = getTargetBlock(exchangeItem);
                int targetMeta = getTargetItemStackMeta(exchangeItem);
                IBlockState targetState = targetBlock.getStateFromMeta(targetMeta);
                world.setBlockState(blockPos, targetState, 3);
                if (block != Blocks.AIR) {
                    block.onBlockHarvested(world, blockPos, targetState, player);
                    block.onBlockDestroyedByPlayer(world, blockPos, targetState);
                    if(!player.capabilities.isCreativeMode) {
                        TileEntity tile = world.getTileEntity(blockPos);
                        block.harvestBlock(world, player, new BlockPos(player.posX, player.posY, player.posZ), state, tile, player.getHeldItemMainhand());
                    }
                }
            }
        }
    }

	public static BlockPos getNextBlockPos(BlockPos pos, EnumFacing side) {
		return new BlockPos(pos).offset(side);
	}
	
	public static  boolean checkBlockInRange(ItemStack item, BlockPos check, BlockPos origin) {
		return Math.abs(check.getX() - origin.getX()) <= getRange(item) && Math.abs(check.getY() - origin.getY()) <= getRange(item) && Math.abs(check.getZ() - origin.getZ()) <= getRange(item);
	}
	
	public static  boolean isVisibleBlock(World world, BlockPos pos) {
		return HyperDimensionalBag.exchangeInvisibleBlock || world.getBlockState(pos).getBlock() == Blocks.AIR || !world.getBlockState(pos).getBlock().isOpaqueCube(world.getBlockState(pos));
	}
	
	private static boolean exchangeBlock(World world, EntityPlayer player, ItemStack item, BlockPos pos, ItemStack firstFocusBlock) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
		if(block == Blocks.AIR) return false;
		int meta = block.getMetaFromState(state);
		ItemStack nowBlock = new ItemStack(block, 1, meta);
		Block targetBlock = getTargetBlock(item);
		int targetBlockMeta = getTargetItemStackMeta(item);
        ItemStack targetBlockStack = new ItemStack(targetBlock, 1, targetBlockMeta);
        IBlockState targetState = targetBlock.getStateFromMeta(targetBlockMeta);
		if(targetBlockStack.isItemEqual(nowBlock) || (!isAllExchangeMode(item) && !firstFocusBlock.isItemEqual(nowBlock))) return false;
		if(decreaseBlockFromInventory(item, player, pos) && world.setBlockState(pos, targetState, 3)){
			block.onBlockHarvested(world, pos, state, player);
			block.onBlockDestroyedByPlayer(world,pos, state);
			if(!player.capabilities.isCreativeMode) {
                TileEntity tile = world.getTileEntity(pos);
                block.harvestBlock(world, player, new BlockPos(player.posX, player.posY, player.posZ), state, tile, player.getHeldItemMainhand());
            }
			return true;
		} else return false;
	}
	
	private static boolean decreaseBlockFromInventory(ItemStack exchangeItem, EntityPlayer player, BlockPos pos) {
		if (player.capabilities.isCreativeMode) return true;
		InventoryPlayer inv = player.inventory;
		ItemStack targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1, getTargetItemStackMeta(exchangeItem));
        for(int i = 0; i < inv.getSizeInventory();i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item == null) continue;
            if (checkValidBlock(player.worldObj, pos, targetBlockStack, item)) {
                item.stackSize--;
                if(item.stackSize == 0) inv.setInventorySlotContents(i, null);
                return true;
            } else if (HyperDimensionalBag.loadSB && item.getItem() instanceof ItemStorageBox && ItemStorageBox.peekItemStackAll(item) != null && checkValidBlock(player.worldObj, pos, targetBlockStack, ItemStorageBox.peekItemStackAll(item)) && ItemStorageBox.peekItemStackAll(item).stackSize > 1) {
                ItemStack copy = ItemStorageBox.peekItemStack(item);
                copy.stackSize = 1;
                ItemStorageBox.removeItemStack(item, copy);
                return true;
            }
        }
		return false;
	}
	
	private boolean hasTargetBlock(ItemStack exchangeItem, EntityPlayer player, BlockPos pos) {
		if (player.capabilities.isCreativeMode) return true;
		InventoryPlayer inv = player.inventory;
		ItemStack targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1, getTargetItemStackMeta(exchangeItem));
		for(ItemStack item : inv.mainInventory) {
            if(item == null) continue;
            if (checkValidBlock(player.worldObj, pos, targetBlockStack, item)) return true;
			else if (HyperDimensionalBag.loadSB && item.getItem() instanceof ItemStorageBox && ItemStorageBox.peekItemStackAll(item) != null && checkValidBlock(player.worldObj, pos, targetBlockStack, ItemStorageBox.peekItemStackAll(item)) && ItemStorageBox.peekItemStackAll(item).stackSize > 1) {
				return true;
			}
		}
		return false;
	}

    private static boolean checkValidBlock(World world, BlockPos pos, ItemStack target, ItemStack check) {
        if (target.getItem() != check.getItem()) return false;
        else if(!target.getHasSubtypes()) return true;
        else if(target.getItemDamage() == check.getItemDamage()) return true;
        else {
            Block targetBlock = Block.getBlockFromItem(target.getItem());
            List<ItemStack> drops = targetBlock.getDrops(world, pos, world.getBlockState(pos), 0);
            for (ItemStack item : drops) {
                if(item.getItem() instanceof ItemBlock && item.isItemEqual(check)) return true;
            }
            return false;
        }
    }

	private static void setTargetBlock(ItemStack item, Block block) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
		nbt.setString("HDB|targetBlockId", block.getRegistryName().toString());
	}
	
	public static Block getTargetBlock(ItemStack item) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
        String blockId = nbt.getString("HDB|targetBlockId");
        if(blockId == null || blockId.isEmpty()) return Blocks.AIR;
        return Block.getBlockFromName(blockId);
	}

    @Deprecated
	private static void setTargetItemStackMeta(ItemStack item, int meta) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
		nbt.setInteger("HDB|targetBlockMeta", meta);
	}

    @Deprecated
	public static int getTargetItemStackMeta(ItemStack item) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
		return nbt.getInteger("HDB|targetBlockMeta");
	}

	public static int getRange(ItemStack item) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
		return nbt.getInteger("HDB|blockRange");
	}
	
	private static void setRange(ItemStack item, int newRange) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
		newRange = (HyperDimensionalBag.maxRange + 1 + newRange) % (HyperDimensionalBag.maxRange + 1);
		nbt.setInteger("HDB|blockRange", newRange);
	}
	
	public static boolean isAllExchangeMode(ItemStack item) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = item.getTagCompound();
		return nbt.getBoolean("HDB|blockAllMode");
	}
	
	private static void setAllExchangeMode(ItemStack item, boolean mode) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = item.getTagCompound();
		nbt.setBoolean("HDB|blockAllMode", mode);
	}

    public static int getBuildMode(ItemStack item) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = item.getTagCompound();
        return nbt.getInteger("HDB|buildMode");
    }

    private static void setBuildMode(ItemStack item, int mode) {
        if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = item.getTagCompound();
        mode = (EnumBuildMode.getMODESLength() + mode) % (EnumBuildMode.getMODESLength());
        nbt.setInteger("HDB|buildMode", mode);
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