package ak.hyperdimensionalbag.client;

import ak.hyperdimensionalbag.item.BlockExchangerItem;
import ak.hyperdimensionalbag.item.BuildMode;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.block.Blocks.AIR;

/**
 * 設置予定ブロックのフレームを描画するクラス Created by A.K. on 14/07/01.
 */
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class RenderBlockSelectionBox {

  @SubscribeEvent
  @SuppressWarnings("unused")
  public void onRenderSelectionBox(DrawHighlightEvent event) {
    Entity entity = event.getInfo().getRenderViewEntity();
    if (!(entity instanceof PlayerEntity)) {
      return;
    }
    PlayerEntity player = (PlayerEntity) entity;
    ItemStack currentItem = player.getHeldItemMainhand();
    if (event.getTarget().getType() == RayTraceResult.Type.BLOCK
        && !currentItem.isEmpty()
        && currentItem.getItem() instanceof BlockExchangerItem) {
      BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) event.getTarget();
      List<BlockPos> list = new ArrayList<>();
      World world = player.world;
      BlockPos blockPos = blockRayTraceResult.getPos();
      BlockState state = world.getBlockState(blockPos);
      Direction face = blockRayTraceResult.getFace();
      ItemStack focusedBlockStack = new ItemStack(state.getBlock());
      int mode = BlockExchangerItem.getBuildMode(currentItem);
      boolean allMode = BlockExchangerItem.isAllExchangeMode(currentItem);
      MatrixStack matrixStack = event.getMatrix();
      IVertexBuilder ivertexbuilder = event.getBuffers().getBuffer(RenderType.getLines());
      if (BuildMode.getMode(mode) == BuildMode.EXCHANGE) {
        searchBlock(world, focusedBlockStack, blockPos, blockPos, face, face, currentItem, list);
        renderBlockListSelectionBox(matrixStack, ivertexbuilder, list, player, event.getPartialTicks());
      }
      int range = BlockExchangerItem.getRange(currentItem);

      if (BuildMode.getMode(mode) == BuildMode.WALL) {
        list = BlockExchangerItem
            .getNextWallBlockPosList(world, player, blockPos, face, range, allMode);
        renderBlockListSelectionBox(matrixStack, ivertexbuilder, list, player, event.getPartialTicks());
      }

      if (BuildMode.getMode(mode) == BuildMode.PILLAR) {
        list = BlockExchangerItem.getNextPillarBlockPosList(world, blockPos, face, range, allMode);
        renderBlockListSelectionBox(matrixStack, ivertexbuilder, list, player, event.getPartialTicks());
      }

      if (BuildMode.getMode(mode) == BuildMode.CUBE) {
        list = BlockExchangerItem
            .getNextCubeBlockPosList(world, player, blockPos, face, range, allMode);
        renderBlockListSelectionBox(matrixStack, ivertexbuilder, list, player, event.getPartialTicks());
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
   * @param side searched Direction
   * @param originSide searching origin Direction
   * @param heldItem heldItem ItemStack instance
   * @param list to render BlockPos instance list
   */
  private void searchBlock(World world, ItemStack focusedBlockStack, BlockPos blockPos, BlockPos origin,
      Direction side, Direction originSide, ItemStack heldItem, List<BlockPos> list) {
    if (!BlockExchangerItem
        .isVisibleBlock(world, BlockExchangerItem.getNextBlockPos(blockPos, originSide)) || list
        .contains(blockPos) || !isValidBlock(world, heldItem, blockPos, focusedBlockStack)) {
      return;
    }
    list.add(blockPos);
    for (Direction direction : Direction.values()) {
      if (side.equals(direction) || originSide.equals(direction) || originSide.getOpposite()
          .equals(direction)) {
        continue;
      }
      BlockPos newPos = BlockExchangerItem.getNextBlockPos(blockPos, direction);
      if (BlockExchangerItem.checkBlockInRange(heldItem, newPos, origin)) {
        searchBlock(world, focusedBlockStack, newPos, origin, direction.getOpposite(), originSide,
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
    BlockState state = world.getBlockState(blockPos);
    Block block = state.getBlock();
    if (block == AIR) {
      return false;
    }
    ItemStack nowBlock = new ItemStack(block);
    Block targetBlock = BlockExchangerItem.getTargetBlock(item);
    ItemStack targetBlockStack = new ItemStack(targetBlock);
    return !(targetBlockStack.isItemEqual(nowBlock)
        || !BlockExchangerItem.isAllExchangeMode(item) && !firstFocusedBlock.isItemEqual(nowBlock));
  }

  /**
   * render selection box
   * @param list BlockPos list
   * @param player PlayerEntity instance
   * @param partialTickItem partialTick
   */
  private void renderBlockListSelectionBox(MatrixStack matrixStack, IVertexBuilder vertexBuilder, List<BlockPos> list, PlayerEntity player,
                                           float partialTickItem) {
    double d3 = 0.002d;
    double playerPosX, playerPosY, playerPosZ;
    GlStateManager.enableBlend();
    GlStateManager.glBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.color4f(0.0F, 0.0F, 0.0F, 0.4F);
    GL11.glLineWidth(2.0F);
    GlStateManager.disableTexture();
    GlStateManager.depthMask(false);
    playerPosX = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double) partialTickItem;
    playerPosY = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * (double) partialTickItem;
    playerPosZ = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double) partialTickItem;
    for (BlockPos blockPos : list) {
      AxisAlignedBB axisAlignedBB = VoxelShapes.fullCube()
          .getBoundingBox().offset(blockPos).expand(d3, d3, d3)
          .offset(-playerPosX, -playerPosY - player.getEyeHeight(), -playerPosZ);
      WorldRenderer.drawBoundingBox(matrixStack, vertexBuilder, axisAlignedBB, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    GlStateManager.depthMask(true);
    GlStateManager.enableTexture();
    GlStateManager.disableBlend();
  }
}
