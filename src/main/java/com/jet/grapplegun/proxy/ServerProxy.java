package com.jet.grapplegun.proxy;

import com.jet.grapplegun.EntityGrapplePuller;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

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
        @SubscribeEvent
        public static void disconnect(PlayerEvent.PlayerLoggedOutEvent event){
            ItemStack mainItem = event.player.getHeldItemMainhand();
            ItemStack offItem = event.player.getHeldItemOffhand();

            if(mainItem.hasTagCompound()){
                NBTTagCompound nbt = mainItem.getTagCompound();
                if(nbt.hasKey("PullerID")) {
                    Entity pullerEntity = event.player.getEntityWorld().getEntityByID(nbt.getInteger("PullerID"));
                    if(pullerEntity != null)
                        pullerEntity.onKillCommand();

                    nbt.setInteger("PullerID", -1);
                }
            }

            if(offItem.hasTagCompound()){
                NBTTagCompound nbt = mainItem.getTagCompound();
                if(nbt.hasKey("PullerID")) {
                    Entity pullerEntity = event.player.getEntityWorld().getEntityByID(nbt.getInteger("PullerID"));
                    if(pullerEntity != null)
                        pullerEntity.onKillCommand();

                    nbt.setInteger("PullerID", -1);
                }
            }
        }
    }
}
