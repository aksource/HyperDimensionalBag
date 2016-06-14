package ak.HyperDimensionalBag.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageKeyPressed implements IMessage {
    public boolean keyCtrlDwon;
    public MessageKeyPressed(){}

    public MessageKeyPressed(boolean pressed) {
        this.keyCtrlDwon = pressed;
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        this.keyCtrlDwon = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.keyCtrlDwon);
    }
}
