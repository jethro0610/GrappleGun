package com.jet.grapplegun.proxy;

import com.jet.grapplegun.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class CommonProxy {
    /*protected static Item itemGrappleGun = new ItemGrappleGun(
            "grapple_gun",
            20,
            10,
            2,
            150,
            150,
            150,
            100);*/

    protected static Item itemGrapple = new ItemGrapple("grapple", 20, 2, 10);
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public World getWorld() {
        return null;
    }

    public EntityPlayer getPlayer(){
        return null;
    }

    @Mod.EventBusSubscriber
    public static class CommonRegistry {
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event){
            event.getRegistry().registerAll(itemGrapple);
        }

        @SubscribeEvent
        public static void registerEntity(RegistryEvent.Register<EntityEntry> event) {
            /*EntityEntry grapplepoint = EntityEntryBuilder.create()
                    .entity(EntityGrappleVisual.class)
                    .id(new ResourceLocation(GrappleGunMod.MODID + ":grapplepoint"), 144)
                    .name("grapplepoint")
                    .tracker(64, 20, false)
                    .build();*/

            EntityEntry grapplepuller = EntityEntryBuilder.create()
                    .entity(EntityGrapplePuller.class)
                    .id(new ResourceLocation(GrappleGunMod.MODID + ":grapplepuller"), 144)
                    .name("grapplepuller")
                    .tracker(64, 20, false)
                    .build();

            event.getRegistry().registerAll(grapplepuller);
        }


    }
}
