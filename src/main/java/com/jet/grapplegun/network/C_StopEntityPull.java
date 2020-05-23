package com.jet.grapplegun.network;

import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class C_StopEntityPull implements IMessage {
    public C_StopEntityPull(){}

    private int grappleID;
    public C_StopEntityPull(Item grappleItem) {
        grappleID = Item.getIdFromItem(grappleItem);
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
    }

    public static class StopEntityPullManager implements IMessageHandler<C_StopEntityPull, IMessage> {
        @Override public IMessage onMessage(C_StopEntityPull message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler()).addScheduledTask(() -> {
                Item item = Item.getItemById(message.grappleID);
                if (item instanceof ItemGrappleGun) {
                    ItemGrappleGun clientGrappleGun = (ItemGrappleGun) item;
                    clientGrappleGun.h_pullEntity = null;
                }
            });
            return null;
        }
    }
}
