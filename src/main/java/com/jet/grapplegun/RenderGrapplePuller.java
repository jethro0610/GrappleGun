package com.jet.grapplegun;

import net.minecraft.client.Minecraft;
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

public class RenderGrapplePuller extends RenderEntity {
    ModelBase mainModel;
    EntityGrapplePuller grapplePuller;
    private static final ResourceLocation HEAD_TEXTURE = new ResourceLocation("grapplegun:grapplehead.png");
    private static final double SEGMENT_LENGTH = 0.5;
    public RenderGrapplePuller(RenderManager renderManagerIn, ModelBase model) {
        super(renderManagerIn);
        mainModel = model;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        grapplePuller = (EntityGrapplePuller) entity;
        if(grapplePuller.getRenderLaunchMult(partialTicks) < 0)
            return;

        Vec3d renderPos = grapplePuller.getRenderPosition(partialTicks);
        Vec3d drawOrigin = new Vec3d(x, y + grapplePuller.getParentEntity().getEyeHeight() / 1.3, z);

        Vec3d renderEndPointPos = grapplePuller.getRenderPullLocation(partialTicks);
        renderEndPointPos = renderEndPointPos.subtract(0, grapplePuller.getParentEntity().getEyeHeight() / 1.3, 0);
        Vec3d vectorToEndPoint = renderEndPointPos.subtract(renderPos);
        vectorToEndPoint = vectorToEndPoint.scale(grapplePuller.getRenderLaunchMult(partialTicks));

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RopeColor ropeColor = grapplePuller.getParentGrapple().getColor();

        // Shrink the line width based on the distance of the camera
        float cameraDist = (float) renderManager.getDistanceToCamera(x, y, z);
        if(cameraDist > 12)
            cameraDist = 12;
        GL11.glLineWidth(15 - cameraDist);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        // Draw the rope in segments
        int segments = (int)Math.floor(vectorToEndPoint.lengthVector() / SEGMENT_LENGTH);
        double extraDist = vectorToEndPoint.lengthVector() - (segments * SEGMENT_LENGTH);
        int colorAdd = 0;
        for(int i = segments; i >= 0; i--) {
            Vec3d segVec = vectorToEndPoint.scale(1/vectorToEndPoint.lengthVector());
            segVec = segVec.scale((i * SEGMENT_LENGTH) + extraDist);

            vertexbuffer.pos(drawOrigin.x + segVec.x, drawOrigin.y + segVec.y,drawOrigin.z + segVec.z);
            vertexbuffer.color(ropeColor.red + colorAdd, ropeColor.green + colorAdd, ropeColor.blue + colorAdd, 255);
            vertexbuffer.endVertex();

            colorAdd = Math.abs((segments % 2) - (i % 2)) * ropeColor.add;
        }
        vertexbuffer.pos(drawOrigin.x, drawOrigin.y, drawOrigin.z);
        vertexbuffer.color(ropeColor.red + colorAdd, ropeColor.green + colorAdd, ropeColor.blue + colorAdd, 255);
        vertexbuffer.endVertex();
        tessellator.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Draw head
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glTranslated(drawOrigin.x + vectorToEndPoint.x, drawOrigin.y + vectorToEndPoint.y, drawOrigin.z + vectorToEndPoint.z);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(ropeColor.red/255.0f, ropeColor.green/255.0f, ropeColor.blue/255.0f, 1.0f);
        bindTexture(HEAD_TEXTURE);
        mainModel.render(entity, 0, 0, 0, (float)grapplePuller.getRenderYaw(partialTicks), (float)grapplePuller.getRenderPitch(partialTicks), 0.5f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}