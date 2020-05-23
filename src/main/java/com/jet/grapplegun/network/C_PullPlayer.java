package com.jet.grapplegun.network;

import com.jet.grapplegun.GrappleGunMod;
import com.jet.grapplegun.ItemGrappleGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;


public class C_PullPlayer implements IMessage {
    public C_PullPlayer(){}

    private int grappleID;
    private int pullToPlayerID;
    public C_PullPlayer(Item grappleItem, EntityPlayer pullToPlayer) {
        grappleID = Item.getIdFromItem(grappleItem);
        pullToPlayerID = pullToPlayer.getEntityId();
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
        buf.writeInt(pullToPlayerID);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
        pullToPlayerID = buf.readInt();
    }

    public static class PullPlayerManager implements IMessageHandler<C_PullPlayer, IMessage> {
        @Override public IMessage onMessage(C_PullPlayer message, MessageContext ctx) {
            if(ctx.side == Side.SERVER)
                return null;

            FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler()).addScheduledTask(() -> {
                GrappleGunMod.proxy.getPlayer().sendMessage(new TextComponentString("Pulled"));
                Item item = Item.getItemById(message.grappleID);
                Entity entity = GrappleGunMod.proxy.getWorld().getEntityByID(message.pullToPlayerID);
                if (item instanceof ItemGrappleGun && entity instanceof EntityPlayer) {
                    ItemGrappleGun clientGrappleGun = (ItemGrappleGun) item;
                    EntityPlayer clientPullToPlayer = (EntityPlayer) entity;
                    clientGrappleGun.rc_pullPlayer = GrappleGunMod.proxy.getPlayer();
                    clientGrappleGun.rc_pullToPlayer = clientPullToPlayer;
                }
            });
            return null;
        }
    }
}
