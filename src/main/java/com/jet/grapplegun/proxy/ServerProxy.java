package com.jet.grapplegun.proxy;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) { super.preInit(event); }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public World getWorld() { return FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld(); }

    @Mod.EventBusSubscriber
    public static class ServerRegistry {
    }
}
