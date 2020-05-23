package com.jet.grapplegun.network;

import com.jet.grapplegun.ParamsGrappleVisual;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;


public class S_CreateGrappleVisual implements IMessage {
    public S_CreateGrappleVisual(){}

    private int grappleID;
    private int owningEntityID;
    private int attachEntityID = 0;
    private double attachPointX;
    private double attachPointY;
    private double attachPointZ;
    float pitch;
    float yaw;
    boolean hit;
    float range;
    double launchSpeed;

    public S_CreateGrappleVisual(Item grappleItem, ParamsGrappleVisual params) {
        grappleID = Item.getIdFromItem(grappleItem);
        owningEntityID = params.owningEntity.getEntityId();
        if(params.attachEntity != null)
            attachEntityID = params.attachEntity.getEntityId();
        attachPointX = params.pos.x;
        attachPointY = params.pos.y;
        attachPointZ = params.pos.z;
        pitch = params.pitch;
        yaw = params.yaw;
        hit = params.hit;
        range = params.pullRange;
        launchSpeed = params.launchSpeed;
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(grappleID);
        buf.writeInt(owningEntityID);
        buf.writeInt(attachEntityID);
        buf.writeDouble(attachPointX);
        buf.writeDouble(attachPointY);
        buf.writeDouble(attachPointZ);
        buf.writeFloat(pitch);
        buf.writeFloat(yaw);
        buf.writeBoolean(hit);
        buf.writeFloat(range);
        buf.writeDouble(launchSpeed);
    }

    @Override public void fromBytes(ByteBuf buf) {
        grappleID = buf.readInt();
        owningEntityID = buf.readInt();
        attachEntityID = buf.readInt();
        attachPointX = buf.readDouble();
        attachPointY = buf.readDouble();
        attachPointZ = buf.readDouble();
        pitch = buf.readFloat();
        yaw = buf.readFloat();
        hit = buf.readBoolean();
        range = buf.readFloat();
        launchSpeed = buf.readDouble();
    }

    public static class CreateGrappleVisualManager implements IMessageHandler<S_CreateGrappleVisual, IMessage> {
        @Override public IMessage onMessage(S_CreateGrappleVisual message, MessageContext ctx) {
            if(ctx.side == Side.CLIENT)
                return null;
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
            serverPlayer.getServerWorld().addScheduledTask(() -> {
                Vec3d attachPoint = new Vec3d(message.attachPointX, message.attachPointY, message.attachPointZ);
                C_SpawnGrappleVisual spawnMessage = new C_SpawnGrappleVisual(message.grappleID,
                        message.owningEntityID,
                        message.attachEntityID,
                        attachPoint, message.pitch,
                        message.yaw,
                        message.hit,
                        message.range,
                        message.launchSpeed);
                NetworkRegistry.TargetPoint target = new NetworkRegistry.TargetPoint(0, attachPoint.x, attachPoint.y, attachPoint.z, 128);
                GrapplePacketManager.INSTANCE.sendToAll(spawnMessage);
            });
            return null;
        }
    }
}

