package ak.hyperdimensionalbag.item;

import ak.hyperdimensionalbag.ConfigUtils;
import ak.hyperdimensionalbag.client.ClientUtils;
import ak.hyperdimensionalbag.inventory.BagInventory;
import ak.hyperdimensionalbag.network.MessageKeyPressed;
import ak.hyperdimensionalbag.network.PacketHandler;
import ak.hyperdimensionalbag.util.ObjectUtils;
import ak.hyperdimensionalbag.util.StorageBoxUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.world.level.block.Blocks.AIR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockExchangerItem extends Item {

  public static final String NBT_KEY_BLOCK_STATE = "HDB|blockstate";
  private static final String NBT_KEY_BLOCK_ID = "HDB|targetBlockId";
  private static final String NBT_KEY_BLOCK_META = "HDB|targetBlockMeta";
  private static final String NBT_KEY_BLOCK_DROPS = "HDB|targetBlockDrops";
  private static final String NBT_KEY_BLOCK_RANGE = "HDB|blockRange";
  private static final String NBT_KEY_BLOCK_ALL_MODE = "HDB|blockAllMode";
  private static final String NBT_KEY_BLOCK_BUILD_MODE = "HDB|buildMode";

  public BlockExchangerItem() {
    super(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).setNoRepair());
  }

  /**
   * do mouse right click action.<br> called on {@link ak.hyperdimensionalbag.network.MessageKeyPressedHandler}
   *
   * @param itemStack  ItemExchanger ItemStack instance
   * @param player     Player instance
   * @param keyPressed true: left Ctrl key is pressed
   */
  public static void onRightClickAction(ItemStack itemStack, Player player, boolean keyPressed) {
    if (keyPressed) {
      if (player.isShiftKeyDown()) {
        setAllExchangeMode(itemStack, !isAllExchangeMode(itemStack));
        var allExchangeMode = String.format("All Exchange Mode : %b", isAllExchangeMode(itemStack));
        player.sendSystemMessage(Component.literal(allExchangeMode));
      } else {
        var nowMode = getBuildMode(itemStack);
        nowMode++;
        setBuildMode(itemStack, nowMode);
        var buildMode = String.format("Builder Mode : %s", BuildMode.getMode(getBuildMode(itemStack)).name());
        player.sendSystemMessage(Component.literal(buildMode));
      }
    } else {
      var nowRange = getRange(itemStack);
      var var1 = (player.isShiftKeyDown()) ? -1 : 1;
      nowRange += var1;
      setRange(itemStack, nowRange);
      var range = 1 + getRange(itemStack) * 2;
      var blockRange = String.format("Range : %d*%d", range, range);
      player.sendSystemMessage(Component.literal(blockRange));
    }
  }

  public static List<BlockPos> getNextWallBlockPosList(Level world, Player player, BlockPos originPosition, Direction side, int range, boolean allMode) {
    List<BlockPos> list = new ArrayList<>();
    //ターゲットしたブロックの面に接するブロックの座標
    var blockPos = originPosition.relative(side);
    var offsetX = side.getStepX();
    var offsetY = side.getStepY();
    var offsetZ = side.getStepZ();
    var dx = 1 - Math.abs(offsetX);
    var dy = 1 - Math.abs(offsetY);
    var dz = 1 - Math.abs(offsetZ);

    var start = 0;
    var end = range * 2;
    if (side == Direction.DOWN || side == Direction.UP) {
      var centerDifX = Math.abs(originPosition.getX() + 0.5D - player.getX());
      var centerDifZ = Math.abs(originPosition.getZ() + 0.5D - player.getZ());

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
    for (var axis1 = start; axis1 <= end; axis1++) {
      for (var axis2 = -range; axis2 <= range; axis2++) {
        var blockPos1 = blockPos.offset(offsetX * axis1 + dx * axis2, offsetY * axis1 + dy * axis2, offsetZ * axis1 + dz * axis2);
        if (world.getBlockState(blockPos1).getBlock() == AIR || allMode) {
          list.add(blockPos1);
        }
      }
    }
    return list;
  }

  public static List<BlockPos> getNextPillarBlockPosList(Level world, BlockPos originPosition, Direction side, int range, boolean allMode) {
    List<BlockPos> list = new ArrayList<>();
    var blockPos = originPosition.relative(side);
    for (var axis1 = 0; axis1 <= range * 2; axis1++) {
      var blockPos1 = blockPos.relative(side, axis1);
      if (world.getBlockState(blockPos1).getBlock() == AIR || allMode) {
        list.add(blockPos1);
      }
    }
    return list;
  }

  public static List<BlockPos> getNextCubeBlockPosList(Level world, Player player, BlockPos originPosition, Direction side, int range, boolean allMode) {
    List<BlockPos> list = new ArrayList<>();
    var blockPos = originPosition.relative(side);
    var offsetX = side.getStepX();
    var offsetY = side.getStepY();
    var offsetZ = side.getStepZ();
    var dx = 1 - Math.abs(offsetX);
    var dy = 1 - Math.abs(offsetY);
    var dz = 1 - Math.abs(offsetZ);
    var dx1 = 1 - Math.abs(offsetZ);
    var dy1 = 1 - Math.abs(offsetY);
    var dz1 = 1 - Math.abs(offsetX);

    var start = 0;
    var end = range * 2;
    var start1 = 0;
    var end1 = range * 2;
    if (side == Direction.DOWN || side == Direction.UP) {
      var centerDifX = Math.abs(originPosition.getX() + 0.5D - player.getX());
      var signX = Math.signum(originPosition.getX() + 0.5D - player.getX());
      var centerDifZ = Math.abs(originPosition.getZ() + 0.5D - player.getZ());
      var signZ = Math.signum(originPosition.getZ() + 0.5D - player.getZ());

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
    for (var axis0 = start1; axis0 <= end1; axis0++) {
      for (var axis1 = start; axis1 <= end; axis1++) {
        for (var axis2 = -range; axis2 <= range; axis2++) {
          blockPos1 = blockPos.offset(offsetX * axis1 + dx * axis2 + dx1 * axis0, offsetY * axis1 + dy * axis2 + dy1 * axis0, offsetZ * axis1 + dz * axis2 + dz1 * axis0);
          if (world.getBlockState(blockPos1).getBlock() == AIR || allMode) {
            list.add(blockPos1);
          }
        }
      }
    }
    return list;
  }

  private static void putBlockToBlockPosList(Level world, Player player, List<BlockPos> list, ItemStack exchangeItem, ItemStack target, boolean allMode) {
    for (var blockPos : list) {
      var state = world.getBlockState(blockPos);
      var block = state.getBlock();
      var nowBlock = new ItemStack(block);
      if (target.sameItem(nowBlock) || (block != AIR && !allMode)) {
        continue;
      }

      if (decreaseBlockFromInventory(exchangeItem, player)) {
        var targetBlock = getTargetBlock(exchangeItem);
        var targetState = targetBlock.defaultBlockState();
        world.setBlock(blockPos, targetState, 3);
        if (block != AIR) {
          block.playerWillDestroy(world, blockPos, targetState, player);
          block.destroy(world, blockPos, targetState);
          if (!player.getAbilities().instabuild) {
            var tile = world.getBlockEntity(blockPos);
            block.playerDestroy(world, player, new BlockPos(player.getX(), player.getY(), player.getZ()), state, tile, player.getMainHandItem());
          }
        }
      }
    }
  }

  public static BlockPos getNextBlockPos(BlockPos pos, Direction side) {
    return new BlockPos(pos).relative(side);
  }

  public static boolean checkBlockInRange(ItemStack item, BlockPos check, BlockPos origin) {
    return Math.abs(check.getX() - origin.getX()) <= getRange(item) && Math.abs(check.getY() - origin.getY()) <= getRange(item) && Math.abs(check.getZ() - origin.getZ()) <= getRange(item);
  }

  public static boolean isInvisibleBlock(Level world, BlockPos pos) {
    return !ConfigUtils.COMMON.exchangeInvisibleBlock && world.getBlockState(pos).getBlock() == AIR;
  }

  private static boolean exchangeBlock(Level world, Player player, ItemStack item, BlockPos pos, ItemStack firstFocusBlock) {
    var state = world.getBlockState(pos);
    var block = state.getBlock();
    if (isInvisibleBlock(world, pos)) {
      return false;
    }
    var nowBlock = new ItemStack(block);
    var targetBlock = getTargetBlock(item);
    var targetBlockStack = new ItemStack(targetBlock);
    var targetState = targetBlock.defaultBlockState();
    if (targetBlockStack.sameItem(nowBlock) || (!isAllExchangeMode(item) && !firstFocusBlock.sameItem(nowBlock))) {
      return false;
    }
    if (decreaseBlockFromInventory(item, player) && world.setBlock(pos, targetState, 3)) {
      block.playerWillDestroy(world, pos, state, player);
      block.destroy(world, pos, state);
      if (!player.getAbilities().instabuild) {
        var tile = world.getBlockEntity(pos);
        block.playerDestroy(world, player, new BlockPos(player.getX(), player.getY(), player.getZ()), state, tile, player.getMainHandItem());
      }
      return true;
    } else {
      return false;
    }
  }

  private static boolean decreaseBlockFromInventory(ItemStack exchangeItem, Player player) {
    if (player.getAbilities().instabuild) {
      return true;
    }
    var inv = player.getInventory();
    var targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1);
    var drops = getTargetItemStackDrops(exchangeItem);
    for (var i = 0; i < inv.getContainerSize(); i++) {
      var itemStack = inv.getItem(i);
      if (itemStack.isEmpty()) {
        continue;
      }
      if (itemStack.getItem() instanceof HDBagItem) {
        var data = new BagInventory(itemStack, player);
        for (var j = 0; j < data.getContainerSize(); j++) {
          var bagItemStack = data.getItem(j);
          if (checkValidBlock(targetBlockStack, bagItemStack, drops)) {
            bagItemStack.shrink(1);
            if (bagItemStack.getCount() == 0) {
              data.setItem(j, ItemStack.EMPTY);
            }
            return true;
          } else if (StorageBoxUtils.checkStorageBox(bagItemStack, targetBlockStack, drops)) {
            StorageBoxUtils.removeStack(bagItemStack);
            return true;
          }
        }
      } else if (checkValidBlock(targetBlockStack, itemStack, drops)) {
        itemStack.shrink(1);
        if (itemStack.getCount() == 0) {
          inv.setItem(i, ItemStack.EMPTY);
        }
        return true;
      } else if (StorageBoxUtils.checkStorageBox(itemStack, targetBlockStack, drops)) {
        StorageBoxUtils.removeStack(itemStack);
        return true;
      }
    }
    return false;
  }

  public static boolean checkValidBlock(ItemStack target, ItemStack check, List<ItemStack> drops) {
    if (target.getItem() == check.getItem()) {
      for (var item : drops) {
        if (item.getItem() instanceof BlockItem && item.sameItem(check)) {
          return true;
        }
      }
    }
    return false;
  }

  private static void setTargetBlock(ItemStack item, Block block) {
    var nbt = item.getOrCreateTag();
    nbt.putString(NBT_KEY_BLOCK_ID, Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString());
  }

  public static Block getTargetBlock(ItemStack item) {
    var nbt = item.getOrCreateTag();
    var blockId = nbt.getString(NBT_KEY_BLOCK_ID);
    if (blockId.isEmpty()) {
      return AIR;
    }
    return ObjectUtils.getBlock(blockId);
  }

  @Deprecated
  public static int getTargetItemStackMeta(ItemStack item) {
    var nbt = item.getOrCreateTag();
    return nbt.getInt(NBT_KEY_BLOCK_META);
  }

  private static void setTargetItemStackDrops(ItemStack item, List<ItemStack> drops) {
    if (drops.isEmpty()) {
      return;
    }
    var nbt = item.getOrCreateTag();
    var tagList = new ListTag();
    for (var itemStack : drops) {
      if (itemStack.getItem() instanceof BlockItem) {
        var compound = new CompoundTag();
        compound.putString(NBT_KEY_BLOCK_ID, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).toString());
        tagList.add(compound);
      }
    }
    nbt.put(NBT_KEY_BLOCK_DROPS, tagList);
  }

  private static NonNullList<ItemStack> getTargetItemStackDrops(ItemStack item) {
    var nbt = item.getOrCreateTag();
    var tagList = nbt.getList(NBT_KEY_BLOCK_DROPS, TAG_COMPOUND);
    NonNullList<ItemStack> drops = NonNullList.create();
    for (var i = 0; i < tagList.size(); i++) {
      var compound = tagList.getCompound(i);
      var blockId = compound.getString(NBT_KEY_BLOCK_ID);
      var block = ObjectUtils.getBlock(blockId);
      var itemStack = new ItemStack(block);
      drops.add(itemStack);
    }
    return drops;
  }

  public static int getRange(ItemStack item) {
    var nbt = item.getOrCreateTag();
    return nbt.getInt(NBT_KEY_BLOCK_RANGE);
  }

  private static void setRange(ItemStack item, int newRange) {
    var nbt = item.getOrCreateTag();
    newRange = (ConfigUtils.COMMON.maxRange + 1 + newRange) % (ConfigUtils.COMMON.maxRange + 1);
    nbt.putInt(NBT_KEY_BLOCK_RANGE, newRange);
  }

  public static boolean isAllExchangeMode(ItemStack item) {
    var nbt = item.getOrCreateTag();
    return nbt.getBoolean(NBT_KEY_BLOCK_ALL_MODE);
  }

  private static void setAllExchangeMode(ItemStack item, boolean mode) {
    var nbt = item.getOrCreateTag();
    nbt.putBoolean(NBT_KEY_BLOCK_ALL_MODE, mode);
  }

  public static int getBuildMode(ItemStack item) {
    var nbt = item.getOrCreateTag();
    return nbt.getInt(NBT_KEY_BLOCK_BUILD_MODE);
  }

  private static void setBuildMode(ItemStack item, int mode) {
    var nbt = item.getOrCreateTag();
    mode = (BuildMode.getMODESLength() + mode) % (BuildMode.getMODESLength());
    nbt.putInt(NBT_KEY_BLOCK_BUILD_MODE, mode);
  }

  @Override
  public InteractionResult useOn(UseOnContext itemUseContext) {
    var player = itemUseContext.getPlayer();
    var hand = InteractionHand.MAIN_HAND;
    var worldIn = itemUseContext.getLevel();
    var blockPos = itemUseContext.getClickedPos();
    var side = itemUseContext.getClickedFace();
    assert player != null;
    var itemStack = player.getItemInHand(hand);
    var state = worldIn.getBlockState(blockPos);
    var targetBlock = getTargetBlock(itemStack);
    var targetBlockStack = new ItemStack(state.getBlock(), 1);
    if (targetBlock == AIR || player.isShiftKeyDown()) {
      setTargetBlock(itemStack, state.getBlock());
      if (!worldIn.isClientSide) {
        NonNullList<ItemStack> drops = NonNullList.create();
        var tileentity = state.hasBlockEntity() ? worldIn.getBlockEntity(itemUseContext.getClickedPos()) : null;
        var builder = (new LootContext.Builder((ServerLevel) worldIn)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(itemUseContext.getClickedPos())).withParameter(LootContextParams.BLOCK_STATE, state).withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileentity).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.TOOL, itemStack);
        drops.addAll(state.getDrops(builder));
        setTargetItemStackDrops(itemStack, drops);
        var registerBlock = String.format("Register block : %s", targetBlockStack.getItem().getName(targetBlockStack).getString());
        player.sendSystemMessage(Component.literal(registerBlock));
      }
      return InteractionResult.SUCCESS;
    }
    var mode = getBuildMode(itemStack);
    if (BuildMode.getMode(mode) == BuildMode.EXCHANGE) {
      //とりあえず、同種のブロックの繋がりを置換。
      searchAndExchangeExchangeableBlock(worldIn, player, targetBlockStack, blockPos, blockPos, side, side, itemStack);
      dropDroppedBlockAtPlayer(worldIn, player);
    }
    List<BlockPos> blockPosList;
    var allMode = isAllExchangeMode(itemStack);
    var range = getRange(itemStack);
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
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
    var itemStack = player.getItemInHand(hand);
    if (world.isClientSide) {
      PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(ClientUtils.CTRL_KEY.isDown()));
      return InteractionResultHolder.success(itemStack);
    }
    return InteractionResultHolder.success(itemStack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    var range = 1 + getRange(stack) * 2;
    var blockRange = String.format("Range : %d*%d", range, range);
    var block = getTargetBlock(stack);
    String blockName;
    if (block != AIR) {
      var targetBlockStack = new ItemStack(block, 1);
      blockName = String.format("Target Block : %s", targetBlockStack.getHoverName().getString());
    } else {
      blockName = "Target Block is not set";
    }
    var mode = String.format("Build Mode : %s", BuildMode.getMode(getBuildMode(stack)).name());

    tooltip.add(Component.literal(blockName));
    tooltip.add(Component.literal(blockRange));
    tooltip.add(Component.literal(mode));
  }

  /**
   * re-drop dropped blocks at player.
   *
   * @param world  Level instance
   * @param player Player instance
   */
  private void dropDroppedBlockAtPlayer(Level world, Player player) {
    var list = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().expandTowards(5d, 5d, 5d));
    var f1 = player.getYRot() * 0.01745329F;
    for (var eItem : list) {
      eItem.setNoPickUpDelay();
      var d0 = player.getX() - Mth.sin(f1) * 0.5D;
      var d1 = player.getY();
      var d2 = player.getZ() + Mth.cos(f1) * 0.5D;
      eItem.setPos(d0, d1, d2);
    }
  }

  /**
   * search exchangeable block and exchange it
   *
   * @param world              Level instance
   * @param player             Player instance
   * @param targetBlockStack   Target block ItemStack instance
   * @param searchingTargetPos searching target BlockPos instance
   * @param origin             Searching origin BlockPos instance
   * @param searchedFace       Direction to except searching
   * @param originFace         First touched face
   * @param heldItem           ItemExchanger ItemStack instance
   */
  private void searchAndExchangeExchangeableBlock(Level world, Player player, ItemStack targetBlockStack, BlockPos searchingTargetPos, BlockPos origin, Direction searchedFace, Direction originFace, ItemStack heldItem) {
    if (!hasTargetBlock(heldItem, player) || !exchangeBlock(world, player, heldItem, searchingTargetPos, targetBlockStack)) {
      return;
    }
    for (var enumFacing : Direction.values()) {
      if (searchedFace.equals(enumFacing) || originFace.equals(enumFacing) || originFace.getOpposite().equals(enumFacing)) {
        continue;
      }
      var newPos = getNextBlockPos(searchingTargetPos, enumFacing);
      if (checkBlockInRange(heldItem, newPos, origin)) {
        searchAndExchangeExchangeableBlock(world, player, targetBlockStack, newPos, origin, enumFacing.getOpposite(), originFace, heldItem);
      }
    }
  }

  /**
   * Player has target block or not
   *
   * @param exchangeItem ItemExchanger ItemStack instance
   * @param player       Player instance
   * @return true: player has target block
   */
  private boolean hasTargetBlock(ItemStack exchangeItem, Player player) {
    if (player.getAbilities().instabuild) {
      return true;
    }
    var inv = player.getInventory();
    var targetBlockStack = new ItemStack(getTargetBlock(exchangeItem), 1);
    var drops = getTargetItemStackDrops(exchangeItem);
    for (var itemStack : inv.items) {
      if (itemStack.isEmpty()) {
        continue;
      }
      if (itemStack.getItem() instanceof HDBagItem) {
        var data = new BagInventory(itemStack, player);
        for (var j = 0; j < data.getContainerSize(); j++) {
          var bagItemStack = data.getItem(j);
          if (checkValidBlock(targetBlockStack, bagItemStack, drops)) {
            return true;
          } else if (StorageBoxUtils.checkStorageBox(bagItemStack, targetBlockStack, drops)) {
            return true;
          }
        }
      } else if (checkValidBlock(targetBlockStack, itemStack, drops)) {
        return true;
      } else if (StorageBoxUtils.checkStorageBox(itemStack, targetBlockStack, drops)) {
        return true;
      }
    }
    return false;
  }

}