package com.jet.grapplegun.proxy;

import com.jet.grapplegun.*;
import com.jet.grapplegun.network.GrapplePacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import scala.collection.parallel.ParIterableLike;

import javax.swing.text.html.parser.Entity;

public class ClientProxy extends CommonProxy {
    public static final ModelGrapple GRAPPLE_MODEL = new ModelGrapple();
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        GrapplePacketManager.registerClientMessages();
        RenderingRegistry.registerEntityRenderingHandler(EntityGrappleVisual.class, new IRenderFactory<EntityGrappleVisual>() {
            @Override
            public Render<? super EntityGrappleVisual> createRenderFor(RenderManager manager) {
                return new RenderGrappleVisual(manager, GRAPPLE_MODEL);
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
            ModelLoader.setCustomModelResourceLocation(itemGrappleGun, 0, new ModelResourceLocation(itemGrappleGun.getRegistryName(), "inventory"));
        }
    }
}
