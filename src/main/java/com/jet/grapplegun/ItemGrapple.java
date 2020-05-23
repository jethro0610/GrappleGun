package com.jet.grapplegun;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrapple extends Item {
    private double sh_range;
    private double sh_pullSpeed;
    private EntityGrapplePuller sh_childPuller;

    public ItemGrapple(String name, double range, double pullSpeed){
        setRegistryName(name);
        setUnlocalizedName(name);
        setCreativeTab(CreativeTabs.COMBAT);
        setMaxStackSize(1);

        sh_range = range;
        sh_pullSpeed = pullSpeed;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if(!worldIn.isRemote)
            return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));

        if(sh_childPuller == null) {
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

            }
            else if(entityDistance < rayDistance)
                GrapplePacketManager.INSTANCE.sendToServer(new S_RequestPull(this, playerIn, Vec3d.ZERO, hitEntity, false));
            else
                GrapplePacketManager.INSTANCE.sendToServer(new S_RequestPull(this, playerIn, rayResult.hitVec, null, false));
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
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

    public double getPullSpeed() {
        return sh_pullSpeed;
    }

    public void setChildPuller(EntityGrapplePuller newChild) { sh_childPuller = newChild; }
}
