package ak.hyperdimensionalbag.client;

import ak.hyperdimensionalbag.item.BlockExchangerItem;
import ak.hyperdimensionalbag.item.BuildMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;


/**
 * 設置予定ブロックのフレームを描画するクラス Created by A.K. on 14/07/01.
 */
@ParametersAreNonnullByDefault
public class RenderBlockSelectionBox {

  @SubscribeEvent
  public void onRenderSelectionBox(DrawSelectionEvent event) {
    var entity = event.getCamera().getEntity();
    if (!(entity instanceof Player player)) {
      return;
    }
    var currentItem = player.getMainHandItem();
    if (event.getTarget().getType() == HitResult.Type.BLOCK
        && !currentItem.isEmpty()
        && currentItem.getItem() instanceof BlockExchangerItem) {
      var blockRayTraceResult = (BlockHitResult) event.getTarget();
      List<BlockPos> list = new ArrayList<>();
      var world = player.level;
      var blockPos = blockRayTraceResult.getBlockPos();
      var state = world.getBlockState(blockPos);
      var face = blockRayTraceResult.getDirection();
      var focusedBlockStack = new ItemStack(state.getBlock());
      var mode = BlockExchangerItem.getBuildMode(currentItem);
      var allMode = BlockExchangerItem.isAllExchangeMode(currentItem);
      var matrixStack = event.getPoseStack();
      var ivertexbuilder = event.getMultiBufferSource().getBuffer(RenderType.lines());
      int range = BlockExchangerItem.getRange(currentItem);
      switch (BuildMode.getMode(mode)) {
        case EXCHANGE -> searchBlock(world, focusedBlockStack, blockPos, blockPos, face, face, currentItem, list);
        case WALL -> list = BlockExchangerItem
                .getNextWallBlockPosList(world, player, blockPos, face, range, allMode);
        case PILLAR -> list = BlockExchangerItem.getNextPillarBlockPosList(world, blockPos, face, range, allMode);
        case CUBE -> list = BlockExchangerItem
                .getNextCubeBlockPosList(world, player, blockPos, face, range, allMode);
      }
      if (!list.isEmpty()) {
        renderBlockListSelectionBox(matrixStack, ivertexbuilder, list, player, event.getPartialTicks());
        event.setCanceled(true);
      }
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
  private void searchBlock(Level world, ItemStack focusedBlockStack, BlockPos blockPos, BlockPos origin,
                           Direction side, Direction originSide, ItemStack heldItem, List<BlockPos> list) {
    if (list.contains(blockPos) || !isValidBlock(world, heldItem, blockPos, focusedBlockStack)) {
      return;
    }
    list.add(blockPos);
    for (var direction : Direction.values()) {
      if (side.equals(direction) || originSide.equals(direction) || originSide.getOpposite()
          .equals(direction)) {
        continue;
      }
      var newPos = BlockExchangerItem.getNextBlockPos(blockPos, direction);
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
  private boolean isValidBlock(Level world, ItemStack item, BlockPos blockPos,
      ItemStack firstFocusedBlock) {
    var state = world.getBlockState(blockPos);
    var block = state.getBlock();
    if (BlockExchangerItem.isInvisibleBlock(world, blockPos)) {
      return false;
    }
    var nowBlock = new ItemStack(block);
    var targetBlock = BlockExchangerItem.getTargetBlock(item);
    var targetBlockStack = new ItemStack(targetBlock);
    return !(targetBlockStack.sameItem(nowBlock)
        || !BlockExchangerItem.isAllExchangeMode(item) && !firstFocusedBlock.sameItem(nowBlock));
  }

  /**
   * render selection box
   * @param list BlockPos list
   * @param player PlayerEntity instance
   * @param partialTickItem partialTick
   */
  private void renderBlockListSelectionBox(PoseStack matrixStack, VertexConsumer vertexBuilder, List<BlockPos> list, Player player,
                                           float partialTickItem) {
    var d3 = 0.002d;
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(770, 771, 1, 0);
    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.4F);
    RenderSystem.disableTexture();
    RenderSystem.depthMask(false);
    var playerPosX = player.xOld + (player.getX() - player.xOld) * (double) partialTickItem;
    var playerPosY = player.yOld + (player.getY() - player.yOld) * (double) partialTickItem;
    var playerPosZ = player.zOld + (player.getZ() - player.zOld) * (double) partialTickItem;
    for (var blockPos : list) {
      var axisAlignedBB = Shapes.block()
          .bounds().move(blockPos).expandTowards(d3, d3, d3)
          .move(-playerPosX, -playerPosY - player.getEyeHeight(), -playerPosZ);
      LevelRenderer.renderLineBox(matrixStack, vertexBuilder, axisAlignedBB, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    RenderSystem.depthMask(true);
    RenderSystem.enableTexture();
    RenderSystem.disableBlend();
  }
}
