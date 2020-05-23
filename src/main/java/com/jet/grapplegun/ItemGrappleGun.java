package com.jet.grapplegun;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.jet.grapplegun.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrappleGun extends Item {
    public float sh_pullRange;
    public double sh_pullSpeed;
    public double sh_launchSpeed;
    public int sh_ropeColorR;
    public int sh_ropeColorG;
    public int sh_ropeColorB;
    public int sh_ropeColorAdd;

    private int h_cooldown;
    private boolean h_pulling;
    private boolean h_sticking;
    private Vec3d h_pullLocation;
    public Entity h_pullEntity = null;
    private double h_stickHeight = 0;

    private EntityPlayer h_queuePlayer = null;
    private Entity h_queuePullEntity = null;
    private Vec3d h_queueLocation = Vec3d.ZERO;

    public EntityGrappleVisual sh_grappleVisual = null;

    public Entity s_pullEntity = null;
    public boolean s_sticking = false;

    public EntityPlayer rc_pullPlayer = null;
    public EntityPlayer rc_pullToPlayer = null;

    public ItemGrappleGun(String name, float range, double launchSpeed, double pullSpeed, int ropeColorR, int ropeColorG, int ropeColorB, int ropeColorAdd){
        setRegistryName(name);
        setUnlocalizedName(name);
        sh_pullRange = range;
        sh_launchSpeed = launchSpeed;
        sh_pullSpeed = pullSpeed;

        sh_ropeColorR = ropeColorR;
        sh_ropeColorG = ropeColorR;
        sh_ropeColorB = ropeColorR;
        sh_ropeColorAdd = ropeColorAdd;
        setCreativeTab(CreativeTabs.COMBAT);
        setMaxStackSize(1);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        if(s_pullEntity != null) {
            s_pullEntity.setVelocity(0, 0, 0);
            s_pullEntity.setNoGravity(false);
            s_pullEntity = null;
        }
        GrapplePacketManager.INSTANCE.sendTo(new C_DropGrapple(this), (EntityPlayerMP)player);
        GrapplePacketManager.INSTANCE.sendToAll(new C_DestroyGrappleVisual(this));
        return true;
    }

    public void holderReset() {
        h_cooldown = 0;
        h_pulling = false;
        h_sticking = false;
        h_pullEntity = null;
        h_stickHeight = 0;
        Minecraft.getMinecraft().player.setNoGravity(false);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if(!worldIn.isRemote)
           return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));

        if(Minecraft.getMinecraft().player != playerIn)
            return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));

        if(h_cooldown <= 0 && sh_grappleVisual == null && h_pullEntity == null) {
            Minecraft.getMinecraft().player.setNoGravity(false);
            holderRemoveVisual();
            h_pulling = false;
            h_sticking = false;

            Entity hitEntity = holderRaycastEntities(playerIn, worldIn);
            RayTraceResult rayResult = worldIn.rayTraceBlocks(playerIn.getPositionEyes(1), playerIn.getPositionEyes(1).add(playerIn.getLookVec().scale(sh_pullRange)));

            double entityDistance = Double.POSITIVE_INFINITY;
            if(hitEntity != null)
                entityDistance = hitEntity.getPositionVector().distanceTo(playerIn.getPositionVector());

            double rayDistance = Double.POSITIVE_INFINITY;
            if(rayResult != null)
                rayDistance = rayResult.hitVec.distanceTo(playerIn.getPositionVector());

            if(entityDistance == Double.POSITIVE_INFINITY && rayDistance == Double.POSITIVE_INFINITY){
                holderCreateVisual(playerIn, playerIn.getPositionEyes(1).add(playerIn.getLookVec().scale(sh_pullRange)), worldIn, false);
                return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
            }
            else if(entityDistance < rayDistance) {
                h_queuePullEntity = hitEntity;
                h_queuePlayer = playerIn;
                holderCreateVisual(playerIn, hitEntity, worldIn, true);
                return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
            }
            else {
                h_queueLocation = rayResult.hitVec.subtract(playerIn.getLookVec().scale(1));
                h_queuePlayer = playerIn;
                holderCreateVisual(playerIn, rayResult.hitVec, worldIn, true);
                return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(worldIn.isRemote) {
            if(entityIn == Minecraft.getMinecraft().player)
                holderUpdate(worldIn, entityIn);

            if(rc_pullPlayer == Minecraft.getMinecraft().player)
                pulledPlayerUpdate();
        }
        else{
            serverUpdate(entityIn);
        }
    }

    private void holderUpdate(World worldIn, Entity entityIn){
        if(h_cooldown > 0)
            h_cooldown--;

        if(h_pulling){
            Vec3d launchVel = h_pullLocation.subtract(entityIn.getPositionVector());
            launchVel = launchVel.scale(1/launchVel.lengthVector()).scale(sh_pullSpeed);
            entityIn.setVelocity(launchVel.x, launchVel.y, launchVel.z);

            if(entityIn.getPositionVector().distanceTo(h_pullLocation) < sh_pullSpeed) {
                h_pulling = false;
                if (!Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !holderIsOnGround(entityIn, worldIn, 2.5, launchVel.y)) {
                    h_sticking = true;
                }
                else
                    holderRemoveVisual();
            }
        }

        if(h_sticking) {
            Vec3d pullVel = h_pullLocation.addVector(0, h_stickHeight, 0).subtract(entityIn.getPositionVector());
            pullVel.scale(0.5);
            entityIn.setVelocity(pullVel.x,pullVel.y,pullVel.z);
            entityIn.setNoGravity(true);

            if (entityIn.isSneaking())
                h_stickHeight -= 0.1;

            if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
                h_stickHeight += 0.1;

            if(h_stickHeight > 0.2) {
                GrapplePacketManager.INSTANCE.sendToServer(new S_UpdateSticking(entityIn));
                entityIn.setNoGravity(false);
                entityIn.setVelocity(0, 0.6,0);
                h_sticking = false;
                h_stickHeight = 0;
                holderRemoveVisual();
            }

            if (holderIsOnGround(entityIn, worldIn, 1, 0 )) {
                GrapplePacketManager.INSTANCE.sendToServer(new S_UpdateSticking(entityIn));
                entityIn.setVelocity(0, 0, 0);
                entityIn.setNoGravity(false);
                h_sticking = false;
                h_stickHeight = 0;
                holderRemoveVisual();
            }
        }
    }

    private void serverUpdate(Entity entityIn){
        if(s_pullEntity != null){
            Vec3d pullVel = getPullVel(entityIn.getPositionEyes(1), s_pullEntity.getPositionVector(), sh_pullSpeed);

            s_pullEntity.motionX = pullVel.x;
            s_pullEntity.motionY = pullVel.y;
            s_pullEntity.motionZ = pullVel.z;
            s_pullEntity.velocityChanged = true;

            if(s_pullEntity.getPositionVector().distanceTo(entityIn.getPositionEyes(1)) < sh_pullSpeed) {
                GrapplePacketManager.INSTANCE.sendTo(new C_StopEntityPull(this), (EntityPlayerMP)entityIn);
                GrapplePacketManager.INSTANCE.sendToAll(new C_DestroyGrappleVisual(this));

                s_pullEntity.motionX = 0;
                s_pullEntity.motionY = 0;
                s_pullEntity.motionZ = 0;
                s_pullEntity.velocityChanged = true;

                s_pullEntity = null;
            }
        }
    }

    private void pulledPlayerUpdate(){
        Vec3d pullVel = getPullVel(rc_pullToPlayer.getPositionEyes(1), rc_pullPlayer.getPositionVector(), sh_pullSpeed);
        rc_pullPlayer.setVelocity(pullVel.x, pullVel.y, pullVel.z);

        if(rc_pullPlayer.getPositionVector().distanceTo(h_pullLocation) < sh_pullSpeed) {
            rc_pullPlayer = null;
            rc_pullToPlayer = null;
        }
    }

    public void holderStartPull() {
        if(h_queuePullEntity != null){
            holderPullEntity(h_queuePullEntity);
        }
        else if(h_queueLocation != Vec3d.ZERO){
            holderPullToLocation(h_queuePlayer, h_queueLocation);
        }
        h_queueLocation = Vec3d.ZERO;
        h_queuePullEntity = null;
        h_queuePlayer = null;
    }

    private void holderPullEntity(Entity pullEntity) {
        h_cooldown = 10;
        h_pullEntity = pullEntity;
        GrapplePacketManager.INSTANCE.sendToServer(new S_PullEntity(pullEntity, this));
    }

    private void holderPullToLocation(EntityPlayer playerIn, Vec3d pullLocation){
        h_cooldown = 10;
        h_pullLocation = pullLocation.subtract(0, playerIn.getEyeHeight() / 2, 0);
        h_pulling = true;
    }

    Entity holderRaycastEntities(EntityPlayer playerIn, World worldIn){
        float d0 = sh_pullRange;
        Vec3d vec3d1 = playerIn.getLook(Minecraft.getMinecraft().getRenderPartialTicks());
        List<Entity> list = worldIn.getEntitiesInAABBexcluding(playerIn, playerIn.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
            }
        }));

        Vec3d castDirection = playerIn.getPositionEyes(1).add(playerIn.getLookVec().scale(sh_pullRange)).subtract(playerIn.getPositionEyes(1));
        castDirection = castDirection.scale(1/castDirection.lengthVector());
        double lowestDistance = Double.POSITIVE_INFINITY;
        Entity hitEntity = null;
        for (int i = 0; i < list.size(); i++) {
            Entity castEntity = list.get(i);
            double dist = castEntity.getPositionVector().distanceTo(playerIn.getPositionVector());

            if(dist < lowestDistance) {
                Vec3d castLocation = playerIn.getPositionEyes(1).add(castDirection.scale(dist));
                if (castEntity.getEntityBoundingBox().grow(0.5).contains(castLocation)){
                    lowestDistance = dist;
                    hitEntity = castEntity;
                }
            }
        }

        return hitEntity;
    }

    private ParamsGrappleVisual holderCreateVisualParams(EntityPlayer playerIn, World worldIn, boolean hit) {
        ParamsGrappleVisual params = new ParamsGrappleVisual();
        params.worldIn = worldIn;
        params.owningEntity = playerIn;
        params.pitch = playerIn.rotationPitch;
        params.yaw = playerIn.rotationYaw;
        params.hit = hit;
        params.pullRange = sh_pullRange;
        params.launchSpeed = sh_launchSpeed;

        return params;
    }

    private EntityGrappleVisual holderCreateVisual(EntityPlayer playerIn, Vec3d pullLocation, World worldIn, boolean hit) {
        ParamsGrappleVisual params = holderCreateVisualParams(playerIn, worldIn, hit);
        params.pos = pullLocation;

        sh_grappleVisual = new EntityGrappleVisual(params);
        worldIn.spawnEntity(sh_grappleVisual);
        sh_grappleVisual.h_parentGrapple = this;

        sh_grappleVisual.sh_colorR = sh_ropeColorR;
        sh_grappleVisual.sh_colorG = sh_ropeColorG;
        sh_grappleVisual.sh_colorB = sh_ropeColorB;
        sh_grappleVisual.sh_colorAdd = sh_ropeColorAdd;

        GrapplePacketManager.INSTANCE.sendToServer(new S_CreateGrappleVisual(this, params));

        return sh_grappleVisual;
    }

    EntityGrappleVisual holderCreateVisual(EntityPlayer playerIn, Entity pullEntity, World worldIn, boolean hit) {
        ParamsGrappleVisual params = holderCreateVisualParams(playerIn, worldIn, hit);
        params.attachEntity = pullEntity;

        sh_grappleVisual = new EntityGrappleVisual(params);
        worldIn.spawnEntity(sh_grappleVisual);
        sh_grappleVisual.h_parentGrapple = this;

        sh_grappleVisual.sh_colorR = sh_ropeColorR;
        sh_grappleVisual.sh_colorG = sh_ropeColorG;
        sh_grappleVisual.sh_colorB = sh_ropeColorB;
        sh_grappleVisual.sh_colorAdd = sh_ropeColorAdd;

        GrapplePacketManager.INSTANCE.sendToServer(new S_CreateGrappleVisual(this, params));

        return sh_grappleVisual;
    }

    void holderRemoveVisual(){
        GrapplePacketManager.INSTANCE.sendToServer(new S_RemoveGrappleVisual(this));
        if(sh_grappleVisual != null) {
            sh_grappleVisual.onKillCommand();
            sh_grappleVisual = null;
        }
    }

    Vec3d getPullVel(Vec3d to, Vec3d from, double speed) {
        Vec3d vel = to.subtract(from);
        return vel.scale(1/vel.lengthVector()).scale(speed);
    }

    boolean holderIsOnGround(Entity playerEntity, World worldIn, double distance, double velocity) {
        if(velocity > 0.2)
            return false;

        RayTraceResult rayResult = worldIn.rayTraceBlocks(playerEntity.getPositionVector(), playerEntity.getPositionVector().addVector(0, -distance, 0));
        if(rayResult == null)
            return false;
        else
            return true;
    }
}
