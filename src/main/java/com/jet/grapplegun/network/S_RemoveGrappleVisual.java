package com.jet.grapplegun.network;

import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class S_RemoveGrappleVisual implements IMessage {
    public S_RemoveGrappleVisual(){}

    private int grappleID;
    public S_RemoveGrappleVisual(Item grappleItem) {
        grappleID = Item.getIdFromItem(grappleItem);
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
    }

    public static class RemoveGrappleVisualManager implements IMessageHandler<S_RemoveGrappleVisual, IMessage> {
        @Override public IMessage onMessage(S_RemoveGrappleVisual message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                Item item = Item.getItemById(message.grappleID);
                if (item instanceof ItemGrappleGun) {
                    List<EntityPlayer> players = ctx.getServerHandler().player.getServerWorld().playerEntities;
                    for ( EntityPlayer player : players) {
                        if(player != ctx.getServerHandler().player)
                            GrapplePacketManager.INSTANCE.sendTo(new C_DestroyGrappleVisual((ItemGrappleGun) item), (EntityPlayerMP)player);
                    }
                }
            });
            return null;
        }
    }
}
