package com.jet.grapplegun;

import com.jet.grapplegun.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;


@Mod(modid = GrappleGunMod.MODID, name = GrappleGunMod.NAME, version = GrappleGunMod.VERSION)
public class GrappleGunMod
{
    public static final String MODID = "grapplegun";
    public static final String NAME = "Grapple Gun";
    public static final String VERSION = "1.0";
    private static Logger logger;

    @Instance
    public static GrappleGunMod instance;

    @SidedProxy(serverSide = "com.jet.grapplegun.proxy.ServerProxy", clientSide = "com.jet.grapplegun.proxy.ClientProxy", modId = "grapplegun")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
