package com.jet.grapplegun.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class GrapplePacketManager {
    public static SimpleNetworkWrapper INSTANCE;
    private static int packetID = 0;

    public static void registerClientMessages() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("grapplegun");

        INSTANCE.registerMessage(S_RequestPull.Handler.class, S_RequestPull.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_StopGrapple.Handler.class, S_StopGrapple.class, packetID++, Side.SERVER);
    }

    public static void registerServerMessages() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("grapplegun");

        INSTANCE.registerMessage(S_RequestPull.Handler.class, S_RequestPull.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_StopGrapple.Handler.class, S_StopGrapple.class, packetID++, Side.SERVER);
    }
}
