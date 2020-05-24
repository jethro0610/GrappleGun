package com.jet.grapplegun.proxy;

import com.jet.grapplegun.*;
import com.jet.grapplegun.network.GrapplePacketManager;
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
    protected static Item itemGrapple = new ItemGrapple("grapple", 20, 2, 10, new RopeColor(50, 50, 50, 50));

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
        public static void registerItems(RegistryEvent.Register<Item> event){
            event.getRegistry().registerAll(itemGrapple);
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


    }
}
