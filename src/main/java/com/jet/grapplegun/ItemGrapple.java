package com.jet.grapplegun;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.jet.grapplegun.network.C_DestroyedPuller;
import com.jet.grapplegun.network.GrapplePacketManager;
import com.jet.grapplegun.network.S_RequestPull;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrapple extends Item {
    private double sh_range;
    private double sh_pullSpeed;
    private double sh_launchTime;
    private RopeColor sh_color;

    private EntityGrapplePuller sh_childPuller;

    public ItemGrapple(String name, double range, double pullSpeed, double launchTime, RopeColor color){
        setRegistryName(name);
        setUnlocalizedName(name);
        setCreativeTab(CreativeTabs.COMBAT);
        setMaxStackSize(1);

        sh_range = range;
        sh_pullSpeed = pullSpeed;
        sh_launchTime = launchTime;
        sh_color = color;
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        if(sh_childPuller != null) {
            sh_childPuller.onKillCommand();
            sh_childPuller = null;
        }
        return true;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if(!worldIn.isRemote)
            return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));

        Entity hitEntity = raytraceEntities(playerIn, worldIn);
        RayTraceResult rayResult = worldIn.rayTraceBlocks(playerIn.getPositionEyes(1), playerIn.getPositionEyes(1).add(playerIn.getLookVec().scale(sh_range)));

        // Get the nearest raycast
        double entityDistance = Double.POSITIVE_INFINITY;
        if(hitEntity != null)
            entityDistance = hitEntity.getPositionVector().distanceTo(playerIn.getPositionVector());
        double rayDistance = Double.POSITIVE_INFINITY;
        if(rayResult != null)
            rayDistance = rayResult.hitVec.distanceTo(playerIn.getPositionVector());

        if(entityDistance == Double.POSITIVE_INFINITY && rayDistance == Double.POSITIVE_INFINITY) {
            Vec3d missVector = playerIn.getPositionEyes(1).add(playerIn.getLookVec().scale(sh_range));
            GrapplePacketManager.INSTANCE.sendToServer(new S_RequestPull(this, playerIn, missVector, null, false));
        }
        else if(entityDistance < rayDistance)
            GrapplePacketManager.INSTANCE.sendToServer(new S_RequestPull(this, playerIn, Vec3d.ZERO, hitEntity, true));
        else
            GrapplePacketManager.INSTANCE.sendToServer(new S_RequestPull(this, playerIn, rayResult.hitVec, null, true));

        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(entityIn instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) entityIn;

            if(player.getHeldItemMainhand().getItem() != this && player.getHeldItemOffhand().getItem() != this){
                if(sh_childPuller != null && !worldIn.isRemote)
                    sh_childPuller.onKillCommand();
            }
        }
    }

    Entity raytraceEntities(EntityPlayer playerIn, World worldIn){
        float d0 = (float)sh_range;
        Vec3d vec3d1 = playerIn.getLook(Minecraft.getMinecraft().getRenderPartialTicks());
        List<Entity> list = worldIn.getEntitiesInAABBexcluding(playerIn, playerIn.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
            }
        }));

        Vec3d castDirection = playerIn.getPositionEyes(1).add(playerIn.getLookVec().scale(sh_range)).subtract(playerIn.getPositionEyes(1));
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

    public void onPullerDestroyed(Entity parentEntity) {
        sh_childPuller = null;
        if(!parentEntity.getEntityWorld().isRemote) {
            System.out.println("Sent destroy message");
            GrapplePacketManager.INSTANCE.sendToAll(new C_DestroyedPuller(this, parentEntity));
        }
        else {
            Minecraft.getMinecraft().player.sendChatMessage("Puller Destroyed");
            if (parentEntity == null)
                return;

            if (parentEntity == GrappleGunMod.proxy.getPlayer()) {
                parentEntity.setNoGravity(false);
                Minecraft.getMinecraft().player.sendChatMessage("Gravity enabled");
            }
        }
    }

    public double getRange() {
        return sh_range;
    }

    public double getPullSpeed() {
        return sh_pullSpeed;
    }

    public double getLaunchTime() {
        return sh_launchTime;
    }

    public RopeColor getColor() {
        return sh_color;
    }

    public void setChildPuller(EntityGrapplePuller newChild) { sh_childPuller = newChild; }

    public EntityGrapplePuller getChildPuller() { return sh_childPuller; }
}
