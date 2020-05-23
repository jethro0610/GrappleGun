package com.jet.grapplegun;

import com.jet.grapplegun.network.GrapplePacketManager;
import com.jet.grapplegun.network.S_RequestPull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
            Vec3d lookVector = playerIn.getPositionVector().add(playerIn.getLookVec().scale(sh_range));
            GrapplePacketManager.INSTANCE.sendToServer(new S_RequestPull(this, playerIn, lookVector));
            //EntityGrapplePuller newPuller = new EntityGrapplePuller(worldIn, this, playerIn, playerIn.getPositionVector().add(playerIn.getLookVec().scale(sh_range)));
            //worldIn.spawnEntity(newPuller);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    public double getPullSpeed() {
        return sh_pullSpeed;
    }

    public void setChildPuller(EntityGrapplePuller newChild) { sh_childPuller = newChild; }
}
