package com.jet.grapplegun.network;

import com.jet.grapplegun.EntityGrapplePuller;
import com.jet.grapplegun.ItemGrapple;
import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class S_RequestPull implements IMessage {
    public S_RequestPull(){}

    private int grappleID;
    private int parentEntityID;
    private Vec3d pullLocation;
    private boolean hit;

    public S_RequestPull(Item itemGrapple, Entity parentEntity, Vec3d pullLocation, boolean hit) {
        grappleID = Item.getIdFromItem(itemGrapple);
        parentEntityID = parentEntity.getEntityId();
        this.pullLocation = pullLocation;
        this.hit = hit;
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
        buf.writeInt(parentEntityID);
        buf.writeDouble(pullLocation.x);
        buf.writeDouble(pullLocation.y);
        buf.writeDouble(pullLocation.z);
        buf.writeBoolean(hit);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
        parentEntityID = buf.readInt();

        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        pullLocation = new Vec3d(x, y, z);

        hit = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<S_RequestPull, IMessage> {
        @Override public IMessage onMessage(S_RequestPull message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;

            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                ItemGrapple parentGrapple = null;
                Entity parentEntity = null;
                Item readItem = Item.getItemById(message.grappleID);

                if(readItem != null && readItem instanceof ItemGrapple)
                    parentGrapple = (ItemGrapple) readItem;

                Entity readEntity = player.getServerWorld().getEntityByID(message.parentEntityID);
                if(readEntity != null)
                    parentEntity = (Entity) readEntity;

                if(parentGrapple != null && parentEntity != null) {
                    //player.sendMessage(new TextComponentString("Got grapple request"));
                    EntityGrapplePuller newPuller = new EntityGrapplePuller(player.getServerWorld(), parentGrapple, parentEntity, message.pullLocation, message.hit);
                    player.getServerWorld().spawnEntity(newPuller);
                }
            });
            return null;
        }
    }
}
