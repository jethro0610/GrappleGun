package com.jet.grapplegun.proxy;

import com.jet.grapplegun.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy {
    public static final ModelGrapple GRAPPLE_MODEL = new ModelGrapple();
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        RenderingRegistry.registerEntityRenderingHandler(EntityGrapplePuller.class, new IRenderFactory<EntityGrapplePuller>() {
            @Override
            public Render<? super EntityGrapplePuller> createRenderFor(RenderManager manager) {
                return new RenderGrapplePuller(manager, GRAPPLE_MODEL);
            }
        });
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public World getWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @EventBusSubscriber
    public static class ClientRegistry {
        @SubscribeEvent
        public static void registerItemModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(itemGrapple, 0, new ModelResourceLocation(itemGrapple.getRegistryName(), "inventory"));
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event){
            EntityPlayer player = GrappleGunMod.proxy.getPlayer();
            if(player != null)
                GrappleGunMod.proxy.velocityLastTick = new Vec3d(player.motionX, player.motionY, player.motionZ);
        }

        @SubscribeEvent
        public static void livingFall(LivingFallEvent event)  {
            if(event.getEntityLiving() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)event.getEntityLiving();

                if(player == GrappleGunMod.proxy.getPlayer())
                    player.setVelocity(GrappleGunMod.proxy.velocityLastTick.x, GrappleGunMod.proxy.velocityLastTick.y, GrappleGunMod.proxy.velocityLastTick.z);
            }
        }
    }
}
