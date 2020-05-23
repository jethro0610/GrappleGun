package com.jet.grapplegun;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.util.Color;

public class EntityGrappleVisual extends Entity {
    public Entity sh_attachEntity = null;
    public Entity sh_owningEntity = null;
    public float sh_pitch;
    public float sh_yaw;
    public float sh_pullRange;

    private boolean sh_hit;
    private boolean sh_isReturning = false;
    private double sh_launchSpeed;
    private double sh_launchTime;
    private double sh_lastLaunchDistance;
    private double sh_launchDistance;
    public int sh_colorR;
    public int sh_colorG;
    public int sh_colorB;
    public int sh_colorAdd;

    public ItemGrappleGun h_parentGrapple = null;
    public EntityGrappleVisual(World worldIn){
        super(worldIn);
    }

    public EntityGrappleVisual(ParamsGrappleVisual params) {
        super(params.worldIn);
        sh_owningEntity = params.owningEntity;
        sh_pitch = params.pitch;
        sh_yaw = params.yaw;
        sh_hit = params.hit;
        sh_pullRange = params.pullRange;
        sh_launchSpeed = params.launchSpeed;

        double distance;
        if(params.attachEntity == null) {
            setPosition(params.pos.x, params.pos.y, params.pos.z);
            distance = params.pos.distanceTo(params.owningEntity.getPositionVector());
        }
        else {
            sh_attachEntity = params.attachEntity;
            setPosition(params.attachEntity.posX, params.attachEntity.posY, params.attachEntity.posZ);
            distance = params.attachEntity.getPositionVector().distanceTo(params.owningEntity.getPositionVector());
        }
        sh_launchTime = (distance / sh_pullRange) * sh_launchSpeed;
        sh_launchDistance = sh_launchTime;
    }

    @Override
    protected void entityInit(){
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public void onEntityUpdate(){
        super.onEntityUpdate();

        if(sh_attachEntity != null)
            setPosition(sh_attachEntity.posX, sh_attachEntity.posY  + sh_attachEntity.getEyeHeight()/2, sh_attachEntity.posZ);

        if(sh_isReturning)
            sh_lastLaunchDistance = sh_launchDistance++;
        else
            sh_lastLaunchDistance = sh_launchDistance--;

        if(!sh_isReturning && sh_launchDistance < 0) {
            sh_launchDistance = 0;
            if (h_parentGrapple != null && sh_hit)
                h_parentGrapple.holderStartPull();

            if(!sh_hit)
                sh_isReturning = true;
        }

        if(sh_isReturning && sh_launchDistance > sh_launchTime) {
            if(h_parentGrapple != null)
                h_parentGrapple.sh_grappleVisual = null;
            onKillCommand();
        }
    }

    public double getLastDistMult() {
        return (double)sh_lastLaunchDistance/sh_launchTime;
    }

    public double getDistMult(){
        return (double)sh_launchDistance/sh_launchTime;
    }
}
