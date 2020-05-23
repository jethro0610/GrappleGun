package com.jet.grapplegun.network;

import com.jet.grapplegun.EntityGrappleVisual;
import com.jet.grapplegun.GrappleGunMod;
import com.jet.grapplegun.ItemGrappleGun;
import com.jet.grapplegun.ParamsGrappleVisual;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;


public class C_SpawnGrappleVisual implements IMessage {
    public C_SpawnGrappleVisual(){}

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

    public C_SpawnGrappleVisual(int grappleID, int owningEntityID, int attachEntityID, Vec3d attachPoint, float pitch, float yaw, boolean hit, float range, double launchSpeed) {
        this.grappleID = grappleID;
        this.owningEntityID = owningEntityID;
        this.attachEntityID = attachEntityID;
        this.attachPointX = attachPoint.x;
        this.attachPointY = attachPoint.y;
        this.attachPointZ = attachPoint.z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.hit = hit;
        this.range = range;
        this.launchSpeed = launchSpeed;
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

    public static class SpawnGrappleVisualManager implements IMessageHandler<C_SpawnGrappleVisual, IMessage> {
        @Override public IMessage onMessage(C_SpawnGrappleVisual message, MessageContext ctx) {
            if(ctx.side == Side.SERVER)
                return null;
            FMLCommonHandler.instance().getWorldThread(ctx.getClientHandler()).addScheduledTask(() -> {
                //GrappleGunMod.proxy.getPlayer().sendMessage(new TextComponentString("Spawn Visual"));
                Item item = Item.getItemById(message.grappleID);
                Entity owningEntity = GrappleGunMod.proxy.getWorld().getEntityByID(message.owningEntityID);

                Entity attachEntity = null;
                if(message.attachEntityID != 0)
                    attachEntity = GrappleGunMod.proxy.getWorld().getEntityByID(message.attachEntityID);
                Vec3d attachPoint = new Vec3d(message.attachPointX, message.attachPointY, message.attachPointZ);

                GrappleGunMod.proxy.getPlayer().sendMessage(new TextComponentString(attachPoint.toString()));

                if (item instanceof ItemGrappleGun && owningEntity != null){
                    ItemGrappleGun grappleGun = (ItemGrappleGun)item;
                    EntityGrappleVisual newVisual = null;

                    ParamsGrappleVisual params = new ParamsGrappleVisual();
                    params.worldIn = GrappleGunMod.proxy.getWorld();
                    params.pos = attachPoint;
                    params.attachEntity = attachEntity;
                    params.owningEntity = owningEntity;
                    params.pitch = message.pitch;
                    params.hit = message.hit;
                    params.pullRange = message.range;
                    params.launchSpeed = message.launchSpeed;

                    newVisual = new EntityGrappleVisual(params);

                    if(newVisual != null && owningEntity != GrappleGunMod.proxy.getPlayer()){
                        GrappleGunMod.proxy.getWorld().spawnEntity(newVisual);
                        newVisual.sh_colorR = grappleGun.sh_ropeColorR;
                        newVisual.sh_colorG = grappleGun.sh_ropeColorG;
                        newVisual.sh_colorB = grappleGun.sh_ropeColorB;
                        newVisual.sh_colorAdd = grappleGun.sh_ropeColorAdd;
                        grappleGun.sh_grappleVisual = newVisual;
                    }
                }
            });
            return null;
        }
    }
}
