package com.jet.grapplegun;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

public class RenderGrappleVisual extends RenderEntity {
    ModelBase mainModel;
    EntityGrappleVisual grappleVisual;
    private static final ResourceLocation HEAD_TEXTURE = new ResourceLocation("grapplegun:grapplehead.png");
    private static final double SEGMENT_LENGTH = 0.5;
    public RenderGrappleVisual(RenderManager renderManagerIn, ModelBase model) {
        super(renderManagerIn);
        mainModel = model;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        grappleVisual = (EntityGrappleVisual) entity;

        // Calculate the end point of the visual
        double pX = getActualPos(grappleVisual.sh_owningEntity.prevPosX, grappleVisual.sh_owningEntity.posX, partialTicks);
        double pY = getActualPos(grappleVisual.sh_owningEntity.prevPosY, grappleVisual.sh_owningEntity.posY, partialTicks) + grappleVisual.sh_owningEntity.getEyeHeight() / 1.25;
        double pZ = getActualPos(grappleVisual.sh_owningEntity.prevPosZ, grappleVisual.sh_owningEntity.posZ, partialTicks);

        double gX = getActualPos(grappleVisual.prevPosX, grappleVisual.posX, partialTicks);
        double gY = getActualPos(grappleVisual.prevPosY, grappleVisual.posY, partialTicks);
        double gZ = getActualPos(grappleVisual.prevPosZ, grappleVisual.posZ, partialTicks);

        Vec3d vectorToPlayer = new Vec3d(pX, pY, pZ).subtract(gX, gY, gZ);
        double actualDistMult = getActualPos(grappleVisual.getLastDistMult(), grappleVisual.getDistMult(), partialTicks);
        Vec3d launchDistVec = vectorToPlayer.scale(actualDistMult);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Color c = new Color(grappleVisual.sh_colorR, grappleVisual.sh_colorG, grappleVisual.sh_colorB, 255);
        GL11.glColor4d(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());

        // Shrink the line width based on the distance of the camera
        float cameraDist = (float) renderManager.getDistanceToCamera(x, y, z);
        if(cameraDist > 12)
            cameraDist = 12;
        GL11.glLineWidth(15 - cameraDist);

        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        // Draw the rope in segments
        int segments = (int)Math.floor((vectorToPlayer.lengthVector() * (1 - actualDistMult)) / SEGMENT_LENGTH);
        int colorAdd = 0;
        for(int i = 0; i <= segments; i++) {
            Vec3d segVec = vectorToPlayer.scale(1/vectorToPlayer.lengthVector());
            segVec = segVec.scale(i * SEGMENT_LENGTH);
            segVec = segVec.add(launchDistVec);

            vertexbuffer.pos(x + segVec.x, y + segVec.y,z + segVec.z);
            vertexbuffer.color(c.getRed() + colorAdd, c.getGreen() + colorAdd, c.getBlue() + colorAdd, c.getAlpha());
            vertexbuffer.endVertex();

            colorAdd = (i % 2) * grappleVisual.sh_colorAdd;
        }
        vertexbuffer.pos(x + vectorToPlayer.x, y + vectorToPlayer.y, z + vectorToPlayer.z);
        vertexbuffer.color(c.getRed() + colorAdd, c.getGreen() + colorAdd, c.getBlue() + colorAdd, c.getAlpha());
        vertexbuffer.endVertex();
        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glPopAttrib();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.translate(x + launchDistVec.x, y + launchDistVec.y, z + launchDistVec.z);

        bindTexture(HEAD_TEXTURE);
        mainModel.render(entity, 0, 0, 0, (float)Math.toRadians(270 - grappleVisual.sh_yaw), (float)Math.toRadians(90 - grappleVisual.sh_pitch), 1);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    double getActualPos(double lastTickPos, double currentPos, float partialTicks) {
        return lastTickPos + (currentPos - lastTickPos) * partialTicks;
    }
}

// Old draw rope code
/*for(int i = 0; i <= segments; i++) {
    Vec3d dirVec = vectorToPlayer.scale(1/vectorToPlayer.lengthVector());
    dirVec = dirVec.scale(i);
    Vec3d segVec = vectorToPlayer.subtract(dirVec);

    vertexbuffer.pos(x + segVec.x, y + segVec.y,z + segVec.z);
    vertexbuffer.color(c.getRed() + colorAdd, c.getGreen() + colorAdd, c.getBlue() + colorAdd, c.getAlpha());
    vertexbuffer.endVertex();

    colorAdd = (i % 2) * 50;
}
vertexbuffer.pos(x + launchDistVec.x, y + launchDistVec.y, z + launchDistVec.z);
vertexbuffer.color(c.getRed() + colorAdd, c.getGreen() + colorAdd, c.getBlue(), c.getAlpha());
vertexbuffer.endVertex();*/