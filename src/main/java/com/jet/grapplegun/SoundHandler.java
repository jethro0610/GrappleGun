package com.jet.grapplegun;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(GrappleGunMod.MODID)
public class SoundHandler {
    @GameRegistry.ObjectHolder("grapplefire")
    public static final SoundEvent GRAPPLE_FIRE = new SoundEvent(new ResourceLocation(GrappleGunMod.MODID, "grapplefire")).setRegistryName("grapplefire");

    @Mod.EventBusSubscriber
    public static class Registry {
        @SubscribeEvent
        public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().register(GRAPPLE_FIRE);
        }
    }
}
