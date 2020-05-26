package com.jet.grapplegun.network;

import com.jet.grapplegun.EntityGrapplePuller;
import com.jet.grapplegun.SoundHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class S_RequestPull implements IMessage {
    public S_RequestPull(){}

    private int handInt;
    private int parentEntityID;
    private Vec3d pullLocation;
    private int pullEntityID;
    private boolean hit;

    public S_RequestPull(EnumHand grappleHand, Entity parentEntity, Vec3d pullLocation, Entity pullEntity, boolean hit) {
        if(grappleHand == EnumHand.MAIN_HAND)
            handInt = 0;
        else
            handInt = 1;

        parentEntityID = parentEntity.getEntityId();
        this.pullLocation = pullLocation;
        if(pullEntity != null)
            pullEntityID = pullEntity.getEntityId();
        else
            pullEntityID = 0;
        this.hit = hit;
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(handInt);
        buf.writeInt(parentEntityID);
        buf.writeDouble(pullLocation.x);
        buf.writeDouble(pullLocation.y);
        buf.writeDouble(pullLocation.z);
        buf.writeInt(pullEntityID);
        buf.writeBoolean(hit);
    }

    @Override public void fromBytes(ByteBuf buf) {
        handInt = buf.readInt();
        parentEntityID = buf.readInt();

        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        pullLocation = new Vec3d(x, y, z);

        pullEntityID = buf.readInt();
        hit = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<S_RequestPull, IMessage> {
        @Override public IMessage onMessage(S_RequestPull message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;

            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {
                EntityPlayer parentEntity = null;
                ItemStack parentStack = null;
                Entity pullEntity = null;

                EnumHand grappleHand;
                if(message.handInt == 0)
                    grappleHand = EnumHand.MAIN_HAND;
                else
                    grappleHand = EnumHand.OFF_HAND;

                Entity readParentEntity = player.getServerWorld().getEntityByID(message.parentEntityID);
                if(readParentEntity != null && readParentEntity instanceof EntityPlayer)
                    parentEntity = (EntityPlayer) readParentEntity;

                parentStack = parentEntity.getHeldItem(grappleHand);

                if(message.pullEntityID != 0) {
                    Entity readPullEntity = player.getServerWorld().getEntityByID(message.pullEntityID);
                    if (readPullEntity != null)
                        pullEntity = readPullEntity;
                }

                boolean canGrapple = true;
                ItemStack mainItem = parentEntity.getHeldItemMainhand();
                ItemStack offItem = parentEntity.getHeldItemOffhand();

                if(mainItem.hasTagCompound()){
                    NBTTagCompound nbt = mainItem.getTagCompound();
                    if(nbt.hasKey("PullerID")){
                        if(nbt.getInteger("PullerID") != -1)
                            canGrapple = false;
                    }
                }

                if(offItem.hasTagCompound()){
                    NBTTagCompound nbt = offItem.getTagCompound();
                    if(nbt.hasKey("PullerID")){
                        if(nbt.getInteger("PullerID") != -1)
                            canGrapple = false;
                    }
                }
                
                if(parentStack != null && parentEntity != null && canGrapple && parentStack.getItemDamage() < parentStack.getMaxDamage()) {
                    parentStack.damageItem(1, player);
                    player.getServerWorld().playSound(null, player.posX, player.posY, player.posZ, SoundHandler.GRAPPLE_FIRE, SoundCategory.MASTER, 1.0f, 1.0f);
                    EntityGrapplePuller newPuller = new EntityGrapplePuller(player.getServerWorld(), grappleHand, parentEntity, message.pullLocation, pullEntity, message.hit);
                    player.getServerWorld().spawnEntity(newPuller);
                }
                else if(parentStack.getItemDamage() >= parentStack.getMaxDamage()){
                    parentStack.damageItem(1, player);
                }
            });
            return null;
        }
    }
}
