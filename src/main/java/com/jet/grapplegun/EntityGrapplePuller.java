package com.jet.grapplegun;

import com.jet.grapplegun.network.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.lwjgl.input.Keyboard;

public class EntityGrapplePuller extends Entity implements IEntityAdditionalSpawnData {
    private ItemGrapple sh_parentGrapple;
    private Entity sh_parentEntity;
    private Vec3d sh_pullLocation;
    private Entity sh_pullEntity;
    private boolean sh_hit;

    private boolean p_pullParent;
    private boolean p_sticking;
    private double p_stickHeight;

    public EntityGrapplePuller(World world) { super(world); }

    public EntityGrapplePuller(World world, ItemGrapple parentGrapple, Entity parentEntity, Vec3d pullLocation, Entity pullEntity, boolean hit) {
        super(world);
        sh_parentEntity = parentEntity;
        sh_parentGrapple = parentGrapple;
        sh_pullLocation = pullLocation;
        sh_pullEntity = pullEntity;
        sh_hit = hit;
        setPositionAndUpdate(parentEntity.posX, parentEntity.posY, parentEntity.posZ);
    }

    @Override
    public void onKillCommand() {
        super.onKillCommand();
        sh_parentGrapple.setChildPuller(null);
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        buf.writeInt(Item.getIdFromItem(sh_parentGrapple));
        buf.writeInt(sh_parentEntity.getEntityId());
        buf.writeDouble(sh_pullLocation.x);
        buf.writeDouble(sh_pullLocation.y);
        buf.writeDouble(sh_pullLocation.z);
        if(sh_pullEntity != null)
            buf.writeInt(sh_pullEntity.getEntityId());
        else
            buf.writeInt(0);
        buf.writeBoolean(sh_hit);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        Item readItem = Item.getItemById(buf.readInt());
        if(readItem != null && readItem instanceof ItemGrapple)
            sh_parentGrapple = (ItemGrapple) readItem;
        sh_parentGrapple.setChildPuller(this);

        Entity readParentEntity = getEntityWorld().getEntityByID(buf.readInt());
        if(readParentEntity != null)
            sh_parentEntity = readParentEntity;

        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        sh_pullLocation = new Vec3d(x, y, z);

        int pullEntityID = buf.readInt();
        if(pullEntityID != 0) {
            Entity readPullEntity = getEntityWorld().getEntityByID(buf.readInt());
            if (readPullEntity != null)
                sh_pullEntity = readPullEntity;
        }

        sh_hit = buf.readBoolean();

        if(sh_parentEntity == GrappleGunMod.proxy.getPlayer() && sh_pullEntity == null)
            p_pullParent = true;
    }

    @Override
    public void onEntityUpdate() {
        if(!world.isRemote)
            serverUpdate();
        else {
            clientUpdate();
            if(sh_parentEntity == GrappleGunMod.proxy.getPlayer())
                parentUpdate();
            if(sh_pullEntity == GrappleGunMod.proxy.getPlayer())
                pulledPlayerUpdate();
        }
    }

    private void serverUpdate(){
        if(sh_parentEntity != null)
            setPositionAndUpdate(sh_parentEntity.posX, sh_parentEntity.posY, sh_parentEntity.posZ);
        else {
            onKillCommand();
            return;
        }

        if(sh_pullEntity != null) {
            if(!(sh_pullEntity instanceof EntityPlayer)) {
                Vec3d pullVel = getPullVel(sh_parentEntity.getPositionEyes(1), sh_pullEntity.getPositionVector(), sh_parentGrapple.getPullSpeed());
                sh_pullEntity.motionX = pullVel.x;
                sh_pullEntity.motionY = pullVel.y;
                sh_pullEntity.motionZ = pullVel.z;
                sh_pullEntity.velocityChanged = true;
            }

            if(sh_pullEntity.getPositionVector().distanceTo(sh_parentEntity.getPositionEyes(1)) < sh_parentGrapple.getPullSpeed()) {
                if(!(sh_pullEntity instanceof EntityPlayer)) {
                    sh_pullEntity.motionX = 0;
                    sh_pullEntity.motionY = 0;
                    sh_pullEntity.motionZ = 0;
                    sh_pullEntity.velocityChanged = true;
                }

                sh_pullEntity = null;
                onKillCommand();
            }
        }
    }

    private void clientUpdate(){
        if(sh_parentEntity != null)
            setPosition(sh_parentEntity.posX, sh_parentEntity.posY, sh_parentEntity.posZ);
    }

    private void parentUpdate() {
        if(p_pullParent) {
            Vec3d pullVel = getPullVel(sh_pullLocation, sh_parentEntity.getPositionVector(), sh_parentGrapple.getPullSpeed());
            sh_parentEntity.setVelocity(pullVel.x, pullVel.y, pullVel.z);

            // Stop pulling the player
            if(sh_parentEntity.getPositionVector().distanceTo(sh_pullLocation) < sh_parentGrapple.getPullSpeed()) {
                p_pullParent = false;
                if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !entityIsCloseToGround(sh_parentEntity, sh_parentGrapple.getPullSpeed() + 0.5, pullVel.y))
                    p_sticking = true;
                else
                    GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this));
            }
        }

        if(p_sticking) {
            Vec3d pullVel = sh_pullLocation.addVector(0, p_stickHeight, 0).subtract(sh_parentEntity.getPositionVector());
            pullVel.scale(0.5);
            sh_parentEntity.setVelocity(pullVel.x,pullVel.y,pullVel.z);
            sh_parentEntity.setNoGravity(true);

            // Move up and down the grapple
            if (sh_parentEntity.isSneaking())
                p_stickHeight -= 0.1;
            if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
                p_stickHeight += 0.1;

            // Hop off the grapple
            if(p_stickHeight > 0.2) {
                GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this));
                sh_parentEntity.setNoGravity(false);
                sh_parentEntity.setVelocity(0, 0.6,0);
                p_sticking = false;
                p_stickHeight = 0;
            }
            // Hop down the grapple
            if (entityIsCloseToGround(sh_parentEntity, 1, 0 )) {
                GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this));
                sh_parentEntity.setVelocity(0, 0, 0);
                sh_parentEntity.setNoGravity(false);
                p_sticking = false;
                p_stickHeight = 0;
            }
        }
    }

    private void pulledPlayerUpdate(){
        if(sh_pullEntity != null) {
            Vec3d pullVel = getPullVel(sh_parentEntity.getPositionEyes(1), sh_pullEntity.getPositionVector(), sh_parentGrapple.getPullSpeed());
            sh_pullEntity.setVelocity(pullVel.x, pullVel.y, pullVel.z);
        }
    }

    Vec3d getPullVel(Vec3d to, Vec3d from, double speed) {
        Vec3d vel = to.subtract(from);
        return vel.scale(1/vel.lengthVector()).scale(speed);
    }

    boolean entityIsCloseToGround(Entity entityIn, double distance, double velocity) {
        if(velocity > 0.2)
            return false;

        RayTraceResult rayResult = GrappleGunMod.proxy.getWorld().rayTraceBlocks(entityIn.getPositionVector(), entityIn.getPositionVector().addVector(0, -distance, 0));
        if(rayResult == null)
            return false;
        else
            return true;
    }

    @Override
    protected void entityInit() {
        GrappleGunMod.proxy.getPlayer().sendMessage(new TextComponentString("Spawned grapple"));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }
}
