package com.jet.grapplegun.network;

import com.jet.grapplegun.GrappleGunMod;
import com.jet.grapplegun.ItemGrappleGun;
import com.jet.grapplegun.proxy.ServerProxy;
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

import java.util.List;

public class S_UpdateSticking implements IMessage {
    public S_UpdateSticking(){}

    private int entityID;
    public S_UpdateSticking(Entity entity) {
        entityID = entity.getEntityId();
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
    }

    public static class S_UpdateStickingManager implements IMessageHandler<S_UpdateSticking, IMessage> {
        @Override public IMessage onMessage(S_UpdateSticking message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;

            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                player.fallDistance = 0;
            });
            return null;
        }
    }
}
