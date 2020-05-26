package com.jet.grapplegun;

import com.jet.grapplegun.network.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.lwjgl.input.Keyboard;

public class EntityGrapplePuller extends Entity implements IEntityAdditionalSpawnData {
    private EnumHand sh_grappleHand;
    private ItemStack sh_parentStack;
    private EntityPlayer sh_parentEntity;
    private Vec3d sh_pullLocation;
    private Entity sh_pullEntity;

    private boolean sh_hit;
    private double sh_launchTime;
    private double sh_curLaunchTime;
    private double sh_lastLaunchTime;
    private LaunchState sh_launchState;

    private boolean p_pullParent;
    private boolean p_sticking;
    private double p_stickHeight;
    private boolean p_cancelledLastTick = false;

    private double c_pitch;
    private double c_lastPitch;
    private double c_yaw;
    private double c_lastYaw;

    public EntityGrapplePuller(World world) { super(world); }

    public EntityGrapplePuller(World world, EnumHand grappleHand, EntityPlayer parentEntity, Vec3d pullLocation, Entity pullEntity, boolean hit) {
        super(world);
        sh_parentEntity = parentEntity;
        sh_grappleHand = grappleHand;
        sh_parentStack = parentEntity.getHeldItem(grappleHand);
        sh_pullLocation = pullLocation;
        sh_pullEntity = pullEntity;
        sh_hit = hit;
        setPositionAndUpdate(parentEntity.posX, parentEntity.posY, parentEntity.posZ);

        if(sh_pullEntity == null)
            sh_launchTime = sh_parentEntity.getPositionVector().distanceTo(sh_pullLocation) / getParentGrapple().getRange();
        else
            sh_launchTime = sh_parentEntity.getPositionVector().distanceTo(sh_pullEntity.getPositionVector()) / getParentGrapple().getRange();
        sh_launchTime *= getParentGrapple().getLaunchTime();

        sh_curLaunchTime = sh_launchTime;
        sh_lastLaunchTime = sh_curLaunchTime;
        sh_launchState = LaunchState.LAUNCHING;

        NBTTagCompound parentNBT;
        if(sh_parentStack.hasTagCompound())
            parentNBT = sh_parentStack.getTagCompound();
        else
            parentNBT = new NBTTagCompound();

        parentNBT.setInteger("PullerID", getEntityId());
        sh_parentStack.setTagCompound(parentNBT);
    }

    @Override
    public void onKillCommand() {
        super.onKillCommand();

        if(sh_parentStack != null) {
            NBTTagCompound parentNBT;
            if(sh_parentStack.hasTagCompound())
                parentNBT = sh_parentStack.getTagCompound();
            else
                parentNBT = new NBTTagCompound();

            parentNBT.setInteger("PullerID", -1);
            sh_parentStack.setTagCompound(parentNBT);
        }
        if(sh_parentEntity != null) {
            if(sh_parentEntity instanceof EntityPlayerMP){
                EntityPlayerMP player = (EntityPlayerMP) sh_parentEntity;
                player.capabilities.allowFlying = false;
            }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        if(sh_grappleHand == EnumHand.MAIN_HAND)
            buf.writeInt(0);
        else
            buf.writeInt(1);

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
        int handInt = buf.readInt();
        if(handInt == 0)
            sh_grappleHand = EnumHand.MAIN_HAND;
        else
            sh_grappleHand = EnumHand.OFF_HAND;

        Entity readParentEntity = getEntityWorld().getEntityByID(buf.readInt());
        if(readParentEntity != null)
            sh_parentEntity = (EntityPlayer) readParentEntity;

        sh_parentStack = sh_parentEntity.getHeldItem(sh_grappleHand);

        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        sh_pullLocation = new Vec3d(x, y, z);

        int pullEntityID = buf.readInt();
        if(pullEntityID != 0) {
            Entity readPullEntity = getEntityWorld().getEntityByID(pullEntityID);
            if (readPullEntity != null)
                sh_pullEntity = readPullEntity;
        }

        sh_hit = buf.readBoolean();

        if(sh_parentEntity == GrappleGunMod.proxy.getPlayer() && sh_pullEntity == null)
            p_pullParent = true;

        if(sh_pullEntity == null)
            recalculatePitchYaw(sh_pullLocation);
        else
            recalculatePitchYaw(sh_pullEntity.getPositionVector());

        if(sh_pullEntity == null)
            sh_launchTime = sh_parentEntity.getPositionVector().distanceTo(sh_pullLocation) / getParentGrapple().getRange();
        else
            sh_launchTime = sh_parentEntity.getPositionVector().distanceTo(sh_pullEntity.getPositionVector()) / getParentGrapple().getRange();
        sh_launchTime *= getParentGrapple().getLaunchTime();

        sh_curLaunchTime = sh_launchTime;
        sh_lastLaunchTime = sh_curLaunchTime;
        sh_launchState = LaunchState.LAUNCHING;
    }

    @Override
    public void onEntityUpdate() {
        if(sh_launchState == LaunchState.LAUNCHING) {
            sh_lastLaunchTime = sh_curLaunchTime--;
            if (sh_curLaunchTime < 0) {
                if(sh_hit)
                    sh_launchState = LaunchState.NONE;
                else
                    sh_launchState = LaunchState.RETURNING;
            }
        }

        if(sh_launchState == LaunchState.RETURNING)
            sh_lastLaunchTime = sh_curLaunchTime++;

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

    private void serverUpdate() {
        if(sh_launchState == LaunchState.RETURNING && sh_curLaunchTime > sh_launchTime) {
            onKillCommand();
            return;
        }
        if(sh_parentEntity != null) {
            setPositionAndUpdate(sh_parentEntity.posX, sh_parentEntity.posY, sh_parentEntity.posZ);
            if(sh_parentEntity instanceof EntityPlayerMP){
                EntityPlayerMP player = (EntityPlayerMP) sh_parentEntity;
                player.capabilities.allowFlying = true;
            }

            if(sh_parentEntity.getHeldItemMainhand() != sh_parentStack && sh_parentEntity.getHeldItemOffhand()!= sh_parentStack){
                onKillCommand();
            }
        }
        else {
            onKillCommand();
            return;
        }

        if(sh_pullEntity != null && sh_launchState == LaunchState.NONE) {
            if(!(sh_pullEntity instanceof EntityPlayer)) {
                Vec3d pullVel = getPullVel(sh_parentEntity.getPositionEyes(1), sh_pullEntity.getPositionVector(), getParentGrapple().getPullSpeed());
                sh_pullEntity.motionX = pullVel.x;
                sh_pullEntity.motionY = pullVel.y;
                sh_pullEntity.motionZ = pullVel.z;
                sh_pullEntity.velocityChanged = true;
            }

            if(sh_pullEntity.getPositionVector().distanceTo(sh_parentEntity.getPositionEyes(1)) < 2) {
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
        if(sh_parentEntity != null) {
            setPosition(sh_parentEntity.posX, sh_parentEntity.posY, sh_parentEntity.posZ);

            if(sh_pullEntity != null && sh_launchState == LaunchState.NONE) {
                recalculatePitchYaw(sh_pullEntity.getPositionVector());
            }

            if(sh_pullEntity == null && sh_launchState == LaunchState.RETURNING) {
                recalculatePitchYaw(sh_pullLocation);
            }
        }
    }

    private void parentUpdate() {
        if(Keyboard.isKeyDown(Keyboard.KEY_R)){
            if(!p_cancelledLastTick)
                GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this, p_sticking));

            p_cancelledLastTick = true;
        }
        else
            p_cancelledLastTick = false;

        if(p_pullParent && sh_launchState == LaunchState.NONE) {
            Vec3d pullVel = getPullVel(getOffsetPullLocation(), sh_parentEntity.getPositionVector(), getParentGrapple().getPullSpeed());
            sh_parentEntity.setVelocity(pullVel.x, pullVel.y, pullVel.z);

            // Stop pulling the player
            if(sh_parentEntity.getPositionVector().distanceTo(sh_pullLocation) < (getMinPullSpeed(1.25) * 2)) {
                p_pullParent = false;
                if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !entityIsCloseToGround(sh_parentEntity, getParentGrapple().getPullSpeed() + 0.5, pullVel.y)) {
                    p_sticking = true;
                }
                else {
                    GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this, false));
                }
            }
        }

        if(p_sticking && sh_launchState == LaunchState.NONE) {
            Vec3d pullVel = getOffsetPullLocation().addVector(0, p_stickHeight, 0).subtract(sh_parentEntity.getPositionVector());
            pullVel.scale(0.5);
            sh_parentEntity.setVelocity(pullVel.x,pullVel.y,pullVel.z);

            // Move up and down the grapple
            if (sh_parentEntity.isSneaking())
                p_stickHeight -= 0.1;
            if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
                p_stickHeight += 0.1;

            // Hop off the grapple
            if(p_stickHeight > 0.2) {
                GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this, true));
                sh_parentEntity.fallDistance = 0;
                sh_parentEntity.setVelocity(0, 0.6,0);
                p_sticking = false;
                p_stickHeight = 0;
            }
            // Hop down the grapple
            if (entityIsCloseToGround(sh_parentEntity, 1, 0 )) {
                GrapplePacketManager.INSTANCE.sendToServer(new S_StopGrapple(this, true));
                sh_parentEntity.fallDistance = 0;
                sh_parentEntity.setVelocity(0, 0, 0);
                p_sticking = false;
                p_stickHeight = 0;
            }
        }
    }

    private void recalculatePitchYaw(Vec3d calculateLoc){
        Vec3d vectorToLocation = calculateLoc.subtract(sh_parentEntity.getPositionEyes(1));

        vectorToLocation = vectorToLocation.scale(1/vectorToLocation.lengthVector());
        Vec3d vecXZ = new Vec3d(vectorToLocation.x, 0, vectorToLocation.z);
        c_pitch = (Math.PI / 2) + Math.atan2(vectorToLocation.y, vecXZ.lengthVector());
        c_lastPitch = c_pitch;
        c_yaw = (1.5 * Math.PI) + Math.atan2(vectorToLocation.x, vectorToLocation.z);
        c_lastYaw = c_yaw;
    }

    private void pulledPlayerUpdate(){
        if(sh_pullEntity != null && sh_pullEntity == Minecraft.getMinecraft().player && sh_launchState == LaunchState.NONE) {
            if(sh_pullEntity.getPositionVector().distanceTo(sh_parentEntity.getPositionEyes(1)) < getParentGrapple().getPullSpeed()) {
                sh_pullEntity.setVelocity(0, 0, 0);
            }
            else {
                Vec3d pullVel = getPullVel(sh_parentEntity.getPositionEyes(1), sh_pullEntity.getPositionVector(), getParentGrapple().getPullSpeed());
                sh_pullEntity.setVelocity(pullVel.x, pullVel.y, pullVel.z);
            }
        }
    }

    public ItemGrapple getParentGrapple(){
        if(sh_parentStack.getItem() instanceof ItemGrapple)
            return (ItemGrapple) sh_parentStack.getItem();
        else
            return null;
    }

    public Entity getPullEntity() {
        return sh_pullEntity;
    }

    public Entity getParentEntity() {
        return sh_parentEntity;
    }

    private double getMinPullSpeed(double clamp) {
        if(getParentGrapple().getPullSpeed() > clamp)
            return getParentGrapple().getPullSpeed();
        else
            return clamp;
    }

    public Vec3d getPullLocation() {
        if(sh_pullEntity == null)
            return sh_pullLocation;
        else
            return sh_pullEntity.getPositionVector();
    }

    public Vec3d getRenderPullLocation(float partialTicks) {
        if(sh_pullEntity == null) {
            return sh_pullLocation;
        }
        else {
            Vec3d prevPos = new Vec3d(sh_pullEntity.prevPosX, sh_pullEntity.prevPosY, sh_pullEntity.prevPosZ);
            return getRenderVec(prevPos, sh_pullEntity.getPositionVector(), partialTicks).addVector(0, sh_pullEntity.getEyeHeight()/2, 0 );
        }
    }

    public Vec3d getRenderPosition(float partialTicks){
        Vec3d prevPos = new Vec3d(sh_parentEntity.prevPosX, sh_parentEntity.prevPosY, sh_parentEntity.prevPosZ);
        return getRenderVec(prevPos, sh_parentEntity.getPositionVector(), partialTicks);
    }

    public double getPitch() {
        return c_pitch;
    }

    public double getRenderPitch(float partialTicks) {
        return getRenderDouble(c_lastPitch, c_pitch, partialTicks);
    }

    public double getYaw() {
        return c_yaw;
    }

    public double getRenderYaw(float partialTicks) {
        return getRenderDouble(c_lastYaw, c_yaw, partialTicks);
    }

    public double getLaunchMult() {
        return 1 - (sh_curLaunchTime/sh_launchTime);
    }

    public double getRenderLaunchMult(float partialTicks){
        if(sh_launchState == LaunchState.NONE)
            return 1;
        else {
            double curLaunchMult = (double)sh_curLaunchTime/sh_launchTime;
            double lastLaunchMult = (double)sh_lastLaunchTime/sh_launchTime;
            return 1 - getRenderDouble(lastLaunchMult, curLaunchMult, partialTicks);
        }
    }

    public boolean isHit() {
        return sh_hit;
    }

    private Vec3d getRenderVec(Vec3d prevPos, Vec3d curPos, float partialTicks) {
        double x = getRenderDouble(prevPos.x, curPos.x, partialTicks);
        double y = getRenderDouble(prevPos.y, curPos.y, partialTicks);
        double z = getRenderDouble(prevPos.z, curPos.z, partialTicks);

        return new Vec3d(x, y, z);
    }

    private double getRenderDouble(double prevPos, double curPos, float partialTicks) {
        return prevPos + (curPos - prevPos) * partialTicks;
    }

    private Vec3d getPullVel(Vec3d to, Vec3d from, double speed) {
        Vec3d vel = to.subtract(from);
        return vel.scale(1/vel.lengthVector()).scale(speed);
    }

    private boolean entityIsCloseToGround(Entity entityIn, double distance, double velocity) {
        if(entityIn.onGround)
            return true;

        if(velocity > 0.2)
            return false;

        RayTraceResult rayResult = GrappleGunMod.proxy.getWorld().rayTraceBlocks(entityIn.getPositionVector(), entityIn.getPositionVector().addVector(0, -distance, 0));
        if(rayResult == null)
            return false;
        else
            return true;
    }

    public Vec3d getOffsetPullLocation() {
        return sh_pullLocation.subtract(0, sh_parentEntity.getEyeHeight(), 0);
    }

    @Override
    protected void entityInit() {
        //GrappleGunMod.proxy.getPlayer().sendMessage(new TextComponentString("Spawned grapple"));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }
}
