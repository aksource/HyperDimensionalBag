package ak.hyperdimensionalbag.item;

import ak.hyperdimensionalbag.ConfigUtils;
import ak.hyperdimensionalbag.client.ClientProxy;
import ak.hyperdimensionalbag.network.MessageKeyPressed;
import ak.hyperdimensionalbag.network.PacketHandler;
import ak.hyperdimensionalbag.util.ObjectUtils;
import ak.hyperdimensionalbag.util.StorageBoxUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static net.minecraft.block.Blocks.AIR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockExchangerItem extends ToolItem {

  public static final String NBT_KEY_BLOCK_STATE = "HDB|blockstate";
  private static final String NBT_KEY_BLOCK_ID = "HDB|targetBlockId";
  private static final String NBT_KEY_BLOCK_META = "HDB|targetBlockMeta";
  private static final String NBT_KEY_BLOCK_DROPS = "HDB|targetBlockDrops";
  private static final String NBT_KEY_BLOCK_RANGE = "HDB|blockRange";
  private static final String NBT_KEY_BLOCK_ALL_MODE = "HDB|blockAllMode";
  private static final String NBT_KEY_BLOCK_BUILD_MODE = "HDB|buildMode";

  public BlockExchangerItem() {
    super(1.0F, 1.0F, ItemTier.DIAMOND, new HashSet<>(),
            new Item.Properties().group(ItemGroup.TOOLS).setNoRepair());
  }

  /**
   * do mouse right click action.<br> called on {@link ak.hyperdimensionalbag.network.MessageKeyPressedHandler}
   *
   * @param itemStack  ItemExchanger ItemStack instance
   * @param player     PlayerEntity instance
   * @param keyPressed true: left Ctrl key is pressed
   */
  public static void onRightClickAction(ItemStack itemStack, PlayerEntity player,
                                        boolean keyPressed) {
    if (keyPressed) {
      if (player.isSneaking()) {
        setAllExchangeMode(itemStack, !isAllExchangeMode(itemStack));
        String allExchangeMode = String
                .format("All Exchange Mode : %b", isAllExchangeMode(itemStack));
        player.sendMessage(new StringTextComponent(allExchangeMode), player.getUniqueID());
      } else {
        int nowMode = getBuildMode(itemStack);
        nowMode++;
        setBuildMode(itemStack, nowMode);
        String buildMode = String
                .format("Builder Mode : %s", BuildMode.getMode(getBuildMode(itemStack)).name());
        player.sendMessage(new StringTextComponent(buildMode), player.getUniqueID());
      }
    } else {
      int nowRange = getRange(itemStack);
      int var1 = (player.isSneaking()) ? -1 : 1;
      nowRange += var1;
      setRange(itemStack, nowRange);
      int range = 1 + getRange(itemStack) * 2;
      String blockRange = String.format("Range : %d*%d", range, range);
      player.sendMessage(new StringTextComponent(blockRange), player.getUniqueID());
    }
  }

  public static List<BlockPos> getNextWallBlockPosList(World world, PlayerEntity player,
                                                       BlockPos originPosition, Direction side, int range, boolean allMode) {
    List<BlockPos> list = new ArrayList<>();
    //ターゲットしたブロックの面に接するブロックの座標
    BlockPos blockPos = originPosition.offset(side);
    int offsetX = side.getXOffset();
    int offsetY = side.getYOffset();
    int offsetZ = side.getZOffset();
    int dx = 1 - Math.abs(offsetX);
    int dy = 1 - Math.abs(offsetY);
    int dz = 1 - Math.abs(offsetZ);

    int start = 0;
    int end = range * 2;
    if (side == Direction.DOWN || side == Direction.UP) {
      double centerDifX = Math.abs(originPosition.getX() + 0.5D - player.getPosX());
      double centerDifZ = Math.abs(originPosition.getZ() + 0.5D - player.getPosZ());

      if (centerDifX < centerDifZ) {
        dz = 0;
      } else {
        dx = 0;
      }

      if (centerDifX < 0.5D && centerDifZ < 0.5D) {
        start = -range;
        end = range;
        //noinspection SuspiciousNameCombination
        offsetX = offsetY;
        offsetY = 0;
      }
    } else {
      dy = 0;
    }
    BlockPos blockPos1;
    for (int axis1 = start; axis1 <= end; axis1++) {
      for (int axis2 = -range; axis2 <= range; axis2++) {
        blockPos1 = blockPos.add(offsetX * axis1 + dx * axis2, offsetY * axis1 + dy * axis2,
                offsetZ * axis1 + dz * axis2);
        if (world.getBlockState(blockPos1).getBlock() == AIR || allMode) {
          list.add(blockPos1);
        }
      }
    }
    return list;
  }

  public static List<BlockPos> getNextPillarBlockPosList(World world, BlockPos originPosition,
                                                         Direction side, int range, boolean allMode) {
    List<BlockPos> list = new ArrayList<>();
    BlockPos blockPos = originPosition.offset(side);
    BlockPos blockPos1;
    for (int axis1 = 0; axis1 <= range * 2; axis1++) {
      blockPos1 = blockPos.offset(side, axis1);
      if (world.getBlockState(blockPos1).getBlock() == AIR || allMode) {
        list.add(blockPos1);
      }
    }
    return list;
  }

  public static List<BlockPos> getNextCubeBlockPosList(World world, PlayerEntity player,
                                                       BlockPos originPosition, Direction side, int range, boolean allMode) {
    List<BlockPos> list = new ArrayList<>();
    BlockPos blockPos = originPosition.offset(side);
    int offsetX = side.getXOffset();
    int offsetY = side.getYOffset();
    int offsetZ = side.getZOffset();
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
    if (side == Direction.DOWN || side == Direction.UP) {
      double centerDifX = Math.abs(originPosition.getX() + 0.5D - player.getPosX());
      double signX = Math.signum(originPosition.getX() + 0.5D - player.getPosX());
      double centerDifZ = Math.abs(originPosition.getZ() + 0.5D - player.getPosZ());
      double signZ = Math.signum(originPosition.getZ() + 0.5D - player.getPosZ());

      if (centerDifX < centerDifZ) {
        dz = dx1 = 0;
        if (signZ <= 0) {
          start1 = -range * 2;
          end1 = 0;
        }
      } else {
        dx = dz1 = 0;
        if (signX <= 0) {
          start1 = -range * 2;
          end1 = 0;
        }
      }

      if (centerDifX < 0.5D && centerDifZ < 0.5D) {
        start = -range;
        end = range;
        start1 = -range;
        end1 = range;
        //noinspection SuspiciousNameCombination
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
          blockPos1 = blockPos.add(offsetX * axis1 + dx * axis2 + dx1 * axis0,
                  offsetY * axis1 + dy * axis2 + dy1 * axis0,
                  offsetZ * axis1 + dz * axis2 + dz1 * axis0);
          if (world.getBlockState(blockPos1).getBlock() == AIR || allMode) {
            list.add(blockPos1);
          }
        }
      }
    }
    return list;
  }

  private static void putBlockToBlockPosList(World world, PlayerEntity player, List<BlockPos> list,
                                             ItemStack exchangeItem, ItemStack target, boolean allMode) {
    for (BlockPos blockPos : list) {
      BlockState state = world.getBlockState(blockPos);
      Block block = state.getBlock();
      ItemStack nowBlock = new ItemStack(block);
      if (target.isItemEqual(nowBlock) || (block != AIR && !allMode)) {
        continue;
      }

      if (decreaseBlockFromInventory(exchangeItem, player)) {
        Block targetBlock = getTargetBlock(exchangeItem);
        BlockState targetState = targetBlock.getDefaultState();
        world.setBlockState(blockPos, targetState, 3);
        if (block != AIR) {
          block.onBlockHarvested(world, blockPos, targetState, player);
          block.onPlayerDestroy(world, blockPos, targetState);
          if (!player.abilities.isCreativeMode) {
            TileEntity tile = world.getTileEntity(blockPos);
            block.harvestBlock(world, player, new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ()),
                    state, tile, player.getHeldItemMainhand());
          }
        }
      }
    }
  }

  public static BlockPos getNextBlockPos(BlockPos pos, Direction side) {
    return new BlockPos(pos).offset(side);
  }

  public static boolean checkBlockInRange(ItemStack item, BlockPos check, BlockPos origin) {
    return Math.abs(check.getX() - origin.getX()) <= getRange(item)
            && Math.abs(check.getY() - origin.getY()) <= getRange(item)
            && Math.abs(check.getZ() - origin.getZ()) <= getRange(item);
  }

  @SuppressWarnings("deprecated")
  public static boolean isVisibleBlock(World world, BlockPos pos) {
    return ConfigUtils.COMMON.exchangeInvisibleBlock
            || world.getBlockState(pos).getBlock() == AIR || !world.getBlockState(pos).getBlock()
            .isTransparent(world.getBlockState(pos));
  }

  private static boolean exchangeBlock(World world, PlayerEntity player, ItemStack item,
                                       BlockPos pos, ItemStack firstFocusBlock) {
    BlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (block == AIR) {
      return false;
    }
    ItemStack nowBlock = new ItemStack(block);
    Block targetBlock = getTargetBlock(item);
    ItemStack targetBlockStack = new ItemStack(targetBlock);
    BlockState targetState = targetBlock.getDefaultState();
    if (targetBlockStack.isItemEqual(nowBlock) || (!isAllExchangeMode(item) && !firstFocusBlock
            .isItemEqual(nowBlock))) {
      return false;
    }
    if (decreaseBlockFromInventory(item, player) && world.setBlockState(pos, targetState, 3)) {
      block.onBlockHarvested(world, pos, state, player);
      block.onPlayerDestroy(world, pos, state);
      if (!player.abilities.isCreativeMode) {
        TileEntity tile = world.getTileEntity(pos);
        block
                .harvestBlock(world, player, new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ()),
                        state,
                        tile, player.getHeldItemMainhand());
      }
      return true;
    } else {
      return false;
    }
  }

  private static boolean decreaseBlockFromInventory(ItemStack exchangeItem, PlayerEntity player) {
    if (player.abilities.isCreativeMode) {
      return true;
    }
    PlayerInventory inv = player.inventory;
    ItemStack targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1);
    List<ItemStack> drops = getTargetItemStackDrops(exchangeItem);
    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack item = inv.getStackInSlot(i);
      if (item.isEmpty()) {
        continue;
      }
      if (checkValidBlock(targetBlockStack, item, drops)) {
        item.shrink(1);
        if (item.getCount() == 0) {
          inv.setInventorySlotContents(i, ItemStack.EMPTY);
        }
        return true;
      } else if (StorageBoxUtils.checkStorageBox(item, targetBlockStack)) {
        StorageBoxUtils.removeStack(item);
        return true;
      }
    }
    return false;
  }

  private static boolean checkValidBlock(ItemStack target, ItemStack check, List<ItemStack> drops) {
    if (target.getItem() != check.getItem()) {
      return false;
    } else {
      for (ItemStack item : drops) {
        if (item.getItem() instanceof BlockItem && item.isItemEqual(check)) {
          return true;
        }
      }
      return false;
    }
  }

  private static void setTargetBlock(ItemStack item, Block block) {
    CompoundNBT nbt = item.getOrCreateTag();
    nbt.putString(NBT_KEY_BLOCK_ID, Objects.requireNonNull(block.getRegistryName()).toString());
  }

  public static Block getTargetBlock(ItemStack item) {
    CompoundNBT nbt = item.getOrCreateTag();
    String blockId = nbt.getString(NBT_KEY_BLOCK_ID);
    if (blockId.isEmpty()) {
      return AIR;
    }
    return ObjectUtils.getBlock(blockId);
  }

  @Deprecated
  public static int getTargetItemStackMeta(ItemStack item) {
    CompoundNBT nbt = item.getOrCreateTag();
    return nbt.getInt(NBT_KEY_BLOCK_META);
  }

  private static void setTargetItemStackDrops(ItemStack item, List<ItemStack> drops) {
    if (drops == null || drops.isEmpty()) {
      return;
    }
    CompoundNBT nbt = item.getOrCreateTag();
    ListNBT tagList = new ListNBT();
    for (ItemStack itemStack : drops) {
      if (itemStack.getItem() instanceof BlockItem) {
        CompoundNBT compound = new CompoundNBT();
        compound.putString(NBT_KEY_BLOCK_ID, Objects
                .requireNonNull(itemStack.getItem().getRegistryName()).toString());
        tagList.add(compound);
      }
    }
    nbt.put(NBT_KEY_BLOCK_DROPS, tagList);
  }

  private static NonNullList<ItemStack> getTargetItemStackDrops(ItemStack item) {
    CompoundNBT nbt = item.getOrCreateTag();
    ListNBT tagList = nbt.getList(NBT_KEY_BLOCK_DROPS, Constants.NBT.TAG_COMPOUND);
    NonNullList<ItemStack> drops = NonNullList.create();
    for (int i = 0; i < tagList.size(); i++) {
      CompoundNBT compound = tagList.getCompound(i);
      String blockId = compound.getString(NBT_KEY_BLOCK_ID);
      Block block = ObjectUtils.getBlock(blockId);
      ItemStack itemStack = new ItemStack(block);
      drops.add(itemStack);
    }
    return drops;
  }

  public static int getRange(ItemStack item) {
    CompoundNBT nbt = item.getOrCreateTag();
    return nbt.getInt(NBT_KEY_BLOCK_RANGE);
  }

  private static void setRange(ItemStack item, int newRange) {
    CompoundNBT nbt = item.getOrCreateTag();
    newRange = (ConfigUtils.COMMON.maxRange + 1 + newRange) % (ConfigUtils.COMMON.maxRange + 1);
    nbt.putInt(NBT_KEY_BLOCK_RANGE, newRange);
  }

  public static boolean isAllExchangeMode(ItemStack item) {
    CompoundNBT nbt = item.getOrCreateTag();
    return nbt.getBoolean(NBT_KEY_BLOCK_ALL_MODE);
  }

  private static void setAllExchangeMode(ItemStack item, boolean mode) {
    CompoundNBT nbt = item.getOrCreateTag();
    nbt.putBoolean(NBT_KEY_BLOCK_ALL_MODE, mode);
  }

  public static int getBuildMode(ItemStack item) {
    CompoundNBT nbt = item.getOrCreateTag();
    return nbt.getInt(NBT_KEY_BLOCK_BUILD_MODE);
  }

  private static void setBuildMode(ItemStack item, int mode) {
    CompoundNBT nbt = item.getOrCreateTag();
    mode = (BuildMode.getMODESLength() + mode) % (BuildMode.getMODESLength());
    nbt.putInt(NBT_KEY_BLOCK_BUILD_MODE, mode);
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext itemUseContext) {
    PlayerEntity player = itemUseContext.getPlayer();
    Hand hand = Hand.MAIN_HAND;
    World worldIn = itemUseContext.getWorld();
    BlockPos blockPos = itemUseContext.getPos();
    Direction side = itemUseContext.getFace();
    assert player != null;
    ItemStack itemStack = player.getHeldItem(hand);
    BlockState state = worldIn.getBlockState(blockPos);
    Block targetBlock = getTargetBlock(itemStack);
    ItemStack targetBlockStack = new ItemStack(state.getBlock(), 1);
    if (targetBlock == AIR
            || player.isSneaking()) {
      setTargetBlock(itemStack, state.getBlock());
      if (!worldIn.isRemote) {
        NonNullList<ItemStack> drops = NonNullList.create();
        TileEntity tileentity = state.hasTileEntity() ? worldIn.getTileEntity(itemUseContext.getPos()) : null;
        LootContext.Builder builder = (new LootContext.Builder((ServerWorld) worldIn)).withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(itemUseContext.getPos())).withParameter(LootParameters.BLOCK_STATE, state).withNullableParameter(LootParameters.BLOCK_ENTITY, tileentity).withNullableParameter(LootParameters.THIS_ENTITY, player).withParameter(LootParameters.TOOL, itemStack);
        state.getDrops(builder);
        setTargetItemStackDrops(itemStack, drops);
        String registerBlock = String
                .format("Register block : %s",
                        targetBlockStack.getItem().getDisplayName(targetBlockStack).getString());
        player.sendMessage(new StringTextComponent(registerBlock), player.getUniqueID());
      }
      return ActionResultType.SUCCESS;
    }
    int mode = getBuildMode(itemStack);
    if (BuildMode.getMode(mode) == BuildMode.EXCHANGE) {
      //とりあえず、同種のブロックの繋がりを置換。
      searchAndExchangeExchangeableBlock(worldIn, player, targetBlockStack, blockPos, blockPos,
              side, side,
              itemStack);
      dropDroppedBlockAtPlayer(worldIn, player);
    }
    List<BlockPos> blockPosList;
    boolean allMode = isAllExchangeMode(itemStack);
    int range = getRange(itemStack);
    if (BuildMode.getMode(mode) == BuildMode.WALL) {
      blockPosList = getNextWallBlockPosList(worldIn, player, blockPos, side, range, allMode);
      putBlockToBlockPosList(worldIn, player, blockPosList, itemStack, targetBlockStack, allMode);
    }

    if (BuildMode.getMode(mode) == BuildMode.PILLAR) {
      blockPosList = getNextPillarBlockPosList(worldIn, blockPos, side, range, allMode);
      putBlockToBlockPosList(worldIn, player, blockPosList, itemStack, targetBlockStack, allMode);
    }

    if (BuildMode.getMode(mode) == BuildMode.CUBE) {
      blockPosList = getNextCubeBlockPosList(worldIn, player, blockPos, side, range, allMode);
      putBlockToBlockPosList(worldIn, player, blockPosList, itemStack, targetBlockStack, allMode);
    }
    return ActionResultType.SUCCESS;
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player,
                                                  @Nonnull Hand hand) {
    ItemStack itemStack = player.getHeldItem(hand);
    if (world.isRemote) {
      PacketHandler.INSTANCE
              .sendToServer(new MessageKeyPressed(ClientProxy.CTRL_KEY.isKeyDown()));
      return ActionResult.resultSuccess(itemStack);
    }
    return ActionResult.resultSuccess(itemStack);
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
                             ITooltipFlag flagIn) {
    int range = 1 + getRange(stack) * 2;
    String blockRange = String.format("Range : %d*%d", range, range);
    Block block = getTargetBlock(stack);
    String blockName;
    if (block != AIR) {
      ItemStack targetBlockStack = new ItemStack(block, 1);
      blockName = String
              .format("Target Block : %s", targetBlockStack.getDisplayName().getString());
    } else {
      blockName = "Target Block is not set";
    }
    String mode = String
            .format("Build Mode : %s", BuildMode.getMode(getBuildMode(stack)).name());

    tooltip.add(new StringTextComponent(blockName));
    tooltip.add(new StringTextComponent(blockRange));
    tooltip.add(new StringTextComponent(mode));
  }

  /**
   * re-drop dropped blocks at player.
   *
   * @param world  World instance
   * @param player PlayerEntity instance
   */
  private void dropDroppedBlockAtPlayer(World world, PlayerEntity player) {
    List<ItemEntity> list = world
            .getEntitiesWithinAABB(ItemEntity.class, player.getBoundingBox().expand(5d, 5d, 5d));
    double d0, d1, d2;
    float f1 = player.rotationYaw * 0.01745329F;
    for (ItemEntity eItem : list) {
      eItem.setNoPickupDelay();
      d0 = player.getPosX() - MathHelper.sin(f1) * 0.5D;
      d1 = player.getPosY();
      d2 = player.getPosZ() + MathHelper.cos(f1) * 0.5D;
      eItem.setPosition(d0, d1, d2);
    }
  }

  /**
   * search exchangeable block and exchange it
   *
   * @param world              World instance
   * @param player             PlayerEntity instance
   * @param targetBlockStack   Target block ItemStack instance
   * @param searchingTargetPos searching target BlockPos instance
   * @param origin             Searching origin BlockPos instance
   * @param searchedFace       Direction to except searching
   * @param originFace         First touched face
   * @param heldItem           ItemExchanger ItemStack instance
   */
  private void searchAndExchangeExchangeableBlock(World world, PlayerEntity player,
                                                  ItemStack targetBlockStack,
                                                  BlockPos searchingTargetPos, BlockPos origin, Direction searchedFace, Direction originFace,
                                                  ItemStack heldItem) {
    if (!isVisibleBlock(world, getNextBlockPos(searchingTargetPos, originFace))
            || !hasTargetBlock(heldItem, player)
            || !exchangeBlock(world, player, heldItem, searchingTargetPos, targetBlockStack)) {
      return;
    }
    for (Direction enumFacing : Direction.values()) {
      if (searchedFace.equals(enumFacing) || originFace.equals(enumFacing) || originFace
              .getOpposite()
              .equals(enumFacing)) {
        continue;
      }
      BlockPos newPos = getNextBlockPos(searchingTargetPos, enumFacing);
      if (checkBlockInRange(heldItem, newPos, origin)) {
        searchAndExchangeExchangeableBlock(world, player, targetBlockStack, newPos, origin,
                enumFacing.getOpposite(),
                originFace, heldItem);
      }
    }
  }

  /**
   * Player has target block or not
   *
   * @param exchangeItem ItemExchanger ItemStack instance
   * @param player       PlayerEntity instance
   * @return true: player has target block
   */
  private boolean hasTargetBlock(ItemStack exchangeItem, PlayerEntity player) {
    if (player.abilities.isCreativeMode) {
      return true;
    }
    PlayerInventory inv = player.inventory;
    ItemStack targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1);
    List<ItemStack> drops = getTargetItemStackDrops(exchangeItem);
    for (ItemStack item : inv.mainInventory) {
      if (item.isEmpty()) {
        continue;
      }
      if (checkValidBlock(targetBlockStack, item, drops)) {
        return true;
      } else if (StorageBoxUtils.checkStorageBox(item, targetBlockStack)) {
        return true;
      }
    }
    return false;
  }

}