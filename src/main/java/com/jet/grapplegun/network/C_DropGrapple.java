package com.jet.grapplegun.network;

import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class C_DropGrapple implements IMessage {
    public C_DropGrapple(){}

    private int grappleID;
    public C_DropGrapple(Item grappleItem) {
        grappleID = Item.getIdFromItem(grappleItem);
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
    }

    public static class DropGrappleManager implements IMessageHandler<C_DropGrapple, IMessage> {
        @Override public IMessage onMessage(C_DropGrapple message, MessageContext ctx) {
            if(ctx.side == Side.SERVER)
                return null;
            FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler()).addScheduledTask(() -> {
                Item heldItem = Item.getItemById(message.grappleID);
                if (heldItem instanceof ItemGrappleGun){
                    ItemGrappleGun clientGrappleGun = (ItemGrappleGun) heldItem;
                    clientGrappleGun.holderReset();
                }
            });
            return null;
        }
    }
}
