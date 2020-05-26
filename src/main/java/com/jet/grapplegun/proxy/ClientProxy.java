package com.jet.grapplegun.proxy;

import com.jet.grapplegun.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

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
            ModelLoader.setCustomModelResourceLocation(woodGrapple, 0, new ModelResourceLocation(woodGrapple.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(stoneGrapple, 0, new ModelResourceLocation(stoneGrapple.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ironGrapple, 0, new ModelResourceLocation(ironGrapple.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(diamondGrapple, 0, new ModelResourceLocation(diamondGrapple.getRegistryName(), "inventory"));
        }
    }
}
