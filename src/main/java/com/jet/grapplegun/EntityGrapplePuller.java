package com.jet.grapplegun;

import com.jet.grapplegun.network.GrapplePacketManager;
import com.jet.grapplegun.network.S_UpdateSticking;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
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
    private boolean sh_hit;

    private boolean p_pullParent;
    private boolean p_sticking;

    public EntityGrapplePuller(World world) { super(world); }

    public EntityGrapplePuller(World world, ItemGrapple parentGrapple, Entity parentEntity, Vec3d pullLocation, boolean sh_hit) {
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
        buf.writeBoolean(sh_hit);
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

        sh_hit = buf.readBoolean();

        if(sh_parentEntity == GrappleGunMod.proxy.getPlayer()) {
            p_pullParent = true;
        }
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
        if(p_pullParent) {
            Vec3d pullVel = getPullVel(sh_pullLocation, sh_parentEntity.getPositionVector(), sh_parentGrapple.getPullSpeed());
            sh_parentEntity.setVelocity(pullVel.x, pullVel.y, pullVel.z);

            // Stop pulling the player
            if(sh_parentEntity.getPositionVector().distanceTo(sh_pullLocation) < sh_parentGrapple.getPullSpeed()) {
                p_pullParent = false;
                if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !entityIsCloseToGround(sh_parentEntity, sh_parentGrapple.getPullSpeed() + 0.5, pullVel.y)) {
                    p_sticking = true;
                }
            }
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
