package com.jet.grapplegun;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityGrapplePuller extends Entity implements IEntityAdditionalSpawnData {
    private ItemGrapple sh_parentGrapple;
    private Entity sh_parentEntity;
    private Vec3d sh_pullLocation;

    public EntityGrapplePuller(World world) { super(world); }

    public EntityGrapplePuller(World world, ItemGrapple parentGrapple, Entity parentEntity, Vec3d pullLocation) {
        super(world);
        sh_parentEntity = parentEntity;
        sh_parentGrapple = parentGrapple;
        sh_pullLocation = pullLocation;
        setPositionAndUpdate(parentEntity.posX, parentEntity.posY, parentEntity.posZ);
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        buf.writeInt(Item.getIdFromItem(sh_parentGrapple));
        buf.writeInt(sh_parentEntity.getEntityId());
        buf.writeDouble(sh_pullLocation.x);
        buf.writeDouble(sh_pullLocation.y);
        buf.writeDouble(sh_pullLocation.z);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        Item readItem = Item.getItemById(buf.readInt());
        if(readItem != null && readItem instanceof ItemGrapple)
            sh_parentGrapple = (ItemGrapple) readItem;
        sh_parentGrapple.setChildPuller(this);

        Entity readEntity = getEntityWorld().getEntityByID(buf.readInt());
        if(readEntity != null)
            sh_parentEntity = (Entity) readEntity;

        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();

        sh_pullLocation = new Vec3d(x, y, z);
    }

    @Override
    public void onEntityUpdate() {
        if(!world.isRemote)
            serverUpdate();
        else {
            clientUpdate();
            if(sh_parentEntity == GrappleGunMod.proxy.getPlayer())
                parentUpdate();
        }
    }

    private void serverUpdate(){
        if(sh_parentEntity != null)
            setPositionAndUpdate(sh_parentEntity.posX, sh_parentEntity.posY, sh_parentEntity.posZ);
        else
            onKillCommand();
    }

    private void clientUpdate(){
        if(sh_parentEntity != null)
            setPosition(sh_parentEntity.posX, sh_parentEntity.posY, sh_parentEntity.posZ);
    }

    private void parentUpdate() {
        Vec3d pullVel = getPullVel(sh_pullLocation, sh_parentEntity.getPositionVector(), sh_parentGrapple.getPullSpeed());
        sh_parentEntity.setVelocity(pullVel.x, pullVel.y, pullVel.z);
    }

    Vec3d getPullVel(Vec3d to, Vec3d from, double speed) {
        Vec3d vel = to.subtract(from);
        return vel.scale(1/vel.lengthVector()).scale(speed);
    }

    @Override
    protected void entityInit(){
        GrappleGunMod.proxy.getPlayer().sendMessage(new TextComponentString("Spawned grapple"));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }
}
