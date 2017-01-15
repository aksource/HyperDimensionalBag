package ak.HyperDimensionalBag.client;

import ak.HyperDimensionalBag.item.ItemBlockExchanger;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static ak.HyperDimensionalBag.item.ItemBlockExchanger.EnumBuildMode;

/**
 * 設置予定ブロックのフレームを描画するクラス
 * Created by A.K. on 14/07/01.
 */
@SideOnly(Side.CLIENT)
public class RenderBlockSelectionBox {

    @SubscribeEvent
    public void onRenderSelectionBox(DrawBlockHighlightEvent event) {
        ItemStack currentItem = event.getPlayer().getHeldItemMainhand();
        if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK
                && currentItem != null
                && currentItem.getItem() instanceof ItemBlockExchanger) {
            List<BlockPos> list = new ArrayList<>();
            RayTraceResult MOP = event.getTarget();
            EntityPlayer player = event.getPlayer();
            World world = event.getPlayer().getEntityWorld();
            BlockPos blockPos = MOP.getBlockPos();
            IBlockState state = world.getBlockState(blockPos);
            EnumFacing face = MOP.sideHit;
            ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
            int mode = ItemBlockExchanger.getBuildMode(currentItem);
            boolean allMode = ItemBlockExchanger.isAllExchangeMode(currentItem);
            if (EnumBuildMode.getMode(mode) == EnumBuildMode.exchange) {
                searchBlock(world, blockStack, blockPos, blockPos, face, face, currentItem, list);
                renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
            }
            int range = ItemBlockExchanger.getRange(currentItem);

            if (EnumBuildMode.getMode(mode) == EnumBuildMode.wall) {
                list = ItemBlockExchanger.getNextWallBlockPosList(world, player, blockPos, face, range, allMode);
                renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
            }

            if (EnumBuildMode.getMode(mode) == EnumBuildMode.pillar) {
                list = ItemBlockExchanger.getNextPillarBlockPosList(world, blockPos, face, range, allMode);
                renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
            }

            if (EnumBuildMode.getMode(mode) == EnumBuildMode.cube) {
                list = ItemBlockExchanger.getNextCubeBlockPosList(world, player, blockPos, face, range, allMode);
                renderBlockListSelectionBox(list, world, player, event.getPartialTicks());
            }
            event.setCanceled(true);
        }
    }

    private void searchBlock(World world, ItemStack blockStack, BlockPos blockPos, BlockPos origin, EnumFacing face, EnumFacing originFace, ItemStack heldItem, List<BlockPos> list) {
        if(!ItemBlockExchanger.isVisibleBlock(world, ItemBlockExchanger.getNextBlockPos(blockPos, originFace)) || list.contains(blockPos) || !isValidBlock(world, heldItem, blockPos, blockStack)) return;
        list.add(blockPos);
        for (EnumFacing direction : EnumFacing.values()) {
            if(face.equals(direction) || originFace.equals(direction) || originFace.getOpposite().equals(direction)) continue;
            BlockPos newPos = ItemBlockExchanger.getNextBlockPos(blockPos, direction);
            if (ItemBlockExchanger.checkBlockInRange(heldItem, newPos, origin)) {
                searchBlock(world, blockStack, newPos, origin, direction.getOpposite(), originFace, heldItem, list);
            }
        }
    }

    private boolean isValidBlock(World world, ItemStack item, BlockPos blockPos, ItemStack firstFocusBlock) {
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        if(block == Blocks.AIR) return false;
        ItemStack nowBlock = new ItemStack(block, 1, block.getMetaFromState(state));
        Block targetBlock = ItemBlockExchanger.getTargetBlock(item);
        int targetBlockMeta = ItemBlockExchanger.getTargetItemStackMeta(item);
        ItemStack targetBlockStack = new ItemStack(targetBlock, 1, targetBlockMeta);
        return !(targetBlockStack.isItemEqual(nowBlock) || !ItemBlockExchanger.isAllExchangeMode(item) && !firstFocusBlock.isItemEqual(nowBlock));
    }

    public void renderBlockListSelectionBox(List<BlockPos> list, World world, EntityPlayer player, float partialTickItem) {
        double d3 = 0.002d;
        double d0, d1, d2;
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        for (BlockPos blockPos : list) {
            d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTickItem;
            d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTickItem;
            d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTickItem;
            IBlockState state = world.getBlockState(blockPos);
            AxisAlignedBB axisAlignedBB = state.getSelectedBoundingBox(world, blockPos).expand(d3, d3, d3).offset(-d0, -d1, -d2);
            RenderGlobal.drawSelectionBoundingBox(axisAlignedBB, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
