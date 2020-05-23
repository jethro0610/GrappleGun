package com.jet.grapplegun;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParamsGrappleVisual {
    public ParamsGrappleVisual() {
        attachEntity = null;
        pos = Vec3d.ZERO;
    }
    public World worldIn;
    public Entity attachEntity;
    public Vec3d pos;
    public Entity owningEntity;
    public float pitch;
    public float yaw;
    public boolean hit;
    public float pullRange;
    public double launchSpeed;
}