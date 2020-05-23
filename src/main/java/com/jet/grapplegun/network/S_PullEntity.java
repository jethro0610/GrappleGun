package com.jet.grapplegun.network;

import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;


public class S_PullEntity implements IMessage {
    public S_PullEntity(){}

    private int entityID;
    private int grappleID;
    public S_PullEntity(Entity pullEntity, Item grappleItem) {
        entityID = pullEntity.getEntityId();
        grappleID = Item.getIdFromItem(grappleItem);
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
        buf.writeInt(grappleID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        grappleID = buf.readInt();
    }

    public static class PullEntityManager implements IMessageHandler<S_PullEntity, IMessage> {
        @Override public IMessage onMessage(S_PullEntity message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;

            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

            serverPlayer.getServerWorld().addScheduledTask(() -> {
                Entity pullEntity = serverPlayer.getServerWorld().getEntityByID(message.entityID);

                Item item = Item.getItemById(message.grappleID);
                if (item instanceof ItemGrappleGun){
                    ItemGrappleGun serverGrappleGun = (ItemGrappleGun) item;
                    if(pullEntity instanceof EntityPlayer) {
                        GrapplePacketManager.INSTANCE.sendTo(new C_PullPlayer(serverGrappleGun, serverPlayer), (EntityPlayerMP)pullEntity);
                    }
                    else {
                        serverGrappleGun.s_pullEntity = pullEntity;
                    }
                }
            });
            return null;
        }
    }
}
