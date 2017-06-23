package ak.HyperDimensionalBag.network;

import ak.HyperDimensionalBag.item.ItemBlockExchanger;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressedHandler implements IMessageHandler<MessageKeyPressed, IMessage> {
    @Override
    public IMessage onMessage(MessageKeyPressed message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().player;
        ItemStack itemStack = player.getHeldItemMainhand();
        if (itemStack != ItemStack.EMPTY && itemStack.getItem() instanceof ItemBlockExchanger) {
            ItemBlockExchanger.onRightClickAction(itemStack, player, message.keyCtrlDwon);
        }
        return null;
    }
}
