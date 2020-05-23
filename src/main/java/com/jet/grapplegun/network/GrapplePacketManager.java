package com.jet.grapplegun.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class GrapplePacketManager {
    public static SimpleNetworkWrapper INSTANCE;
    private static int packetID = 0;

    public static void registerClientMessages() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("grapplegun");

        INSTANCE.registerMessage(C_DropGrapple.DropGrappleManager.class, C_DropGrapple.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_StopEntityPull.StopEntityPullManager.class, C_StopEntityPull.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_PullPlayer.PullPlayerManager.class, C_PullPlayer.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_SpawnGrappleVisual.SpawnGrappleVisualManager.class, C_SpawnGrappleVisual.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_DestroyGrappleVisual.DestroyGrappleVisualManager.class, C_DestroyGrappleVisual.class, packetID++, Side.CLIENT);

        INSTANCE.registerMessage(S_PullEntity.PullEntityManager.class, S_PullEntity.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_CreateGrappleVisual.CreateGrappleVisualManager.class, S_CreateGrappleVisual.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_RemoveGrappleVisual.RemoveGrappleVisualManager.class, S_RemoveGrappleVisual.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_UpdateSticking.S_UpdateStickingManager.class, S_UpdateSticking.class, packetID++, Side.SERVER);
    }

    public static void registerServerMessages() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("grapplegun");

        INSTANCE.registerMessage(C_DropGrapple.DropGrappleManager.class, C_DropGrapple.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_StopEntityPull.StopEntityPullManager.class, C_StopEntityPull.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_PullPlayer.PullPlayerManager.class, C_PullPlayer.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_SpawnGrappleVisual.SpawnGrappleVisualManager.class, C_SpawnGrappleVisual.class, packetID++, Side.CLIENT);
        INSTANCE.registerMessage(C_DestroyGrappleVisual.DestroyGrappleVisualManager.class, C_DestroyGrappleVisual.class, packetID++, Side.CLIENT);

        INSTANCE.registerMessage(S_PullEntity.PullEntityManager.class, S_PullEntity.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_CreateGrappleVisual.CreateGrappleVisualManager.class, S_CreateGrappleVisual.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_RemoveGrappleVisual.RemoveGrappleVisualManager.class, S_RemoveGrappleVisual.class, packetID++, Side.SERVER);
        INSTANCE.registerMessage(S_UpdateSticking.S_UpdateStickingManager.class, S_UpdateSticking.class, packetID++, Side.SERVER);
    }
}
