package com.jet.grapplegun.network;

import com.jet.grapplegun.GrappleGunMod;
import com.jet.grapplegun.ItemGrapple;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class C_DestroyedPuller implements IMessage {
    public C_DestroyedPuller(){}

    private int grappleID;
    private int parentEntityID;

    public C_DestroyedPuller(ItemGrapple grapple, Entity parentEntity) {
        grappleID = Item.getIdFromItem(grapple);
        if(parentEntity != null)
            parentEntityID = parentEntity.getEntityId();
        else
            parentEntityID = 0;
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
        buf.writeInt(parentEntityID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
        parentEntityID = buf.readInt();
    }

    public static class Handler implements IMessageHandler<C_DestroyedPuller, IMessage> {
        @Override public IMessage onMessage(C_DestroyedPuller message, MessageContext ctx) {
            if(ctx.side == Side.SERVER)
                return null;

            FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler()).addScheduledTask(() -> {
                ItemGrapple grapple = null;
                Entity parentEntity = null;

                if(message.parentEntityID != 0) {
                    Entity readParentEntity = GrappleGunMod.proxy.getWorld().getEntityByID(message.parentEntityID);
                    if (readParentEntity != null)
                        parentEntity = readParentEntity;
                }

                Item readItem = Item.getItemById(message.grappleID);
                if(readItem != null && readItem instanceof ItemGrapple)
                    grapple = (ItemGrapple) readItem;

                grapple.onPullerDestroyed(parentEntity);
            });
            return null;
        }
    }
}