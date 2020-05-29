package com.jet.grapplegun.proxy;

import com.jet.grapplegun.*;
import com.jet.grapplegun.network.GrapplePacketManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class CommonProxy {
    protected static Item woodGrapple = new ItemGrapple("woodgrapple", 10, 1, 4, 40 * 4, new RopeColor(102, 68, 0, 50));
    protected static Item stoneGrapple = new ItemGrapple("stonegrapple", 15, 1.25, 7, 25 * 4, new RopeColor(40, 40, 40, 50));
    protected static Item ironGrapple = new ItemGrapple("irongrapple", 20, 1.5, 7, 35 * 4, new RopeColor(120, 120, 120, 50));
    protected static Item goldGrapple = new ItemGrapple("goldgrapple", 15, 1.75, 6, 30 * 4, new RopeColor(200, 170, 70, 50));
    protected static Item diamondGrapple = new ItemGrapple("diamondgrapple", 25, 2, 7, 40 * 4, new RopeColor(0, 102, 102, 50));

    public void preInit(FMLPreInitializationEvent event) { GrapplePacketManager.registerMessages(); }

    public void init(FMLInitializationEvent event) { }

    public void postInit(FMLPostInitializationEvent event) { }

    public World getWorld() {
        return null;
    }

    public EntityPlayer getPlayer(){
        return null;
    }

    @Mod.EventBusSubscriber
    public static class CommonRegistry {
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(woodGrapple, stoneGrapple, ironGrapple, goldGrapple, diamondGrapple);
        }

        @SubscribeEvent
        public static void registerEntity(RegistryEvent.Register<EntityEntry> event) {
            EntityEntry grapplepuller = EntityEntryBuilder.create()
                    .entity(EntityGrapplePuller.class)
                    .id(new ResourceLocation(GrappleGunMod.MODID + ":grapplepuller"), 144)
                    .name("grapplepuller")
                    .tracker(64, 20, false)
                    .build();

            event.getRegistry().registerAll(grapplepuller);
        }

        @SubscribeEvent
        public static void blockBreak(PlayerEvent.BreakSpeed event) {
            boolean airFix = false;
            ItemStack mainItem = event.getEntityPlayer().getHeldItemMainhand();
            ItemStack offItem = event.getEntityPlayer().getHeldItemOffhand();

            if(mainItem.hasTagCompound()){
                NBTTagCompound nbt = mainItem.getTagCompound();
                if(nbt.hasKey("PullerID")){
                    if(nbt.getInteger("PullerID") != -1)
                        airFix = true;
                }
            }

            if(offItem.hasTagCompound()){
                NBTTagCompound nbt = offItem.getTagCompound();
                if(nbt.hasKey("PullerID")){
                    if(nbt.getInteger("PullerID") != -1)
                        airFix = true;
                }
            }

            if(airFix && !event.getEntityPlayer().onGround) {
                event.setNewSpeed(event.getNewSpeed() * 5);
            }
        }

        @SubscribeEvent
        public static void arrowFire(ArrowLooseEvent event) {
            boolean isGrappling = false;
            ItemStack mainItem = event.getEntityPlayer().getHeldItemMainhand();
            ItemStack offItem = event.getEntityPlayer().getHeldItemOffhand();

            if(mainItem.hasTagCompound()){
                NBTTagCompound nbt = mainItem.getTagCompound();
                if(nbt.hasKey("PullerID")){
                    if(nbt.getInteger("PullerID") != -1)
                        isGrappling = true;
                }
            }

            if(offItem.hasTagCompound()){
                NBTTagCompound nbt = offItem.getTagCompound();
                if(nbt.hasKey("PullerID")){
                    if(nbt.getInteger("PullerID") != -1)
                        isGrappling = true;
                }
            }

            if(isGrappling)
                event.getEntityPlayer().setVelocity(0, 0,0);
        }
    }
}
