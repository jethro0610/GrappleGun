package com.jet.grapplegun.network;

import com.jet.grapplegun.EntityGrapplePuller;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class S_StopGrapple implements IMessage {
    public S_StopGrapple(){}

    private int pullerID;
    private boolean cancelFallDamage;
    public S_StopGrapple(EntityGrapplePuller grapplePuller, boolean cancelFallDamage) {
        pullerID = grapplePuller.getEntityId();
        this.cancelFallDamage = cancelFallDamage;
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(pullerID);
        buf.writeBoolean(cancelFallDamage);
    }

    @Override public void fromBytes(ByteBuf buf) {
        pullerID = buf.readInt();
        cancelFallDamage = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<S_StopGrapple, IMessage> {
        @Override public IMessage onMessage(S_StopGrapple message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;

            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                EntityGrapplePuller grapplePuller = null;
                Entity readEntity = player.getServerWorld().getEntityByID(message.pullerID);
                if(readEntity != null && readEntity instanceof EntityGrapplePuller)
                    grapplePuller = (EntityGrapplePuller) readEntity;

                if(grapplePuller != null) {
                    if(grapplePuller.isHit() && grapplePuller.getLaunchMult() >= 1) {
                        Entity pullEntity = grapplePuller.getPullEntity();
                        if(pullEntity != null){
                            pullEntity.motionX = 0;
                            pullEntity.motionY = 0;
                            pullEntity.motionZ = 0;
                            pullEntity.velocityChanged = true;
                        }
                        grapplePuller.onKillCommand();
                        if (message.cancelFallDamage)
                            player.fallDistance = 0;
                    }
                }
            });
            return null;
        }
    }
}