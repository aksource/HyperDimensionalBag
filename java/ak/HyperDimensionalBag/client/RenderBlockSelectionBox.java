package ak.HyperDimensionalBag.client;

import ak.HyperDimensionalBag.item.ItemBlockExchanger;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

/**
 * Created by A.K. on 14/07/01.
 */
@SideOnly(Side.CLIENT)
public class RenderBlockSelectionBox {

    @SubscribeEvent
    public void onRenderSelectionBox(DrawBlockHighlightEvent event) {
        ItemStack currentItem = event.currentItem;
        if (event.target.typeOfHit == BLOCK
                && currentItem != null
                && currentItem.getItem() instanceof ItemBlockExchanger) {
            List<ChunkPosition> list = new ArrayList<>();
            MovingObjectPosition MOP = event.target;
            EntityPlayer player = event.player;
            World world = event.player.worldObj;
            Block block = world.getBlock(MOP.blockX, MOP.blockY, MOP.blockZ);
            int meta = world.getBlockMetadata(MOP.blockX, MOP.blockY, MOP.blockZ);
            ChunkPosition chunk = new ChunkPosition(MOP.blockX, MOP.blockY, MOP.blockZ);
            int face = MOP.sideHit;
            ItemStack blockStack = new ItemStack(block, 1, meta);
            ForgeDirection direction = ForgeDirection.VALID_DIRECTIONS[face];
            searchBlock(world, blockStack, chunk, chunk, direction, direction, currentItem, list);
            renderBlockListSelectionBox(list, world, player, event.partialTicks);
            event.setCanceled(true);
        }
    }

    private void searchBlock(World world, ItemStack blockStack, ChunkPosition chunkPos, ChunkPosition origin, ForgeDirection face, ForgeDirection originFace, ItemStack heldItem, List<ChunkPosition> list) {
        if(!ItemBlockExchanger.isVisibleBlock(world, ItemBlockExchanger.getNextChunkPosition(chunkPos, originFace)) || list.contains(chunkPos) || !isValidBlock(world, heldItem, chunkPos, blockStack)) return;
        list.add(chunkPos);
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            if(face.equals(direction) || originFace.equals(direction) || originFace.getOpposite().equals(direction)) continue;
            ChunkPosition newPos = ItemBlockExchanger.getNextChunkPosition(chunkPos, direction);
            if (ItemBlockExchanger.checkBlockInRange(heldItem, newPos, origin)) {
                searchBlock(world, blockStack, newPos, origin, direction.getOpposite(), originFace, heldItem, list);
            }
        }
    }

    private boolean isValidBlock(World world, ItemStack item, ChunkPosition chunk, ItemStack firstFocusBlock) {
        Block block = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
        if(block == Blocks.air) return false;
        int meta = world.getBlockMetadata(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
        ItemStack nowBlock = new ItemStack(block, 1, meta);
        Block targetBlock = ItemBlockExchanger.getTargetBlock(item);
        int targetBlockMeta = ItemBlockExchanger.getTargetBlockMeta(item);
        ItemStack targetBlockStack = new ItemStack(targetBlock, 1, targetBlockMeta);
        return !(targetBlockStack.isItemEqual(nowBlock) || !ItemBlockExchanger.isAllExchangeMode(item) && !firstFocusBlock.isItemEqual(nowBlock));
    }

    public void renderBlockListSelectionBox(List<ChunkPosition> list, World world, EntityPlayer player, float partialTickItem) {
        double d3 = 0.002d;
        double d0, d1, d2;
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        for (ChunkPosition chunk : list) {
            d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTickItem;
            d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTickItem;
            d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTickItem;
            Block block = world.getBlock(chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ);
            AxisAlignedBB axisAlignedBB = block.getSelectedBoundingBoxFromPool(world, chunk.chunkPosX, chunk.chunkPosY, chunk.chunkPosZ).expand(d3, d3, d3).getOffsetBoundingBox(-d0, -d1, -d2);
            RenderGlobal.drawOutlinedBoundingBox(axisAlignedBB, 0xFFFFFF);
        }
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
