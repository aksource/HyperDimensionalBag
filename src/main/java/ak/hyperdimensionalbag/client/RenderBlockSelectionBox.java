package ak.hyperdimensionalbag.client;

import static ak.hyperdimensionalbag.item.ItemBlockExchanger.EnumBuildMode;

import ak.hyperdimensionalbag.item.ItemBlockExchanger;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * 設置予定ブロックのフレームを描画するクラス Created by A.K. on 14/07/01.
 */
@OnlyIn(Dist.CLIENT)
public class RenderBlockSelectionBox {

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void onRenderSelectionBox(DrawBlockHighlightEvent event) {
    ItemStack currentItem = event.getPlayer().getHeldItemMainhand();
    if (event.getTarget().type == RayTraceResult.Type.BLOCK
        && !currentItem.isEmpty()
        && currentItem.getItem() instanceof ItemBlockExchanger) {
      List<BlockPos> list = new ArrayList<>();
      RayTraceResult MOP = event.getTarget();
      EntityPlayer player = event.getPlayer();
      World world = event.getPlayer().getEntityWorld();
      BlockPos blockPos = MOP.getBlockPos();
      IBlockState state = world.getBlockState(blockPos);
      EnumFacing face = MOP.sideHit;
      ItemStack focusedBlockStack = new ItemStack(state.getBlock());
      int mode = ItemBlockExchanger.getBuildMode(currentItem);
      boolean allMode = ItemBlockExchanger.isAllExchangeMode(currentItem);
      if (EnumBuildMode.getMode(mode) == EnumBuildMode.exchange) {
        searchBlock(world, focusedBlockStack, blockPos, blockPos, face, face, currentItem, list);
        renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
      }
      int range = ItemBlockExchanger.getRange(currentItem);

      if (EnumBuildMode.getMode(mode) == EnumBuildMode.wall) {
        list = ItemBlockExchanger
            .getNextWallBlockPosList(world, player, blockPos, face, range, allMode);
        renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
      }

      if (EnumBuildMode.getMode(mode) == EnumBuildMode.pillar) {
        list = ItemBlockExchanger.getNextPillarBlockPosList(world, blockPos, face, range, allMode);
        renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
      }

      if (EnumBuildMode.getMode(mode) == EnumBuildMode.cube) {
        list = ItemBlockExchanger
            .getNextCubeBlockPosList(world, player, blockPos, face, range, allMode);
        renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
      }
      event.setCanceled(true);
    }
  }

  /**
   * search exchangeable block
   * @param world World instance
   * @param focusedBlockStack focused block ItemStack instance
   * @param blockPos BlockPos instance
   * @param origin searching origin BlockPos instance
   * @param face searched EnumFacing
   * @param originFace searching origin EnumFacing
   * @param heldItem heldItem ItemStack instance
   * @param list to render BlockPos instance list
   */
  private void searchBlock(World world, ItemStack focusedBlockStack, BlockPos blockPos, BlockPos origin,
      EnumFacing face, EnumFacing originFace, ItemStack heldItem, List<BlockPos> list) {
    if (!ItemBlockExchanger
        .isVisibleBlock(world, ItemBlockExchanger.getNextBlockPos(blockPos, originFace)) || list
        .contains(blockPos) || !isValidBlock(world, heldItem, blockPos, focusedBlockStack)) {
      return;
    }
    list.add(blockPos);
    for (EnumFacing direction : EnumFacing.values()) {
      if (face.equals(direction) || originFace.equals(direction) || originFace.getOpposite()
          .equals(direction)) {
        continue;
      }
      BlockPos newPos = ItemBlockExchanger.getNextBlockPos(blockPos, direction);
      if (ItemBlockExchanger.checkBlockInRange(heldItem, newPos, origin)) {
        searchBlock(world, focusedBlockStack, newPos, origin, direction.getOpposite(), originFace,
            heldItem, list);
      }
    }
  }

  /**
   * Check block is valid or not
   * @param world World instance
   * @param item ItemExchanger ItemStack instance
   * @param blockPos BlockPos instance
   * @param firstFocusedBlock first focused block ItemStack instance
   * @return true: block is valid
   */
  private boolean isValidBlock(World world, ItemStack item, BlockPos blockPos,
      ItemStack firstFocusedBlock) {
    IBlockState state = world.getBlockState(blockPos);
    Block block = state.getBlock();
    if (block == Blocks.AIR) {
      return false;
    }
    ItemStack nowBlock = new ItemStack(block);
    Block targetBlock = ItemBlockExchanger.getTargetBlock(item);
    ItemStack targetBlockStack = new ItemStack(targetBlock);
    return !(targetBlockStack.isItemEqual(nowBlock)
        || !ItemBlockExchanger.isAllExchangeMode(item) && !firstFocusedBlock.isItemEqual(nowBlock));
  }

  /**
   * render selection box
   * @param list BlockPos list
   * @param world World instance
   * @param player EntityPlayer instance
   * @param partialTickItem partialTick
   */
  private void renderBlockListSelectionBox(List<BlockPos> list, World world, EntityPlayer player,
      float partialTickItem) {
    double d3 = 0.002d;
    double d0, d1, d2;
    GlStateManager.enableBlend();
    OpenGlHelper.glBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.color4f(0.0F, 0.0F, 0.0F, 0.4F);
    GL11.glLineWidth(2.0F);
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false);
    for (BlockPos blockPos : list) {
      d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTickItem;
      d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTickItem;
      d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTickItem;
      AxisAlignedBB axisAlignedBB = VoxelShapes.fullCube()
          .getBoundingBox().offset(blockPos).expand(d3, d3, d3)
          .offset(-d0, -d1, -d2);
      WorldRenderer.drawSelectionBoundingBox(axisAlignedBB, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    GlStateManager.depthMask(true);
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }
}
