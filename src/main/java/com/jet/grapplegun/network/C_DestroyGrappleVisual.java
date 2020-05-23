package com.jet.grapplegun.network;

import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class C_DestroyGrappleVisual implements IMessage {
    public C_DestroyGrappleVisual(){}

    private int grappleID;
    public C_DestroyGrappleVisual(Item grappleItem) {
        grappleID = Item.getIdFromItem(grappleItem);
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
    }

    public static class DestroyGrappleVisualManager implements IMessageHandler<C_DestroyGrappleVisual, IMessage> {
        @Override public IMessage onMessage(C_DestroyGrappleVisual message, MessageContext ctx) {
            if(ctx.side == Side.SERVER)
                return null;
            FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler()).addScheduledTask(() -> {
                Item item = Item.getItemById(message.grappleID);
                if (item instanceof ItemGrappleGun) {
                    ItemGrappleGun grappleGun = (ItemGrappleGun) item;
                    if (grappleGun != null) {
                        if(grappleGun.sh_grappleVisual != null) {
                            grappleGun.sh_grappleVisual.onKillCommand();
                            grappleGun.sh_grappleVisual = null;
                        }
                    }
                }
            });
            return null;
        }
    }
}
